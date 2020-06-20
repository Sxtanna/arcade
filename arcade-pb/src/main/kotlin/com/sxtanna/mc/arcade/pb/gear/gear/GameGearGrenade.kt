package com.sxtanna.mc.arcade.pb.gear.gear

import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.gear.part.GameGearPartEffect
import com.sxtanna.mc.arcade.pb.gear.base.GameGearPB
import com.sxtanna.mc.arcade.pb.gear.part.GameGearPartGrenade
import com.sxtanna.mc.arcade.util.Effect

class GameGearGrenade(game: Game) : GameGearPB(game, "Grenadier")
{
	
	init
	{
		// pb
		+GameGearPartGrenade(this)
		
		// base
		+GameGearPartEffect(this, Effect.SPEED, 3)
	}
	
}