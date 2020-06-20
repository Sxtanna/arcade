package com.sxtanna.mc.arcade.game.gear.part

import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.game.gear.base.GameGearPart
import com.sxtanna.mc.arcade.util.Effect
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask

class GameGearPartEffect(gear: GameGear, private val effect: Effect, private val level: Int) : GameGearPart(gear, effect.name)
{
	
	private var updater = null as? BukkitTask?
	private val players = mutableSetOf<Player>()
	
	
	override fun load()
	{
		super.load()
		
		updater = gear.game.timer(60)
		{
			players.forEach(this::giveEffect)
		}
	}
	
	override fun kill()
	{
		super.kill()
		
		updater?.cancel()
		updater = null
		
		players.clear()
	}
	
	
	override fun give(data: Player)
	{
		players += data
		
		giveEffect(data)
	}
	
	override fun take(data: Player)
	{
		players -= data
		
		effect.take(data)
	}
	
	
	
	private fun giveEffect(user: Player)
	{
		if (effect !is Effect.PotionEffects)
		{
			effect.give(user, 20 * 10)
		}
		else
		{
			effect.give(user, 20 * 10, level, icon = false)
		}
	}
	
}