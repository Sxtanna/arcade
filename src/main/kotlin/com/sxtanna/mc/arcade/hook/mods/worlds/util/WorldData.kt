package com.sxtanna.mc.arcade.hook.mods.worlds.util

import org.bukkit.World
import org.bukkit.WorldType
import org.bukkit.generator.ChunkGenerator

data class WorldData(val type: WorldType?, val envr: World.Environment?, val gens: ChunkGenerator?)