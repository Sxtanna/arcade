package com.sxtanna.mc.arcade

import com.sxtanna.mc.arcade.data.Point
import com.sxtanna.mc.arcade.data.base.Vec3
import com.sxtanna.mc.arcade.func.KORM
import com.sxtanna.mc.arcade.game.area.GameArea
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
object GameAreaTests
{
	
	@Test
	internal fun testPush()
	{
		val area = GameArea()
		
		area.named = "test"
		area.built = "Sxtanna"
		
		/*area.point[0] = listOf(
			Point(Vec3.ZERO, Vec3.ZERO),
			Point(Vec3.ZERO, Vec3.ZERO)
		                      )*/
		
		println("Hello!")
		println(KORM.push(area))
	}
	
}