package com.sxtanna.mc.arcade.pu

import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.base.GameInfo
import com.sxtanna.mc.arcade.hook.ArcadeGamePlugin
import com.sxtanna.mc.arcade.pu.game.GamePU
import org.bukkit.Material

class ArcadeGamePluginPU : ArcadeGamePlugin()
{
	
	override val info = GameInfo("Push", "PU", listOf("Push the enemy team back"), Material.SLIME_BALL)
	
	
	override fun make(): Game
	{
		return GamePU(this)
	}
	
}