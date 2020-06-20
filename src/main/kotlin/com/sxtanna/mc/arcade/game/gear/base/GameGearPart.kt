package com.sxtanna.mc.arcade.game.gear.base

import com.sxtanna.mc.arcade.base.Addon
import com.sxtanna.mc.arcade.base.Gives
import com.sxtanna.mc.arcade.base.Named
import com.sxtanna.mc.arcade.base.State
import com.sxtanna.mc.arcade.base.Takes
import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.base.GameUser
import com.sxtanna.mc.arcade.game.data.GameData
import com.sxtanna.mc.arcade.hook.base.ArcadePlugin
import com.sxtanna.mc.arcade.time.Cooldown
import com.sxtanna.mc.arcade.time.using.Coolable
import org.bukkit.entity.Player
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import java.util.concurrent.TimeUnit
import kotlin.properties.ReadOnlyProperty

abstract class GameGearPart protected constructor(protected val gear: GameGear, final override val name: String) : Named, Addon, State, Gives<Player>, Takes<Player>, Coolable.CoolableEntity, GameUser, Listener
{
	
	override val game: Game
		get() = gear.game
	
	override val plugin: ArcadePlugin
		get() = game.plugin
	
	
	override val cooldown: Cooldown
		get() = gear.game.plugin.arcade.cooldowns
	
	
	override val cooldownTime: Long
		get() = -1L
	
	override val cooldownUnit: TimeUnit
		get() = TimeUnit.MILLISECONDS
	
	
	override fun load()
	{
		gear.game.plugin.server.pluginManager.registerEvents(this, gear.game.plugin)
	}
	
	override fun kill()
	{
		HandlerList.unregisterAll(this)
	}
	
	
	override fun give(data: Player)
	{
	
	}
	
	override fun take(data: Player)
	{
	
	}
	
	
	fun using(user: Player): Boolean
	{
		return gear.using(user)
	}
	
	
	protected fun <T : Any> option(data: GameData<T>): ReadOnlyProperty<Any, T>
	{
		return gear.option(data)
	}
	
	protected fun <O : Any, T : Any> option(data: GameData<O>, transformer: (O) -> T): ReadOnlyProperty<Any, T>
	{
		return gear.option(data, transformer)
	}
	
}