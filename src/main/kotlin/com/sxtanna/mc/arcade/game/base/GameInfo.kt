package com.sxtanna.mc.arcade.game.base

import com.sxtanna.mc.arcade.base.Named
import org.bukkit.Material

data class GameInfo(override val name: String, val exte: String, val desc: List<String>, val icon: Material) : Named
{
	
	init
	{
		values += this
	}
	
	companion object
	{
		
		private val values = sortedSetOf<GameInfo>(compareBy { it.name })
		
		
		fun values(): Set<GameInfo>
		{
			return values
		}
		
		fun search(name: String): GameInfo?
		{
			return values.find { it.name.equals(name, true) }
		}
		
	}
	
}