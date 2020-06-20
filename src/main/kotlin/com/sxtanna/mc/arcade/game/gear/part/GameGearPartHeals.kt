package com.sxtanna.mc.arcade.game.gear.part

import com.sxtanna.mc.arcade.data.Stack
import com.sxtanna.mc.arcade.data.base.Meta
import com.sxtanna.mc.arcade.func.isRight
import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.game.gear.base.GameGearPart
import com.sxtanna.mc.arcade.game.team.TeamState
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionType
import kotlin.math.min

class GameGearPartHeals(gear: GameGear, private val amount: Int, private val healing: Double) : GameGearPart(gear, "Elixir")
{
	
	override fun give(data: Player)
	{
		super.give(data)
		
		data.inventory.addItem(thrown[amount])
	}
	
	override fun take(data: Player)
	{
		super.take(data)
		
		data.inventory.removeItem(thrown[amount])
	}
	
	
	@EventHandler
	fun PlayerInteractEvent.onThrow()
	{
		if (!using(player) || hand != EquipmentSlot.HAND || !action.isRight() || gear.game.state(player) != TeamState.LIVE)
		{
			return
		}
		
		val item = item
		if (item == null || item.type != thrown.item.type || item.amount < 1)
		{
			return
		}
		
		if (!use(player))
		{
			return
		}
		
		if (item.amount-- <= 0)
		{
			player.inventory.remove(item)
		}
		
		launch(player)
	}
	
	@EventHandler
	fun PotionSplashEvent.onEffects()
	{
		if (!gear.game.state().active() || !entity.hasMetadata(name))
		{
			return
		}
		
		isCancelled = true
		
		val user = entity.shooter as? Player ?: return
		
		affectedEntities.filterIsInstance<Player>().forEach()
		{
			if (gear.game.team(user) != gear.game.team(it))
			{
				return@forEach // not on the same team
			}
			
			val maxHealth = it.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: return@forEach
			it.health = min(maxHealth, it.health + healing)
		}
	}
	
	
	private fun launch(player: Player)
	{
		val thrown = player.launchProjectile(ThrownPotion::class.java)
		
		thrown.item = potion[1]
		thrown.velocity = thrown.velocity.multiply(1.2)
		
		thrown.setMetadata(name, FixedMetadataValue(gear.game.plugin, 0))
	}
	
	
	private companion object
	{
		val thrown = Stack(Material.EMERALD)[Meta.NAME, "§l§2Elixir"]
		val potion = Stack(Material.SPLASH_POTION)[Meta.POTION_TYPE, PotionType.INSTANT_HEAL]
	}
	
}