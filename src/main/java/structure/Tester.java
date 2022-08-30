package structure;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import model.HashMap;
import model.Page;

public class Tester 
{
	public static void main(String[] args)
	{
		Path path = Paths.get("data", "data.txt");
		List<String> field_types = new LinkedList<String>();
		field_types.add("string");
		field_types.add("integer");
		field_types.add("boolean");
		int index = 0;
		Page page = new SimplePage(path, field_types, index);
		Map vol = new VolatileHashMap();
		List listX = new LinkedList();
		listX.add("XYZ");
		listX.add(4234);
		listX.add(true);
		List listY = new LinkedList();
		listY.add("ABC");
		listY.add(23);
		listY.add(null);
		List listZ = new LinkedList();
		listZ.add("F");
		listZ.add(44);
		listZ.add(false);
		vol.put(listX.get(index), listX);
		vol.put(listY.get(index), listY);
		vol.put(listZ.get(index), listZ);
		Map per = new PersistentHashMap(page, vol);
		System.out.println(vol);
		System.out.println(per);
		System.out.println(per.size());
		per.remove(listX.get(index));
		System.out.println(per.size());
		per.putAll(vol);
		System.out.println(per);
		System.out.println(per.size());
		
		for(Object key : per.keySet())
			System.out.println(key);
		/*Map map = new PersistentHashMap(page);
		List listA = new LinkedList();
		listA.add("ABC");
		listA.add(234);
		listA.add(false);
		map.put(listA.get(index),listA);
		System.out.println(map.containsKey(listA.get(1)));
		map.remove(listA.get(index));
		System.out.println(map);*/
	}
}
