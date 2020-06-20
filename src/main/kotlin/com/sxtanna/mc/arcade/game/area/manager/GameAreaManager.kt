package com.sxtanna.mc.arcade.game.area.manager

import com.sxtanna.mc.arcade.base.Addon
import com.sxtanna.mc.arcade.base.State
import com.sxtanna.mc.arcade.func.AtomicCounter
import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.area.GameArea
import com.sxtanna.mc.arcade.game.base.GameInfo
import com.sxtanna.mc.arcade.game.base.GamePlace
import com.sxtanna.mc.arcade.hook.ArcadeGamePlugin
import org.bukkit.World

/**
 * A class that loads game areas... wow
 */
interface GameAreaManager : Addon, State
{
	
	override val plugin: ArcadeGamePlugin
	
	
	// the game this is for
	val info: GameInfo
	
	
	/**
	 * The amount of areas for this game
	 */
	fun amount(): Int
	
	/**
	 * A random area for the game
	 */
	fun random(done: (area: GameArea?) -> Unit)
	
	/**
	 * Get an area by it's name
	 */
	fun select(name: String, done: (area: GameArea?) -> Unit)
	
	/**
	 * Add an area to this game
	 */
	fun insert(area: GameArea, done: (pass: Boolean) -> Unit)
	
	/**
	 * The names of all areas
	 */
	fun names(): List<String>
	
	/**
	 * The actual areas
	 */
	fun areas(): List<GameArea>
	
	
	fun loadWorld(game: Game, area: GameArea, done: (data: World?) -> Unit)
	{
		plugin.arcade.plugin.worlds.scan()
		plugin.arcade.plugin.worlds.load(area.named, done)
	}
	
	fun killWorld(game: Game, area: GameArea)
	{
		val world = area.world ?: return
		val users = world.players
		
		if (users.isEmpty())
		{
			plugin.arcade.plugin.worlds.kill(world, save = false, deleteFolder = true, chunkRefresh = true)
			return
		}
		
		val wait = AtomicCounter(users.size)
		{
			plugin.arcade.plugin.worlds.kill(world, save = false, deleteFolder = true, chunkRefresh = true)
		}
		
		users.forEach()
		{ user ->
			game.move(user, GamePlace.LOBBY)
			{
				wait.count()
			}
		}
	}
	
}