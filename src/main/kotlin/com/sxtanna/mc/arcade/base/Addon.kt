package com.sxtanna.mc.arcade.base

import com.sxtanna.mc.arcade.hook.base.ArcadePlugin
import org.bukkit.NamespacedKey
import org.bukkit.Server
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.logging.Logger

interface Addon
{
	
	val plugin: ArcadePlugin
	
	val logger: Logger
		get() = plugin.logger
	
	val server: Server
		get() = plugin.server
	
	
	fun queue(function: () -> Unit): BukkitTask
	{
		return server.scheduler.runTask(plugin, function)
	}
	
	
	fun later(delay: Long, function: (BukkitRunnable) -> Unit): BukkitTask
	{
		return object : BukkitRunnable()
		{
			override fun run()
			{
				function.invoke(this)
			}
		}.runTaskLater(plugin, delay)
	}
	
	fun laterAsync(delay: Long, function: (BukkitRunnable) -> Unit): BukkitTask
	{
		return object : BukkitRunnable()
		{
			override fun run()
			{
				function.invoke(this)
			}
		}.runTaskLaterAsynchronously(plugin, delay)
	}
	
	
	fun timer(period: Long, delay: Long = 0L, function: (BukkitRunnable) -> Unit): BukkitTask
	{
		return object : BukkitRunnable()
		{
			override fun run()
			{
				function.invoke(this)
			}
		}.runTaskTimer(plugin, delay, period)
	}
	
	fun timerAsync(period: Long, delay: Long = 0L, function: (BukkitRunnable) -> Unit): BukkitTask
	{
		return object : BukkitRunnable()
		{
			override fun run()
			{
				function.invoke(this)
			}
		}.runTaskTimerAsynchronously(plugin, delay, period)
	}
	
	
	
	fun <T : Any> PersistentDataHolder.getData(type: PersistentDataType<*, T>, name: NamespacedKey = plugin.arcade.namespace): T?
	{
		return persistentDataContainer.get(name, type)
	}
	
	fun <T : Any> PersistentDataHolder.hasData(type: PersistentDataType<*, T>, name: NamespacedKey = plugin.arcade.namespace): Boolean
	{
		return persistentDataContainer.has(name, type)
	}
	
	fun <T : Any> PersistentDataHolder.setData(type: PersistentDataType<*, T>, data: T?, name: NamespacedKey = plugin.arcade.namespace)
	{
		if (data == null)
		{
			persistentDataContainer.remove(name)
		}
		else
		{
			persistentDataContainer.set(name, type, data)
		}
	}
	
	
	fun <T : Any> ItemStack.getData(type: PersistentDataType<*, T>, name: NamespacedKey = plugin.arcade.namespace): T?
	{
		return itemMeta?.getData(type, name)
	}
	
	fun <T : Any> ItemStack.hasData(type: PersistentDataType<*, T>, name: NamespacedKey = plugin.arcade.namespace): Boolean
	{
		return itemMeta?.hasData(type, name) ?: false
	}
	
	fun <T : Any> ItemStack.setData(type: PersistentDataType<*, T>, data: T?, name: NamespacedKey = plugin.arcade.namespace)
	{
		itemMeta = itemMeta.apply { setData(type, data, name) }
	}
	
	
}