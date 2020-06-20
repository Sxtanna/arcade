package com.sxtanna.mc.arcade.hook.mods.worlds.menu

import com.sxtanna.mc.arcade.data.Stack
import com.sxtanna.mc.arcade.data.base.Meta
import com.sxtanna.mc.arcade.hook.ArcadeBasePlugin
import com.sxtanna.mc.arcade.hook.base.ArcadePlugin
import com.sxtanna.mc.arcade.menu.Menu
import com.sxtanna.mc.arcade.menu.Size
import com.sxtanna.mc.arcade.menu.Text
import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.Chunk
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldType
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

sealed class MenuWorlds(size: Size, name: String) : Menu(size, name)
{
	
	class MenuList(override val plugin: ArcadeBasePlugin)
		: MenuWorlds(Size.ROWS_5, "Worlds")
	{
		
		override fun make()
		{
			var slot = 0
			
			val cached = plugin.worlds.cached()
			val loaded = plugin.worlds.loaded()
			
			cached.sorted().forEach()
			{ name ->
				
				val lore = if (name in loaded)
					listOf("", "&7Click to move to this world!")
				else
					listOf("", "&7Click to load and move to this world!")
				
				val item = Stack(if (name in loaded) Material.JACK_O_LANTERN else Material.CARVED_PUMPKIN)[Meta.NAME, "&e$name"][Meta.LORE, lore][1]
				
				set(slot++, item)
				{ who, _ ->
					plugin.worlds.load(name)
					{ world ->
						if (world == null)
							who.sendMessage("${Colour.Red}failed to load world")
						else
							who.performCommand("worlds move ${world.name.replace(' ', '_')}")
					}
				}
			}
		}
		
	}
	
