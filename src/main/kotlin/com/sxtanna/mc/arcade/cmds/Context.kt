package com.sxtanna.mc.arcade.cmds

import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.command.CommandSender

data class Context(val sender: CommandSender, val alias: String, val input: List<String>)
{
	
	fun reply(text: String, sender: CommandSender = this.sender)
	{
		sender.sendMessage(Colour.color(text))
	}
	
	fun error(text: String, sender: CommandSender = this.sender)
	{
		sender.sendMessage(Colour.color("&c$text"))
	}
	
	
	fun List<String>.filter(index: Int): List<String>
	{
		if (input.lastIndex < index)
		{
			return this
		}
		
		return filter { it.startsWith(input[index], true) }
	}
	
	fun splitByComma(out: MutableList<String>, data: () -> List<String>)
	{
		val list = data()
		val prev = input.last()
		
		if (prev.isBlank() || !prev.contains(','))
		{
			out += list.filter(input.lastIndex)
			return
		}
		
		val held = input.last().toLowerCase().split(',')
		val last = held.last()
		
		out += list.filter { it.toLowerCase() !in held && (last.isBlank() || it.startsWith(last, true))}.map { "${prev.dropLast(last.length)}$it" }
	}
	
}