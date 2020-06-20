package com.sxtanna.mc.arcade.pb.gear.gear

import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.gear.part.GameGearPartEffect
import com.sxtanna.mc.arcade.pb.gear.base.GameGearPB
import com.sxtanna.mc.arcade.pb.gear.part.GameGearPartHandgun
import com.sxtanna.mc.arcade.util.Effect

class GameGearHandgun(game: Game) : GameGearPB(game, "Handgun")
{
	
	init
	{
		// pb
		+GameGearPartHandgun(this)
		
		// base
		+GameGearPartEffect(this, Effect.SPEED, 1)
	}
	
}