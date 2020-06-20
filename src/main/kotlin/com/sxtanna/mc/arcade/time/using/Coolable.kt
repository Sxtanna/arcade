package com.sxtanna.mc.arcade.time.using

import com.sxtanna.mc.arcade.base.Named
import com.sxtanna.mc.arcade.time.Cooldown
import com.sxtanna.mc.arcade.time.reply.Reply
import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import java.time.Duration
import java.util.UUID
import java.util.concurrent.TimeUnit

interface Coolable : Named
{
	
	val cooldown: Cooldown
	
	val cooldownTime: Long
	
	val cooldownUnit: TimeUnit
	
	
	fun use(uuid: UUID, time: Long = cooldownTime, unit: TimeUnit = cooldownUnit, done: Reply? = null): Boolean
	{
		return time == -1L || cooldown.use(uuid, name, time, unit, done)
	}
	
	
	interface CoolableEntity : Coolable
	{
		
		fun use(entity: Entity, time: Long = cooldownTime, unit: TimeUnit = cooldownUnit, done: Reply? = null): Boolean
		{
			return use(entity.uniqueId, time, unit, done)
		}
		
		fun useNotifying(entity: Entity, time: Long = cooldownTime, unit: TimeUnit = cooldownUnit, onActionBar: Boolean = false): Boolean
		{
			val state = use(entity.uniqueId, time, unit, Reply.Execute {
				val text = "${Colour.Yellow}You can now use ${Colour.Gold}${it}"
				if (onActionBar && entity is Player)
				{
					entity.sendActionBar(text)
				}
				else
				{
					entity.sendMessage(text)
				}
			})
			
			if (!state)
			{
				val left = cooldown.time(entity.uniqueId, name)
				if (left > 0)
				{
					val dura = Duration.ofMillis(left + 1000)
					
					val text = if (dura.toHoursPart() <= 0)
					{
						String.format("%02d:%02d", dura.toMinutesPart(), dura.toSecondsPart())
					}
					else
					{
						String.format("%02d:%02d:%02d", dura.toHoursPart(), dura.toMinutesPart(), dura.toSecondsPart())
					}
					
					val message = "${Colour.Red}You must wait ${Colour.Green}$text${Colour.Red} to use ${Colour.Gold}$name${Colour.Red} again"
					
					if (onActionBar && entity is Player)
					{
						entity.sendActionBar(message)
					}
					else
					{
						entity.sendMessage(message)
					}
				}
			}
			
			return state
		}
		
	}
	
}