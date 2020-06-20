package com.sxtanna.mc.arcade.oitq

import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.base.GameInfo
import com.sxtanna.mc.arcade.hook.ArcadeGamePlugin
import com.sxtanna.mc.arcade.oitq.game.GameOITQ
import org.bukkit.Material

class ArcadeGamePluginOITQ : ArcadeGamePlugin()
{
	
	override val info = GameInfo("One In The Quiver", "OITQ", listOf("stupid arrow game"), Material.ARROW)
	
	
	override fun make(): Game
	{
		return GameOITQ(this)
	}
	
}