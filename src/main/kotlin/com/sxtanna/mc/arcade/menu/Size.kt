package com.sxtanna.mc.arcade.menu

enum class Size
{
	
	ROWS_1,
	ROWS_2,
	ROWS_3,
	ROWS_4,
	ROWS_5,
	ROWS_6;
	
	
	val inventorySize = (ordinal + 1) * 9
	
}