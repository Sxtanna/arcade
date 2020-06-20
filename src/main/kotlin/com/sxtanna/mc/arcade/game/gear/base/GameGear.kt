package com.sxtanna.mc.arcade.game.gear.base

import com.sxtanna.mc.arcade.base.Gives
import com.sxtanna.mc.arcade.base.Icons
import com.sxtanna.mc.arcade.base.Named
import com.sxtanna.mc.arcade.base.Shown
import com.sxtanna.mc.arcade.base.State
import com.sxtanna.mc.arcade.base.Takes
import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.base.GameUser
import com.sxtanna.mc.arcade.game.data.GameData
import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import java.util.UUID
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class GameGear protected constructor(override val game: Game, final override val name: String) : Named, Shown, State, Icons, Gives<Player>, Takes<Player>, GameUser, Listener
{
	
	protected val using = mutableSetOf<UUID>()
	protected val parts = mutableSetOf<GameGearPart>()
	
	
	final override val internal = name
	final override val external = "${Colour.Bold}$internal"
	
	
	override fun load()
	{
		parts.forEach(GameGearPart::load)
		
		game.plugin.server.pluginManager.registerEvents(this, game.plugin)
	}
	
	override fun kill()
	{
		using.clear()
		parts.forEach(GameGearPart::kill)
		
		HandlerList.unregisterAll(this)
	}
	
	override fun icon(): ItemStack
	{
		return ItemStack(Material.GLASS)
	}
	
	override fun give(data: Player)
	{
		using += data.uniqueId
		
		parts.forEach()
		{
			it.give(data)
		}
	}
	
	override fun take(data: Player)
	{
		using -= data.uniqueId
		
		parts.forEach()
		{
			it.take(data)
		}
	}
	
	
	fun using(user: Player): Boolean
	{
		return user.uniqueId in using
	}
	
	
	protected operator fun GameGearPart.unaryPlus()
	{
		parts += this
	}
	
	
	fun <T : Any> option(data: GameData<T>): ReadOnlyProperty<Any, T>
	{
		return object : ReadOnlyProperty<Any, T>
		{
			override fun getValue(thisRef: Any, property: KProperty<*>): T
			{
				return game[data]
			}
		}
	}
	
	fun <O : Any, T : Any> option(data: GameData<O>, transformer: (O) -> T): ReadOnlyProperty<Any, T>
	{
		return object : ReadOnlyProperty<Any, T>
		{
			override fun getValue(thisRef: Any, property: KProperty<*>): T
			{
				return transformer.invoke(game[data])
			}
		}
	}
	
}