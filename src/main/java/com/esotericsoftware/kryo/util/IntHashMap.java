
package com.esotericsoftware.kryo.util;

public class IntHashMap<T> {
	private Entry[] table;
	private float loadFactor;
	private int size, mask, capacity, threshold;

	public IntHashMap () {
		this(16, 0.75f);
	}

	public IntHashMap (int initialCapacity) {
		this(initialCapacity, 0.75f);
	}

	public IntHashMap (int initialCapacity, float loadFactor) {
		if (initialCapacity > 1 << 30) throw new IllegalArgumentException("initialCapacity is too large.");
		if (initialCapacity < 0) throw new IllegalArgumentException("initialCapacity must be greater than zero.");
		if (loadFactor <= 0) throw new IllegalArgumentException("initialCapacity must be greater than zero.");
		capacity = 1;
		while (capacity < initialCapacity) {
			capacity <<= 1;
		}
		this.loadFactor = loadFactor;
		this.threshold = (int)(capacity * loadFactor);
		this.table = new Entry[capacity];
		this.mask = capacity - 1;
	}

	public boolean containsValue (Object value) {
		Entry[] table = this.table;
		for (int i = table.length; i-- > 0;)
			for (Entry e = table[i]; e != null; e = e.next)
				if (e.value.equals(value)) return true;
		return false;
	}

	public boolean containsKey (int key) {
		int index = ((int)key) & mask;
		for (Entry e = table[index]; e != null; e = e.next)
			if (e.key == key) return true;
		return false;
	}

	public T get (int key) {
		int index = key & mask;
		for (Entry e = table[index]; e != null; e = e.next)
			if (e.key == key) return (T)e.value;
		return null;
	}

	public T put (int key, T value) {
		int index = key & mask;
		// Check if key already exists.
		for (Entry e = table[index]; e != null; e = e.next) {
			if (e.key != key) continue;
			Object oldValue = e.value;
			e.value = value;
			return (T)oldValue;
		}
		table[index] = new Entry(key, value, table[index]);
		if (size++ >= threshold) {
			// Rehash.
			int newCapacity = 2 * capacity;
			Entry[] newTable = new Entry[newCapacity];
			Entry[] src = table;
			int bucketmask = newCapacity - 1;
			for (int j = 0; j < src.length; j++) {
				Entry e = src[j];
				if (e != null) {
					src[j] = null;
					do {
						Entry next = e.next;
						index = e.key & bucketmask;
						e.next = newTable[index];
						newTable[index] = e;
						e = next;
					} while (e != null);
				}
			}
			table = newTable;
			capacity = newCapacity;
			threshold = (int)(newCapacity * loadFactor);
			mask = capacity - 1;
		}
		return null;
	}

	public T remove (int key) {
		int index = key & mask;
		Entry prev = table[index];
		Entry e = prev;
		while (e != null) {
			Entry next = e.next;
			if (e.key == key) {
				size--;
				if (prev == e) {
					table[index] = next;
				} else {
					prev.next = next;
				}
				return (T)e.value;
			}
			prev = e;
			e = next;
		}
		return null;
	}

	public int size () {
		return size;
	}

	public void clear () {
		Entry[] table = this.table;
		for (int index = table.length; --index >= 0;)
			table[index] = null;
		size = 0;
	}

	static class Entry {
		final int key;
		Object value;
		Entry next;

		Entry (int k, Object v, Entry n) {
			key = k;
			value = v;
			next = n;
		}
	}
}
