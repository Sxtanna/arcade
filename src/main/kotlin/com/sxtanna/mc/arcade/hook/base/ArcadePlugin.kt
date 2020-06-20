package com.sxtanna.mc.arcade.hook.base

import com.sxtanna.mc.arcade.Arcade
import org.bukkit.plugin.java.JavaPlugin

abstract class ArcadePlugin : JavaPlugin()
{
	
	abstract val arcade: Arcade
	
}