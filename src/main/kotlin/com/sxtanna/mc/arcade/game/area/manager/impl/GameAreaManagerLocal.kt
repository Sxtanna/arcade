package com.sxtanna.mc.arcade.game.area.manager.impl

import com.sxtanna.mc.arcade.func.KORM
import com.sxtanna.mc.arcade.func.ZipState
import com.sxtanna.mc.arcade.func.archive
import com.sxtanna.mc.arcade.func.extract
import com.sxtanna.mc.arcade.game.area.GameArea
import com.sxtanna.mc.arcade.game.area.manager.GameAreaManager
import com.sxtanna.mc.arcade.game.base.GameInfo
import com.sxtanna.mc.arcade.hook.ArcadeGamePlugin
import java.io.File

/**
 * A game area loader that uses local korm flat files
 */
class GameAreaManagerLocal(override val plugin: ArcadeGamePlugin, override val info: GameInfo, private val directory: File) : GameAreaManager
{
	
	private var index = null as? GameAreaIndex?
	private val cache = mutableMapOf<String, GameArea>()
	
	
	override fun load()
	{
		if (!directory.exists())
		{
			directory.mkdirs()
		}
		
		this.index = KORM.pull(directory.resolve("index.korm")).to()
		
		if (this.index == null)
		{
			val index = GameAreaIndex(mutableListOf())
			KORM.push(index, directory.resolve("index.korm"))
			
			this.index = index
		}
	}
	
	override fun kill()
	{
		index = null
		
		cache.values.forEach()
		{
			plugin.arcade.plugin.worlds.kill(it.world ?: return@forEach, save = false, deleteFolder = true)
			it.world = null
		}
		
		cache.clear()
	}
	
	
	override fun amount(): Int
	{
		return index?.names?.size ?: 0
	}
	
	
	override fun random(done: (area: GameArea?) -> Unit)
	{
		// get a random name, or call done with null if none
		val name = index?.names?.random() ?: return done.invoke(null)
		
		// select the area
		select(name, done)
	}
	
	
	override fun select(name: String, done: (data: GameArea?) -> Unit)
	{
		// get the real name of the area, or call done if none or not found
		val real = index?.names?.find { it.equals(name, true) } ?: return done.invoke(null)
		
		val area = cache[real] ?: loadArea(real)
		if (area == null)
		{
			return done.invoke(null)
		}
		
		if (!server.worldContainer.resolve(real).exists())
		{
			loadArea(real)
		}
		
		done.invoke(area)
	}
	
	override fun insert(area: GameArea, done: (pass: Boolean) -> Unit)
	{
		cache[area.named] = area
		
		done.invoke(saveArea(area))
	}
	
	
	override fun names(): List<String>
	{
		return index?.names ?: emptyList()
	}
	
	override fun areas(): List<GameArea>
	{
		return cache.values.toList()
	}
	
	
	private fun loadArea(name: String): GameArea?
	{
		// this zip contains the game area information and the actual world
		val path = server.worldContainer.resolve(name)
		val file = directory.resolve("$name.zip")
		
		if (extract(file, path) != ZipState.PASS)
		{
			return null
		}
		
		val area = KORM.pull(path.resolve("area.korm")).to<GameArea>()
		if (area != null)
		{
			cache[name] = area
			
			// shuffle spawn points
			area.point.values.forEach()
			{
				it.shuffle()
			}
		}
		
		
		
		return area
	}
	
	private fun saveArea(area: GameArea): Boolean
	{
		val path = server.worldContainer.resolve(area.named)
		val file = directory.resolve("${area.named}.zip")
		
		KORM.push(area, path.resolve("area.korm"))
		
		return archive(path, file) == ZipState.PASS
	}
	
	
	private data class GameAreaIndex(val names: MutableList<String>)
	
}