package com.sxtanna.mc.arcade.hook.mods.worlds

import com.sxtanna.mc.arcade.data.base.Vec3
import com.sxtanna.mc.arcade.func.AtomicCounter
import com.sxtanna.mc.arcade.func.KORM
import com.sxtanna.mc.arcade.hook.ArcadeBasePlugin
import com.sxtanna.mc.arcade.hook.mods.Module
import com.sxtanna.mc.arcade.hook.mods.worlds.cmds.CommandWorld
import com.sxtanna.mc.arcade.hook.mods.worlds.util.CleanRoomGenerator
import com.sxtanna.mc.arcade.hook.mods.worlds.util.WorldData
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.WorldType
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.event.world.WorldLoadEvent
import org.bukkit.event.world.WorldUnloadEvent
import org.bukkit.generator.ChunkGenerator
import java.util.logging.Level
import kotlin.math.pow

internal class WorldsModule(override val plugin: ArcadeBasePlugin) : Module("Worlds")
{
	
	private val cached = mutableSetOf<String>()
	private val loaded = mutableSetOf<String>()
	
	private val commandWorld = CommandWorld(plugin)
	
	override fun load()
	{
		super.load()
		
		scan()
		
		commandWorld.load()
		
		server.worlds.map(World::getName).toCollection(loaded)
		
		KORM.pushWith<ChunkGenerator>
		{ writer, data ->
			if (data == null)
			{
				writer.writeBase("null")
			}
			else
			{
				writer.writeBase(data::class.java.simpleName)
			}
		}
		KORM.pullWith<ChunkGenerator>
		{ _, types ->
			when (types.singleOrNull()?.asBase()?.data?.toString())
			{
				"CleanRoomGenerator" ->
				{
					CleanRoomGenerator
				}
				else                 ->
				{
					null
				}
			}
		}
	}
	
	override fun kill()
	{
		super.kill()
		
		commandWorld.kill()
		
		cached.clear()
		loaded.clear()
	}
	
	
	fun cached(): List<String>
	{
		return cached.toList()
	}
	
	fun loaded(): List<String>
	{
		return loaded.toList()
	}
	
	
	fun load(name: String, done: (world: World?) -> Unit)
	{
		if (name !in cached)
		{
			return done.invoke(null)
		}
		
		val world = server.getWorld(name)
		
		if (world != null)
		{
			return done.invoke(world)
		}
		
		val make = WorldCreator(name)
		
		val data = KORM.pull(server.worldContainer.resolve(name).resolve("data.korm")).to<WorldData>()
		
		if (data != null)
		{
			if (data.type != null)
			{
				make.type(data.type)
				
				if (data.type == WorldType.CUSTOMIZED)
				{
					make.generator(CleanRoomGenerator)
				}
			}
			if (data.envr != null)
			{
				make.environment(data.envr)
			}
			if (data.gens != null)
			{
				make.generator(data.gens)
			}
		}
		
		queue { done.invoke(make.createWorld()) }
	}
	
	fun load(world: World, center: Vec3, radius: Int, state: (counter: AtomicCounter) -> Unit, done: () -> Unit)
	{
		val count = AtomicCounter((((radius - 1) * 2.0) + 1).pow(2.0).toInt())
		{
			done.invoke()
		}
		
		val oX = center.x.toInt() shl 4
		val oZ = center.z.toInt() shl 4
		
		for (x in -(radius - 1) until radius)
		{
			for (z in -(radius - 1) until radius)
			{
				world.getChunkAtAsync(oX + x, oZ + z).whenComplete()
				{ _, _ ->
					count.count()
					state.invoke(count)
				}
			}
		}
	}
	
	fun kill(world: World, save: Boolean = true, deleteFolder: Boolean = false, chunkRefresh: Boolean = false)
	{
		if (chunkRefresh)
		{
			println("performing chunk refresh on: ${world.name}")
			
			world.loadedChunks.forEach()
			{ chunk ->
				chunk.unload(false)
			}
			
			return
		}
		
		try
		{
			require(server.unloadWorld(world, save))
			{
				"unload cancelled"
			}
		}
		catch (ex: Exception)
		{
			return plugin.logger.log(Level.SEVERE, "failed to unload world: ${world.name}", ex)
		}
		
		loaded -= world.name
		
		if (deleteFolder)
		{
			cached -= world.name
			
			if (!world.worldFolder.deleteRecursively())
			{
				world.worldFolder.deleteOnExit()
			}
		}
	}
	
	
	fun make(name: String, type: WorldType, envr: World.Environment, done: (world: World?) -> Unit = {})
	{
		val creator = WorldCreator(name)
		creator.environment(envr)
		
		if (type != WorldType.CUSTOMIZED)
		{
			creator.type(type)
		}
		else
		{
			creator.type(WorldType.FLAT)
			creator.generator(CleanRoomGenerator)
		}
		
		val world = creator.createWorld()
		
		if (world != null)
		{
			if (type == WorldType.CUSTOMIZED)
			{
				val block = world.getBlockAt(0, 100, 0)
				block.type = Material.BEDROCK
			}
			
			world.spawnLocation = world.getHighestBlockAt(0, 0).location
			world.save()
			
			if (envr != World.Environment.NORMAL || creator.generator() != null)
			{
				KORM.push(WorldData(type, envr, creator.generator()), server.worldContainer.resolve(world.name).resolve("data.korm"))
			}
		}
		
		done.invoke(world)
	}
	
	fun move(world: World, targets: Collection<Player>, done: (Player) -> Unit = {})
	{
		move(world.spawnLocation, targets, done)
	}
	
	fun move(location: Location, targets: Collection<Player>, done: (Player) -> Unit = {})
	{
		var moved = 10
		var block = location.block
		
		while (!block.getRelative(BlockFace.DOWN).type.isSolid && moved-- > 0)
		{
			block = block.getRelative(BlockFace.DOWN)
		}
		
		val safe = block.getRelative(BlockFace.DOWN).type.isSolid
		
		targets.forEach()
		{ user ->
			user.teleportAsync(location).whenComplete()
			{ _, _ ->
				done.invoke(user)
				
				if (!safe)
				{
					user.allowFlight = true
					user.isFlying = true
				}
			}
		}
	}
	
	
	fun scan()
	{
		val files = server.worldContainer.listFiles()?.filterNotNull()?.toMutableList() ?: return
		
		cached.clear()
		
		// remove
		files.removeAll { it.isFile }
		
		files.forEach()
		{ directory ->
			
			val isWorldFolder = directory.list()?.contains("level.dat") ?: false
			
			if (isWorldFolder)
			{
				cached += directory.name
			}
		}
	}
	
	
	@EventHandler
	fun WorldInitEvent.onInit()
	{
		world.keepSpawnInMemory = false
	}
	
	@EventHandler
	fun WorldLoadEvent.onLoad()
	{
		cached += world.name
		loaded += world.name
	}
	
	@EventHandler
	fun WorldUnloadEvent.onKill()
	{
		loaded -= world.name
	}
	
}