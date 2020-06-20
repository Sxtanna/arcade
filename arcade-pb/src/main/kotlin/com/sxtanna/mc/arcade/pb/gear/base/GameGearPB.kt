package com.sxtanna.mc.arcade.pb.gear.base

import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.gear.gear.GameGearTeamArmor
import com.sxtanna.mc.arcade.game.gear.part.GameGearPartHeals
import org.bukkit.inventory.ItemStack

abstract class GameGearPB(game: Game, name: String) : GameGearTeamArmor(game, name)
{
	
	init
	{
		+GameGearPartHeals(this, 3, 20.0)
	}
	
	
	override fun icon(): ItemStack
	{
		return parts.filterIsInstance<GameGearPartGun>().firstOrNull()?.stack?.get(1) ?: super.icon()
	}
	
}