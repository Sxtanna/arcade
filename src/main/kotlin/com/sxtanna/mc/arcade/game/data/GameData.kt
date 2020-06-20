@file:Suppress("ClassName")

package com.sxtanna.mc.arcade.game.data

import com.sxtanna.mc.arcade.base.Named
import com.sxtanna.mc.arcade.game.base.GameUser
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.EnumSet
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class GameData<T> : Named, ReadOnlyProperty<GameUser, T>
{
	
	final override val name by lazy { this::class.java.simpleName ?: "Unknown" }
	
	init
	{
		values += this
	}
	
	
	abstract val def: T
	
	open fun def(): T
	{
		return def
	}
	
	open fun get(data: Any): T
	{
		@Suppress("UNCHECKED_CAST")
		return data as T
	}
	
	override fun getValue(thisRef: GameUser, property: KProperty<*>): T
	{
		return thisRef.game[this]
	}
	
	
	infix fun <O> map(function: (T) -> O): ReadOnlyProperty<GameUser, O>
	{
		return object : ReadOnlyProperty<GameUser, O>
		{
			override fun getValue(thisRef: GameUser, property: KProperty<*>): O
			{
				return function.invoke(this@GameData.getValue(thisRef, property))
			}
		}
	}
	
	
	abstract class BoolData(final override val def: Boolean)
		: GameData<Boolean>()
	
	abstract class NumbData(final override val def: Number)
		: GameData<Number>()
	
	abstract class TextData(final override val def: String)
		: GameData<String>()
	
	abstract class EnumData<E : Enum<E>>(override val def: EnumSet<E>)
		: GameData<EnumSet<E>>()
	
	abstract class DataData<T>(override val def: T)
		: GameData<T>()
	
	abstract class FuncData<T>(private val atomic: Boolean, private val function: () -> T)
		: GameData<T>()
	{
		
		override val def by lazy { function.invoke() }
		
		
		override fun def(): T
		{
			if (atomic)
			{
				return function.invoke()
			}
			
			return def
		}
		
	}
	
	
	interface AreaOpt
	{
		fun apply(data: Any, area: World)
	}
	
	interface UserOpt
	{
		fun apply(data: Any, user: Player)
	}
	
	
	companion object
	{
		
		private val values = mutableSetOf<GameData<*>>()
		
		fun values(): Set<GameData<*>>
		{
			return values
		}
		
	}
	
}