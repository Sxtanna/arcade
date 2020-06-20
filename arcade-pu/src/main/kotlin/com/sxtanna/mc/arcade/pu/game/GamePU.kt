package com.sxtanna.mc.arcade.pu.game

import com.sxtanna.mc.arcade.data.Color
import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.data.GameDatas
import com.sxtanna.mc.arcade.game.team.GameTeam
import com.sxtanna.mc.arcade.game.team.TeamState
import com.sxtanna.mc.arcade.hook.mods.damage.base.HurtEvent
import com.sxtanna.mc.arcade.pu.ArcadeGamePluginPU
import com.sxtanna.mc.arcade.pu.gear.gear.GameGearHold
import com.sxtanna.mc.arcade.pu.gear.gear.GameGearPush
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageEvent
import java.util.EnumSet

class GamePU(override val plugin: ArcadeGamePluginPU) : Game()
{
	
	init
	{
		+GameTeam(this, "Red", Color.RED)
		+GameTeam(this, "Blue", Color.BLUE)
		
		
		+GameGearHold(this)
		+GameGearPush(this)
		
		
		this[GameDatas.OPTION_AREA_TIME] = 1000
		this[GameDatas.OPTION_AREA_TIME_PASS] = false
		
		this[GameDatas.OPTION_AREA_LOAD_SIZE] = 5
		
		this[GameDatas.OPTION_USER_REGENERATION] = false
		this[GameDatas.OPTION_USER_GAMEMODE] = GameMode.SURVIVAL
		
		this[GameDatas.OPTION_NO_DAMAGE_FROM] = EnumSet.of(EntityDamageEvent.DamageCause.FALL)
	}
	
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	fun HurtEvent.onKnock()
	{
		if (!state().active())
		{
			return
		}
		
		val damager = damager as? Player ?: return
		val damaged = damaged as? Player ?: return
		
		if (state(damager) != TeamState.LIVE || state(damaged) != TeamState.LIVE)
		{
			return
		}
		
		knockRise *= 2
		knockBack *= 10
		
		hurtsBypassing = true
	}
	
}