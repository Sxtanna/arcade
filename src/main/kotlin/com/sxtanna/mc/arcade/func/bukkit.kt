package com.sxtanna.mc.arcade.func

import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.MetadataValue
import org.bukkit.metadata.Metadatable
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import java.util.concurrent.TimeUnit

fun TimeUnit.convertToTicks(time: Long): Long
{
	return TimeUnit.MILLISECONDS.convert(time, this) / 50
}


fun formatBool(boolean: Boolean, boolT: String = "&aENABLED", boolF: String = "&cDISABLED"): String
{
	return if (boolean) boolT else boolF
}


fun materialsLike(name: String): List<Material>
{
	return Material.values().filter { it.name.contains(name, true) }
}



fun Action.isHand(): Boolean
{
	return this != Action.PHYSICAL
}

fun Action.isLeft(): Boolean
{
	return this == Action.LEFT_CLICK_AIR || this == Action.LEFT_CLICK_BLOCK
}

fun Action.isRight(): Boolean
{
	return this == Action.RIGHT_CLICK_AIR || this == Action.RIGHT_CLICK_BLOCK
}


fun Material.isWool(): Boolean
{
	return this.name.endsWith("_WOOL")
}

fun Material.isStainedGlass(): Boolean
{
	return this.name.endsWith("_STAINED_GLASS")
}

fun Material.isStainedGlassPane(): Boolean
{
	return this.name.endsWith("_STAINED_GLASS_PANE")
}


fun Metadatable.getMeta(name: String, plugin: Plugin): MetadataValue?
{
	return getMetadata(name).find { it.owningPlugin == plugin }
}

fun Metadatable.setMeta(name: String, plugin: Plugin, value: Any)
{
	setMetadata(name, FixedMetadataValue(plugin, value))
}


fun Player.resetPlayer()
{
	health = getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
	foodLevel = 20
	saturation = 20F
	
	if (gameMode != GameMode.CREATIVE && gameMode != GameMode.SPECTATOR)
	{
		allowFlight = false
		isFlying = false
	}
	
	isCollidable = true
	
	activePotionEffects.map(PotionEffect::getType).forEach(this::removePotionEffect)
}
