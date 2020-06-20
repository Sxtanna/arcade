package com.sxtanna.mc.arcade.hook.mods.worlds.util

import org.bukkit.World
import org.bukkit.generator.ChunkGenerator
import java.util.Random

object CleanRoomGenerator : ChunkGenerator()
{
	
	override fun isParallelCapable(): Boolean
	{
		return true
	}
	
	override fun generateChunkData(world: World, random: Random, x: Int, z: Int, biome: BiomeGrid): ChunkData
	{
		return createChunkData(world)
	}
	
}