package com.sxtanna.mc.arcade.hook.mods.helper

import com.sxtanna.mc.arcade.hook.base.ArcadePlugin
import com.sxtanna.mc.arcade.hook.mods.Module
import com.sxtanna.mc.arcade.hook.mods.helper.cmds.CommandFly
import com.sxtanna.mc.arcade.hook.mods.helper.cmds.CommandMode
import com.sxtanna.mc.arcade.hook.mods.helper.cmds.CommandPing

internal class HelperModule(override val plugin: ArcadePlugin) : Module("Helper")
{
	
	private val commandFly  = CommandFly(plugin)
	private val commandMode = CommandMode(plugin)
	private val commandPing = CommandPing(plugin)
	
	
	override fun load()
	{
		super.load()
		
		commandFly.load()
		commandPing.load()
		commandMode.load()
	}
	
	override fun kill()
	{
		super.kill()
		
		commandFly.kill()
		commandPing.kill()
		commandMode.kill()
	}
	
}