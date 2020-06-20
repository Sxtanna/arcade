package com.sxtanna.mc.arcade.game.wait.area

import com.sxtanna.mc.arcade.base.Addon
import com.sxtanna.mc.arcade.base.Icons
import com.sxtanna.mc.arcade.base.Shown
import com.sxtanna.mc.arcade.base.State
import com.sxtanna.mc.arcade.data.Armor
import com.sxtanna.mc.arcade.func.KORM
import com.sxtanna.mc.arcade.func.getMeta
import com.sxtanna.mc.arcade.func.setMeta
import com.sxtanna.mc.arcade.game.Game
import com.sxtanna.mc.arcade.game.gear.base.GameGear
import com.sxtanna.mc.arcade.game.team.GameTeam
import com.sxtanna.mc.arcade.game.wait.menu.SelectorMenu
import com.sxtanna.mc.arcade.hook.ArcadeGamePlugin
import com.sxtanna.mc.arcade.hook.mods.damage.base.HurtEvent
import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitTask

class Selector(override val plugin: ArcadeGamePlugin, private val game: Game, val mode: SelectorMode) : Addon, State, Listener
{
	
	private val chests = mutableSetOf<Block>()
	private val stands = mutableSetOf<ArmorStand>()
	
	private val mapped = mutableMapOf<Shown, ArmorStand>()
	
	private var update = null as? BukkitTask?
	private val choice = mutableMapOf<Player, ArmorStand>()
	
	
	override fun load()
	{
		plugin.server.pluginManager.registerEvents(this, plugin)
		
		val world = plugin.arcade.waitingArea.world ?: return
		val spawn = plugin.arcade.waitingArea.spawn ?: return
		
		val gearChest = world.getBlockAt(8, 142, 5)
		val teamChest = world.getBlockAt(8, 142, 9)
		
		chests += gearChest
		chests += teamChest
		
		gearChest.setMetadata(META_GAME_GEAR, FixedMetadataValue(plugin, 0))
		teamChest.setMetadata(META_GAME_TEAM, FixedMetadataValue(plugin, 0))
		
		stands += world.spawn(gearChest.location.add(0.5, 0.0, 0.5), ArmorStand::class.java)
		{ stand ->
			stand.isSmall = true
			stand.isVisible = false
			
			stand.customName = "Select Gear"
			stand.isCustomNameVisible = true
		}
		
		stands += world.spawn(teamChest.location.add(0.5, 0.0, 0.5), ArmorStand::class.java)
		{ stand ->
			stand.isSmall = true
			stand.isVisible = false
			
			stand.customName = "Select Team"
			stand.isCustomNameVisible = true
		}
		
		val selectors = KORM.pull(world.worldFolder.resolve("selector.korm")).to<SelectorData>() ?: return
		
		val positions = when (mode)
		                {
			                SelectorMode.TEAM ->
			                {
				                selectors.team ?: selectors.gear
			                }
			                SelectorMode.GEAR ->
			                {
				                selectors.gear ?: selectors.team
			                }
		                } ?: return
		
		val tags = when (mode)
		{
			SelectorMode.TEAM ->
			{
				META_GAME_TEAM
			}
			SelectorMode.GEAR ->
			{
				META_GAME_GEAR
			}
		}
		
		val values: List<Shown> = when (mode)
		{
			SelectorMode.TEAM ->
			{
				game.teams()
			}
			SelectorMode.GEAR ->
			{
				game.gears()
			}
		}
		
		val iter = positions.iterator()
		
		values.forEach()
		{ shown ->
			if (!iter.hasNext())
			{
				return@forEach
			}
			
			val pos = iter.next()
			val dir = spawn.toVector().subtract(pos.toBukkit()).normalize()
			
			val stand = world.spawn(Location(world, pos.x, pos.y, pos.z).setDirection(dir), ArmorStand::class.java)
			{ stand ->
				
				stand.isSilent = true
				stand.isInvulnerable = true
				
				stand.setArms(true)
				stand.setGravity(false)
				stand.setBasePlate(false)
				
				stand.customName = shown.external
				stand.isCustomNameVisible = true
				
				
				stand.setMeta(tags, plugin, shown)
				
				
				val equipment = stand.equipment ?: return@spawn
				
				equipment.helmet = ARMOR_HEAD.get()
				equipment.chestplate = ARMOR_BODY.get()
				equipment.leggings = ARMOR_LEGS.get()
				equipment.boots = ARMOR_FEET.get()
				
				
				if (shown is Icons)
				{
					val item = shown.icon()
					
					if (item.type != Material.AIR)
					{
						equipment.setItemInMainHand(item)
					}
				}
				
				if (shown !is GameTeam)
				{
					return@spawn
				}
				
				val armors = equipment.armorContents
				
				for (armor in armors)
				{
					val meta = (armor.itemMeta as? LeatherArmorMeta) ?: continue
					meta.setColor(shown.color.dye.color)
					
					armor.itemMeta = meta
				}
				
				equipment.armorContents = armors
			}
			
			stands += stand
			
			mapped[shown] = stand
		}
		
		game.users().forEach()
		{ user ->
			val shown: Shown? = when (mode)
			{
				SelectorMode.TEAM ->
				{
					game.teamPick(user)
				}
				SelectorMode.GEAR ->
				{
					game.gearPick(user)
				}
			}
			
			if (shown != null)
			{
				inform(user, shown)
			}
		}
		
		update = timerAsync(4)
		{
			game.users().forEach()
			{ user ->
				
				val stand = choice[user] ?: return@forEach
				
				if (!stand.isValid)
				{
					choice.remove(user)
					return@forEach
				}
				
				user.spawnParticle(Particle.FIREWORKS_SPARK, stand.eyeLocation.subtract(0.0, 0.4, 0.0), 5, 0.18, 0.3, 0.18, 0.002)
			}
		}
	}
	
