package com.sxtanna.mc.arcade

import com.sxtanna.mc.arcade.base.Addon
import com.sxtanna.mc.arcade.base.State
import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.base.GameInfo
import com.sxtanna.mc.arcade.game.cmds.CommandGame
import com.sxtanna.mc.arcade.game.cmds.CommandMaps
import com.sxtanna.mc.arcade.game.data.GameDatas
import com.sxtanna.mc.arcade.game.wait.WaitingArea
import com.sxtanna.mc.arcade.hook.ArcadeBasePlugin
import com.sxtanna.mc.arcade.hook.ArcadeGamePlugin
import com.sxtanna.mc.arcade.hook.mods.worlds.test.PlayerRecording
import com.sxtanna.mc.arcade.time.Cooldown
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.logging.Level

class Arcade(override val plugin: ArcadeBasePlugin) : Addon, State, Listener
{
	
	val cooldowns = Cooldown()
	val namespace = NamespacedKey(plugin, "arcade")
	
	
	internal val commandGame = CommandGame(plugin)
	internal val commandMaps = CommandMaps(plugin)
	
	internal val waitingArea = WaitingArea(plugin)
	
	internal var currentGame = null as? Game?
	internal val cachedGames = mutableMapOf<GameInfo, ArcadeGamePlugin>()

	internal val recordings = PlayerRecording(plugin)
	
	
	/**
	 * Loads the arcade addon.
	 */
	override fun load()
	{
		recordings.load()

		this.cooldowns.load()
		
		this.waitingArea.load()
		
		this.commandGame.load()
		this.commandMaps.load()
		
		server.pluginManager.registerEvents(this, plugin)
		
		GameDatas
	}
	
	/**
	 * Kills the arcade addon.
	 */
	override fun kill()
	{
		recordings.kill()

		killGame()
		
		this.cooldowns.kill()
		
		this.waitingArea.kill()
		
		this.commandGame.kill()
		this.commandMaps.kill()
		
		this.cachedGames.clear()
		
		HandlerList.unregisterAll(this)
	}
	
	/**
	 * Retrieve a list of all loaded games.
	 */
	fun list(): List<GameInfo>
	{
		return cachedGames.keys.toList()
	}
	
	/**
	 * Retrieve a specific game by its name.
	 */
	fun find(name: String): GameInfo?
	{
		return list().find { it.name.equals(name, true) || it.exte.equals(name, true) }
	}
	
	/**
	 * Retrieve the current game being ran
	 */
	fun liveGame(): Game?
	{
		return this.currentGame
	}
	
	/**
	 * Load a game.
	 */
	fun loadGame(info: GameInfo): Game
	{
		val plugin = requireNotNull(cachedGames[info])
		{
			"Cannot load unregistered game: ${info.name}|${info.exte}"
		}
		
		val last = currentGame
		
		// handle cleanup of old game
		if (last != null)
		{
			// only cleanup game if it's different
			if (last.info == info)
			{
				return last
			}
			
			killGame()
		}
		
		val next = plugin.make()
		currentGame = next
		
		next.load()
		
		return next
	}
	
	/**
	 * Kill the current game.
	 *
	 * @return true if the game was killed successfully
	 */
	fun killGame(): Boolean
	{
		if (this.currentGame == null)
		{
			return false
		}
		
		try
		{
			this.currentGame?.kill()
		}
		catch (ex: Exception)
		{
			plugin.logger.log(Level.SEVERE, "failed to kill game: ${this.currentGame?.name}", ex)
			return false
		}
		
		this.currentGame = null
		
		return true
	}
	
	
	fun ArcadeGamePlugin.loadGameInfo()
	{
		cachedGames[info] = this
		plugin.logger.info("Successfully loaded game: ${info.name}|${info.exte}")
	}
	
	fun ArcadeGamePlugin.killGameInfo()
	{
		cachedGames -= info
		plugin.logger.info("Successfully killed game: ${info.name}|${info.exte}")
	}
	
	
	@EventHandler
	fun PlayerJoinEvent.onJoin()
	{
		liveGame()?.join(player)
	}
	
	@EventHandler
	fun PlayerQuitEvent.onQuit()
	{
		liveGame()?.quit(player)
	}
	
}