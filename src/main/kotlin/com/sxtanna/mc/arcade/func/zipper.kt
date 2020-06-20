package com.sxtanna.mc.arcade.func

import org.zeroturnaround.zip.ZipUtil
import java.io.File

/**
 * takes a path and makes it a zip
 */
fun archive(path: File, file: File): ZipState
{
	if (!path.exists())
	{
		return ZipState.FAIL(reason = "path doesn't exist")
	}
	if (!file.exists())
	{
		file.createNewFile()
	}
	
	try
	{
		ZipUtil.pack(path, file)
	}
	catch (ex: Throwable)
	{
		return ZipState.FAIL(ex)
	}
	
	return ZipState.PASS
}

/**
 * takes a zip and extracts it to a path
 */
fun extract(file: File, path: File): ZipState
{
	if (!file.exists())
	{
		return ZipState.FAIL(reason = "file doesn't exist")
	}
	if (!path.exists())
	{
		path.mkdirs()
	}
	
	try
	{
		ZipUtil.unpack(file, path)
	}
	catch (ex: Throwable)
	{
		return ZipState.FAIL(ex)
	}
	
	return ZipState.PASS
}


sealed class ZipState
{
	
	object PASS
		: ZipState()
	
	data class FAIL(val throwable: Throwable? = null, val reason: String? = throwable?.message)
		: ZipState()
	
}