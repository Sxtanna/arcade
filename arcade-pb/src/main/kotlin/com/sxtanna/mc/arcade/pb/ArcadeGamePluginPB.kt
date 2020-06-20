package com.sxtanna.mc.arcade.pb

import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.base.GameInfo
import com.sxtanna.mc.arcade.hook.ArcadeGamePlugin
import com.sxtanna.mc.arcade.pb.game.GamePB
import org.bukkit.Material

class ArcadeGamePluginPB : ArcadeGamePlugin()
{
	
	override val info = GameInfo("Paintball", "PB", listOf("It's paintball"), Material.SNOWBALL)
	
	override fun make(): Game
	{
		return GamePB(this)
	}
	
}