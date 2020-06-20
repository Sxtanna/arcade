package com.sxtanna.mc.arcade.hook.mods.worlds.test

import com.sxtanna.mc.arcade.hook.base.ArcadePlugin
import com.sxtanna.mc.arcade.hook.mods.Module
import com.sxtanna.recorder.Recorder
import com.sxtanna.recorder.base.Record
import com.sxtanna.recorder.base.Target
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.entity.Villager
import org.bukkit.event.EventHandler
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.scheduler.BukkitTask
import java.util.Optional

internal class PlayerRecording(override val plugin: ArcadePlugin) : Module("Player Recording")
{
	
	private val rewinds = mutableMapOf<Player, BukkitTask>()
	private val targets = mutableMapOf<Player, TargetPlayer>()
	private val records = mutableMapOf<Player, Recorder<RecordPlayer>>()
	
	
	override fun load()
	{
		super.load()
		
		server.onlinePlayers.forEach()
		{ player ->
			targets[player] = TargetPlayer(player)
			records[player] = Recorder.create<RecordPlayer>(LIMIT)
		}
	}
	
	override fun kill()
	{
		super.kill()
		
		rewinds.clear()
		targets.clear()
		records.clear()
	}
	
	
	@EventHandler
	fun PlayerJoinEvent.onJoin()
	{
		targets[player] = TargetPlayer(player)
		records[player] = Recorder.create<RecordPlayer>(LIMIT)
	}
	
	@EventHandler
	fun PlayerQuitEvent.onQuit()
	{
		targets -= player
		records -= player
	}
	
	
	@EventHandler
	fun PlayerMoveEvent.onMove()
	{
		if (player in rewinds)
		{
			return
		}
		
		val record = records[player] ?: return
		val target = targets[player] ?: return
		
		record.record(target)
	}
	
	@EventHandler
	fun AsyncPlayerChatEvent.onChat()
	{
		if (!message.startsWith("#"))
		{
			return
		}
		
		val rewind = rewinds[player]
		val record = records[player] ?: return
		val target = targets[player] ?: return
		
		var staged = false
		var entity = null as? Entity?
		
		isCancelled = true
		
		val action: () -> Optional<RecordPlayer> = when (message.split(' ').firstOrNull())
		{
			"#cancel" ->
			{
				rewind?.cancel()
				entity?.remove()
				
				record.clear()
				
				rewinds -= player
				
				return
			}
			"#replay" ->
			{
				record::pull
			}
			"#rewind" ->
			{
				record::take
			}
			"#staged_replay" ->
			{
				staged = true
				record::pull
			}
			"#staged_rewind" ->
			{
				staged = true
				record::take
			}
			else      ->
			{
				return
			}
		}
		
		val amount = record.count()
		if (amount == 0)
		{
			return
		}
		
		queue()
		{
			if (staged)
			{
				val loc = action.invoke().orElse(null)?.location ?: return@queue
				
				entity = loc.world.spawn(loc, Villager::class.java)
				{
					it.setAI(false)
					it.setGravity(false)
					it.isInvulnerable = true
				}
			}
			
			rewinds[player] = timer(1)
			{
				if (record.count() == 0)
				{
					rewinds -= player
					record.clear()
					
					entity?.remove()
					
					return@timer it.cancel()
				}
				
				player.sendActionBar("executing... ${amount - record.count()}/$amount")
				
				if (!staged || entity == null)
				{
					action.invoke().ifPresent(target::revert)
				}
				else
				{
					action.invoke().map(RecordPlayer::location).ifPresent { entity?.teleport(it) }
				}
			}
		}
		
	}
	
	
	private companion object
	{
		const val LIMIT = 600
	}
	
	
	class RecordPlayer(val location: Location) : Record
	{
		
		override fun clone(): Optional<RecordPlayer>
		{
			return Optional.of(RecordPlayer(location.clone()))
		}
		
	}
	
	class TargetPlayer(val player: Player) : Target<RecordPlayer>
	{
		
		override fun revert(record: RecordPlayer)
		{
			player.teleport(record.location)
		}
		
		override fun record(): RecordPlayer
		{
			player.sendActionBar("recording...")
			return RecordPlayer(player.location.clone())
		}
		
	}
	
}