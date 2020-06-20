package com.sxtanna.mc.arcade.hook

import com.sxtanna.mc.arcade.Arcade
import com.sxtanna.mc.arcade.base.State
import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.area.manager.GameAreaManager
import com.sxtanna.mc.arcade.game.area.manager.impl.GameAreaManagerLocal
import com.sxtanna.mc.arcade.game.base.GameInfo
import com.sxtanna.mc.arcade.hook.base.ArcadePlugin
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

abstract class ArcadeGamePlugin : ArcadePlugin(), State, Listener
{
	
	abstract val info: GameInfo
	
	final override lateinit var arcade: Arcade
		private set
	
	final override fun onLoad()
	{
		arcade = checkNotNull(server.servicesManager.load(Arcade::class.java))
		{
			"Failed to retrieve arcade service"
		}
	}
	
	final override fun onEnable()
	{
		with(arcade)
		{
			loadGameInfo()
		}
		
		load()
	}
	
	final override fun onDisable()
	{
		with(arcade)
		{
			killGameInfo()
		}
		
		kill()
	}
	
	override fun load()
	{
		server.pluginManager.registerEvents(this, this)
	}
	
	override fun kill()
	{
		HandlerList.unregisterAll(this as Listener)
	}
	
	abstract fun make(): Game
	
	open fun area(): GameAreaManager
	{
		return GameAreaManagerLocal(this, info, dataFolder.resolve("maps"))
	}
	
}