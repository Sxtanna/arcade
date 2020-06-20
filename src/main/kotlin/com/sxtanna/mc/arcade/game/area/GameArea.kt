package com.sxtanna.mc.arcade.game.area

import com.sxtanna.mc.arcade.data.Bound
import com.sxtanna.mc.arcade.data.Point
import com.sxtanna.mc.arcade.data.base.Vec3
import org.bukkit.World

class GameArea
{
	
	var named = ""
	var built = ""
	
	var spawn = Vec3.ZERO
	var bound = Bound.ZERO
	
	val point = mutableMapOf<Int, MutableList<Point>>()
	
	
	@Transient
	var world = null as? World?
	
}