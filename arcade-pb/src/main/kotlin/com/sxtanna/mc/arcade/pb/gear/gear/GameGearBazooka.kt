package com.sxtanna.mc.arcade.pb.gear.gear

import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.gear.part.GameGearPartEffect
import com.sxtanna.mc.arcade.pb.gear.base.GameGearPB
import com.sxtanna.mc.arcade.pb.gear.part.GameGearPartBazooka
import com.sxtanna.mc.arcade.util.Effect

class GameGearBazooka(game: Game) : GameGearPB(game, "Bazooka")
{
	
	init
	{
		// pb
		+GameGearPartBazooka(this)
		
		// base
		+GameGearPartEffect(this, Effect.SPEED, 2)
	}
	
}