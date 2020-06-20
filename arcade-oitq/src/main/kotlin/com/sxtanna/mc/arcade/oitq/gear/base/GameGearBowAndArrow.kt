package com.sxtanna.mc.arcade.oitq.gear.base

import com.sxtanna.mc.arcade.data.Stack
import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.game.gear.part.GameGearPartStack
import org.bukkit.Material

abstract class GameGearBowAndArrow(game: Game, name: String) : GameGear(game, name)
{
	
	init
	{
		+GameGearPartStack(this, 1, Stack(Material.BOW))
		+GameGearPartStack(this, 1, Stack(Material.ARROW))
	}
	
}