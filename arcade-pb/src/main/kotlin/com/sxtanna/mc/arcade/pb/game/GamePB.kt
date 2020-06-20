package com.sxtanna.mc.arcade.pb.game

import com.sxtanna.mc.arcade.data.Color
import com.sxtanna.mc.arcade.data.base.Vec3
import com.sxtanna.mc.arcade.func.generateSphere
import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.data.GameData
import com.sxtanna.mc.arcade.game.data.GameDatas
import com.sxtanna.mc.arcade.game.team.GameTeam
import com.sxtanna.mc.arcade.pb.ArcadeGamePluginPB
import com.sxtanna.mc.arcade.pb.cmds.CommandPB
import com.sxtanna.mc.arcade.pb.gear.gear.GameGearBazooka
import com.sxtanna.mc.arcade.pb.gear.gear.GameGearGrenade
import com.sxtanna.mc.arcade.pb.gear.gear.GameGearHandgun
import com.sxtanna.mc.arcade.pb.gear.gear.GameGearMiniGun
import org.bukkit.entity.Projectile
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.EntityDamageEvent
import java.util.EnumSet
import kotlin.reflect.KClass

class GamePB(override val plugin: ArcadeGamePluginPB) : Game()
{
	
	init
	{
		// teams
		+GameTeam(this, "Red", Color.RED)
		+GameTeam(this, "Blue", Color.BLUE)
		
		// gears
		+GameGearHandgun(this)
		+GameGearBazooka(this)
		+GameGearMiniGun(this)
		+GameGearGrenade(this)
		
		// options
		this[GameDatas.OPTION_NO_DAMAGE_FROM] = EnumSet.copyOf(causes)
		
		this[GameDatas.OPTION_AREA_TIME] = 1000
		this[GameDatas.OPTION_AREA_TIME_PASS] = false
		
		this[GameDatas.OPTION_USER_REGENERATION] = false
	}
	
	
	private val commandPB = CommandPB(plugin)
	
	
	override fun load()
	{
		super.load()
		
		set(Datas.OPTION_SPLASH_VECS, generateSphere(get(Datas.OPTION_SPLASH_SIZE).toDouble(), false))
		
		commandPB.load()
	}
	
	override fun kill()
	{
		super.kill()
		
		commandPB.kill()
	}
	
	private companion object
	{
		
		val causes = setOf(
			EntityDamageEvent.DamageCause.FALL,
			EntityDamageEvent.DamageCause.ENTITY_ATTACK
		                  )
		
	}
	
	internal object Datas
	{
		// game options
		
		/**
		 * whether bullets should be affected by gravity or not
		 */
		object OPTION_BULLET_DROP
			: GameData.BoolData(false)
		
		/**
		 * the actual type to use when spawning the projectile
		 */
		object OPTION_BULLET_TYPE
			: GameData.DataData<KClass<out Projectile>>(Snowball::class)
		
		/**
		 * base chance of a specific block getting splashed
		 */
		object OPTION_SPLASH_HITS
			: GameData.NumbData(40.0)
		
		/**
		 * radius of a paintball splash
		 */
		object OPTION_SPLASH_SIZE
			: GameData.NumbData(4.0)
		
		/**
		 * storage option for splash vector offsets
		 */
		object OPTION_SPLASH_VECS
			: GameData.DataData<Set<Vec3>>(emptySet())
		
		// gun options
		
	}
	
}