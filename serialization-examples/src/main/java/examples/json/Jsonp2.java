package examples.json;

import javax.json.*;
import javax.json.stream.*;
import java.io.*;
import java.util.*;

public class Jsonp2 {
	public static void main(String[] args) {
		Set<List<String>> a = new HashSet<>();
		a.add(Arrays.asList("beta", "zeta", "eta", "theta"));
		a.add(Arrays.asList("epsilon", "upsilon"));
		a.add(Arrays.asList("phi", "psi", "chi"));
		write(a);
		System.out.println(a);
		
		Set<List<String>> b = read();
		System.out.println(b);
		
		System.out.println(a.equals(b));
		System.out.println(a == b);
	}
	
	public static void write(Set<List<String>> set) {
		try {
			JsonObjectBuilder object_builder = Json.createObjectBuilder();
		    for (List<String> list: set) {
		    	JsonArrayBuilder array_builder = Json.createArrayBuilder();
		    	for (String element: list) {
		    		array_builder.add(element);
		    	}
		    	JsonArray json_array = array_builder.build();
		    	
		    	object_builder.add(list.get(0), json_array);
		    }
		    JsonObject json_root_object = object_builder.build();
		    
		    String filename = "data/jsonp2.json";
			Map<String, Boolean> props = new HashMap<>();
			props.put(JsonGenerator.PRETTY_PRINTING, true);
		    JsonWriter writer = Json.createWriterFactory(props).createWriter(new FileOutputStream(filename));
		    writer.writeObject(json_root_object);
		    writer.close();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Set<List<String>> read() {
		Set<List<String>> set = null;
		try {
			String filename = "data/jsonp2.json";
			JsonReader reader = Json.createReader(new FileInputStream(filename));
		    JsonObject json_object = reader.readObject();
		    reader.close();
		    
		    set = new HashSet<>();
		    for (String key: json_object.keySet()) {
		    	JsonArray json_array = json_object.getJsonArray(key);
		    	List<String> list = new LinkedList<>();
		    	for (int i = 0; i < json_array.size(); i++) {
		    		list.add(json_array.getString(i));
		    	}
		    	set.add(list);
		    }
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return set;
	}
}
