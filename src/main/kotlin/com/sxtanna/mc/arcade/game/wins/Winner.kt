package com.sxtanna.mc.arcade.game.wins

import com.sxtanna.mc.arcade.game.team.GameTeam
import org.bukkit.entity.Player

sealed class Winner
{

	data class User(val user: Player)
		: Winner()
	
	data class Team(val team: GameTeam)
		: Winner()
	
}