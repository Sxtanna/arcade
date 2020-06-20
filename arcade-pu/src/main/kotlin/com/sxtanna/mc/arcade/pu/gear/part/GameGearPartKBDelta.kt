package com.sxtanna.mc.arcade.pu.gear.part

import com.sxtanna.mc.arcade.func.properName
import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.game.gear.base.GameGearPart
import com.sxtanna.mc.arcade.hook.mods.damage.base.HurtEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import java.util.concurrent.TimeUnit

class GameGearPartKBDelta(gear: GameGear, private val mode: Mode, private val delt: Delt) : GameGearPart(gear, "${mode.properName()} ${delt.properName()} knockback")
{
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun HurtEvent.onHurt()
	{
		val damaged = damaged as? Player ?: return
		val damager = damager as? Player ?: return
		
		val using = when (mode)
		{
			Mode.GIVE ->
			{
				using(damager)
			}
			Mode.TAKE ->
			{
				using(damaged)
			}
		}
		
		if (!using)
		{
			return
		}
		
		knockBack = when (delt)
		{
			Delt.LESS ->
			{
				knockBack * 1
			}
			Delt.MORE ->
			{
				knockBack * 7
				knockRise * 2
			}
		}
		
		cooldown.hurt(this, 30, TimeUnit.SECONDS, "PUSH")
	}
	
	
	enum class Mode
	{
		
		GIVE,
		TAKE,
		
	}
	
	enum class Delt
	{
		
		LESS,
		MORE,
		
	}
	
}