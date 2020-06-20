package com.sxtanna.mc.arcade.hook.mods.helper.cmds

import com.sxtanna.mc.arcade.cmds.Command
import com.sxtanna.mc.arcade.cmds.Context
import com.sxtanna.mc.arcade.hook.base.ArcadePlugin
import org.bukkit.entity.Player

class CommandFly(override val plugin: ArcadePlugin) : Command("fly")
{
	
	override fun Context.evaluate()
	{
		val player = sender as? Player ?: return error("You must be a player")
		
		
		val userName = input.getOrNull(0)
		if (userName == null)
		{
			return toggleFlight(player)
		}
		
		val user = server.getPlayer(userName)
		if (user != null)
		{
			return toggleFlight(user)
		}
		
		error("Player named $userName not found")
	}
	
	override fun Context.complete(out: MutableList<String>)
	{
	
	}
	
	
	private fun Context.toggleFlight(player: Player)
	{
		player.allowFlight = !player.allowFlight
		
		if (player.allowFlight && !player.isOnGround)
		{
			player.isFlying = true
		}
		
		reply("Flight mode: ${if (player.allowFlight) "enabled" else "disabled"}")
	}
	
}