package com.sxtanna.mc.arcade.hook.mods.damage.menu

import com.sxtanna.mc.arcade.data.Stack
import com.sxtanna.mc.arcade.data.base.Meta
import com.sxtanna.mc.arcade.func.formatBool
import com.sxtanna.mc.arcade.hook.ArcadeBasePlugin
import com.sxtanna.mc.arcade.menu.Menu
import com.sxtanna.mc.arcade.menu.Size
import org.bukkit.Material

class MenuDamage(override val plugin: ArcadeBasePlugin) : Menu(Size.ROWS_3, "Damage Engine")
{
	
	override fun make()
	{
		val delay = Stack(Material.CLOCK)
		delay[Meta.NAME, "&6Delay"]
		delay[Meta.LORE,
				listOf("  &8&l> ${formatBool(plugin.damage.delay)}", "",
				       "&7prevent an entity from",
				       "&7taking damage multiple",
				       "&7times within 500ms.")]
		
		this[11, delay[1]] = { _, _ ->
			plugin.damage.delay = !plugin.damage.delay
			redo()
		}
		
		
		val knock = Stack(Material.SLIME_BALL)
		knock[Meta.NAME, "&6Knockback"]
		knock[Meta.LORE,
				listOf("  &8&l> ${formatBool(plugin.damage.knock)}", "",
				       "&7pushes an entity away",
				       "&7from their damager",
				       "&7proportionate to the",
				       "&7damage taken.")]
		
		this[13, knock[1]] = { _, _ ->
			plugin.damage.knock = !plugin.damage.knock
			redo()
		}
		
		
		val pause = Stack(Material.BARRIER)
		pause[Meta.NAME, "&6Pause"]
		pause[Meta.LORE,
				listOf("  &8&l> ${formatBool(plugin.damage.pause)}", "",
				       "&7prevent any damage from",
				       "&7actually being applied",
				       "&7to the entity.")]
		
		this[15, pause[1]] = { _, _ ->
			plugin.damage.pause = !plugin.damage.pause
			redo()
		}
		
		
		val reset = Stack(Material.LAVA_BUCKET)
		reset[Meta.NAME, "&c&lReset Settings"]
		
		this[26, reset[1]] = { _, _ ->
			plugin.damage.delay = true
			plugin.damage.knock = true
			plugin.damage.pause = false
			
			redo()
		}
	}
	
}