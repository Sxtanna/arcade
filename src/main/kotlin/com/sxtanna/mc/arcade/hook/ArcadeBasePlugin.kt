package com.sxtanna.mc.arcade.hook

import com.sxtanna.mc.arcade.Arcade
import com.sxtanna.mc.arcade.hook.base.ArcadePlugin
import com.sxtanna.mc.arcade.hook.mods.damage.DamageModule
import com.sxtanna.mc.arcade.hook.mods.helper.HelperModule
import com.sxtanna.mc.arcade.hook.mods.worlds.WorldsModule
import org.bukkit.plugin.ServicePriority

class ArcadeBasePlugin : ArcadePlugin()
{
	
	override lateinit var arcade: Arcade
		private set
	
	internal lateinit var damage: DamageModule
		private set
	internal lateinit var worlds: WorldsModule
		private set
	internal lateinit var helper: HelperModule
		private set
	
	
	override fun onLoad()
	{
		this.arcade = Arcade(this)
		
		this.damage = DamageModule(this)
		this.worlds = WorldsModule(this)
		this.helper = HelperModule(this)
		
		server.servicesManager.register(Arcade::class.java, this.arcade, this, ServicePriority.Highest)
	}
	
	override fun onEnable()
	{
		this.damage.load()
		this.worlds.load()
		this.helper.load()
		
		this.arcade.load()
	}
	
	override fun onDisable()
	{
		this.arcade.kill()
		
		this.damage.kill()
		this.worlds.kill()
		this.helper.kill()
		
		server.servicesManager.unregister(Arcade::class.java, this.arcade)
	}
	
}