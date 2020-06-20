package com.sxtanna.mc.arcade.pb.gear.base

import com.sxtanna.mc.arcade.data.Stack
import com.sxtanna.mc.arcade.func.isRight
import com.sxtanna.mc.arcade.func.isStainedGlass
import com.sxtanna.mc.arcade.func.isStainedGlassPane
import com.sxtanna.mc.arcade.func.isWool
import com.sxtanna.mc.arcade.func.randChance
import com.sxtanna.mc.arcade.func.randFloat64
import com.sxtanna.mc.arcade.func.setMeta
import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.game.gear.base.GameGearPart
import com.sxtanna.mc.arcade.game.team.TeamState
import com.sxtanna.mc.arcade.hook.mods.damage.base.HurtEvent
import com.sxtanna.mc.arcade.pb.game.GamePB
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import kotlin.reflect.KClass

abstract class GameGearPartGun(gear: GameGear, name: String, val stack: Stack) : GameGearPart(gear, name)
{
	
	abstract val sound: Sound
	
	abstract val soundLevel: Double
	abstract val soundPitch: Double
	
	abstract val bulletMsPrSc: Double
	
	abstract val bulletStrayX: Double
	abstract val bulletStrayY: Double
	abstract val bulletStrayZ: Double
	
	abstract val bulletAmount: Int
	abstract val bulletDamage: Double
	
	
	override fun load()
	{
		super.load()
		
		if (!gear.game[GamePB.Datas.OPTION_BULLET_DROP])
		{
			Companion.load(gear.game.plugin)
		}
	}
	
	override fun kill()
	{
		super.kill()
		
		if (!gear.game[GamePB.Datas.OPTION_BULLET_DROP])
		{
			Companion.kill()
		}
	}
	
	
	override fun give(data: Player)
	{
		val item = stack[1]
		val prev = data.inventory.getItem(0)
		
		item.setData(PersistentDataType.STRING, "gun")
		
		data.inventory.setItem(0, item)
		
		if (prev != null)
		{
			data.inventory.addItem(prev)
		}
	}
	
	override fun take(data: Player)
	{
		data.inventory.removeItem(stack[64])
	}
	
	
	open fun onShoot(player: Player)
	{
		val loc = player.eyeLocation
		val dir = player.location.direction.normalize()
		
		// in front of them
		loc.add(0.0, -0.2, 0.0)
		
		repeat(bulletAmount)
		{
			shoot(player, loc, dir.clone())
		}
		
		loc.world.playSound(loc, sound, SoundCategory.PLAYERS, soundLevel.toFloat(), soundPitch.toFloat())
	}
	
	
	@EventHandler
	fun PlayerInteractEvent.onShoot()
	{
		if (!using(player) || hand != EquipmentSlot.HAND || !action.isRight())
		{
			return
		}
		
		val item = item
		if (item == null || item.getData(PersistentDataType.STRING) != "gun")
		{
			return
		}
		
		if (!use(player))
		{
			return
		}
		
		onShoot(player)
	}
	
	@EventHandler
	fun HurtEvent.onDamageBySnowball()
	{
		val damager = damager as? Player ?: return
		val damaged = damaged as? Player ?: return
		
		if (gear.game.state(damager) != TeamState.LIVE || gear.game.state(damaged) != TeamState.LIVE)
		{
			isCancelled = true
			return
		}
		if (game.sameTeam(damaged, damager))
		{
			isCancelled = true
			return
		}
		
		val damage = projectile?.getMetadata(PB_DAMAGE)?.getOrNull(0)?.asDouble() ?: return
		val weapon = projectile?.getMetadata(PB_WEAPON)?.getOrNull(0)?.asString() ?: return
		
		this.damage = damage
		
		this.delayBypassing = true
		this.armorBypassing = true
		this.knockBypassing = true
		this.hShotBypassing = weapon.endsWith("Bazooka")
	}
	
	@EventHandler
	fun ProjectileHitEvent.onCollide()
	{
		val block = hitBlock ?: return
		val color = (entity.shooter as? Player)?.let(gear.game::team)?.color ?: return
		
		val vecs = gear.game[GamePB.Datas.OPTION_SPLASH_VECS]
		if (vecs.isEmpty())
		{
			return
		}
		
		vecs.forEach()
		{ vec ->
			val other = block.getRelative(vec.x.toInt(), vec.y.toInt(), vec.z.toInt())
			
			if (!randChance(gear.game[GamePB.Datas.OPTION_SPLASH_HITS].toDouble() - (other.location.distance(block.location) * 3)))
			{
				return@forEach
			}
			
			val next = when
			{
				other.type.isWool()             -> color.wool
				other.type.isStainedGlass()     -> color.glass
				other.type.isStainedGlassPane() -> color.pane
				else                            ->
				{
					return@forEach
				}
			}
			
			other.type = next
		}
	}
	
	
	protected fun shoot(who: LivingEntity,
	                    
	                    loc: Location,
	                    dir: Vector,
	                    
	                    msPrSc: Double = bulletMsPrSc,
	                    damage: Double = bulletDamage,
	                    
	                    type: KClass<out Projectile> = game[GamePB.Datas.OPTION_BULLET_TYPE],
	                    drop: Boolean = game[GamePB.Datas.OPTION_BULLET_DROP])
	{
		val stray = if (bulletStrayX == 0.0 && bulletStrayY == 0.0 && bulletStrayZ == 0.0)
		{
			Vector(0, 0, 0)
		}
		else
		{
			Vector(randFloat64(bulletStrayX) - (bulletStrayX / 2),
			       randFloat64(bulletStrayY) - (bulletStrayY / 2),
			       randFloat64(bulletStrayZ) - (bulletStrayZ / 2))
		}
		
		val vector = dir.multiply(msPrSc).add(stray)
		
		val bullet = loc.world.spawn(loc, type.java)
		{
			it.shooter = who
			it.velocity = vector
			
			it.setMeta(PB_DAMAGE, game.plugin, damage)
			
			val weapon = who.equipment?.itemInMainHand?.itemMeta?.displayName ?: return@spawn
			
			it.setMeta(PB_WEAPON,
			           game.plugin, weapon)
			it.setMeta(HurtEvent.WEAPON_TAG,
			           game.plugin, weapon)
		}
		
		if (!drop)
		{
			keep[bullet] = vector
		}
	}
	
	
	private companion object
	{
		const val PB_DAMAGE = "pb_damage"
		const val PB_WEAPON = "pb_weapon"
		
		
		private var task = null as? BukkitTask?
		private val keep = mutableMapOf<Entity, Vector>()
		
		
		fun load(plugin: Plugin)
		{
			if (task != null)
			{
				return
			}
			
			val runnable = Runnable()
			{
				val iter = keep.iterator()
				
				iter.forEachRemaining()
				{ next ->
					if (!next.key.isValid || next.key.ticksLived > (20 * 20) /* 20 seconds */)
					{
						iter.remove()
						next.key.remove()
					}
					else
					{
						next.key.velocity = next.value
					}
				}
			}
			
			task = plugin.server.scheduler.runTaskTimer(plugin, runnable, 0L, 1L)
		}
		
		fun kill()
		{
			if (task == null)
			{
				return
			}
			
			task?.cancel()
			task = null
			
			keep.clear()
		}
		
	}
	
}