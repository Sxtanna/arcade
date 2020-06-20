package com.sxtanna.mc.arcade

import com.sxtanna.mc.arcade.func.archive
import com.sxtanna.mc.arcade.func.extract
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.TestMethodOrder
import java.io.File

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
object ZipperTests
{
	
	private val path = File("test")
	private val file = File("test.zip")
	
	
	@Test
	@Order(0)
	internal fun testArchive()
	{
		archive(path, file)
		
		assertTrue(file.exists())
		assertTrue(path.deleteRecursively())
	}
	
	@Test
	@Order(1)
	internal fun testExtract()
	{
		extract(file, path)
		
		assertTrue(path.exists())
		assertTrue(file.deleteRecursively())
	}
	
}