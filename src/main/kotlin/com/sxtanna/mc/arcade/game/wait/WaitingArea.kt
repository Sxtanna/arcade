package com.sxtanna.mc.arcade.game.wait

import com.sxtanna.mc.arcade.base.Addon
import com.sxtanna.mc.arcade.base.State
import com.sxtanna.mc.arcade.func.resetPlayer
import com.sxtanna.mc.arcade.hook.ArcadeBasePlugin
import com.sxtanna.mc.arcade.hook.mods.damage.base.HurtEvent
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockBurnEvent
import org.bukkit.event.block.BlockFadeEvent
import org.bukkit.event.block.BlockFormEvent
import org.bukkit.event.block.BlockFromToEvent
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityInteractEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.weather.WeatherChangeEvent
import java.util.logging.Level

class WaitingArea(override val plugin: ArcadeBasePlugin) : Addon, State, Listener
{
	
	internal var world = null as? World?
		private set
	internal var spawn = null as? Location?
		private set
	
	override fun load()
	{
		plugin.server.pluginManager.registerEvents(this, plugin)
		
		plugin.worlds.load("lobby")
		out@{ world ->
			
			if (world == null)
			{
				return@out plugin.logger.log(Level.WARNING, "failed to load lobby world")
			}
			
			this.world = world
			
			world.entities.forEach()
			{
				if (it is Player)
				{
					return@forEach
				}
				
				it.remove()
			}
			
			this.spawn = Location(world, 0.5, 143.0, 0.5, 90F, 0F)
			
			server.onlinePlayers.forEach(this::reset)
		}
	}
	
	override fun kill()
	{
		HandlerList.unregisterAll(this)
		
		world = null
		spawn = null
	}
	
	
	@EventHandler
	fun PlayerJoinEvent.onJoin()
	{
		reset(player)
	}
	
	@EventHandler
	fun PlayerQuitEvent.onQuit()
	{
		reset(player)
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun HurtEvent.onHurt()
	{
		if (damaged.world != world || damaged !is Player)
		{
			return
		}
		
		isCancelled = true
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun PlayerMoveEvent.onMove()
	{
		if (player.world != world || to.y > 50)
		{
			return
		}
		
		move(player)
		{}
	}
	
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun FoodLevelChangeEvent.onHunger()
	{
		if (entity.world != world)
		{
			return
		}
		
		isCancelled = true
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun CreatureSpawnEvent.onSpawn()
	{
		if (location.world != world || entity is ArmorStand || spawnReason == CreatureSpawnEvent.SpawnReason.CUSTOM)
		{
			return
		}
		
		isCancelled = true
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun EntityInteractEvent.onTrample()
	{
		if (entity.world != world || block.type != Material.FARMLAND)
		{
			return
		}
		
		isCancelled = true
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun BlockFromToEvent.onBlockChange()
	{
		if (block.world != world)
		{
			return
		}
		
		isCancelled = true
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun BlockBurnEvent.onBlockBurn()
	{
		if (block.world != world)
		{
			return
		}
		
		isCancelled = true
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun BlockFadeEvent.onBlockFade()
	{
		if (block.world != world)
		{
			return
		}
		
		isCancelled = true
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun BlockGrowEvent.onBlockGrow()
	{
		if (block.world != world)
		{
			return
		}
		
		isCancelled = true
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun BlockFormEvent.onBlockForm()
	{
		if (block.world != world)
		{
			return
		}
		
		isCancelled = true
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun BlockBreakEvent.onBlockBreak()
	{
		if (block.world != world || player.gameMode == GameMode.CREATIVE)
		{
			return
		}
		
		isCancelled = true
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun BlockPlaceEvent.onBlockPlace()
	{
		if (block.world != world || player.gameMode == GameMode.CREATIVE)
		{
			return
		}
		
		isCancelled = true
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun WeatherChangeEvent.onWeatherChange()
	{
		if (world != this@WaitingArea.world || !toWeatherState())
		{
			return
		}
		
		isCancelled = true
	}
	
	
	fun move(user: Player, done: (user: Player) -> Unit)
	{
		val spawn = spawn ?: return
		
		if (user.location.world == world && user.location.distance(spawn) < 20)
		{
			return done.invoke(user)
		}
		
		plugin.worlds.move(spawn, listOf(user), done)
	}
	
	
	private fun reset(user: Player)
	{
		move(user)
		{
			user.resetPlayer()
			user.setBedSpawnLocation(spawn, true)
			
			if (!user.isOp)
			{
				user.gameMode = GameMode.ADVENTURE
			}
			
			server.onlinePlayers.forEach()
			{ other ->
				other.showPlayer(plugin, user)
				user.showPlayer(plugin, other)
			}
		}
	}
	
}