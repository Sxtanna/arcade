package com.sxtanna.mc.arcade.pb.gear.part

import com.sxtanna.mc.arcade.data.Stack
import com.sxtanna.mc.arcade.data.base.Meta
import com.sxtanna.mc.arcade.func.generateSphere
import com.sxtanna.mc.arcade.func.randChance
import com.sxtanna.mc.arcade.func.randFloat64
import com.sxtanna.mc.arcade.game.data.GameData
import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.pb.game.GamePB
import com.sxtanna.mc.arcade.pb.gear.base.GameGearPartGun
import com.sxtanna.mc.arcade.time.reply.Reply
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.concurrent.TimeUnit

class GameGearPartGrenade(gear: GameGear) : GameGearPartGun(gear, "Grenades", item)
{
	override val sound = Sound.ENTITY_GENERIC_EXPLODE
	
	override val soundLevel = 5.0
	override val soundPitch = 0.8
	
	override val bulletMsPrSc = 0.8
	
	override val bulletStrayX by Datas.GUN_GRENADE_STRAYX map Number::toDouble
	override val bulletStrayY by Datas.GUN_GRENADE_STRAYY map Number::toDouble
	override val bulletStrayZ by Datas.GUN_GRENADE_STRAYZ map Number::toDouble
	
	override val bulletAmount by Datas.GUN_GRENADE_AMOUNT map Number::toInt
	override val bulletDamage by Datas.GUN_GRENADE_DAMAGE map Number::toDouble
	
	override val cooldownTime = 1500L
	override val cooldownUnit = TimeUnit.MILLISECONDS
	
	
	override fun give(data: Player)
	{
		val item = stack[5]
		val prev = data.inventory.getItem(0)
		
		item.setData(PersistentDataType.STRING, "gun")
		data.inventory.setItem(0, item)
		
		if (prev != null)
		{
			data.inventory.addItem(prev)
		}
	}
	
	override fun onShoot(player: Player)
	{
		val hand = player.inventory.itemInMainHand
		if (hand.amount-- <= 0)
		{
			player.inventory.remove(hand)
		}
		
		val bullet = player.world.spawn(player.eyeLocation, Item::class.java)
		{ drop ->
			
			drop.setItemStack(item[1])
			drop.setCanMobPickup(false)
			
			drop.thrower = player.uniqueId
			
			drop.pickupDelay = Int.MAX_VALUE
			
			drop.velocity = player.location.direction.normalize().multiply(1.5)
		}
		
		
		player.world.playSound(player.location, Sound.ENTITY_WITHER_SHOOT, 5.0F, 0.6F)
		
		game.timer(1)
		{
			if (!bullet.isValid || !game.state().active())
			{
				it.cancel()
				bullet.remove()
				
				return@timer
			}
			
			if (bullet.isOnGround || bullet.location.add(bullet.velocity).block.type != Material.AIR)
			{
				it.cancel()
				bullet.remove()
				
				handleShooting(player, bullet.location.add(0.0, 0.6, 0.0))
				
				return@timer
			}
			
			player.world.spawnParticle(Particle.SMOKE_LARGE, bullet.location, 2, 0.01, 0.01, 0.01, 0.001)
		}
	}
	
	override fun use(entity: Entity, time: Long, unit: TimeUnit, done: Reply?): Boolean
	{
		return useNotifying(entity, time, unit, onActionBar = true)
	}
	
	private fun handleShooting(player: Player, loc: Location)
	{
		val vecs = vecs
				.map()
				{
					loc.block.location.add(it.toBukkit()).toVector().subtract(loc.toVector()).normalize()
				}
				.shuffled()
				.iterator()
		
		if (!vecs.hasNext())
		{
			return
		}
		
		loc.world.playSound(loc, sound, SoundCategory.PLAYERS, soundLevel.toFloat(), soundPitch.toFloat())
		
		repeat(bulletAmount)
		{
			if (!vecs.hasNext())
			{
				return
			}
			
			if (randChance(60 + randFloat64(40.0)))
			{
				return@repeat
			}
			
			shoot(player, loc, vecs.next(), drop = true)
		}
	}
	
	private companion object
	{
		val data = FireworkEffect.builder().withColor(Color.BLACK).build()
		val item = Stack(Material.FIREWORK_STAR)[Meta.FIREWORK_EFFECT, data]
		val vecs = generateSphere(6.0, true).filter { it.y > 0 }.shuffled().take(100)
	}
	
	internal object Datas
	{
		object GUN_GRENADE_STRAYX
			: GameData.NumbData(0.0)
		
		object GUN_GRENADE_STRAYY
			: GameData.NumbData(0.0)
		
		object GUN_GRENADE_STRAYZ
			: GameData.NumbData(0.0)
		
		
		object GUN_GRENADE_AMOUNT
			: GameData.NumbData(36)
		
		object GUN_GRENADE_DAMAGE
			: GameData.NumbData(0.6)
	}
	
}