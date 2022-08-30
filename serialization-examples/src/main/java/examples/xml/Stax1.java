package examples.xml;

import javax.xml.stream.*;
import java.io.*;
import java.util.*;

public class Stax1 {
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
			String filename = "data/stax1.xml";
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(new FileWriter(filename));

			writer.writeStartDocument();
			writer.writeStartElement("map");
			for (String key: map.keySet()) {
				writer.writeStartElement("entry");
				writer.writeAttribute("key", key.toString());
				writer.writeCharacters(map.get(key).toString());
				writer.writeEndElement();
			}
			writer.writeEndElement();
			writer.writeEndDocument();

			writer.close();
		} 
		catch (XMLStreamException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, Integer> read() {
		try {
			String filename = "data/stax1.xml";
			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(filename));
			
			Map<String, Integer> result = null;
			String key = null;
			Integer value = null;
			while (reader.hasNext()) {
				reader.next();
				
				if (reader.getEventType() == XMLStreamReader.START_ELEMENT) {
					if (reader.getLocalName().equalsIgnoreCase("map")) {
						result = new HashMap<>();
					}
					else if (reader.getLocalName().equalsIgnoreCase("entry")) {
						key = reader.getAttributeValue(null, "key");
						value = Integer.parseInt(reader.getElementText());
					}
				}

				if (reader.getEventType() == XMLStreamReader.END_ELEMENT) {
					if (reader.getLocalName().equalsIgnoreCase("map")) {
						return result;
					}					
					else if (reader.getLocalName().equalsIgnoreCase("entry")) {
						result.put(key, value);
						key = null;
						value = null;
					}
				}
			}
			reader.close();
		} 
		catch (XMLStreamException e) {
			e.printStackTrace();
		} 
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
