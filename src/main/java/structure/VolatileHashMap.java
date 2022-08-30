package structure;

import model.*;

import java.util.Map;
import java.util.Iterator;

public class VolatileHashMap<K, V>
	implements HashMap<K, V>
{
	//private Map<K, V> capsule;
	private int size;
	private int capacity;
	//private int loadFactor;
	private Object[][] map;
	private final int initialDepth = 31;
	private int chainLength = 3;
	private final int primes[] = {67, 137, 269, 521, 1117, 2273, 5261, 10729, 21187, 47111, 94933, 199889, 398569, 796933};
	private int primeIndex = 0;
	//private int maxCol = 0;
	//private int counter = 0;
	
	public VolatileHashMap() 
	{
		//capsule = new java.util.HashMap<>();
		clear();
	}
	
	public VolatileHashMap(Map<? extends K, ? extends V> copy) 
	{
		//capsule = new java.util.HashMap<>(copy);
		size = 0;
		capacity = initialDepth;
		map = new Object[initialDepth][chainLength];
		putAll(copy);
		//TODO: Add actual support for moving the load factor over from other maps
	}

	@Override
	public void clear() 
	{
		//capsule.clear();
		capacity = initialDepth;
		size = 0;
		map = new Object[initialDepth][chainLength];
	}
	
	/*public Object[][] asArray()
	{
		return map;
	}*/

	@Override
	public int size() 
	{
		//return capsule.size();
		return size;
	}
	
	/*public int capacity()
	{
		return capacity;
	}*/

	@Override
	public boolean isEmpty() 
	{
		//return capsule.isEmpty();
		return size == 0;
	}

	@Override
	public double loadFactor() 
	{
		//throw new UnsupportedOperationException();
		return (double)size / ((double)chainLength);
	}
	
	private void newChains()
	{
		Object[][] newMap = new Object[capacity][chainLength];
		for(int i = 0; i < newMap.length; i++)
		{
			for(int j = 0; j < newMap[i].length; j++)
			{
				if(map[i][j] == null)
					break;
				if(map[i][j] instanceof SimpleEntry || map[i][j] instanceof RemovedEntry)
				{
					newMap[i][j] = map[i][j];
				}
			}
		}
		map = newMap;
		//put(entry.getKey(), entry.getValue());
	}
	
	private void resizeChains(int index, SimpleEntry<K, V> entry)
	{
		int maxLength = 0;
		if(chainLength == 5 && entry != null)
			resize(index, entry, true);
		else
		{
			for(int i = 0; i < map.length; i++)
			{
				for(int j = 0; j < map[i].length; j++)
				{
					if(map[i][j] == null)
						break;
					if(j > maxLength)
						maxLength = j;
				}
				if(maxLength == 5)
					break;
			}
			if(maxLength <= 3)
				chainLength = 3;
			else if(maxLength <= 4)
				chainLength = 4;
			else
				chainLength = 5;
		
		
			if(entry != null && chainLength == 5)
				resize(index, entry, true);
			else
			{
				resize(capacity, entry, false);
			}
		}
	}

	@Override
	public V put(K key, V value) 
	{
		//System.out.println(chainLength);
		//TODO: Revise how the remove references are handled
		V previous = null;
		int hash = hash(key) % capacity;
		//System.out.println(hash(key) + " " +  hash);
		int i = 0;
		int pos = -1;
		boolean complete = false;
		while(i < chainLength)
		{
			if(map[hash][i] == null)
			{
				map[hash][i] = new SimpleEntry<>(key, value);
				complete = true;
				size++;
				//System.out.println(counter++);
				break;
			}
			else if(map[hash][i] instanceof RemovedEntry && pos == -1)
			{
				pos = i;
			}
			else if(map[hash][i] instanceof SimpleEntry)
			{
				if(((SimpleEntry<K, V>) map[hash][i]).getKey().equals(key))
				{
					previous = ((SimpleEntry<K, V>) map[hash][i]).getValue();
					((SimpleEntry<K, V>)map[hash][i]).setValue(value); //= new SimpleEntry<>(key, value);
					complete = true;
					//System.out.println("Previous: " + value);
					//System.out.println("New: " + ((SimpleEntry<K, V>) map[hash][i]).getValue());
					break;
				}
			}
			i++;
		}
		//if(i == chainLength)
			//System.out.println("YOOO");
		if(!complete && pos != -1)
		{
			//System.out.println("SPECIAL");
			map[hash][pos] = new SimpleEntry<>(key, value);
			complete = true;
			size++;
			//if(pos > maxCol)
				//maxCol = pos;
		}
		//else if(i > maxCol)
			//maxCol = i;
		
		//System.out.println(complete);
		
		if(!complete)
		{
			resizeChains(primes[primeIndex], new SimpleEntry<K, V>(key, value));
		}
		return previous;
	}

	@Override
	public V get(Object key) 
	{
		//return capsule.get(key);
		int hash = hash(key) % capacity;
		int i = 0;
		while(map[hash][i] != null && i < chainLength)
		{
			if(map[hash][i] instanceof SimpleEntry)
			{
				if(((SimpleEntry<K, V>) map[hash][i]).getKey().equals(key))
				{
					return ((SimpleEntry<K, V>) map[hash][i]).getValue();
				}
			}
			i++;
		}
		return null;
	}

	@Override
	public V remove(Object key) 
	{
		//return capsule.remove(key);
		int hash = hash(key) % capacity;
		int i = 0;
		V value = null;
		while(map[hash][i] != null && i < chainLength)
		{
			if(map[hash][i] instanceof SimpleEntry)
			{
				if(((SimpleEntry<K, V>) map[hash][i]).getKey().equals(key))
				{
					value = ((SimpleEntry<K, V>) map[hash][i]).getValue();
					map[hash][i] = new RemovedEntry();
					size--;
					break;
				}
			}
			i++;
		}
		resizeChains(-1, null);
		return value;
	}

	@Override
	public boolean containsKey(Object key) 
	{
		//TODO: Revise the remove tags
		//return capsule.containsKey(key);
		int hash = hash(key) % capacity;
		//System.out.println(hash);
		int i = 0;
		while(map[hash][i] != null && i < chainLength)
		{
			if(map[hash][i] instanceof SimpleEntry)
			{
				if(((SimpleEntry<K, V>) map[hash][i]).getKey().equals(key))
					return true;
			}
			i++;
		}
		return false;
	}

	@Override
	public boolean containsValue(Object value) 
	{
		for(int i = 0; i < map.length; i++)
		{
			for(int j = 0; j < map[i].length; j++)
			{
				if(map[i][j] == null)
					break;
				else if(map[i][j] instanceof SimpleEntry)
				{
					if(((SimpleEntry<K, V>) map[i][j]).getValue().equals(value))
					{
						return true;
					}
				}
			}
		}
		return false;
		//return capsule.containsValue(value);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) 
	{
		for(K key : m.keySet())
		{
			put(key, m.get(key));
		}
	}

	@Override
	public String toString() 
	{
		String response = "{";
		for(int i = 0; i < map.length; i++)
		{
			for(int j = 0; j < map[i].length; j++)
			{
				if(map[i][j] == null)
					break;
				if(map[i][j] instanceof SimpleEntry)
				{
					SimpleEntry<K, V> entry = (SimpleEntry<K, V>)(map[i][j]);
					response += (" [ Key = " + entry.getKey() + " | Value = " + entry.getValue() + " ] ");				
				}
			}
		}
		response += "}";
		return response;
	}

	@Override
	public boolean equals(Object o) 
	{
		//return capsule.equals(o);
		if(!(o instanceof Map))
			return false;
		
		Map<K, V> otherMap = (Map<K, V>) o;
		if(size() != otherMap.size())
			return false;
		
		for(K key : otherMap.keySet())
		{
			if(!get(key).equals(otherMap.get(key)))
				return false;
		}
		return true;
	}

	@Override
	public int hashCode() 
	{
		//return capsule.hashCode();
		return map.hashCode();
	}

	@Override
	public Iterator<Map.Entry<K, V>> iterator() 
	{
		//TODO: Fix the Iterator Bugs
		//return capsule.entrySet().iterator();
		//return new Iterator<Map.Entry<K, V>>();
		//return new entrySet();
		//System.out.println("DEBUG");
		return new MyIterator();
		//return new Iterator<SimpleEntry<K, V>>();
	}
	
	private class MyIterator implements Iterator<Map.Entry<K,V>>
	{
		int row = 0;
		int col = 0;
		
		public MyIterator()
		{
			boolean found = false;
			for(int i = row + 1; i < map.length; i++)
			{
				for(int j = 0; j < map.length; j++)
				{
					if(map[i][j] == null)
						break;
					if(map[i][j] instanceof SimpleEntry)
					{
						row = i;
						col = j;
						found = true;
						break;
					}
				}
				if(found)
					break;
			}
		}
		
		public boolean hasNext()
		{
			for(int i = col; i < chainLength; i++)
			{
				if(row >= capacity)
					return false;
				if(map[row][i] == null)
					break;
				if(map[row][i] instanceof SimpleEntry)
					return true;
			}
			for(int i = row + 1; i < map.length; i++)
			{
				for(int j = 0; j < map.length; j++)
				{
					if(map[i][j] == null)
						break;
					if(map[i][j] instanceof SimpleEntry)
						return true;
				}
			}
			return false;
		}
		
		public SimpleEntry<K, V> next()
		{
			for(int i = col; i < chainLength; i++)
			{
				if(map[row][i] == null)
					break;
				if(map[row][i] instanceof SimpleEntry)
				{
					boolean rowAdded = false;
					if(i + 1 < chainLength)
						col = i + 1;
					else
					{
						row++;
						col = 0;
						rowAdded = true;
					}
					//System.out.println(row + " " + col + " " + chainLength + " " + capacity + " A");
					if(!rowAdded)
						return (SimpleEntry<K, V>) map[row][i];
					else
						return (SimpleEntry<K, V>) map[row - 1][i];
				}
			}
			for(int i = row + 1; i < map.length; i++)
			{
				for(int j = 0; j < map.length; j++)
				{
					if(map[i][j] == null)
						break;
					if(map[i][j] instanceof SimpleEntry)
					{
						row = i + 1;
						if(j + 1 < chainLength)
							col = j + 1;
						else
						{
							row++;
							col = 0;
						}
						//System.out.println(row + " " + col + " " + chainLength + " " + capacity + " B");
						return (SimpleEntry<K, V>) map[i][j];
					}
				}
			}
			//System.out.println("FAILURE");
			return null;
		}
	}
	
	private int hash(Object key)
	{
		int hash = 0;
		if(key instanceof String)
		{
			String str = (String)key;
			for(int i = 0; i < str.length(); i++)
			{
				hash += (str.charAt(i) * 13 * (i + 1) * str.charAt(str.length() - 1) * str.length()) + str.charAt(str.length()/2) * i * i;
			}
			//System.out.println(hash);
		}
		else
		{
			hash = key.hashCode();
		}
		return hash;
	}
	
	//TODO: KNOWN ISSUE WITH RESIZE must be fixed next, then look at problems with keeping track of size
	private void resize(int newCapacity, Object newEntry, boolean primeUsed)
	{
		//System.out.println("Resize : " + this.capacity + " | " + newCapacity);
		Object[][] newMap = new Object[newCapacity][chainLength];

		for(int i = 0; i < map.length; i++)
		{
			for(int j = 0; j < map[i].length; j++)
			{
				if(map[i][j] == null)
					break;
				else if(map[i][j] instanceof SimpleEntry)
				{
					SimpleEntry<K, V> entry = new SimpleEntry<>((SimpleEntry<K, V>)map[i][j]);
					int hash = hash(entry.getKey()) % newCapacity;
					//System.out.println(hash);
					int k = 0;
					//boolean found = false;
					while(k < chainLength)
					{
						if(newMap[hash][k] == null)
						{
							newMap[hash][k] = new SimpleEntry<K, V>(entry);
							//found = true;
							break;
							//System.out.println("SUCCESS");
						}
						k++;
					}
				}
			}
		}
		//if(primeIndex == 1)
			//System.out.println(actualSize(newMap));
		if(primeUsed)
			primeIndex++;
		map = newMap;
		capacity = newCapacity;
		//if(primeIndex < 4)
			//System.out.println(toString());
		//System.out.println(toString());
		
		if(newEntry == null)
			return;
		
		SimpleEntry<K, V> asEntry = new SimpleEntry<K, V>((SimpleEntry<K, V>) newEntry);
		int hash = hash(asEntry.getKey()) % capacity;
		for(int i = 0; i < chainLength; i++)
		{
			if(map[hash][i] == null)
			{
				map[hash][i] = new SimpleEntry<K, V>(asEntry);
				size++;
				return;
			}
			else if(map[hash][i] instanceof RemovedEntry)
			{
				map[hash][i] = new SimpleEntry<K, V>(asEntry);
				size++;
				return;
			}
		}
		//System.out.println(loadFactor());
		//primeIndex++;
		resize(primes[primeIndex], asEntry, true);
		//put(asEntry.getKey(), asEntry.getValue());
	}
	
	/*private int actualSize(Object[][] a)
	{
		int actualSize = 0;
		for(int i = 0; i < a.length; i++)
		{
			for(int j = 0; j < a[i].length; j++)
			{
				if(a[i][j] instanceof SimpleEntry)
				{
					actualSize++;
					//System.out.println(((SimpleEntry<K, V>)a[i][j]).getKey());
					//System.out.println(i + " " + j);
				}
				/*if(a[i][j] instanceof RemovedEntry)
				{
					//System.out.println("REMOVED");
					actualSize++;
				}*/
			/*}
		}
		return actualSize;
	}*/
}