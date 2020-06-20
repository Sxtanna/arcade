package com.sxtanna.mc.arcade.pu.gear.gear

import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.gear.gear.GameGearTeamArmor
import com.sxtanna.mc.arcade.pu.gear.part.GameGearPartKBDelta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class GameGearPush(game: Game) : GameGearTeamArmor(game, "Push")
{
	
	init
	{
		+GameGearPartKBDelta(this, GameGearPartKBDelta.Mode.GIVE, GameGearPartKBDelta.Delt.MORE)
	}
	
	
	override fun icon(): ItemStack
	{
		return ItemStack(Material.SLIME_BLOCK)
	}
	
}