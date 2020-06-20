package com.sxtanna.mc.arcade.menu

import com.sxtanna.mc.arcade.base.Addon
import com.sxtanna.mc.arcade.base.State
import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent

abstract class Text : Addon, State, Listener
{
	
	abstract val block: Block
	
	private var done = false
	private var load = false
	
	private var prev = null as? BlockState?
	private var sign = null as? Sign?
	
	
	override fun load()
	{
		if (load)
		{
			return
		}
		
		load = true
		server.pluginManager.registerEvents(this, plugin)
		
		prev = block.state
		
		block.setBlockData(Material.OAK_SIGN.createBlockData(), false)
		sign = block.state as Sign
	}
	
	override fun kill()
	{
		load = false
		done = false
		
		HandlerList.unregisterAll(this)
		
		prev?.update(true)
		
		prev = null
		sign = null
	}
	
	
	abstract fun make()
	
	abstract fun done(data: List<String>, player: Player)
	
	
	fun open(player: Player)
	{
		if (!done)
		{
			done = true
			
			load()
			make()
			
			sign?.update(true)
		}
		
		listenFor = player
		
		server.scheduler.runTaskLater(plugin, { _ -> player.openSign(sign ?: return@runTaskLater kill()) }, 2L)
	}
	
	protected operator fun Lines.get(text: String)
	{
		sign?.setLine(this.ordinal, Colour.color(text))
	}
	
	
	private var listenFor = null as? Player?
	
	@EventHandler
	fun SignChangeEvent.onChange()
	{
		if (player != listenFor)
		{
			return
		}
		
		kill()
		done(lines.toList(), player)
	}
	
	
	enum class Lines
	{
		
		LINE_0,
		LINE_1,
		LINE_2,
		LINE_3,
		
	}
	
}