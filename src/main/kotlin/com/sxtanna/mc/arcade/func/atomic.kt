package com.sxtanna.mc.arcade.func

import com.sxtanna.mc.arcade.util.Colour
import java.util.concurrent.atomic.AtomicInteger

class AtomicCounter(private val start: Int, private val finish: () -> Unit)
{
	
	private val target = AtomicInteger(start)
	
	
	fun count()
	{
		if (target.decrementAndGet() == 0)
		{
			finish.invoke()
		}
	}
	
	fun ready(): Boolean
	{
		return target.get() > 0
	}
	
	
	fun remaining(): Int
	{
		return target.get()
	}
	
	fun statusBar(): String
	{
		val length = 20
		
		val brackL = "["
		val brackM = "="
		val brackR = "]"
		
		val gCount = map((start - remaining()), 0, start, 0, length).toInt()
		val nCount = length - gCount
		
		return "$brackL${Colour.Green}${brackM.repeat(gCount)}${Colour.Gray}${brackM.repeat(nCount)}$brackR"
	}
	
}