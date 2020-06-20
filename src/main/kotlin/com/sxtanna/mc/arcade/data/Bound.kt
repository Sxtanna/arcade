package com.sxtanna.mc.arcade.data

import com.sxtanna.mc.arcade.data.base.Vec3
import org.bukkit.util.BoundingBox

data class Bound(val min: Vec3, val max: Vec3)
{
	
	fun lenX(): Double
	{
		return max.x - min.x
	}
	
	fun lenY(): Double
	{
		return max.y - min.y
	}
	
	fun lenZ(): Double
	{
		return max.z - min.z
	}
	
	
	
	fun midX(): Double
	{
		return (min.x + lenX()) * 0.5
	}
	
	fun midY(): Double
	{
		return (min.y + lenY()) * 0.5
	}
	
	fun midZ(): Double
	{
		return (min.z + lenZ()) * 0.5
	}
	
	
	fun midpoint(): Vec3
	{
		return Vec3(midX(), midY(), midZ())
	}
	
	
	fun toBukkit(): BoundingBox
	{
		return BoundingBox.of(min.toBukkit(), max.toBukkit())
	}
	
	
	companion object
	{
		val ZERO = Bound(Vec3.ZERO, Vec3.ZERO)
	}
	
}