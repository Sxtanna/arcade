package com.sxtanna.mc.arcade.menu

import com.sxtanna.mc.arcade.base.Addon
import com.sxtanna.mc.arcade.base.Named
import com.sxtanna.mc.arcade.base.State
import com.sxtanna.mc.arcade.data.Stack
import com.sxtanna.mc.arcade.data.base.Meta
import com.sxtanna.mc.arcade.lang.Lang
import com.sxtanna.mc.arcade.lang.LangKey
import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import kotlin.math.max
import kotlin.math.min

abstract class Menu(private val size: Size, final override val name: String) : Named, Addon, State, Listener, InventoryHolder
{
	
	private var done = false
	private var load = false
	private val data = mutableMapOf<Int, (who: Player, how: ClickType) -> Unit>()
	
	protected open val prev = null as? Menu?
	protected open val menu by lazy()
	{
		server.createInventory(this, size.inventorySize, Colour.color(name))
	}
	
	
	operator fun get(player: Player)
	{
		if (!done)
		{
			done = true
			make()
			load()
		}
		
		open(player)
		
		player.closeInventory(InventoryCloseEvent.Reason.OPEN_NEW)
		player.openInventory(menu)
	}
	
	protected operator fun set(slot: Int, item: ItemStack)
	{
		menu.setItem(slot, item)
	}
	
	protected operator fun set(slot: Int, item: ItemStack, action: (who: Player, how: ClickType) -> Unit)
	{
		this[slot] = item
		data[slot] = action
	}
	
	
	protected open fun make()
	{
	
	}
	
	protected open fun redo()
	{
		data.clear()
		menu.clear()
		
		make()
	}
	
	protected open fun open(player: Player)
	{
	
	}
	
	
	final override fun load()
	{
		if (load)
		{
			return
		}
		
		load = true
		server.pluginManager.registerEvents(this, plugin)
	}
	
	final override fun kill()
	{
		load = false
		done = false
		
		HandlerList.unregisterAll(this)
		
		data.clear()
		menu.clear()
	}
	
	final override fun getInventory(): Inventory
	{
		return this.menu
	}
	
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun InventoryCloseEvent.onClose()
	{
		if (inventory.holder != this@Menu)
		{
			return
		}
		
		kill()
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun InventoryClickEvent.onClick()
	{
		if (slotType == InventoryType.SlotType.OUTSIDE || clickedInventory?.holder != this@Menu)
		{
			return
		}
		
		val who = whoClicked as? Player ?: return
		val how = click
		
		isCancelled = true
		
		data[slot]?.invoke(who, how)
	}
	
	
	private enum class MenuText(override val default: String) : LangKey
	{
		
		NEXT_PAGE_NAME("&aNext Page"),
		NEXT_PAGE_LORE("    &a&m    >\n\n&7You are on page:&f {page_number}"),
		
		PREV_PAGE_NAME("&aPrev Page"),
		PREV_PAGE_LORE("    &a&m    >\n\n&7You are on page:&f {page_number}"),
		
	}
	
	
	class Page<T>(val menu: Menu, val prevSlot: Int, val nextSlot: Int, data: Collection<T>, chunk: Int)
	{
		
		private var index = 0
		private val pages = data.chunked(chunk)
		
		
		fun init()
		{
			if (pages.size <= 1)
			{
				return
			}
			
			if (index > 0)
			{
				val prev = Stack(Material.OAK_SIGN)
				
				prev[Meta.NAME, Lang.make(MenuText.PREV_PAGE_NAME, "page_number", index)]
				prev[Meta.LORE, Lang.make(MenuText.PREV_PAGE_LORE, "page_number", index).split('\n')]
				
				menu[prevSlot, prev[1]] = { _, _ ->
					prev()
				}
			}
			
			if (index < pages.lastIndex)
			{
				val next = Stack(Material.OAK_SIGN)
				
				next[Meta.NAME, Lang.make(MenuText.NEXT_PAGE_NAME, "page_number", index)]
				next[Meta.LORE, Lang.make(MenuText.NEXT_PAGE_LORE, "page_number", index).split('\n')]
				
				menu[nextSlot, next[1]] = { _, _ ->
					next()
				}
			}
		}
		
		
		fun next()
		{
			index = min(index++, pages.lastIndex)
			menu.redo()
		}
		
		fun prev()
		{
			index = max(index--, 0)
			menu.redo()
		}
		
		
		fun page(): List<T>
		{
			return pages.getOrNull(index) ?: emptyList()
		}
		
		fun each(consumer: (T) -> Unit)
		{
			page().forEach(consumer)
		}
		
	}
	
}