	class MenuMake(override val plugin: ArcadeBasePlugin, private val done: (Player, World) -> Unit = { _, _ -> })
		: MenuWorlds(Size.ROWS_4, "Make World")
	{
		
		private var worldName = null as? String?
		private var worldType = null as? WorldType?
		private var worldEnvr = null as? World.Environment?
		
		
		override fun make()
		{
			// name
			val name = Stack(Material.PAPER)[Meta.NAME, "&6World Name"]
			
			if (worldName != null)
			{
				name[Meta.LORE, listOf("  &f$worldName")]
			}
			
			set(11, name[1])
			{ who, _ ->
				who.closeInventory()
				NameText(who.eyeLocation.block).open(who)
			}
			
			
			// type
			val typeType = when (worldType)
			{
				WorldType.NORMAL     ->
				{
					Material.GRASS_BLOCK
				}
				WorldType.FLAT       ->
				{
					Material.GREEN_CARPET
				}
				WorldType.AMPLIFIED  ->
				{
					Material.SHULKER_SHELL
				}
				WorldType.CUSTOMIZED ->
				{
					Material.COMMAND_BLOCK
				}
				else                 ->
				{
					Material.GLASS
				}
			}
			
			val type = Stack(typeType)[Meta.NAME, "&6World Type"]
			var typeName = ""
			
			if (worldType != null)
			{
				typeName = checkNotNull(worldType?.name?.toLowerCase()?.capitalize())
				{
					"Not possible"
				}
				
				if (typeName == "Customized")
				{
					typeName = "Void"
				}
				
				
				type[Meta.LORE, listOf("  &f$typeName")]
			}
			
			set(13, type[1])
			{ who, _ ->
				MenuType()[who]
			}
			
			
			// environment
			val envrType = when (worldEnvr)
			{
				World.Environment.NORMAL  ->
				{
					Material.GRASS_BLOCK
				}
				World.Environment.NETHER  ->
				{
					Material.NETHERRACK
				}
				World.Environment.THE_END ->
				{
					Material.END_STONE
				}
				null                      ->
				{
					Material.GLASS
				}
			}
			
			val envr = Stack(envrType)[Meta.NAME, "&6World Environment"]
			var envrName = ""
			
			if (worldEnvr != null)
			{
				envrName = when (worldEnvr)
				{
					World.Environment.NORMAL  ->
					{
						"Overworld"
					}
					World.Environment.NETHER  ->
					{
						"The Nether"
					}
					World.Environment.THE_END ->
					{
						"The End"
					}
					null                      ->
					{
						throw IllegalStateException("Not possible")
					}
				}
				
				envr[Meta.LORE, listOf("  &f$envrName")]
			}
			
			set(15, envr[1])
			{ who, _ ->
				MenuEnvr()[who]
			}
			
			
			val make = Stack(Material.EMERALD_BLOCK)[Meta.NAME, "&2&lCreate World"]
			
			val lore = when
			{
				worldName == null -> listOf("", "&cRequires World Name")
				worldType == null -> listOf("", "&cRequires World Type")
				worldEnvr == null -> listOf("", "&cRequires World Environment")
				else              ->
				{
					listOf("", "&b${typeName} &9${envrName.split(' ').last().toLowerCase()} &7named &e$worldName")
				}
			}
			
			make[Meta.LORE, lore]
			
			set(31, make[1])
			{ who, _ ->
				
				val finalName = worldName ?: return@set
				val finalType = worldType ?: return@set
				val finalEnvr = worldEnvr ?: return@set
				
				who.closeInventory()
				who.sendTitle("", "${Colour.Gold}Creating world...", 20, 80, 30)
				
				plugin.worlds.make(finalName, finalType, finalEnvr)
				{ world ->
					
					if (world == null)
					{
						who.sendTitle("", "${Colour.Red}Failed to create world!", 10, 40, 5)
					}
					else
					{
						who.sendTitle("${Colour.Green}Successfully created world ${Colour.Yellow}${world.name}", "${Colour.Gray}you are being moved", 10, 60, 5)
						
						later(10)
						{
							who.teleport(world.spawnLocation)
							who.sendTitle("", "")
							
							done.invoke(who, world)
						}
					}
				}
			}
		}
		
		
		private inner class NameText(override val block: Block) : Text()
		{
			
			override val plugin = this@MenuMake.plugin
			
			
			override fun make()
			{
				Lines.LINE_1["^".repeat(15)]
				Lines.LINE_2["Enter World Name"]
				Lines.LINE_3["=".repeat(15)]
			}
			
			override fun done(data: List<String>, player: Player)
			{
				worldName = data[0].takeIf(String::isNotBlank)?.takeIf { ' ' !in it }?.let(Colour::strip)
				
				if (worldName == null)
				{
					player.sendMessage("${Colour.Red}Invalid world name!")
				}
				
				this@MenuMake[player]
			}
			
		}
		
		private inner class MenuType : Menu(Size.ROWS_3, "World Type")
		{
			
			override val plugin = this@MenuMake.plugin
			
			override fun make()
			{
				set(10, type0[1])
				{ who, _ ->
					worldType = WorldType.NORMAL
					this@MenuMake[who]
				}
				set(12, type1[1])
				{ who, _ ->
					worldType = WorldType.FLAT
					this@MenuMake[who]
				}
				set(14, type2[1])
				{ who, _ ->
					worldType = WorldType.AMPLIFIED
					this@MenuMake[who]
				}
				set(16, type3[1])
				{ who, _ ->
					worldType = WorldType.CUSTOMIZED
					this@MenuMake[who]
				}
			}
			
		}
		
		private inner class MenuEnvr : Menu(Size.ROWS_3, "World Environment")
		{
			
			override val plugin = this@MenuMake.plugin
			
			override fun make()
			{
				set(11, envr0[1])
				{ who, _ ->
					worldEnvr = World.Environment.NORMAL
					this@MenuMake[who]
				}
				set(13, envr1[1])
				{ who, _ ->
					worldEnvr = World.Environment.NETHER
					this@MenuMake[who]
				}
				set(15, envr2[1])
				{ who, _ ->
					worldEnvr = World.Environment.THE_END
					this@MenuMake[who]
				}
			}
			
		}
		
		
		private companion object
		{
			
			val type0 = Stack(Material.GRASS_BLOCK)[Meta.NAME, "&fNormal"]
			val type1 = Stack(Material.GREEN_CARPET)[Meta.NAME, "&fFlat"]
			val type2 = Stack(Material.SHULKER_SHELL)[Meta.NAME, "&fAmplified"]
			val type3 = Stack(Material.COMMAND_BLOCK)[Meta.NAME, "&fVoid World"]
			
			val envr0 = Stack(Material.GRASS_BLOCK)[Meta.NAME, "&fOverworld"]
			val envr1 = Stack(Material.NETHERRACK)[Meta.NAME, "&fThe Nether"]
			val envr2 = Stack(Material.END_STONE)[Meta.NAME, "&fThe End"]
			
		}
		
	}
	
	class MenuInfo(override val plugin: ArcadePlugin, val worldName: String, val worldData: World?)
		: MenuWorlds(Size.ROWS_5, "World Info: ${Colour.Yellow}$worldName")
	{
		/*
		 * 6 data points
		 *  - generic
		 *    - world name
		 *    - world uuid
		 *    - world type
		 *    - world environment
		 *  - players
		 *    - player count
		 *    - player names {clamped}
		 *  - entities
		 *    - entity count
		 *    - entity graph {clamped distribution of entity types}
		 *    - entity cramp {where most entities are}
		 *  - chunks
		 *    - chunk count
		 *    - chunk tickets
		 *  - weather
		 *    - world time
		 *    - world weather {rain,snow,sunny}
		 *  - misc
		 *    - view distance
		 *    - gamerules
		 *
		 *
		 *  - generic  | 10 | cobblestone
		 *  - players  | 13 | player_head
		 *  - entities | 16 | turtle_egg
		 *  - chunks   | 28 | grass_block
		 *  - weather  | 31 | clock
		 *  - misc     | 34 | end_crystal
		 */
		
		
		override fun make()
		{
			val chunk: Chunk
			
		}
		
	}
	
}