package com.sxtanna.mc.arcade.pb.cmds

import com.sxtanna.mc.arcade.cmds.Command
import com.sxtanna.mc.arcade.cmds.Context
import com.sxtanna.mc.arcade.pb.ArcadeGamePluginPB
import com.sxtanna.mc.arcade.pb.game.GamePB
import com.sxtanna.mc.arcade.pb.gear.part.GameGearPartBazooka
import com.sxtanna.mc.arcade.pb.gear.part.GameGearPartHandgun
import com.sxtanna.mc.arcade.pb.gear.part.GameGearPartMiniGun
import com.sxtanna.mc.arcade.util.Colour

class CommandPB(override val plugin: ArcadeGamePluginPB) : Command("pb")
{
	
	override fun Context.evaluate()
	{
		if (input.isEmpty())
		{
			return
		}
		
		
		val gamePB = plugin.arcade.liveGame() as? GamePB ?: return error("current game isn't Paintball")
		
		when (input[0].toLowerCase())
		{
			"damage" ->
			{
				if (input.size < 2)
				{
					return
				}
				
				val option = when (input[1].toLowerCase())
				{
					"bazooka" -> GameGearPartBazooka.Datas.GUN_BAZOOKA_DAMAGE
					"handgun" -> GameGearPartHandgun.Datas.GUN_HANDGUN_DAMAGE
					"minigun" -> GameGearPartMiniGun.Datas.GUN_MINIGUN_DAMAGE
					else      ->
					{
						return error("invalid gun name")
					}
				}
				
				val old = gamePB[option]
				val new = input.getOrNull(2)?.toDoubleOrNull() ?: return error("you must supply a valid number")
				
				gamePB[option] = new
				
				reply("${Colour.Yellow}${option.name}${Colour.Gray} was updated ${Colour.Green}$old${Colour.DGray}->${Colour.Green}$new")
			}
		}
	}
	
	override fun Context.complete(out: MutableList<String>)
	{
		
	}
	
}