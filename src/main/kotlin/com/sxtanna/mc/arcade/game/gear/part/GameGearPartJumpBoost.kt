package com.sxtanna.mc.arcade.game.gear.part

import com.sxtanna.mc.arcade.data.Stack
import com.sxtanna.mc.arcade.func.isRight
import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.game.gear.base.GameGearPart
import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.Sound
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleFlightEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.scheduler.BukkitTask
import java.util.concurrent.TimeUnit

class GameGearPartJumpBoost(gear: GameGear, private val uses: Int, private val strength: Double, private val restoreEvery: Int, private val activator: Activator, override val cooldownTime: Long, override val cooldownUnit: TimeUnit) : GameGearPart(gear, "Jump Boost")
{
	
	private var update = null as? BukkitTask?
	private val values = mutableMapOf<Player, Int>()
	
	
	override fun load()
	{
		super.load()
		
		if (restoreEvery < 0)
		{
			return
		}
		
		update = gear.game.timer(restoreEvery.toLong())
		{
			values.entries.forEach()
			{
				if (it.value >= uses)
				{
					return@forEach
				}
				
				it.setValue(it.value + 1)
				it.key.sendActionBar("${Colour.Green}+1 ${Colour.Aqua}jump charges")
			}
		}
	}
	
	override fun kill()
	{
		super.kill()
		
		values.clear()
		
		update?.cancel()
		update = null
	}
	
	
	override fun give(data: Player)
	{
		super.give(data)
		
		values[data] = uses
		
		when (activator)
		{
			is Activator.Jump ->
			{
				data.allowFlight = true
			}
			is Activator.Item ->
			{
				data.inventory.addItem(activator.stack[activator.count])
			}
		}
	}
	
	override fun take(data: Player)
	{
		super.take(data)
		
		values -= data
		
		when (activator)
		{
			is Activator.Jump ->
			{
				data.allowFlight = false
			}
			is Activator.Item ->
			{
				data.inventory.removeItem(activator.stack[1])
			}
		}
	}
	
	
	@EventHandler
	fun PlayerToggleFlightEvent.onActivateJump()
	{
		if (!using(player) || !isFlying || activator !is Activator.Jump)
		{
			return
		}
		
		isCancelled = true
		
		if (!useNotifying(player, onActionBar = true))
		{
			return
		}
		
		player.allowFlight = false
		
		useJumpBoost(player)
	}
	
	@EventHandler
	fun PlayerInteractEvent.onActivateItem()
	{
		if (!using(player) || hand != EquipmentSlot.HAND || !action.isRight() || activator !is Activator.Item)
		{
			return
		}
		
		val item = item ?: return
		if (!item.isSimilar(activator.stack.item))
		{
			return
		}
		
		isCancelled = true
		
		if (!useNotifying(player, onActionBar = true))
		{
			return
		}
		
		useJumpBoost(player)
	}
	
	
	override fun useNotifying(entity: Entity, time: Long, unit: TimeUnit, onActionBar: Boolean): Boolean
	{
		val uses = values[entity as? Player ?: return false]
		
		if (uses == null || uses == 0)
		{
			val text = "${Colour.Yellow}0 ${Colour.Red}jump charges remaining"
			if (!onActionBar)
			{
				entity.sendMessage(text)
			}
			else
			{
				entity.sendActionBar(text)
			}
			
			return false
		}
		
		val use = super.useNotifying(entity, time, unit, onActionBar)
		
		if (use)
		{
			values[entity] = uses - 1
			
			val text = "${Colour.Yellow}${uses - 1} ${Colour.Red}jump charges remaining"
			if (!onActionBar)
			{
				entity.sendMessage(text)
			}
			else
			{
				entity.sendActionBar(text)
			}
		}
		
		return use
	}
	
	
	private fun useJumpBoost(player: Player)
	{
		val velocity = player.location.direction.multiply(strength)
		velocity.y *= 0.4
		
		player.velocity = velocity
		player.world.playSound(player.location, Sound.ENTITY_GHAST_SHOOT, 5.0F, 1.3F)
		
		if (activator !is Activator.Jump)
		{
			return
		}
		
		gear.game.timerAsync(1, 5)
		{
			if (!player.isOnGround)
			{
				return@timerAsync
			}
			
			player.allowFlight = true
			it.cancel()
		}
	}
	
	
	
	sealed class Activator
	{
		
		object Jump
			: Activator()
		
		class Item(val count: Int, val stack: Stack)
			: Activator()
		
	}
	
}