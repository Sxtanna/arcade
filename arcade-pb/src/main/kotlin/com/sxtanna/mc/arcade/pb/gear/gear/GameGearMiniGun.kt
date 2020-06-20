package com.sxtanna.mc.arcade.pb.gear.gear

import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.pb.gear.base.GameGearPB
import com.sxtanna.mc.arcade.pb.gear.part.GameGearPartMiniGun

class GameGearMiniGun(game: Game) : GameGearPB(game, "MiniGun")
{
	
	init
	{
		// pb
		+GameGearPartMiniGun(this)
	}
	
}