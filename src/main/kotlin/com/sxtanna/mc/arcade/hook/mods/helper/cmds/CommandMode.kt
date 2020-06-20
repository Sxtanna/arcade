package com.sxtanna.mc.arcade.hook.mods.helper.cmds

import com.sxtanna.mc.arcade.cmds.Command
import com.sxtanna.mc.arcade.cmds.Context
import com.sxtanna.mc.arcade.hook.base.ArcadePlugin
import org.bukkit.GameMode
import org.bukkit.entity.Player

class CommandMode(override val plugin: ArcadePlugin) : Command("gamemode")
{
	
	// /command {gamemode} {player}
	
	override fun Context.evaluate()
	{
		val sender = sender as? Player ?: return error("You must be a player for this")
		
		var gamemode = when(alias.toLowerCase())
		{
			"gms" -> GameMode.SURVIVAL
			"gma" -> GameMode.ADVENTURE
			"gmc" -> GameMode.CREATIVE
			"gmsp" -> GameMode.SPECTATOR
			else -> null
		}
		
		if (gamemode != null)
		{
			val userName = input.getOrNull(0)
			if (userName == null)
			{
				sender.gameMode = gamemode
				return reply("Your gamemode is now $gamemode.")
			}
			
			val user = server.getPlayer(userName)
			if (user != null)
			{
				sender.gameMode = gamemode
				return reply("Your gamemode is now $gamemode.", user)
			}
			
			return error("Player name $userName not found.")
		}
		
		val modeName = input.getOrNull(0)
		if (modeName != null)
		{
			gamemode = GameMode.values().find { it.name.equals(modeName, true) } ?: return error("Gamemode $modeName not found.")
		}
		
		gamemode ?: return
		
		val userName = input.getOrNull(1)
		if (userName == null)
		{
			sender.gameMode = gamemode
			return reply("Your gamemode is now $gamemode.")
		}
		
		val target = server.getPlayer(userName)
		if (target != null)
		{
			sender.gameMode = gamemode
			return reply("Your gamemode is now $gamemode.", target)
		}
		
		error("Player name $userName not found.")
	}
	
	override fun Context.complete(out: MutableList<String>)
	{
	
	}
}