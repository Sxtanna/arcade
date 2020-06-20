package com.sxtanna.mc.arcade.hook.mods.damage.base

import com.sxtanna.mc.arcade.Arcade
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import org.bukkit.event.entity.EntityDamageEvent.DamageCause
import kotlin.math.max

class HurtEvent(damage: Double, val damaged: LivingEntity, val damager: LivingEntity?, val projectile: Projectile?, val cause: DamageCause, val arcade: Arcade) : Event(), Cancellable
{
	
	var damage: Double = damage
		set(value) {
			field = max(0.0, value)
		}
	
	// whether this event bypasses armor damage reduction
	var armorBypassing = false
	
	// whether this event bypasses damage delays
	var delayBypassing = false
	
	// whether this event bypasses damage knockback
	var knockBypassing = false
	
	// whether this event bypasses headshot damage
	var hShotBypassing = true
	
	// whether this event will actually do damage
	var hurtsBypassing = false
	
	
	var headshotDamage = -1.0
	val headshotPassed = projectile != null && damaged is Player && (projectile.location.y - damaged.location.y) > 1.35
	
	
	var knockRise = if (damaged.isOnGround) 0.08 else 0.20
	
	var knockBack = if (damaged.isOnGround) 0.09 else 0.02
	
	
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
	
	companion object
	{
	
		const val DAMAGE_TAG = "damage"
		const val WEAPON_TAG = "weapon"
		
		@JvmStatic
		val handlerList = HandlerList()
	}
	
}