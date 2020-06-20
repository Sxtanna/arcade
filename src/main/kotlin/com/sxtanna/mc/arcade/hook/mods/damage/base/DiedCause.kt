package com.sxtanna.mc.arcade.hook.mods.damage.base

import com.sxtanna.mc.arcade.util.Colour
import com.sxtanna.mc.arcade.util.Helper.properName
import org.bukkit.entity.Arrow
import org.bukkit.entity.EntityType.*
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent.DamageCause.*
import org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALLING_BLOCK
import org.bukkit.event.entity.EntityDamageEvent.DamageCause.LIGHTNING

class DiedCause @JvmOverloads constructor(private val died: DiedEvent, // special ability used, ex. `Flog`
                                          private val ability: String? = null, // special way of dying, ex. `trampled` instead of `killed`
                                          private val special: String? = null, // proper name of cause, ex. `a Cactus`
                                          private val propers: String? = null, // newest damage logged, ex. `Flame:Sxtanna`
                                          private val definite: Boolean = false,
                                          private val damaged: String? = died.hurtEvent.arcade.cooldowns.get(died.hurtEvent.damaged.uniqueId, HurtEvent.DAMAGE_TAG))
{
	
	fun deathMessage(): String?
	{
		val builder = StringBuilder()
		
		builder.append(Colour.Val)
				.append(died.hurtEvent.damaged.name)
				.append(Colour.Reset)
				.append(" ")
		
		if (definite && special != null)
		{
			builder.append(Colour.Val)
					.append(special)
		}
		else
		{
			// how they were killed [shot|killed]
			
			builder.append(Colour.White)
					.append("was ")
					.append(Colour.Msg)
					.append(special ?: if (died.hurtEvent.projectile != null) "shot" else "killed")
					.append(Colour.White)
					.append(" by ")
					.append(Colour.Reset)
			
			if (damaged != null)
			{
				val damaged = damaged.split(":").toTypedArray()
				builder.append(Colour.Val)
						.append(damaged[1])
						.append(Colour.White)
						.append(" with ")
						.append(Colour.Gold)
						.append(damaged[0])
			}
			else if (died.hurtEvent.damager != null)
			{
				val human = died.hurtEvent.damager is Player
				
				builder.append(Colour.Val)
						.append(if (human) "" else "a ")
						.append(properName(died.hurtEvent.damager))
						.append(Colour.Reset)
				
				if (ability != null)
				{
					builder.append(Colour.White)
							.append(" using ")
							.append(Colour.Gold)
							.append(ability)
							.append(Colour.Reset)
				}
				else if (died.hurtEvent.projectile != null)
				{
					val shot = died.hurtEvent.projectile
					if (shot.hasMetadata(HurtEvent.WEAPON_TAG))
					{
						builder.append(Colour.White)
								.append(" with ")
								.append(Colour.Gold)
								.append(shot.getMetadata(HurtEvent.WEAPON_TAG)[0].asString())
					}
				}
			}
			else if (died.hurtEvent.projectile != null)
			{
				val arrow = died.hurtEvent.projectile is Arrow
				builder.append(Colour.Val)
						.append(if (arrow) "an " else "a ")
						.append(properName(died.hurtEvent.projectile))
			}
			else if (propers != null)
			{
				builder.append(Colour.Val)
						.append(propers)
			}
		}
		
		
		builder.append(Colour.White)
				.append(".")
				.append(Colour.Reset)
		
		if (builder.contains("shot") && died.hurtEvent.headshotPassed && !died.hurtEvent.hShotBypassing)
		{
			builder.append(' ')
					.append(Colour.Red)
					.append(Colour.Bold)
					.append("HEADSHOT!")
					.append(Colour.Reset)
		}
		
		return Colour.color(builder.toString())
	}
	
	companion object
	{
		
		fun of(event: DiedEvent): DiedCause
		{
			return when (event.hurtEvent.cause)
			{
				ENTITY_ATTACK   ->
				{
					var special: String? = null
					
					when (event.hurtEvent.damager!!.type)
					{
						HUSK, DROWNED, ZOMBIE, ZOMBIE_VILLAGER -> special = "eaten"
						SPIDER, CAVE_SPIDER                    -> special = "bitten"
						GHAST, CREEPER                         -> special = "exploded"
					}
					
					DiedCause(event, null, special, null)
				}
				FALL            -> DiedCause(event, null, null, "fall damage")
				CONTACT         -> DiedCause(event, null, "pricked", "a cactus")
				SUFFOCATION     -> DiedCause(event, null, "suffocated", "a block")
				VOID            -> DiedCause(event, null, "absorbed", "the void")
				FALLING_BLOCK   -> DiedCause(event, null, "crushed", "a block")
				STARVATION      -> DiedCause(event, null, "starved", null, true)
				DROWNING        -> DiedCause(event, null, "drowned", null, true)
				FIRE, FIRE_TICK -> DiedCause(event, null, "burned", "a fire")
				LAVA            -> DiedCause(event, null, "melted", "lava")
				LIGHTNING       -> DiedCause(event, null, "struck", "lightning")
				else            ->
				{
					DiedCause(event)
				}
			}
		}
	}
	
}