package com.sxtanna.mc.arcade.func

import java.lang.ref.WeakReference

data class WeakHashSet<T : Any> private constructor(private val backend: MutableSet<WeakReference<T>>) : MutableSet<T>
{
	constructor() : this(mutableSetOf())
	
	
	override val size: Int
		get() = backend.size
	
	override fun add(element: T): Boolean
	{
		return backend.add(WeakReference(element))
	}
	
	override fun addAll(elements: Collection<T>): Boolean
	{
		return backend.addAll(elements.map(::WeakReference))
	}
	
	override fun remove(element: T): Boolean
	{
		return backend.removeAll { it.get() == element }
	}
	
	override fun removeAll(elements: Collection<T>): Boolean
	{
		return backend.removeAll { it.get() in elements }
	}
	
	override fun clear()
	{
		backend.clear()
	}
	
	override fun iterator(): MutableIterator<T>
	{
		return WeakIterator(backend.iterator())
	}
	
	override fun retainAll(elements: Collection<T>): Boolean
	{
		return backend.retainAll { it.get() in elements }
	}
	
	override fun contains(element: T): Boolean
	{
		return backend.any { it.get() == element }
	}
	
	override fun containsAll(elements: Collection<T>): Boolean
	{
		return backend.none { it.get() !in elements }
	}
	
	override fun isEmpty(): Boolean
	{
		return backend.isEmpty()
	}
	
	private data class WeakIterator<T : Any>(private val iterator: MutableIterator<WeakReference<T>>) : MutableIterator<T>
	{
		override fun hasNext(): Boolean
		{
			return iterator.hasNext()
		}
		
		override fun next(): T
		{
			var next = iterator.next().get()
			
			if (next != null)
			{
				return next
			}
			
			while (next == null && hasNext())
			{
				next = iterator.next().get()
			}
			
			return requireNotNull(next)
			{
				"no more values that aren't lost references"
			}
		}
		
		override fun remove()
		{
			iterator.remove()
		}
	}
	
}