package com.sxtanna.mc.arcade.hook.mods.worlds.cmds

import com.sxtanna.mc.arcade.cmds.Command
import com.sxtanna.mc.arcade.cmds.Context
import com.sxtanna.mc.arcade.hook.ArcadeBasePlugin
import com.sxtanna.mc.arcade.hook.mods.worlds.menu.MenuWorlds
import com.sxtanna.mc.arcade.util.Colour
import org.bukkit.entity.Player

internal class CommandWorld(override val plugin: ArcadeBasePlugin) : Command("Worlds")
{
	
	override fun Context.evaluate()
	{
		if (input.isEmpty())
		{
			return MenuWorlds.MenuList(plugin)[sender as? Player ?: return reply("You must be a player to do this")]
		}
		
		when (input[0].toLowerCase())
		{
			"list" -> // /world list
			{
				MenuWorlds.MenuList(plugin)[sender as? Player ?: return reply("You must be a player to do this")]
				
				server.worlds.forEach()
				{
					reply("world: ${it.name}")
				}
			}
			"make" -> // /world make
			{
				MenuWorlds.MenuMake(plugin)[sender as? Player ?: return reply("You must be a player to do this")]
			}
			"move" -> // /world move {world_name} {player_list}
			{
				if (input.size < 2)
				{
					return reply("${Colour.Red}USAGE: ${Colour.Gray}/worlds [list, make, move] {world_name} {player_list}")
				}
				
				val world = server.worlds.find { it.name.equals(input[1], true) }
				if (world == null)
				{
					return reply("world named '${input[1]}' not found!")
				}
				
				val users = if (input.size < 3)
				{
					listOf(sender as? Player ?: return reply("You must be a player to do this"))
				}
				else
				{
					input[2].split(',').mapNotNull { server.getPlayer(it) }
				}
				
				if (users.isEmpty())
				{
					return reply("No players found in provided argument")
				}
				
				plugin.worlds.move(world, users)
				{
					reply("You were moved to ${Colour.Yellow}${world.name}", it)
				}
			}
			else ->
			{
				reply("${Colour.Red}USAGE: ${Colour.Gray}/worlds [list, make, move] {world_name} {player_list}")
			}
		}
	}
	
	override fun Context.complete(out: MutableList<String>)
	{
		when (input.size)
		{
			0, 1 ->
			{
				out += listOf("list", "make", "move").filter(0)
			}
			else ->
			{
				if (input.size > 3 || !input[0].equals("move", true))
				{
					return
				}
				
				if (input.size == 2)
				{
					out += server.worlds.map { it.name.replace(' ', '_') }.filter(1)
					return
				}
				
				splitByComma(out) { server.onlinePlayers.map { it.name } }
			}
		}
	}
	
}