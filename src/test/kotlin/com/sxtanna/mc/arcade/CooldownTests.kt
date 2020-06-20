package com.sxtanna.mc.arcade

import com.sxtanna.mc.arcade.time.Cooldown
import com.sxtanna.mc.arcade.time.reply.Reply
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

@TestInstance(PER_CLASS)
object CooldownTests
{
	
	private val cool = Cooldown()
	private val uuid = UUID.randomUUID()
	
	
	@BeforeEach
	internal fun setUp()
	{
		cool.load()
	}
	
	@AfterEach
	internal fun tearDown()
	{
		cool.kill()
	}
	
	
	@Test
	internal fun testUseOnce()
	{
		val state = cool.use(uuid, "Test", 1, TimeUnit.SECONDS)
		assertTrue(state)
		{
			"state should be true"
		}
	}
	
	@Test
	internal fun testUseTwice()
	{
		val state0 = cool.use(uuid, "Test", 1, TimeUnit.SECONDS)
		assertTrue(state0)
		{
			"0 state should be true"
		}
		
		val state1 = cool.use(uuid, "Test", 1, TimeUnit.SECONDS)
		assertFalse(state1)
		{
			"1 state should be false"
		}
	}
	
	@Test
	internal fun testNotify()
	{
		val reply = AtomicBoolean()
		val state = cool.use(uuid, "Reply", 500, TimeUnit.MILLISECONDS, Reply.Execute {
			reply.set(true)
			println("$it is done")
		})
		
		assertTrue(state)
		{
			"state should be true"
		}
		
		assertFalse(reply.get())
		{
			"reply should be false"
		}
		
		Thread.sleep(501)
		
		assertTrue(reply.get())
		{
			"reply should be true"
		}
	}
	
}