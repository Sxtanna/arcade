package com.sxtanna.mc.arcade.game.gear.part

import com.sxtanna.mc.arcade.data.Armor
import com.sxtanna.mc.arcade.data.Armor.ArmorPart
import com.sxtanna.mc.arcade.data.Armor.ArmorType
import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.game.gear.base.GameGearPart
import org.bukkit.entity.Player

class GameGearPartArmor(gear: GameGear, private val part: ArmorPart, private val type: ArmorType) : GameGearPart(gear, "Armor")
{
	
	private val armor = Armor(part, type)
	
	
	override fun give(data: Player)
	{
		armor.give(data)
	}
	
}