package com.sxtanna.mc.arcade.game.data

import com.sxtanna.mc.arcade.game.wins.WinnerCheck
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import java.util.EnumSet

object GameDatas
{
	
	init
	{
		this::class.nestedClasses.forEach()
		{
			it.objectInstance // Initialize all game datas
		}
	}
	
	
	object OPTION_CAN_PLACE
		: GameData.BoolData(false)
	
	object OPTION_CAN_BREAK
		: GameData.BoolData(false)
	
	object OPTION_CAN_DROP
		: GameData.BoolData(false)
	
	object OPTION_NO_DAMAGE
		: GameData.BoolData(false)
	
	object OPTION_NO_DAMAGE_FROM
		: GameData.EnumData<EntityDamageEvent.DamageCause>(EnumSet.noneOf(EntityDamageEvent.DamageCause::class.java))
	
	object OPTION_NO_HUNGER
		: GameData.BoolData(true)
	
	object OPTION_AREA_TIME
		: GameData.NumbData(5000), GameData.AreaOpt
	{
		
		override fun apply(data: Any, area: World)
		{
			area.fullTime = get(data).toLong()
		}
		
	}
	
	object OPTION_AREA_TIME_PASS
		: GameData.BoolData(true), GameData.AreaOpt
	{
		
		override fun apply(data: Any, area: World)
		{
			area.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, get(data))
		}
		
	}
	
	object OPTION_AREA_ALLOW_SPAWNING
		: GameData.BoolData(false)
	
	object OPTION_AREA_LOAD_SIZE
		: GameData.NumbData(10)
	
	object OPTION_FORCE_TEAM_BALANCE
		: GameData.BoolData(true)
	
	object OPTION_USER_GAMEMODE
		: GameData.DataData<GameMode>(GameMode.ADVENTURE), GameData.UserOpt
	{
		override fun apply(data: Any, user: Player)
		{
			user.gameMode = get(data)
		}
	}
	
	object OPTION_USER_START_HEALTH
		: GameData.NumbData(20.0), GameData.UserOpt
	{
		override fun apply(data: Any, user: Player)
		{
			user.health = get(data).toDouble()
		}
	}
	
	object OPTION_USER_START_SATURATION
		: GameData.NumbData(20), GameData.UserOpt
	{
		override fun apply(data: Any, user: Player)
		{
			user.foodLevel = get(data).toInt()
		}
	}
	
	object OPTION_USER_REGENERATION
		: GameData.BoolData(true), GameData.AreaOpt
	{
		
		override fun apply(data: Any, area: World)
		{
			area.setGameRule(GameRule.NATURAL_REGENERATION, get(data))
		}
		
	}
	
	object OPTION_USER_DEATH_CAUSES_SPEC
		: GameData.BoolData(true)
	
	object OPTION_USER_FRIENDLY_FIRE
		: GameData.BoolData(false)
	
	object OPTION_GAME_ENDS_INSTANTLY
		: GameData.BoolData(false)
	
	object COUNTDOWN_START
		: GameData.NumbData(20)
	
	object COUNTDOWN_VALUE
		: GameData.NumbData(COUNTDOWN_START.def())
	
	object OPTION_GAME_WIN_CHECK
		: GameData.DataData<WinnerCheck?>(null)
	
}