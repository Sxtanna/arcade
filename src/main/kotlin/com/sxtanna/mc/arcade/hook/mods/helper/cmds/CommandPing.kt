package com.sxtanna.mc.arcade.hook.mods.helper.cmds

import com.sxtanna.mc.arcade.cmds.Command
import com.sxtanna.mc.arcade.cmds.Context
import com.sxtanna.mc.arcade.hook.base.ArcadePlugin
import org.bukkit.entity.Player

class CommandPing(override val plugin: ArcadePlugin) : Command("Ping")
{
	
	override fun Context.evaluate()
	{
		val player = sender as? Player ?: return error("You must be a player")
		reply("Your ping is: ${player.spigot().ping}")
	}
	
	override fun Context.complete(out: MutableList<String>)
	{
	
	}
	
}