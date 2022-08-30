package structure;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import model.HashMap;
import model.Page;
//import structure.PersistentHashMap.MyIterator;

/*
 * TODO: Review Module 8 Video to see how to do the SimpleEntry!!!
 */

public class PersistentHashMap 
implements HashMap<Object, List<Object>>
{
	private int size;
	private int capacity;
	//private int loadFactor;
	//private Object[][] map;
	private Page page;
	private final int initialDepth = 31;
	private int chainLength = 3;
	private final int primes[] = {67, 137, 269, 521, 1117, 2273, 5261, 10729, 21187, 47111, 94933, 199889, 398569, 796933};
	private int primeIndex = 0;
	//private int maxCol = 0;
	//private int counter = 0;
	
	public PersistentHashMap(Page page) 
	{
		//capsule = new java.util.HashMap<>();
		capacity = initialDepth;
		//size = 0;
		this.page = page;
		page.length(initialDepth * chainLength);
		page.size(0);
		size = 0;
	}
	
	public PersistentHashMap(Page page, Map<? extends Object, ? extends List<Object>> copy) 
	{
		//capsule = new java.util.HashMap<>(copy);
		//size = 0;
		capacity = initialDepth;
		this.page = page;
		page.length(initialDepth * chainLength);
		page.size(0);
		size = 0;
		putAll(copy);
		//size = 0;
		//TODO: Add actual support for moving the load factor over from other maps
	}

	@Override
	public void clear() 
	{
		//capsule.clear();
		capacity = initialDepth;
		page = new SimplePage(page.getPath(), page.getFieldTypes(), page.getKeyIndex());
		page.length(initialDepth * chainLength);
		page.size(0);
		size = 0;
		//size = 0;
	}
	
	/*public Object[][] asArray()
	{
		return map;
	}*/

	/*@Override
	public int size() 
	{
		return size;
		//return capsule.size();
	}
	
	/*public int capacity()
	{
		return capacity;
	}*/

	@Override
	public boolean isEmpty() 
	{
		//return capsule.isEmpty();
		return page.size() == 0;
	}

	@Override
	public double loadFactor() 
	{
		//throw new UnsupportedOperationException();
		return (double)page.size() / ((double)chainLength);
	}
	
	private void newChains()
	{
		Page newPage = new SimplePage(page.getPath(), page.getFieldTypes(), page.getKeyIndex());
		for(int i = 0; i < newPage.length(); i++)
			newPage.write(i, page.read(i));
		
		for(int i = 0; i < capacity; i++)
		{
			for(int j = 0; j < chainLength; j++)
			{
				if(page.read((i * chainLength) + j) == null)
					break;
				if(page.read((i * chainLength) + j) instanceof SimpleEntry || page.isRemoved((i * chainLength) + j))
				{
					newPage.write((i * chainLength) + j, page.read((i * chainLength) + j));
				}
			}
		}
		page = newPage;
		//put(entry.getKey(), entry.getValue());
	}
	
	private void resizeChains(int index, SimpleEntry<Object, List<Object>> entry)
	{
		//System.out.println("RESIZE CHAINS");
		int maxLength = 0;
		if(chainLength == 5 && entry != null)
			resize(index, entry, true);
		else
		{
			for(int i = 0; i < capacity; i++)
			{
				for(int j = 0; j < chainLength; j++)
				{
					if(page.isNull((i * chainLength) + j))
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
	public List<Object> put(Object key, List<Object> value) 
	{
		if(value.get(page.getKeyIndex()) != key)
			return null;
		
		boolean checker = false;
		if(key.equals("c") && value.get(1).equals(60))
			checker = true;
		if(key.equals("c") && value.get(1).equals(142))
			checker = true;
		
		//System.out.println(chainLength);
		//TODO: Revise how the remove references are handled
		List<Object> previous = null;
		int hash = hash(key) % capacity;
		//System.out.println(hash(key) + " " +  hash);
		int i = 0;
		int pos = -1;
		boolean complete = false;
		while(i < chainLength)
		{			
			if(page.isNull((hash * chainLength) + i))
			{
				page.write((hash * chainLength) + i, value); //POTENTIAL ERROR (SIMPLE ENTRY ?)
				complete = true;
				page.size(page.size() + 1);
				size++;
				//System.out.println(counter++);
				if(checker)
					System.out.println("FLAG");
				
				break;
			}
			else if(page.isRemoved((hash * chainLength) + i) && pos == -1)
			{
				pos = i;
			}
			else /*if(map[hash][i] instanceof SimpleEntry)*/
			{	
				if(page.read((hash * chainLength) + i).get(page.getKeyIndex()).equals(key))
				{
					previous = page.read((hash * chainLength) + i);
					page.write((hash * chainLength) + i, value); //= new SimpleEntry<>(key, value);
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
			page.write((hash * chainLength) + pos, value);
			complete = true;
			page.size(page.size() + 1);
			size++;
			//if(pos > maxCol)
				//maxCol = pos;
		}
		//else if(i > maxCol)
			//maxCol = i;
		
		//System.out.println(complete);
		
		if(!complete)
		{
			resizeChains(primes[primeIndex], new SimpleEntry<Object, List<Object>>(key, value));
		}
		return previous;
	}

	@Override
	public List<Object> get(Object key) 
	{
		//return capsule.get(key);
		int hash = hash(key) % capacity;
		int i = 0;
		while(!page.isNull((hash * chainLength) + i) && i < chainLength)
		{
			if(page.isRecord((hash * chainLength) + i))
			{
				if(page.read((hash * chainLength) + i).get(page.getKeyIndex()).equals(key))
				{
					return page.read((hash * chainLength) + i);
				}
			}
			i++;
		}
		return null;
	}

	@Override
	public List<Object> remove(Object key) 
	{
		//return capsule.remove(key);
		int hash = hash(key) % capacity;
		int i = 0;
		List<Object> value = null;
		while(!page.isNull((hash * chainLength) + i) && i < chainLength)
		{
			if(page.isRecord((hash * chainLength) + i))
			{
				if(page.read((hash * chainLength) + i).get(page.getKeyIndex()).equals(key))
				{
					value = page.read((hash * chainLength) + i);
					page.writeRemoved((hash * chainLength) + i);
					page.size(page.size() - 1);
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
		while(!page.isNull((hash * chainLength) + i) && i < chainLength)
		{
			if(page.isRecord((hash * chainLength) + i))
			{
				if(page.read((hash * chainLength) + i).get(page.getKeyIndex()).equals(key))
					return true;
			}
			i++;
		}
		return false;
	}

	@Override
	/*
	 * POSSIBLE ERROR WITH length() methods to investigate
	 */
	public boolean containsValue(Object value) 
	{
		for(int i = 0; i < capacity; i++)
		{
			for(int j = 0; j < chainLength; j++)
			{
				if(page.isNull((i * chainLength) + j))
					break;
				else if(page.isRecord((i * chainLength) + j))
				{
					if(page.read((i * chainLength) + j).equals(value))
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
	public void putAll(Map<? extends Object, ? extends List<Object>> m) 
	{
		for(List<Object> value : m.values())
		{
			put(value.get(page.getKeyIndex()), value);
		}
	}

	@Override
	public String toString() 
	{
		String response = "{";
		for(int i = 0; i < capacity; i++)
		{
			for(int j = 0; j < chainLength; j++)
			{
				if(page.isNull((i * chainLength) + j))
					break;
				if(page.isRecord((i * chainLength) + j))
				{
					List<Object> entry = page.read((i * chainLength) + j);
					response += (" [ Key = " + entry.get(page.getKeyIndex()) + " | Value = " + entry + " ] ");				
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
		
		Map<Object, List<Object>> otherMap = (Map<Object, List<Object>>) o;
		if(size() != otherMap.size())
			return false;
		
		for(Object key : otherMap.keySet())
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
		return page.hashCode();
	}

	@Override
	public Iterator<Map.Entry<Object, List<Object>>> iterator() 
	{
		//TODO: Fix the Iterator Bugs
		//return capsule.entrySet().iterator();
		//return new Iterator<Map.Entry<K, V>>();
		//return new entrySet();
		//System.out.println("DEBUG");
		return new MyIterator();
		//return new Iterator<SimpleEntry<K, V>>();
	}
	
	private class MyIterator implements Iterator<Map.Entry<Object,List<Object>>>
	{
		int row = 0;
		int col = 0;
		
		public MyIterator()
		{
			boolean found = false;
			for(int i = row + 1; i < capacity; i++)
			{
				for(int j = 0; j < chainLength; j++) //MAY NEED TO CHANGE chainLength to capacity
				{
					if(page.isNull((i * chainLength) + j))
						break;
					if(page.isRecord((i * chainLength) + j))
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
				if(page.isNull((row * chainLength) + i))
					break;
				if(page.isRecord((row * chainLength) + i))
					return true;
			}
			for(int i = row + 1; i < capacity; i++)
			{
				for(int j = 0; j < chainLength; j++)
				{
					if(page.isNull((i * chainLength) + j))
						break;
					if(page.isRecord((i * chainLength) + j))
						return true;
				}
			}
			return false;
		}
		
		public SimpleEntry<Object, List<Object>> next()
		{
			for(int i = col; i < chainLength; i++)
			{
				if(page.isNull((row * chainLength) + i))
					break;
				if(page.isRecord((row * chainLength) + i))
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
						return new SimpleEntry(page.read((row * chainLength) + i).get(page.getKeyIndex()), page.read((row * chainLength) + i)); //MAY NEED TO CHANGE TO SIMPLE ENTRY !!!!!!!!!!!
					else
						return new SimpleEntry(page.read(((row - 1) * chainLength) + i).get(page.getKeyIndex()), page.read(((row - 1) * chainLength) + i));
				}
			}
			for(int i = row + 1; i < capacity; i++)
			{
				for(int j = 0; j < chainLength; j++)//Flag
				{
					if(page.isNull((i * chainLength) + j))
						break;
					if(page.isRecord((i * chainLength) + j))
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
						return new SimpleEntry(page.read((i * chainLength) + j).get(page.getKeyIndex()), page.read((i * chainLength) + j));
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
		//System.out.println("RESIZE BEGIN");
		
		//System.out.println("Resize : " + this.capacity + " | " + newCapacity);
		//Page newPage = new SimplePage(page.getPath(), page.getFieldTypes(), page.getKeyIndex()); //POTENTIAL ERROR WITH FILE PATH

		Object[] newMap = new Object[newCapacity * chainLength];
		
		//System.out.println("YOO");
		
		for(int i = 0; i < capacity; i++)
		{
			for(int j = 0; j < chainLength; j++)
			{
				if(page.isNull((i * chainLength) + j))
					break;
				else if(page.isRecord((i * chainLength) + j))
				{
					List entry = page.read((i * chainLength) + j);
					int hash = hash(entry.get(page.getKeyIndex())) % newCapacity;
					//System.out.println(hash);
					int k = 0;
					//boolean found = false;
					while(k < chainLength)
					{
						if(newMap[(hash * chainLength) + k] == null)
						{
							newMap[(hash * chainLength) + k]  = entry;
							//found = true;
							/*if(entry.get(0).equals("c") && entry.get(1).equals(142))
								System.out.println("NEW FLAG");*/
							break;
							//System.out.println("SUCCESS");
						}
						k++;
					}
				}
				page.writeNull((i * chainLength) + j);
			}
		}
		//if(primeIndex == 1)
			//System.out.println(actualSize(newMap));
		if(primeUsed)
			primeIndex++;
	
		page.length(newCapacity * chainLength);
		capacity = newCapacity;
		
		//page = new SimplePage(page.getPath(), page.getFieldTypes(), page.getKeyIndex());
		for(int i = 0; i < newMap.length; i++)
		{
			if(newMap[i] != null)
				page.write(i, (List<Object>)newMap[i]);
		}
		
		
		
		if(newEntry == null)
			return;
		
		SimpleEntry<Object, List<Object>> asEntry = new SimpleEntry<Object, List<Object>>((SimpleEntry<Object, List<Object>>) newEntry);
		int hash = hash(asEntry.getKey()) % capacity;
		for(int i = 0; i < chainLength; i++)
		{
			if(page.isNull((hash * chainLength) + i))
			{
				page.write((hash * chainLength) + i, asEntry.getValue());
				page.size(page.size() + 1);
				size++;
				return;
			}
			else if(page.isRemoved((hash * chainLength) + i))
			{
				page.write((hash * chainLength) + i, asEntry.getValue());
				page.size(page.size() + 1);
				size++;
				return;
			}
		}
		//System.out.println(size == page.size());
		//System.out.println(loadFactor());
		//primeIndex++;
		resize(primes[primeIndex], asEntry, true);
		//put(asEntry.getKey(), asEntry.getValue());
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		//System.out.println(toString());
		//System.out.println(page.size() + " " + page.length());
		//return page.size();
		return size;
	}
}
