package com.sxtanna.mc.arcade.game.gear.part

import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.game.gear.base.GameGearPart
import com.sxtanna.mc.arcade.game.team.TeamState
import com.sxtanna.mc.arcade.hook.mods.damage.base.HurtEvent
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent

class GameGearPartNoFall(gear: GameGear) : GameGearPart(gear, "No Fall")
{
	
	@EventHandler
	fun HurtEvent.onFallDamage()
	{
		val damaged = damaged as? Player ?: return
		
		if (cause != EntityDamageEvent.DamageCause.FALL || !gear.game.state().active() || !using(damaged) || gear.game.state(damaged) != TeamState.LIVE)
		{
			return
		}
		
		isCancelled = true
	}
	
}