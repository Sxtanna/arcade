package com.sxtanna.mc.arcade.disp.effect.type

import com.sxtanna.mc.arcade.disp.effect.Effect
import com.sxtanna.mc.arcade.hook.base.ArcadePlugin
import org.bukkit.Particle
import org.bukkit.util.Vector
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class EffectCircle(override val plugin: ArcadePlugin) : Effect(Particle.FLAME)
{
	
	private val amount = 36
	private val radius = .4
	private val offset = (2 * PI) / amount
	
	private val deltas = generate(amount)
	{
		Vector(cos(it * offset) * radius, 0.0, sin(it * offset) * radius)
	}
	
	override fun step(instance: Instance)
	{
		val loc = instance.pos().add(0.0, 2.1, 0.0)
		
		if (!instance[WHOLE_CIRCLE])
		{
			show(loc, deltas[instance.step() % amount])
			{
				it.world.spawnParticle(type, it, 1, 0.0, 0.0, 0.0, 0.0)
			}
			
			return
		}
		
		deltas.forEach()
		{ delta ->
			show(loc, delta)
			{
				it.world.spawnParticle(type, it, 1, 0.0, 0.0, 0.0, 0.0)
			}
		}
		
	}
	
	private object WHOLE_CIRCLE
		: EffectData.BoolData(false)
	
}