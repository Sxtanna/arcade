package com.sxtanna.mc.arcade.hook.mods.damage.base

import org.bukkit.Location
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.inventory.ItemStack

class DiedEvent(val hurtEvent: HurtEvent, val drops: MutableList<ItemStack>) : Event(), Cancellable
{
	
	val causeOfDeath = DiedCause.of(this)
	
	
	var shouldDropItems = true
	var shouldDoRespawn = true
	
	var respawnLocation = null as? Location?
	
	
	private var cancelled = false
	
	override fun isCancelled(): Boolean
	{
		return cancelled
	}
	
	override fun setCancelled(cancel: Boolean)
	{
		cancelled = cancel
	}
	
	override fun getHandlers(): HandlerList
	{
		return handlerList
	}
	
	private companion object
	{
		@JvmStatic
		val handlerList = HandlerList()
	}
	
}