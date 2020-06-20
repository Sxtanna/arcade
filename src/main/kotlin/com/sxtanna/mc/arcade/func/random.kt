package com.sxtanna.mc.arcade.func

import java.util.concurrent.ThreadLocalRandom

private val RANDOM = ThreadLocalRandom.current()


fun randFloat64(max: Double): Double
{
	return RANDOM.nextDouble(max)
}


fun randChance(outOf100: Number): Boolean
{
	return randFloat64(100.0) <= outOf100.toDouble()
}