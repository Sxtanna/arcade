package com.sxtanna.mc.arcade.cmds

import com.sxtanna.mc.arcade.base.Addon
import com.sxtanna.mc.arcade.base.Named
import com.sxtanna.mc.arcade.base.State
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import java.util.logging.Level

abstract class Command(final override val name: String) : Named, State, Addon, CommandExecutor, TabCompleter
{
	
	override fun load()
	{
		val command = server.getPluginCommand(name) ?: return
		
		command.setExecutor(this)
		command.tabCompleter = this
	}
	
	override fun kill()
	{
		val command = server.getPluginCommand(name) ?: return
		command.setExecutor(null)
		command.tabCompleter = null
	}
	
	
	final override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean
	{
		try
		{
			Context(sender, label, args.toList()).evaluate()
		}
		catch (ex: Exception)
		{
			plugin.logger.log(Level.WARNING, "Failed to evaluate Command:$name", ex)
		}
		
		return true
	}
	
	final override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String>
	{
		val out = mutableListOf<String>()
		
		try
		{
			Context(sender, alias, args.toList()).complete(out)
		}
		catch (ex: Exception)
		{
			plugin.logger.log(Level.WARNING, "Failed to complete Command:$name", ex)
		}
		
		return out
	}
	
	
	protected abstract fun Context.evaluate()
	
	protected abstract fun Context.complete(out: MutableList<String>)
	
}