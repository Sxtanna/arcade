package com.sxtanna.mc.arcade.pb.gear.part

import com.sxtanna.mc.arcade.data.Stack
import com.sxtanna.mc.arcade.data.base.Meta
import com.sxtanna.mc.arcade.game.data.GameData
import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.pb.game.GamePB
import com.sxtanna.mc.arcade.pb.gear.base.GameGearPartGun
import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.Material
import org.bukkit.Sound
import java.util.concurrent.TimeUnit

class GameGearPartMiniGun(gear: GameGear) : GameGearPartGun(gear, "MiniGun", item)
{
	override val sound = Sound.ENTITY_ENDER_DRAGON_HURT
	
	override val soundLevel = 0.6
	override val soundPitch = 2.0
	
	override val bulletMsPrSc = 4.0
	
	override val bulletStrayX by Datas.GUN_MINIGUN_STRAYX map Number::toDouble
	override val bulletStrayY by Datas.GUN_MINIGUN_STRAYY map Number::toDouble
	override val bulletStrayZ by Datas.GUN_MINIGUN_STRAYZ map Number::toDouble
	
	override val bulletAmount by Datas.GUN_MINIGUN_AMOUNT map Number::toInt
	override val bulletDamage by Datas.GUN_MINIGUN_DAMAGE map Number::toDouble
	
	override val cooldownTime = 110L
	override val cooldownUnit = TimeUnit.MILLISECONDS
	
	private companion object
	{
		val item = Stack(Material.DIAMOND_HORSE_ARMOR)[Meta.NAME, "${Colour.Yellow}MiniGun"]
	}
	
	internal object Datas
	{
		object GUN_MINIGUN_STRAYX
			: GameData.NumbData(0.4)
		
		object GUN_MINIGUN_STRAYY
			: GameData.NumbData(0.4)
		
		object GUN_MINIGUN_STRAYZ
			: GameData.NumbData(0.4)
		
		
		object GUN_MINIGUN_AMOUNT
			: GameData.NumbData(1)
		
		object GUN_MINIGUN_DAMAGE
			: GameData.NumbData(1)
	}
}