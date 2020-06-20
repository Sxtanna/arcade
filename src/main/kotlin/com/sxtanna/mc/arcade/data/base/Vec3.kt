package com.sxtanna.mc.arcade.data.base

import com.sxtanna.korm.data.custom.KormList
import org.bukkit.util.Vector

@KormList(["x", "y", "z"])
data class Vec3(val x: Double, val y: Double, val z: Double)
{
	constructor(x: Number, y: Number, z: Number) : this(x.toDouble(), y.toDouble(), z.toDouble())
	
	
	fun mid(vec: Vec3): Vec3
	{
		return Vec3((x + vec.x) / 2.0, (y + vec.y) / 2.0, (z + vec.z) / 2.0)
	}
	
	
	fun toBukkit(): Vector
	{
		return Vector(x, y, z)
	}
	
	
	companion object
	{
		val ZERO = Vec3(0.0, 0.0, 0.0)
	}
	
}