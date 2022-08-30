package examples.xml;

import javax.xml.stream.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import java.io.*;
import java.util.*;

public class Stax2 {
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
			StringWriter output = new StringWriter();
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(output);
			
			writer.writeStartDocument();
			writer.writeStartElement("set");
		    for (List<String> list: set) {
				writer.writeStartElement("list");
				writer.writeAttribute("key", list.get(0));
		    	for (String element: list) {
					writer.writeEmptyElement("element");
					writer.writeAttribute("value", element.toString());
		    	}
				writer.writeEndElement();
		    }
			writer.writeEndElement();
			writer.writeEndDocument();
			
			writer.close();
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    
		    String filename = "data/stax2.xml";
		    Source from = new StreamSource(new StringReader(output.toString()));
		    Result to = new StreamResult(new FileWriter(filename));
		    transformer.transform(from, to);
		}
		catch (XMLStreamException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Set<List<String>> read() {
		try {
			String filename = "data/stax2.xml";
			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(filename));
			
			Set<List<String>> result = null;
			List<String> list = null;
			String value = null;
			while (reader.hasNext()) {
				reader.next();
				
				if (reader.getEventType() == XMLStreamReader.START_ELEMENT) {
					if (reader.getLocalName().equalsIgnoreCase("set")) {
						result = new HashSet<>();
					}
					else if (reader.getLocalName().equalsIgnoreCase("list")) {
						list = new LinkedList<>();
					}
					else if (reader.getLocalName().equalsIgnoreCase("element")) {
						value = reader.getAttributeValue(null, "value");
					}
				}
				
				if (reader.getEventType() == XMLStreamReader.END_ELEMENT) {
					if (reader.getLocalName().equalsIgnoreCase("set")) {
						return result;
					}
					else if (reader.getLocalName().equalsIgnoreCase("list")) {
						result.add(list);
						list = null;
					}
					else if (reader.getLocalName().equalsIgnoreCase("element")) {
						list.add(value);
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
