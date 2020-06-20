package com.sxtanna.mc.arcade.game.base

enum class GameState
{
	
	AWAIT, // nothing with this game is happening
	
	LOBBY, // players are in the lobby
	READY, // game is counting down to start
	START, // players are teleported to the map, and the game countdown is started
	GOING, // game is currently running
	ENDED, // the requirements for a win condition have been met
	RESET; // game is being reset, will return itself back to LOBBY
	
	
	fun active(): Boolean
	{
		return this in START..ENDED
	}
	
	
	sealed class SetCause
	{
		
		object NORMAL
			: SetCause()
		
		object PLAYER
			: SetCause()
		
		
		data class FAILURE(val message: String?)
			: SetCause()
		
	}
	
}