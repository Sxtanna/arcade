package com.sxtanna.mc.arcade.game.wins

import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.team.TeamState

abstract class WinnerCheck
{
	
	abstract fun Game.check(): Winner?
	
	
	
	object User : WinnerCheck()
	{
		
		override fun Game.check(): Winner?
		{
			// check if only one living user is available
			val live = users(TeamState.LIVE)
			if (live.size == 1)
			{
				return Winner.User(live.single())
			}
			
			// check if there's only one player in the game
			val user = users()
			if (user.size == 1)
			{
				return Winner.User(user.single())
			}
			
			return null
		}
		
	}
	
	object Team : WinnerCheck()
	{
		
		override fun Game.check(): Winner?
		{
			// check if only one living team is available
			val live = teams().filter { it[TeamState.LIVE].isNotEmpty() }
			if (live.size == 1)
			{
				return Winner.Team(live.single())
			}
			
			// check if only one living user is available
			val user = users()
			if (user.size == 1)
			{
				return Winner.Team(team(user.single()) ?: teams().first())
			}
			
			// check if all users are on the same team
			val team = user.firstOrNull()?.let(::team)
			if (team != null && user.all { team(it) == team })
			{
				return Winner.Team(team)
			}
			
			return null
		}
		
	}
	
}