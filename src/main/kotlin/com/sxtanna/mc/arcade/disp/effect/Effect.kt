package com.sxtanna.mc.arcade.disp.effect

import com.sxtanna.mc.arcade.base.Addon
import com.sxtanna.mc.arcade.base.State
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import java.util.concurrent.atomic.AtomicInteger

abstract class Effect(val type: Particle) : Addon, State
{
	
	val data = mutableMapOf<EffectData<*>, Any?>()
	val inst = mutableSetOf<Instance>()
	
	
	abstract fun step(instance: Instance)
	
	
	override fun load()
	{
	
	}
	
	override fun kill()
	{
		data.clear()
		inst.toList().forEach(Instance::kill)
		inst.clear()
	}
	
	
	fun make(ent: Entity): Instance
	{
		return Instance { ent.location }
	}
	
	fun make(pos: Location): Instance
	{
		return Instance { pos }
	}
	
	fun show(loc: Location, vec: Vector, function: (Location) -> Unit)
	{
		loc.add(vec)
		function.invoke(loc)
		loc.subtract(vec)
	}
	
	
	protected fun generate(amount: Int, function: (Int) -> Vector): List<Vector>
	{
		val vectors = mutableListOf<Vector>()
		
		repeat(amount)
		{
			vectors += function(it)
		}
		
		return vectors
	}
	
	
	inner class Instance(val pos: () -> Location) : State
	{
		
		private val step = AtomicInteger()
		private val data = mutableMapOf<EffectData<*>, Any?>()
		
		
		override fun load()
		{
			this.data += this@Effect.data
			
			inst += this
		}
		
		override fun kill()
		{
			step.set(0)
			data.clear()
			
			inst -= this
		}
		
		
		fun step(): Int
		{
			return step.getAndIncrement()
		}
		
		
		operator fun <T> get(data: EffectData<T>): T
		{
			return this.data[data]?.let(data::get) ?: data.def()
		}
		
		operator fun <T> set(data: EffectData<T>, value: T)
		{
			this.data[data] = value
		}
		
	}
	
	
	abstract class EffectData<T>
	{
		
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
		
		abstract class BoolData(final override val def: Boolean)
			: EffectData<Boolean>()
		
		abstract class NumbData(final override val def: Number)
			: EffectData<Number>()
		
	}
	
}