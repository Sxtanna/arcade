package com.sxtanna.mc.arcade.data

import com.sxtanna.mc.arcade.base.Gives
import com.sxtanna.mc.arcade.data.Armor.ArmorPart.*
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

data class Armor(val part: ArmorPart, val type: ArmorType) : Gives<Player>
{
	
	@Transient
	private var cached: ItemStack? = null
	
	
	override fun give(data: Player)
	{
		val item = get()
		
		when (part)
		{
			HEAD -> data.equipment?.helmet = item
			BODY -> data.equipment?.chestplate = item
			LEGS -> data.equipment?.leggings = item
			FEET -> data.equipment?.boots = item
		}
	}
	
	
	fun get(): ItemStack
	{
		return cached ?: run()
		{
			val suff = when (part)
			{
				HEAD -> "HELMET"
				BODY -> "CHESTPLATE"
				LEGS -> "LEGGINGS"
				FEET -> "BOOTS"
			}
			
			val type = checkNotNull(Material.getMaterial("${type}_$suff"))
			{
				"failed to resolve type of: $this"
			}
			
			val item = ItemStack(type)
			cached = item
			
			item
		}
	}
	
	
	enum class ArmorPart
	{
		
		HEAD,
		BODY,
		LEGS,
		FEET,
		
	}
	
	enum class ArmorType
	{
		
		LEATHER,
		CHAINMAIL,
		GOLDEN,
		IRON,
		DIAMOND,
		
	}
	
}