	override fun kill()
	{
		HandlerList.unregisterAll(this)
		
		stands.forEach()
		{ stand ->
			stand.remove()
		}
		
		chests.clear()
		stands.clear()
		
		mapped.clear()
		choice.clear()
		
		update?.cancel()
		update = null
	}
	
	
	fun inform(user: Player, shown: Shown)
	{
		choice[user] = mapped[shown] ?: return
	}
	
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun PlayerInteractAtEntityEvent.onSelect()
	{
		if (rightClicked !in stands)
		{
			return
		}
		
		isCancelled = true
		
		if (!rightClicked.hasMetadata(META_GAME_TEAM) && !rightClicked.hasMetadata(META_GAME_GEAR))
		{
			return
		}
		
		select(rightClicked as ArmorStand, player)
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	fun HurtEvent.onSelect()
	{
		if (damaged !in stands)
		{
			return
		}
		
		isCancelled = true
		
		if (!damaged.hasMetadata(META_GAME_TEAM) && !damaged.hasMetadata(META_GAME_GEAR))
		{
			return
		}
		
		select(damaged as ArmorStand, damager as? Player ?: return)
	}
	
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun PlayerInteractEvent.onOpenChest()
	{
		val block = clickedBlock ?: return
		
		if (block !in chests)
		{
			return
		}
		
		isCancelled = true
		
		val (data, mode) = when
		{
			block.hasMetadata(META_GAME_GEAR) ->
			{
				game.gears() as List<Shown> to SelectorMode.GEAR
			}
			block.hasMetadata(META_GAME_TEAM) ->
			{
				game.teams() as List<Shown> to SelectorMode.TEAM
			}
			else                              ->
			{
				return
			}
		}
		
		player.playSound(player.location, Sound.BLOCK_CHEST_OPEN, 1.0F, 0.8F)
		
		SelectorMenu(plugin, game, this@Selector, data, mode)[player]
	}
	
	
	private fun select(stand: ArmorStand, player: Player)
	{
		selectTeam(stand, player)
		selectGear(stand, player)
	}
	
	
	private fun selectTeam(stand: ArmorStand, player: Player)
	{
		val team = stand.getMeta(META_GAME_TEAM, plugin)?.value() as? GameTeam ?: return
		
		if (game.pick(player, team))
		{
			inform(player, team)
			player.sendTitle(team.external, "${Colour.Gray}Team Chosen", 10, 40, 15)
		}
	}
	
	private fun selectGear(stand: ArmorStand, player: Player)
	{
		val gear = stand.getMeta(META_GAME_GEAR, plugin)?.value() as? GameGear ?: return
		
		if (game.pick(player, gear))
		{
			inform(player, gear)
			player.sendTitle(gear.external, "${Colour.Gray}Gear Chosen", 10, 40, 15)
		}
	}
	
	
	private companion object
	{
		
		const val META_GAME_TEAM = "game_team_select"
		const val META_GAME_GEAR = "game_gear_select"
		
		private val ARMOR_HEAD = Armor(Armor.ArmorPart.HEAD, Armor.ArmorType.LEATHER)
		private val ARMOR_BODY = Armor(Armor.ArmorPart.BODY, Armor.ArmorType.LEATHER)
		private val ARMOR_LEGS = Armor(Armor.ArmorPart.LEGS, Armor.ArmorType.LEATHER)
		private val ARMOR_FEET = Armor(Armor.ArmorPart.FEET, Armor.ArmorType.LEATHER)
		
	}
	
}