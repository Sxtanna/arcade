package com.sxtanna.mc.arcade.game.gear.gear

import com.sxtanna.mc.arcade.data.Armor.ArmorPart
import com.sxtanna.mc.arcade.data.Armor.ArmorType
import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.game.gear.part.GameGearPartArmor
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.LeatherArmorMeta

open class GameGearTeamArmor(game: Game, name: String) : GameGear(game, name)
{
	
	init
	{
		+GameGearPartArmor(this, ArmorPart.HEAD, ArmorType.LEATHER)
		+GameGearPartArmor(this, ArmorPart.BODY, ArmorType.LEATHER)
		+GameGearPartArmor(this, ArmorPart.LEGS, ArmorType.LEATHER)
		+GameGearPartArmor(this, ArmorPart.FEET, ArmorType.LEATHER)
	}
	
	
	override fun give(data: Player)
	{
		super.give(data)
		
		val team = game.team(data) ?: return
		
		for (armor in data.inventory.armorContents)
		{
			val meta = (armor.itemMeta as? LeatherArmorMeta) ?: continue
			meta.setColor(team.color.dye.color)
			
			armor.itemMeta = meta
		}
		
		data.updateInventory()
	}
	
	override fun take(data: Player)
	{
		super.take(data)
		
		data.inventory.setArmorContents(arrayOfNulls(4))
		
		data.updateInventory()
	}
	
}