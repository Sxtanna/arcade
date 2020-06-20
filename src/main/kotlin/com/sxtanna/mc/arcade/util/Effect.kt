package com.sxtanna.mc.arcade.util

import com.sxtanna.mc.arcade.base.Named
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

sealed class Effect : Named
{
	
	abstract fun give(entity: LivingEntity, time: Int)
	
	abstract fun take(entity: LivingEntity)
	
	
	abstract class PotionEffects protected constructor(private val type: PotionEffectType) : Effect()
	{
		
		override val name = type.name.toLowerCase().capitalize()
		
		
		override fun give(entity: LivingEntity, time: Int)
		{
			give(entity, time, 1)
		}
		
		override fun take(entity: LivingEntity)
		{
			entity.removePotionEffect(type)
		}
		
		fun give(entity: LivingEntity, time: Int, level: Int = 1, ambient: Boolean = true, particles: Boolean = false, icon: Boolean = true, force: Boolean = true)
		{
			entity.addPotionEffect(PotionEffect(type, time, level, ambient, particles, icon), force)
		}
		
	}
	
	
	object SLOW
		: PotionEffects(PotionEffectType.SLOW)
	
	object SPEED
		: PotionEffects(PotionEffectType.SPEED)
	
}