package com.sxtanna.mc.arcade.data.base

import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.FireworkEffect
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkEffectMeta
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionType

sealed class Meta<T : Any>
{
	
	abstract fun apply(item: ItemStack, meta: ItemMeta, data: T)
	
	
	object NAME : Meta<String>()
	{
		
		override fun apply(item: ItemStack, meta: ItemMeta, data: String)
		{
			meta.setDisplayName(Colour.color(data))
		}
		
	}
	
	object LORE : Meta<List<String>>()
	{
		
		override fun apply(item: ItemStack, meta: ItemMeta, data: List<String>)
		{
			meta.lore = data.map(Colour::color)
		}
		
	}
	
	object FLAG : Meta<List<ItemFlag>>()
	{
		
		override fun apply(item: ItemStack, meta: ItemMeta, data: List<ItemFlag>)
		{
			meta.addItemFlags(*data.toTypedArray())
		}
		
	}
	
	
	object ENCHANT : Meta<Pair<Enchantment, Int>>()
	{
		
		override fun apply(item: ItemStack, meta: ItemMeta, data: Pair<Enchantment, Int>)
		{
			meta.addEnchant(data.first, data.second, true)
		}
		
	}
	
	
	object POTION_DATA : Meta<PotionData>()
	{
		
		override fun apply(item: ItemStack, meta: ItemMeta, data: PotionData)
		{
			(meta as? PotionMeta)?.apply { basePotionData = data  }
		}
		
	}
	
	object POTION_TYPE : Meta<PotionType>()
	{
		
		override fun apply(item: ItemStack, meta: ItemMeta, data: PotionType)
		{
			POTION_DATA.apply(item, meta, PotionData(data))
		}
		
	}
	
	
	object FIREWORK_EFFECT : Meta<FireworkEffect>()
	{
		
		override fun apply(item: ItemStack, meta: ItemMeta, data: FireworkEffect)
		{
			(meta as? FireworkEffectMeta)?.effect = data
		}
		
	}
	
}