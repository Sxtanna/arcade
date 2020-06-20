package com.sxtanna.mc.arcade.func

import com.sxtanna.mc.arcade.data.base.Vec3


fun map(value: Number, oldMin: Number, oldMax: Number, newMin: Number, newMax: Number): Double
{
	return (value.toDouble() - oldMin.toDouble()) / (oldMax.toDouble() - oldMin.toDouble()) * (newMax.toDouble() - newMin.toDouble()) + newMin.toDouble()
}

fun generateSphere(radius: Double, hollow: Boolean): Set<Vec3>
{
	val vecs = mutableSetOf<Vec3>()
	
	var x = -radius
	
	while (x <= radius)
	{
		var y = -radius
		
		
		while (y <= radius)
		{
			var z = -radius
			
			
			while (z <= radius)
			{
				val dist = (((0 - x) * (0 - x)) + ((0 - z) * (0 - z)) + ((0 - y) * (0 - y)))
				
				if (dist < (radius * radius) && !(hollow && dist < ((radius - 1) * (radius - 1))))
				{
					vecs += Vec3(x, y, z)
				}
				
				z++
			}
			y++
		}
		x++
	}
	
	return vecs
}