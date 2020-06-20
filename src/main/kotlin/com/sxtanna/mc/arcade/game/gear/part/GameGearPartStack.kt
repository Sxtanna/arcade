package com.sxtanna.mc.arcade.game.gear.part

import com.sxtanna.mc.arcade.data.Stack
import com.sxtanna.mc.arcade.func.properName
import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.game.gear.base.GameGearPart
import org.bukkit.entity.Player

class GameGearPartStack(gear: GameGear, private val amount: Int, private val stack: Stack) : GameGearPart(gear, stack.item.type.properName())
{
	
	init
	{
		stack.meta { isUnbreakable = true }
	}
	
	
	override fun give(data: Player)
	{
		super.give(data)
		
		data.inventory.addItem(stack[amount])
	}
	
	override fun take(data: Player)
	{
		super.take(data)
		
		data.inventory.removeItem(stack[1])
	}
	
}