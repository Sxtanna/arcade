package com.sxtanna.mc.arcade.hook.mods

import com.sxtanna.mc.arcade.base.Addon
import com.sxtanna.mc.arcade.base.Named
import com.sxtanna.mc.arcade.base.State
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

internal abstract class Module(final override val name: String) : Named, State, Addon, Listener
{
	
	override fun load()
	{
		server.pluginManager.registerEvents(this, plugin)
	}
	
	override fun kill()
	{
		HandlerList.unregisterAll(this)
	}
	
}