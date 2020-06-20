package com.sxtanna.mc.arcade.pb.gear.part

import com.sxtanna.mc.arcade.data.Stack
import com.sxtanna.mc.arcade.data.base.Meta
import com.sxtanna.mc.arcade.game.data.GameData
import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.pb.game.GamePB
import com.sxtanna.mc.arcade.pb.gear.base.GameGearPartGun
import com.sxtanna.mc.arcade.time.reply.Reply
import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Entity
import java.util.concurrent.TimeUnit

class GameGearPartBazooka(gear: GameGear) : GameGearPartGun(gear, "Bazooka", item)
{
	
	override val sound = Sound.ENTITY_GENERIC_EXPLODE
	
	override val soundLevel = 0.5
	override val soundPitch = 2.0
	
	override val bulletMsPrSc = 1.9
	
	override val bulletStrayX by Datas.GUN_BAZOOKA_STRAYX map Number::toDouble
	override val bulletStrayY by Datas.GUN_BAZOOKA_STRAYY map Number::toDouble
	override val bulletStrayZ by Datas.GUN_BAZOOKA_STRAYZ map Number::toDouble
	
	override val bulletAmount by Datas.GUN_BAZOOKA_AMOUNT map Number::toInt
	override val bulletDamage by Datas.GUN_BAZOOKA_DAMAGE map Number::toDouble
	
	override val cooldownTime = 3L
	override val cooldownUnit = TimeUnit.SECONDS
	
	
	override fun use(entity: Entity, time: Long, unit: TimeUnit, done: Reply?): Boolean
	{
		return useNotifying(entity, time, unit, onActionBar = true)
	}
	
	
	private companion object
	{
		val item = Stack(Material.GOLDEN_HORSE_ARMOR)[Meta.NAME, "${Colour.Yellow}Bazooka"]
	}
	
	internal object Datas
	{
		object GUN_BAZOOKA_STRAYX
			: GameData.NumbData(0.6)
		
		object GUN_BAZOOKA_STRAYY
			: GameData.NumbData(1.1)
		
		object GUN_BAZOOKA_STRAYZ
			: GameData.NumbData(0.6)
		
		
		object GUN_BAZOOKA_AMOUNT
			: GameData.NumbData(6)
		
		object GUN_BAZOOKA_DAMAGE
			: GameData.NumbData(5.0)
	}
	
}