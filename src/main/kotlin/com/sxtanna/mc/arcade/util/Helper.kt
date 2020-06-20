package com.sxtanna.mc.arcade.util

import org.bukkit.entity.Entity
import org.bukkit.entity.TNTPrimed
import org.bukkit.entity.Tameable

object Helper
{
	
	fun properName(entity: Entity?): String
	{
		if (entity == null)
		{
			return "Unknown"
		}
		if (entity is TNTPrimed)
		{
			return "Tnt"
		}
		val custom = entity.customName
		if (custom != null)
		{
			return custom
		}
		
		val proper = entity.name
		if (entity !is Tameable)
		{
			return proper
		}
		
		val owner = entity.owner
		return if (owner == null) proper else "$owner's $proper"
	}
	
}