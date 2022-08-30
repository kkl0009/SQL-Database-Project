package structure;
import java.util.Map.*;

public class SimpleEntry<K, V> 
	implements Entry<K, V>
{
	private K key;
	private V value;
	
	public SimpleEntry(K key, V value)
	{
		this.key = key;
		this.value = value;
	}
	
	public SimpleEntry(Entry<? extends K, ? extends V> entry)
	{
		key = entry.getKey();
		value = entry.getValue();
	}
	
	@Override
	public K getKey() {
		return key;
	}

	@Override
	public V getValue() {
		return value;
	}

	@Override
	public V setValue(V value) {
		this.value = value;
		return value;
	}

}
