package com.sxtanna.mc.arcade.lang

import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.command.CommandSender

object Lang : MutableMap<LangKey, String> by mutableMapOf()
{
	
	fun make(key: LangKey, vararg placeholders: Any): String
	{
		require(placeholders.isEmpty() || placeholders.size % 2 == 0)
		{
			"Placeholders must all have values: ${placeholders.contentToString()}"
		}
		
		
		var text = getOrDefault(key, key.default)
		
		if (placeholders.isNotEmpty())
		{
			var i = 0
			while (i < placeholders.size)
			{
				val k = placeholders[i]
				val v = placeholders[i + 1]
				
				require(k is String)
				{
					"Placeholder values has an object out of position: ${k.javaClass}[$i]{$k}"
				}
				
				text = text.replace("{$k}", v.toString())
				i += 2
			}
		}
		
		return Colour.color(text)
	}
	
	fun send(recipient: CommandSender, key: LangKey, vararg placeholders: Any)
	{
		recipient.sendMessage(make(key, *placeholders))
	}
	
}