@file:Suppress("UNCHECKED_CAST")

package com.sxtanna.mc.arcade.data

import com.sxtanna.mc.arcade.data.base.Meta
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

class Stack(val item: ItemStack)
{
	
	constructor(type: Material) : this(ItemStack(type))
	
	init
	{
		item.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE)
	}
	
	
	operator fun get(amount: Int): ItemStack
	{
		val item = this.item.clone()
		item.amount = amount
		
		return item
	}
	
	operator fun <T : Any> get(meta: Meta<T>, data: T): Stack
	{
		return meta()
		{
			meta.apply(item, this, data)
		}
	}
	
	
	fun meta(function: ItemMeta.() -> Unit): Stack
	{
		item.itemMeta = item.itemMeta.apply(function)
		return this
	}
	
	
}