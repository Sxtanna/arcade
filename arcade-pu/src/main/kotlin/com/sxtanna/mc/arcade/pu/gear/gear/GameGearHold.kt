package com.sxtanna.mc.arcade.pu.gear.gear

import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.gear.gear.GameGearTeamArmor
import com.sxtanna.mc.arcade.game.gear.part.GameGearPartEffect
import com.sxtanna.mc.arcade.pu.gear.part.GameGearPartKBDelta
import com.sxtanna.mc.arcade.util.Effect
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class GameGearHold(game: Game) : GameGearTeamArmor(game, "Hold")
{
	
	init
	{
		+GameGearPartKBDelta(this, GameGearPartKBDelta.Mode.TAKE, GameGearPartKBDelta.Delt.LESS)
		
		+GameGearPartEffect(this, Effect.SLOW, 1)
	}
	
	
	override fun icon(): ItemStack
	{
		return ItemStack(Material.BEDROCK)
	}
	
}