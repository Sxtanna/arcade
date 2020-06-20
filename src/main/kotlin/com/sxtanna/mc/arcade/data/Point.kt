package com.sxtanna.mc.arcade.data

import com.sxtanna.korm.data.custom.KormList
import com.sxtanna.mc.arcade.data.base.Vec3
import org.bukkit.Location
import org.bukkit.World

@KormList(["pos", "dir"])
data class Point(val pos: Vec3, val dir: Vec3)
{
	
	@Transient
	var uses = 0
	
	
	fun toBukkit(world: World): Location
	{
		return pos.toBukkit().toLocation(world).setDirection(dir.toBukkit())
	}
	
}