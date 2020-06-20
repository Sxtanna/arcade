package com.sxtanna.mc.arcade.game.menu

import com.sxtanna.mc.arcade.data.Stack
import com.sxtanna.mc.arcade.data.base.Meta
import com.sxtanna.mc.arcade.game.base.GameState
import com.sxtanna.mc.arcade.game.data.GameDatas
import com.sxtanna.mc.arcade.hook.ArcadeBasePlugin
import com.sxtanna.mc.arcade.menu.Menu
import com.sxtanna.mc.arcade.menu.Size
import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemFlag

class MenuGame(override val plugin: ArcadeBasePlugin) : Menu(Size.ROWS_6, "${Colour.DGray}Arcade")
{
	
	private var click = -1L
	private val games = Page(this, 47, 51, plugin.arcade.cachedGames.keys, 10)
	
	
	override fun make()
	{
		super.make()
		
		val current = plugin.arcade.currentGame
		
		if (current != null)
		{
			val item = Stack(current.info.icon)
			
			item[Meta.NAME, "${Colour.White}${current.info.name}"]
			item[Meta.LORE, current.info.desc.map { "  ${Colour.Gray}$it" }]
			item[Meta.ENCHANT, Enchantment.DIG_SPEED to 1]
			item[Meta.FLAG, listOf(ItemFlag.HIDE_ENCHANTS)]
			
			currSlots.forEach()
			{ slot ->
				this[slot] = item[1]
			}
		}
		
		val gameSlots = gameSlots.iterator()
		
		games.each()
		{ info ->
			if (!gameSlots.hasNext())
			{
				return@each
			}
			
			val slot = gameSlots.nextInt()
			val item = Stack(info.icon)
			
			item[Meta.NAME, "${Colour.White}${info.name}"]
			item[Meta.LORE, info.desc.map { "  ${Colour.Gray}$it" }]
			
			if (current?.info == info)
			{
				item[Meta.ENCHANT, Enchantment.DIG_SPEED to 1]
				item[Meta.FLAG, listOf(ItemFlag.HIDE_ENCHANTS)]
			}
			
			this[slot, item[1]] = out@{ _, _ ->
				
				if (!timedClick())
				{
					return@out
				}
				
				if (current?.info != info)
				{
					later(1)
					{
						laterAsync(5)
						{
							redo()
						}
						
						plugin.arcade.loadGame(info)
					}
				}
			}
		}
		
		
		games.init()
		
		
		val buttonStart = Stack(if (current != null) Material.GREEN_STAINED_GLASS_PANE else Material.LIGHT_GRAY_STAINED_GLASS_PANE)
		
		if (current == null || current.state() > GameState.READY)
		{
			buttonStart[Meta.NAME, " "]
		}
		else
		{
			buttonStart[Meta.NAME, "${Colour.White}${Colour.Bold}Start Game"]
		}
		
		startSlots.forEach()
		{ slot ->
			this[slot, buttonStart[1]] = out@{ _, how ->
				
				if (!timedClick())
				{
					return@out
				}
				
				val game = current ?: return@out
				
				if (game.state() != GameState.READY)
				{
					game.state(GameState.READY, GameState.SetCause.PLAYER)
					
					return@out redo()
				}
				
				game.reduceCountdown(if (how == ClickType.SHIFT_LEFT) 5 else 1)
				redo()
			}
		}
		
		
		val buttonStop = Stack(if (current != null) Material.RED_STAINED_GLASS_PANE else Material.LIGHT_GRAY_STAINED_GLASS_PANE)
		if (current == null)
		{
			buttonStop[Meta.NAME, " "]
		}
		else
		{
			buttonStop[Meta.NAME, "${Colour.White}${Colour.Bold}${if (current.state().active()) "Stop" else "Kill"} Game"]
			buttonStop[Meta.LORE, listOf(
				"  ${Colour.Gray}SHIFT_LEFT to stop now",
				"  ${Colour.Gray}CONTROL_DROP to kill game"
			                            )]
		}
		
		stopSlots.forEach()
		{ slot ->
			this[slot, buttonStop[1]] = out@{ _, how ->
				
				if (!timedClick())
				{
					return@out
				}
				
				val game = current ?: return@out
				
				if (how == ClickType.CONTROL_DROP)
				{
					plugin.arcade.killGame()
					return@out redo()
				}
				
				if (how == ClickType.SHIFT_LEFT)
				{
					game[GameDatas.OPTION_GAME_ENDS_INSTANTLY] = true
				}
				
				game.state(GameState.ENDED, GameState.SetCause.PLAYER)
				redo()
			}
		}
	}
	
	
	private fun timedClick(): Boolean
	{
		if (click == -1L)
		{
			click = System.currentTimeMillis()
			return true
		}
		
		if (System.currentTimeMillis() - click < TIME)
		{
			return false
		}
		
		click = System.currentTimeMillis()
		return true
	}
	
	
	private companion object
	{
		const val TIME = (1000 * 2.5).toLong()
		
		val currSlots = intArrayOf(
			4
		                          )
		
		val gameSlots = intArrayOf(
			20, 21, 22, 23, 24,
			29, 30, 31, 32, 33
		                          )
		
		val startSlots = intArrayOf(
			18,
			27
		                           )
		
		val stopSlots = intArrayOf(
			26,
			35
		                          )
	}
	
}