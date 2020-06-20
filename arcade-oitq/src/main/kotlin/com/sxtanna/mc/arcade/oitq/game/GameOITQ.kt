package com.sxtanna.mc.arcade.oitq.game

import com.sxtanna.mc.arcade.data.Color
import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.data.GameDatas
import com.sxtanna.mc.arcade.game.team.GameTeam
import com.sxtanna.mc.arcade.oitq.ArcadeGamePluginOITQ
import com.sxtanna.mc.arcade.oitq.gear.gear.GameGearJumper

class GameOITQ(override val plugin: ArcadeGamePluginOITQ) : Game()
{

	init
	{
		// teams
		+GameTeam(this, "Player", Color.YELLOW)
		
		// gears
		+GameGearJumper(this)
		
		
		this[GameDatas.OPTION_USER_FRIENDLY_FIRE] = true
		
		this[GameDatas.COUNTDOWN_START] = 10
	}
	
}