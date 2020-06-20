package com.sxtanna.mc.arcade.game.team

import com.sxtanna.mc.arcade.base.Icons
import com.sxtanna.mc.arcade.base.Named
import com.sxtanna.mc.arcade.base.Shown
import com.sxtanna.mc.arcade.base.State
import com.sxtanna.mc.arcade.data.Color
import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.base.GameUser
import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class GameTeam(override val game: Game, override val name: String, val color: Color) : Named, Shown, State, Icons, GameUser
{
	
	private val users = mutableMapOf<Player, TeamState>()
	
	
	override val internal = name
	override val external = "${color.chat}$internal"
	
	
	override fun load()
	{
	
	}
	
	override fun kill()
	{
		users.clear()
	}
	
	
	override fun icon(): ItemStack
	{
		return ItemStack(color.wool)
	}
	
	
	fun join(user: Player, notify: Boolean = true): Boolean
	{
		if (user in this)
		{
			return false
		}
		
		users[user] = TeamState.LIVE
		
		if (notify)
		{
			user.sendMessage(Colour.color("${Colour.Gray}You have joined $external${Colour.Gray} team!"))
		}
		
		return true
	}
	
	fun quit(user: Player, notify: Boolean = true): Boolean
	{
		if (user !in this)
		{
			return false
		}
		
		users -= user
		
		if (notify)
		{
			user.sendMessage(Colour.color("${Colour.Gray}You have left $external${Colour.Gray} team!"))
		}
		
		return true
	}
	
	
	fun size(): Int
	{
		return users.size
	}
	
	
	
	operator fun get(state: TeamState): List<Player>
	{
		return users.entries.filter { state == TeamState.NONE || it.value == state }.map { it.key }
	}
	
	
	operator fun get(user: Player): TeamState
	{
		return users[user] ?: TeamState.NONE
	}
	
	operator fun set(user: Player, state: TeamState)
	{
		if (user !in this)
		{
			return
		}
		
		users[user] = state
	}
	
	
	operator fun contains(user: Player): Boolean
	{
		return user in users
	}
	
	
	override fun toString(): String
	{
		return "GameTeam(name: $name, users: ${users.map { "${it.key.name}|${it.value}" }})"
	}
	
}