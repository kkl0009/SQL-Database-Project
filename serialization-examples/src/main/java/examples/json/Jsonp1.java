package examples.json;

import javax.json.*;
import java.io.*;
import java.util.*;

public class Jsonp1 {
	public static void main(String[] args) {
		Map<String, Integer> a = new HashMap<>();
		a.put("alpha", 5);
		a.put("beta",  4);
		a.put("delta", 5);
		a.put("gamma", 5);
		a.put("tau",   3);
		a.put("pi",    2);
		write(a);
		System.out.println(a);
		
		Map<String, Integer> b = read();
		System.out.println(b);
		
		System.out.println(a.equals(b));
		System.out.println(a == b);
	}
	
	public static void write(Map<String, Integer> map) {
		try {
		    JsonObjectBuilder object_builder = Json.createObjectBuilder();
		    for (String key: map.keySet()) {
		    	object_builder.add(key, map.get(key));
		    }
		    JsonObject json_root_object = object_builder.build();
		    
		    String filename = "data/jsonp1.json";
		    JsonWriter writer = Json.createWriter(new FileOutputStream(filename));
		    writer.writeObject(json_root_object);
		    writer.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Map<String, Integer> read() {
		Map<String, Integer> result = null;
		try {
			String filename = "data/jsonp1.json";
			JsonReader reader = Json.createReader(new FileInputStream(filename));
		    JsonObject json_root_object = reader.readObject();
		    reader.close();
		    
		    result = new HashMap<>();
		    for (String key: json_root_object.keySet()) {
		    	result.put(key, json_root_object.getInt(key));
		    }
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
