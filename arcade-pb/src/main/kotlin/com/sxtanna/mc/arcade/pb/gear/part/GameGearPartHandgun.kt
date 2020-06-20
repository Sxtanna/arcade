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

class GameGearPartHandgun(gear: GameGear) : GameGearPartGun(gear, "HandGun", item)
{
	
	override val sound = Sound.ENTITY_ENDER_DRAGON_SHOOT
	
	override val soundLevel = 0.8
	override val soundPitch = 2.0
	
	override val bulletMsPrSc = 2.9
	
	override val bulletStrayX by Datas.GUN_HANDGUN_STRAYX map Number::toDouble
	override val bulletStrayY by Datas.GUN_HANDGUN_STRAYY map Number::toDouble
	override val bulletStrayZ by Datas.GUN_HANDGUN_STRAYZ map Number::toDouble
	
	override val bulletAmount by Datas.GUN_HANDGUN_AMOUNT map Number::toInt
	override val bulletDamage by Datas.GUN_HANDGUN_DAMAGE map Number::toDouble
	
	override val cooldownTime = 600L
	override val cooldownUnit = TimeUnit.MILLISECONDS
	
	
	private companion object
	{
		val item = Stack(Material.IRON_HORSE_ARMOR)[Meta.NAME, "${Colour.Yellow}HandGun"]
	}
	
	internal object Datas
	{
		object GUN_HANDGUN_STRAYX
			: GameData.NumbData(0.0)
		
		object GUN_HANDGUN_STRAYY
			: GameData.NumbData(0.0)
		
		object GUN_HANDGUN_STRAYZ
			: GameData.NumbData(0.0)
		
		
		object GUN_HANDGUN_AMOUNT
			: GameData.NumbData(1)
		
		object GUN_HANDGUN_DAMAGE
			: GameData.NumbData(11.0)
	}
	
}