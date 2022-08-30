package model;

import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.Iterator;

/** 
 * Defines the protocols for a hash map.
 * 
 * Do not modify at all.
 */
public interface HashMap<K, V>
	extends Map<K, V>,
			Iterable<Map.Entry<K, V>>
{
	@Override public abstract void clear();

	@Override public abstract int size();
	@Override public abstract boolean isEmpty();
	
	/**
	 * Returns the load factor (alpha), defined
	 * to be the number of entries in the map
	 * divided by either the length of the array
	 * or page (when open addressing) or the
	 * number of chains (when separate chaining).
	 * 
	 * @return the load factor
	 */
	public abstract double loadFactor();

	@Override public abstract V put(K key, V value);
	@Override public abstract V get(Object key);
	@Override public abstract V remove(Object key);

	@Override public abstract boolean containsKey(Object key);
	@Override public abstract boolean containsValue(Object value);
	
	@Override public abstract void putAll(Map<? extends K, ? extends V> m);
	
	@Override public abstract String toString();
	@Override public abstract boolean equals(Object o);
	@Override public abstract int hashCode();

	@Override public abstract Iterator<Map.Entry<K, V>> iterator();
	
	@Override
	public default Set<Map.Entry<K, V>> entrySet() {
		return new java.util.AbstractSet<Map.Entry<K, V>>() {
			@Override
			public Iterator<Map.Entry<K, V>> iterator() {
				return HashMap.this.iterator();
			}

			@Override
			public int size() {
				return HashMap.this.size();
			}
		};
	}
	
	@Override
	public default Set<K> keySet() {
		return new java.util.AbstractSet<K>() {
			@Override
			public Iterator<K> iterator() {
				return new Iterator<K>() {
					Iterator<Map.Entry<K, V>> entryIterator = HashMap.this.entrySet().iterator();
					
					@Override
					public boolean hasNext() {
						return entryIterator.hasNext();
					}

					@Override
					public K next() {
						return entryIterator.next().getKey();
					}
				};
			}

			@Override
			public int size() {
				return HashMap.this.size();
			}
		};
	}
	
	@Override
	public default Collection<V> values() {
		return new java.util.AbstractCollection<V>() {
			@Override
			public Iterator<V> iterator() {
				return new Iterator<V>() {
					Iterator<Map.Entry<K, V>> entryIterator = HashMap.this.entrySet().iterator();
					
					@Override
					public boolean hasNext() {
						return entryIterator.hasNext();
					}

					@Override
					public V next() {
						return entryIterator.next().getValue();
					}
				};
			}

			@Override
			public int size() {
				return HashMap.this.size();
			}
		};
	}
}