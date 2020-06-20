package com.sxtanna.mc.arcade.time.reply

sealed class Reply : (String) -> Unit
{
	
	abstract override fun invoke(name: String)
	
	
	object Console : Reply()
	{
		
		override fun invoke(name: String)
		{
			println("$name is done")
		}
		
	}
	
	class Execute(private val function: (String) -> Unit) : Reply()
	{
		
		override fun invoke(name: String)
		{
			function.invoke(name)
		}
		
	}
	
}