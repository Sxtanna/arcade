package com.sxtanna.mc.arcade.game.wait.menu

import com.sxtanna.mc.arcade.base.Icons
import com.sxtanna.mc.arcade.base.Shown
import com.sxtanna.mc.arcade.data.Stack
import com.sxtanna.mc.arcade.data.base.Meta
import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.game.team.GameTeam
import com.sxtanna.mc.arcade.game.wait.area.Selector
import com.sxtanna.mc.arcade.game.wait.area.SelectorMode
import com.sxtanna.mc.arcade.hook.base.ArcadePlugin
import com.sxtanna.mc.arcade.menu.Menu
import com.sxtanna.mc.arcade.menu.Size
import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class SelectorMenu(override val plugin: ArcadePlugin, val game: Game, val selector: Selector, val data: List<Shown>, val mode: SelectorMode) : Menu(Data.values()[if (data.isEmpty()) 0 else data.size - 1].size, "${Colour.Gray}Select your ${mode.name.toLowerCase().capitalize()}")
{
	
	private var selected = null as? Shown?
	
	
	override fun make()
	{
		val slots = Data.values()[if (data.isEmpty()) 0 else data.size - 1].slot.iterator()
		
		data.forEach()
		{ shown ->
			if (!slots.hasNext())
			{
				return
			}
			
			val item = if (shown is Icons)
			{
				Stack(shown.icon())
			}
			else
			{
				Stack(Material.WHITE_STAINED_GLASS_PANE)
			}
			
			if (selected == shown)
			{
				item[Meta.LORE, listOf("  ${Colour.Gray}Selected")]
			}
			
			val slot = slots.nextInt()
			
			set(slot, item[Meta.NAME, shown.external][1])
			{ who, _ ->
				val result = when (mode)
				{
					SelectorMode.TEAM ->
					{
						game.pick(who, shown as GameTeam)
					}
					SelectorMode.GEAR ->
					{
						game.pick(who, shown as GameGear)
					}
				}
				
				if (result)
				{
					selected = shown
					redo()
					
					if (mode == selector.mode)
					{
						selector.inform(who, shown)
					}
				}
			}
		}
	}
	
	override fun open(player: Player)
	{
		val gearPick = game.gearPick(player)
		val teamPick = game.teamPick(player)
		
		if (gearPick == null && teamPick == null)
		{
			return
		}
		
		for ((slot, item) in inventory.contents.withIndex())
		{
			if (item == null || !item.hasItemMeta() || !item.itemMeta.hasDisplayName())
			{
				continue
			}
			
			val displayName = Colour.strip(item.itemMeta.displayName)
			
			val gearName = gearPick?.external?.let(Colour::strip)
			val teamName = teamPick?.external?.let(Colour::strip)
			
			if (displayName != gearName && displayName != teamName)
			{
				continue
			}
			
			set(slot, Stack(item)[Meta.LORE, listOf("  ${Colour.Gray}Selected")][1])
		}
	}
	
	
	private enum class Data(val size: Size, vararg val slot: Int)
	{
		
		SIZE_1(Size.ROWS_3, 13),
		SIZE_2(Size.ROWS_3, 12, 14),
		SIZE_3(Size.ROWS_3, 11, 13, 15),
		SIZE_4(Size.ROWS_3, 10, 12, 14, 16),
		SIZE_5(Size.ROWS_3, 10, 11, 13, 15, 16),
		SIZE_6(Size.ROWS_4, 10, 20, 12, 14, 24, 16),
		SIZE_7(Size.ROWS_4, 10, 20, 12, 22, 14, 24, 16),
		SIZE_8(Size.ROWS_5, 10, 12, 14, 16, 28, 30, 32, 34),
		SIZE_9(Size.ROWS_5, 10, 12, 14, 16, 22, 28, 30, 32, 34),
		
	}
	
}