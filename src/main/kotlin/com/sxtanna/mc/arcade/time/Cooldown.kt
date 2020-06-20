package com.sxtanna.mc.arcade.time

import com.sxtanna.mc.arcade.base.State
import com.sxtanna.mc.arcade.hook.mods.damage.base.HurtEvent
import com.sxtanna.mc.arcade.time.Cooldown.Entry.Meta
import com.sxtanna.mc.arcade.time.Cooldown.Entry.Meta.Data
import com.sxtanna.mc.arcade.time.reply.Reply
import org.bukkit.entity.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max

class Cooldown : State
{
	
	private var thread = null as? ScheduledExecutorService?
	
	private val values = ConcurrentHashMap<UUID, Value>()
	private val notify = ConcurrentHashMap<AtomicLong, MutableSet<Entry.Cool>>()
	
	
	override fun load()
	{
		thread = Executors.newSingleThreadScheduledExecutor()
		
		thread?.scheduleAtFixedRate(this::clean, 0L, 1L, TimeUnit.MINUTES)
		thread?.scheduleAtFixedRate(this::reply, 0L, 1L, NOTIFY_PRECISION)
	}
	
	override fun kill()
	{
		thread?.shutdownNow()
		thread = null
		
		notify.values.forEach { it.clear() }
		notify.clear()
		
		values.values.forEach { it.clear() }
		values.clear()
	}
	
	
	fun use(uuid: UUID, name: String, time: Long, unit: TimeUnit, done: Reply? = null): Boolean
	{
		return values.computeIfAbsent(uuid, ::Value).use(name, time, unit, done)
	}
	
	
	fun get(uuid: UUID, name: String): String?
	{
		val value = values[uuid]
		if (value == null || value.empty())
		{
			return null
		}
		
		return value.get(name)
	}
	
	fun add(uuid: UUID, name: String, time: Long, unit: TimeUnit, info: String, weight: Int)
	{
		values.computeIfAbsent(uuid, ::Value).add(name, time, unit, info, weight)
	}
	
	
	fun stop(uuid: UUID, name: String)
	{
		values[uuid]?.stop(name)
	}
	
	fun time(uuid: UUID, name: String): Long
	{
		return values[uuid]?.time(name) ?: -1L
	}
	
	fun hurt(hurt: HurtEvent, time: Long, unit: TimeUnit, info: String)
	{
		add(hurt.damaged.uniqueId, HurtEvent.DAMAGE_TAG, time, unit, "$info:${hurt.damager?.name}", hurt.damage.toInt())
	}
	
	
	private fun clean()
	{
		val iter = values.values.iterator()
		
		iter.forEachRemaining()
		{ next ->
			next.clean()
			
			if (next.empty())
			{
				iter.remove()
			}
		}
	}
	
	private fun reply()
	{
		val iter = notify.entries.iterator()
		
		iter.forEachRemaining()
		{ next ->
			if (next.key.decrementAndGet() > 0)
			{
				return@forEachRemaining
			}
			
			val entry = next.value
			entry.forEach()
			{
				if (!it.cancelled)
				{
					it.reply()
				}
			}
			
			entry.clear()
			iter.remove()
		}
	}
	
	
	override fun equals(other: Any?): Boolean
	{
		if (this === other) return true
		if (other !is Cooldown) return false
		
		if (values != other.values) return false
		if (notify != other.notify) return false
		if (thread != other.thread) return false
		
		return true
	}
	
	override fun hashCode(): Int
	{
		var result = values.hashCode()
		result = 31 * result + notify.hashCode()
		result = 31 * result + thread.hashCode()
		return result
	}
	
	override fun toString(): String
	{
		return "Cooldown(values: ${values.size}, notify: ${notify.size})"
	}
	
	
	private sealed class Entry
	{
		
		abstract val name: String
		abstract val over: Long
		
		var cancelled = false
		
		
		open fun completed(): Boolean
		{
			return System.currentTimeMillis() >= this.over
		}
		
		open fun remaining(): Long
		{
			return max(0, this.over - System.currentTimeMillis())
		}
		
		
		data class Cool(override val name: String, override val over: Long, val done: Reply?) : Entry()
		{
			
			fun reply()
			{
				done?.invoke(this.name)
			}
			
		}
		
		data class Meta(override val name: String, override val over: Long) : Entry()
		{
			
			val data = mutableSetOf<Data>()
			
			
			override fun completed(): Boolean
			{
				return data.isEmpty() || data.all { it.completed() }
			}
			
			
			data class Data(val info: String, val weight: Int, val over: Long)
			{
				
				fun completed(): Boolean
				{
					return System.currentTimeMillis() >= this.over
				}
				
				fun remaining(): Long
				{
					return max(0, this.over - System.currentTimeMillis())
				}
				
			}
			
			fun add(info: String, weight: Int, over: Long)
			{
				data += Data(info, weight, over)
			}
			
		}
		
	}
	
	private inner class Value(uuid: UUID)
	{
		
		private val data = ConcurrentHashMap<String, Entry>()
		
		
		fun use(name: String, time: Long, unit: TimeUnit, done: Reply?): Boolean
		{
			val old = data[name]
			
			if (old == null || old.completed())
			{
				val cool = Entry.Cool(name, System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(time, unit), done)
				this.data[name] = cool
				
				if (done != null)
				{
					notify.getOrPut(AtomicLong(NOTIFY_PRECISION.convert(time, unit)), ::mutableSetOf).add(cool)
				}
				
				return true
			}
			
			return false
		}
		
		fun add(name: String, time: Long, unit: TimeUnit, info: String, weight: Int)
		{
			val meta = this.data.getOrPut(name)
			{
				Meta(name, -1)
			}
			
			(meta as Meta).add(info, weight, System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(time, unit))
		}
		
		fun get(name: String): String?
		{
			val meta = data[name] as? Meta ?: return null
			var best = null as? Data?
			
			meta.data.removeIf { it.completed() }
			
			for (data in meta.data)
			{
				if (best == null || (data.remaining() > best.remaining() || data.weight > best.remaining()) && !data.completed())
				{
					best = data
				}
			}
			
			return best?.info
		}
		
		
		fun time(name: String): Long
		{
			return data[name]?.remaining() ?: -1L
		}
		
		fun stop(name: String)
		{
			data.remove(name)?.cancelled = true
		}
		
		
		fun empty(): Boolean
		{
			return data.isEmpty()
		}
		
		fun clear()
		{
			data.clear()
		}
		
		fun clean()
		{
			data.entries.removeIf { it.value.completed() }
		}
		
	}
	
	
	private companion object
	{
		
		val NOTIFY_PRECISION = TimeUnit.MILLISECONDS
	}
	
}