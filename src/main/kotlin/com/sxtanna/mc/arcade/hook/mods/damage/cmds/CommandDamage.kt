package com.sxtanna.mc.arcade.hook.mods.damage.cmds

import com.sxtanna.mc.arcade.cmds.Command
import com.sxtanna.mc.arcade.cmds.Context
import com.sxtanna.mc.arcade.func.formatBool
import com.sxtanna.mc.arcade.hook.ArcadeBasePlugin
import com.sxtanna.mc.arcade.hook.mods.damage.menu.MenuDamage
import org.bukkit.entity.Player

internal class CommandDamage(override val plugin: ArcadeBasePlugin) : Command("damage")
{
	
	override fun Context.evaluate()
	{
		if (input.isEmpty())
		{
			return MenuDamage(plugin)[sender as? Player ?: return]
		}
		
		val prop = when (input[0].toLowerCase())
		{
			"delay" -> plugin.damage::delay
			"knock" -> plugin.damage::knock
			"pause" -> plugin.damage::pause
			else    ->
			{
				return error("invalid property")
			}
		}
		
		val next = if (input.size > 1) when (input[1].toLowerCase())
		{
			"true"  -> true
			"false" -> false
			else    ->
			{
				return error("invalid boolean")
			}
		}
		else
		{
			!prop.get()
		}
		
		
		prop.set(next)
		reply("&6${input[0].toLowerCase()} &7is now ${formatBool(prop.get())}")
	}
	
	override fun Context.complete(out: MutableList<String>)
	{
		when (input.size)
		{
			0, 1 ->
			{
				out += listOf("delay", "knock", "pause").filter(0)
			}
			else ->
			{
				if (input.size > 2)
				{
					return
				}
				
				val state = when (input[0].toLowerCase())
				{
					"delay" -> plugin.damage.delay
					"knock" -> plugin.damage.knock
					"pause" -> plugin.damage.pause
					else    ->
					{
						return
					}
				}
				
				out += "${!state}"
			}
		}
	}
	
}