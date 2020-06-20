package com.sxtanna.mc.arcade.data

import org.bukkit.ChatColor
import org.bukkit.DyeColor
import org.bukkit.Material

enum class Color(val code: Char, val chat: ChatColor, val dye: DyeColor, val wool: Material, val glass: Material,  val pane: Material)
{
	
	BLACK('0', ChatColor.BLACK, DyeColor.BLACK, Material.BLACK_WOOL, Material.BLACK_STAINED_GLASS, Material.BLACK_STAINED_GLASS_PANE),
	WHITE('f', ChatColor.WHITE, DyeColor.WHITE, Material.WHITE_WOOL, Material.WHITE_STAINED_GLASS, Material.WHITE_STAINED_GLASS_PANE),
	
	RED('c', ChatColor.RED, DyeColor.RED, Material.RED_WOOL, Material.RED_STAINED_GLASS, Material.RED_STAINED_GLASS_PANE),
	BLUE('9', ChatColor.BLUE, DyeColor.BLUE, Material.BLUE_WOOL, Material.BLUE_STAINED_GLASS, Material.BLUE_STAINED_GLASS_PANE),
	
	YELLOW('e', ChatColor.YELLOW, DyeColor.YELLOW,  Material.YELLOW_WOOL, Material.YELLOW_STAINED_GLASS, Material.YELLOW_STAINED_GLASS_PANE),
	
}