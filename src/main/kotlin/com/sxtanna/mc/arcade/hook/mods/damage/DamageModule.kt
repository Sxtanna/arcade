package com.sxtanna.mc.arcade.hook.mods.damage

import com.sxtanna.mc.arcade.func.randChance
import com.sxtanna.mc.arcade.hook.ArcadeBasePlugin
import com.sxtanna.mc.arcade.hook.mods.Module
import com.sxtanna.mc.arcade.hook.mods.damage.base.DiedEvent
import com.sxtanna.mc.arcade.hook.mods.damage.base.HurtEvent
import com.sxtanna.mc.arcade.hook.mods.damage.cmds.CommandDamage
import org.bukkit.EntityEffect
import org.bukkit.GameRule
import org.bukkit.Material.AIR
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

internal class DamageModule(override val plugin: ArcadeBasePlugin) : Module("Damage")
{
	
	var delay = true
	var knock = true
	var pause = false
	
	private val commandDamage = CommandDamage(plugin)
	
	
	override fun load()
	{
		super.load()
		
		commandDamage.load()
	}
	
	override fun kill()
	{
		super.kill()
		
		commandDamage.kill()
	}
	
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	fun EntityDamageEvent.onDamage()
	{
		if (entity !is LivingEntity)
		{
			return // not alive, can't be hurt
		}
		
		isCancelled = true
		
		val finalDamaged: LivingEntity = entity as LivingEntity
		val finalDamager: LivingEntity?
		
		val projectile: Projectile?
		
		
		val damager = (this as? EntityDamageByEntityEvent)?.damager
		
		if (damager !is Projectile)
		{
			projectile = null
			finalDamager = damager as? LivingEntity
		}
		else
		{
			projectile = damager
			
			val shooter = projectile.shooter
			finalDamager = if (shooter !is LivingEntity)
			{
				null
			}
			else
			{
				shooter
			}
		}
		
		val hurt = HurtEvent(damage, finalDamaged, finalDamager, projectile, cause, plugin.arcade)
		if (!hurt.callEvent())
		{
			return
		}
		
		if (projectile != null && !projectile.isDead)
		{
			projectile.remove()
		}
		
		val startDamagedHealth = finalDamaged.health
		if (startDamagedHealth <= 0.0)
		{
			return // already dead yo
		}
		
		val startDamage = hurt.damage
		var finalDamage = if (hurt.armorBypassing)
		{
			startDamage
		}
		else
		{
			val armor = finalDamaged.getAttribute(Attribute.GENERIC_ARMOR)?.value ?: 0.0
			val tough = finalDamaged.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS)?.value ?: 0.0
			
			startDamage * (1 - min(20.0, max(armor / 5.0, armor - startDamage / (2 + tough / 4.0))) / 25.0)
		}
		
		if (hurt.headshotPassed && !hurt.hShotBypassing)
		{
			if (hurt.headshotDamage == -1.0)
			{
				hurt.headshotDamage = finalDamage * 0.25
			}
			
			finalDamage += hurt.headshotDamage
		}
		
		if (finalDamage <= 0.0)
		{
			return // no damage to do
		}
		
		if (!hurt.delayBypassing && delay && !plugin.arcade.cooldowns.use(finalDamaged.uniqueId, DAMAGE_TAG, 500, TimeUnit.MILLISECONDS))
		{
			return // can't be hurt yet
		}
		
		finalDamaged.playEffect(EntityEffect.HURT)
		
		if (!hurt.knockBypassing && knock && finalDamager != null)
		{
			// knock away
			val vector = finalDamaged.location.toVector().subtract(finalDamager.location.toVector()).normalize()
			vector.x = finalDamage * hurt.knockBack * vector.x
			vector.y = finalDamage * hurt.knockRise
			vector.z = finalDamage * hurt.knockBack * vector.z
			
			if (vector.x.isFinite() && vector.y.isFinite() && vector.z.isFinite())
			{
				finalDamaged.velocity = vector
			}
		}
		
		if (hurt.hurtsBypassing || pause)
		{
			return // actually taking damage is paused
		}
		
		val finalDamagedHealth = max(0.0, startDamagedHealth - finalDamage)
		if (finalDamagedHealth > 0.0)
		{
			finalDamaged.health = finalDamagedHealth
			return // they aren't dead yet
		}
		
		val died = DiedEvent(hurt, getItems(finalDamaged).toMutableList())
		if (!died.callEvent())
		{
			finalDamaged.health = 0.5
			return // they can't be killed
		}
		
		if (died.shouldDropItems)
		{
			val keepInventory = finalDamaged.world.getGameRuleValue(GameRule.KEEP_INVENTORY) ?: false
			
			if (!keepInventory || finalDamaged !is Player)
			{
				(finalDamaged as? Player)?.inventory?.clear()
				
				val location = finalDamaged.location
				died.drops.forEach()
				{
					location.world.dropItemNaturally(location, it)
				}
			}
		}
		
		finalDamaged.setMetadata(DEATHS_TAG, FixedMetadataValue(plugin, died))
		finalDamaged.health = 0.0
		
		if (finalDamaged is Player && died.shouldDoRespawn)
		{
			later(40)
			{
				finalDamaged.spigot().respawn()
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun EntityDeathEvent.onEntityDeath()
	{
		drops.clear()
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun PlayerDeathEvent.onPlayerDeath()
	{
		drops.clear()
		
		val meta = entity.getMetadata(DEATHS_TAG).getOrNull(0)?.value() as? DiedEvent ?: return
		deathMessage = meta.causeOfDeath.deathMessage()
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	fun PlayerRespawnEvent.onRespawn()
	{
		val meta = player.getMetadata(DEATHS_TAG).getOrNull(0)?.value() as? DiedEvent ?: return
		respawnLocation = meta.respawnLocation ?: return
	}
	
	
	private fun getItems(entity: LivingEntity): List<ItemStack>
	{
		val drops = mutableSetOf<ItemStack?>()
		
		if (entity is Player)
		{
			drops += entity.inventory.contents
		}
		
		val equipment = entity.equipment
		if (equipment != null)
		{
			try
			{
				val headDC = equipment.helmetDropChance
				val bodyDC = equipment.chestplateDropChance
				val legsDC = equipment.leggingsDropChance
				val feetDC = equipment.bootsDropChance
				val lHanDC = equipment.itemInOffHandDropChance
				val rHanDC = equipment.itemInMainHandDropChance
				
				if (headDC == 1.0f || randChance(headDC * 100.0))
				{
					drops += equipment.helmet
				}
				if (bodyDC == 1.0f || randChance(bodyDC * 100.0))
				{
					drops += equipment.chestplate
				}
				if (legsDC == 1.0f || randChance(legsDC * 100.0))
				{
					drops += equipment.leggings
				}
				if (feetDC == 1.0f || randChance(feetDC * 100.0))
				{
					drops += equipment.boots
				}
				if (lHanDC == 1.0f || randChance(lHanDC * 100.0))
				{
					drops += equipment.itemInOffHand
				}
				if (rHanDC == 1.0f || randChance(rHanDC * 100.0))
				{
					drops += equipment.itemInMainHand
				}
			}
			catch (ignored: ClassCastException)
			{
				// strange issue
			}
		}
		
		return drops.filterNotNull().filter { it.type != AIR }
	}
	
	
	companion object
	{
		const val DEATHS_TAG = "deaths"
		const val DAMAGE_TAG = "damage_cooldown"
	}
	
}