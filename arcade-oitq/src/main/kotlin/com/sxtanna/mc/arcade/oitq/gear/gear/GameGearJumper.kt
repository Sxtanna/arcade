package com.sxtanna.mc.arcade.oitq.gear.gear

import com.sxtanna.mc.arcade.data.Stack
import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.gear.part.GameGearPartJumpBoost
import com.sxtanna.mc.arcade.game.gear.part.GameGearPartNoFall
import com.sxtanna.mc.arcade.oitq.gear.base.GameGearBowAndArrow
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.concurrent.TimeUnit

class GameGearJumper(game: Game) : GameGearBowAndArrow(game, "Jumper")
{
	
	init
	{
		+GameGearPartNoFall(this)
		+GameGearPartJumpBoost(this, 3, 2.5, 20 * 10, GameGearPartJumpBoost.Activator.Item(1, stack), 2, TimeUnit.SECONDS)
	}
	
	override fun icon(): ItemStack
	{
		return stack[1]
	}
	
	
	private companion object
	{
		val stack = Stack(Material.STONE_AXE)
	}
	
}