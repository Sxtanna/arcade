package com.sxtanna.mc.arcade.func

import com.sxtanna.korm.Korm

val KORM = Korm()


fun Enum<*>.properName(): String
{
	return name.toLowerCase().capitalize()
}