package com.sxtanna.mc.arcade.game.cmds

import com.sxtanna.mc.arcade.cmds.Command
import com.sxtanna.mc.arcade.cmds.Context
import com.sxtanna.mc.arcade.game.base.GameState
import com.sxtanna.mc.arcade.game.data.GameDatas
import com.sxtanna.mc.arcade.game.menu.MenuGame
import com.sxtanna.mc.arcade.hook.ArcadeBasePlugin
import org.bukkit.entity.Player

class CommandGame(override val plugin: ArcadeBasePlugin) : Command("game")
{
	
	override fun Context.evaluate()
	{
		if (input.isEmpty())
		{
			return MenuGame(plugin)[sender as? Player ?: return error("you must be a player")]
		}
		
		when (input[0].toLowerCase())
		{
			"load"  ->
			{
				if (input.size < 2)
				{
					return // tell them they need to specify the game
				}
				
				val info = plugin.arcade.find(input[1]) ?: return reply("Game named ${input[1]} not found")
				val game = plugin.arcade.loadGame(info)
				
				reply("successfully loaded game: ${info.name}")
				
				server.onlinePlayers.forEach()
				{ user ->
					game.join(user)
				}
			}
			"kill"  ->
			{
				val game = plugin.arcade.liveGame() ?: return reply("there is no game active")
				val good = plugin.arcade.killGame()
				
				reply("${if (good) "successfully killed" else "failed to kill"} ${game.name}")
			}
			"start" ->
			{
				val game = plugin.arcade.liveGame() ?: return reply("there is no game active")
				
				if (game.state() == GameState.READY)
				{
					game.reduceCountdown()
				}
				else
				{
					game.state(GameState.READY, GameState.SetCause.PLAYER)
				}
			}
			"stop"  ->
			{
				val game = plugin.arcade.liveGame() ?: return reply("there is no game active")
				
				if (input.size > 1 && input[1].equals("now", true))
				{
					game[GameDatas.OPTION_GAME_ENDS_INSTANTLY] = true
				}
				
				game.state(GameState.ENDED, GameState.SetCause.PLAYER)
			}
		}
	}
	
	override fun Context.complete(out: MutableList<String>)
	{
	
	}
	
}