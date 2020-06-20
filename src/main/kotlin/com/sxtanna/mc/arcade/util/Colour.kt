package com.sxtanna.mc.arcade.util

import org.bukkit.ChatColor

object Colour
{
	
	const val Bold = "§l"
	const val Uline = "§n"
	const val Strike = "§m"
	const val Magic = "§k"
	const val Italic = "§o"
	const val Reset = "§r"
	
	
	const val Black = "§0"
	const val DBlue = "§1"
	const val DGreen = "§2"
	const val DAqua = "§3"
	const val DRed = "§4"
	const val DPurple = "§5"
	const val Gold = "§6"
	const val Gray = "§7"
	const val DGray = "§8"
	const val Blue = "§9"
	const val Green = "§a"
	const val Aqua = "§b"
	const val Red = "§c"
	const val Purple = "§d"
	const val Yellow = "§e"
	const val White = "§f"
	
	
	const val Val = Yellow
	const val Err = Red
	const val Bad = DRed + Bold
	const val Msg = Aqua
	const val Sep = DGray + Bold
	const val Txt = Gray
	
	const val None = ""
	const val Line = "\n"
	
	
	fun strip(string: String): String
	{
		return ChatColor.stripColor(color(string)) ?: ""
	}
	
	fun color(string: String): String
	{
		return ChatColor.translateAlternateColorCodes('&', string)
	}
	
}