package com.sxtanna.mc.arcade.game.cmds

import com.sxtanna.mc.arcade.cmds.Command
import com.sxtanna.mc.arcade.cmds.Context
import com.sxtanna.mc.arcade.hook.base.ArcadePlugin
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class CommandMaps(override val plugin: ArcadePlugin) : Command("maps")
{
	
	override fun Context.evaluate()
	{
		val player = sender as? Player ?: return
		
		val inventory = PermanentInventory()
		server.pluginManager.registerEvents(inventory, plugin)
		
		player.openInventory(inventory.inventory)
	}
	
	override fun Context.complete(out: MutableList<String>)
	{
	
	}
	
	
	inner class PermanentInventory : InventoryHolder, Listener
	{
		
		private val backing = server.createInventory(this, 18, "Permanent >:)")
		
		init
		{
			var slot = 0
			
			Tag.WOOL.values.forEach()
			{ wool ->
				backing.setItem(slot++, ItemStack(wool))
			}
		}
		
		override fun getInventory(): Inventory
		{
			return backing
		}
		
		
		@EventHandler
		fun InventoryClickEvent.onClick()
		{
			if (inventory.holder !is PermanentInventory)
			{
				return
			}
			
			isCancelled = true
			
			if (click != ClickType.CONTROL_DROP)
			{
				return
			}
			
			whoClicked.closeInventory()
		}
		
		@EventHandler
		fun InventoryCloseEvent.onClose()
		{
			if (inventory.holder !is PermanentInventory)
			{
				return
			}
			
			if (reason == InventoryCloseEvent.Reason.PLUGIN)
			{
				return HandlerList.unregisterAll(this@PermanentInventory)
			}
			
			queue()
			{
				player.openInventory(inventory)
			}
		}
		
	}
	
}