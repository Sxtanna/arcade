package com.sxtanna.mc.arcade.util

import com.sxtanna.mc.arcade.base.Addon
import com.sxtanna.mc.arcade.base.State
import com.sxtanna.mc.arcade.hook.base.ArcadePlugin
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

class Marker(override val plugin: ArcadePlugin, private val target: LivingEntity, private val block: Material) : Addon, State
{
	
	private var update = null as? BukkitTask?
	private val marker = mutableSetOf<Entity>()
	
	
	override fun load()
	{
		marker += target.world.spawn(target.eyeLocation, ArmorStand::class.java)
		{ stand ->
			stand.isSmall = true
			stand.isVisible = false
			stand.isInvulnerable = true
			
			stand.setGravity(false)
			stand.setHelmet(ItemStack(block))
		}
		
		update = timer(1)
		{
			if (!target.isValid || marker.any { !it.isValid })
			{
				return@timer kill()
			}
			
			marker.forEach()
			{
				it.teleport(target.eyeLocation)
			}
		}
	}
	
	override fun kill()
	{
		update?.cancel()
		update = null
		
		marker.forEach(Entity::remove)
		marker.clear()
	}
	
}