package driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.codehaus.groovy.syntax.Types;

import core.Database;
import model.Driver;
import model.Response;
import model.Table;
import structure.SimpleResponse;

//package javax.xml.stream;

public class Export
	implements Driver
{	
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//EXPORT\s+([a-zA-Z][a-zA-Z0-9_]*)\s+(?:(?:TO\s+([a-zA-Z][a-zA-Z0-9]*.(?:XML|JSON)))|(?:AS\s+(XML|JSON)))
			"EXPORT\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s+(?:(?:TO\\s+([a-zA-Z][a-zA-Z0-9]*.(?:XML|JSON)))|(?:AS\\s+(XML|JSON)))",
			Pattern.CASE_INSENSITIVE
		);
	}

	
	@Override
	public Response execute(String query, Database db) 
	{
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;
		
		String tableName = matcher.group(1);
		
		String fileName = matcher.group(2);
		
		String fileType = matcher.group(3);
		
		if(!db.getTables().containsKey(tableName))
			return new SimpleResponse(false, "No table named " + tableName + " exists in the database", null);
		
		Table table = db.getTables().get(tableName);
		List<String> colTypes = table.getColumnTypes();
		
		if(fileName == null)
		{
			if(fileType.equalsIgnoreCase("JSON"))
			{
				fileName = tableName + ".json";
			}
			else if(fileType.equalsIgnoreCase("XML"))
			{
				fileName = tableName + ".xml";
			}
			else
				return new SimpleResponse(false, "Invalid file type passed in", null);
		}
		
		File file = new File("data/" + fileName);
		if(file.exists())
			return new SimpleResponse(false, "A file named " + fileName + " already exists. Please choose a different name", null);
		
		if(fileType == null)
		{
			if(fileName.toUpperCase().charAt(fileName.length()-1) == 'N')
				fileType = "JSON";
			else if(fileName.toUpperCase().charAt(fileName.length()-1) == 'L')
			{
				fileType = "XML";
			}
			else
				return new SimpleResponse(false, "Invalid format for file name", null);
		}
		
		if(fileType.equalsIgnoreCase("JSON"))
		{
			Set<List<String>> hash = new HashSet<>();
			//List<String> primaryInfo = new LinkedList<String>();
			//primaryInfo.add(tableName);
			//hash.add(primaryInfo);
			//hash.add(table.getColumnNames());
			//hash.add(table.getColumnTypes());
			
			Set<Object> keys = table.getState().keySet();
			for(Object key : keys)
			{
				List<Object> row = table.getState().get(key);
				List<String> strRow = new LinkedList<String>();
				for(int i = 0; i < row.size(); i++)
				{
					if(row.get(i) == null)
					{
						strRow.add(";");
					}
					else if(colTypes.get(i).equalsIgnoreCase("string"))
					{
						String newString = (String)row.get(i);
						newString = newString.substring(1, newString.length() - 1);
						strRow.add("" + newString);
					}
					else
					{
						strRow.add("" + row.get(i));
					}
				}
				hash.add(strRow);
			}
			
			writeJSON(hash, fileName, table, table.getPrimaryIndex());
		}
		else if(fileType.equalsIgnoreCase("XML"))
		{
			Set<List<String>> hash = new HashSet<>();
			
			Set<Object> keys = table.getState().keySet();
			for(Object key : keys)
			{
				List<Object> row = table.getState().get(key);
				List<String> strRow = new LinkedList<String>();
				for(int i = 0; i < row.size(); i++)
				{
					if(row.get(i) == null)
					{
						strRow.add(";");
					}
					else if(colTypes.get(i).equalsIgnoreCase("string"))
					{
						String newString = (String)row.get(i);
						newString = newString.substring(1, newString.length() - 1);
						strRow.add("" + newString);
					}
					else
					{
						strRow.add("" + row.get(i));
					}
				}
				hash.add(strRow);
			}
			
			//System.out.println(hash);
			writeXML(hash, fileName, table);
		}
		else
			return new SimpleResponse(false, "Error reading file type", null);
		
		return new SimpleResponse(true, "Successfully exported table " + tableName + " to a file " + fileName, table);
	}

	public void writeJSON(Set<List<String>> set, String fileName, Table table, int primaryIndex) {
		try {
			JsonObjectBuilder object_builder = Json.createObjectBuilder();
			
			JsonObjectBuilder schema_object_builder = Json.createObjectBuilder();
			//TableName, ColumnNames, Types
			//tabName.add("" + table.getPrimaryIndex());
			JsonArrayBuilder array_builder_name = Json.createArrayBuilder();
	    	array_builder_name.add(table.getTableName());	    	
	    	JsonArray json_array_name = array_builder_name.build();
	    	schema_object_builder.add("Table_Name", json_array_name);
	    	
	    	JsonArrayBuilder array_builder_prim = Json.createArrayBuilder();
	    	array_builder_prim.add("" + table.getPrimaryIndex());	    	
	    	JsonArray json_array_prim = array_builder_prim.build();
	    	schema_object_builder.add("Primary_Index", json_array_prim);
	    	
	    	List<String> colNames = new LinkedList<String>();
	    	for(int i = 0; i < table.getColumnNames().size(); i++)
	    	{
	    		colNames.add(table.getColumnNames().get(i));
	    	}
			JsonArrayBuilder array_builder_cols = Json.createArrayBuilder();
	    	for (String element: colNames) {
	    		array_builder_cols.add(element);
	    	}
	    	JsonArray json_array_cols = array_builder_cols.build();
	    	schema_object_builder.add("Col_Names", json_array_cols);
			
	    	List<String> colTypes = new LinkedList<String>();
	    	for(int i = 0; i < table.getColumnTypes().size(); i++)
	    	{
	    		colTypes.add(table.getColumnTypes().get(i));
	    	}
			JsonArrayBuilder array_builder_types = Json.createArrayBuilder();
	    	for (String element: colTypes) {
	    		array_builder_types.add(element);
	    	}
	    	JsonArray json_array_types = array_builder_types.build();
	    	schema_object_builder.add("Col_Types", json_array_types);
	    	
	    	JsonObjectBuilder row_object_builder = Json.createObjectBuilder();
	    	
	    	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    	
		    for (List<String> list: set) {	   
		    	JsonArrayBuilder array_builder = Json.createArrayBuilder();
		    	for (String element: list) {
		    		if(!element.equals(";"))
		    			array_builder.add(element);
		    		else
		    			array_builder.addNull();
		    	}
		    	JsonArray json_array = array_builder.build();
		    	row_object_builder.add(list.get(primaryIndex), json_array);
		    }
		    
		    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		    
	    	object_builder.add("Schema", schema_object_builder);
	    	object_builder.add("Data", row_object_builder);
	    	
		    JsonObject json_root_object = object_builder.build();
		    //JsonObject row_json_root_object = row_object_builder.build();
		    
		    String filename = "data/" + fileName;
			Map<String, Boolean> props = new HashMap<>();
			props.put(JsonGenerator.PRETTY_PRINTING, true);
		    JsonWriter writer = Json.createWriterFactory(props).createWriter(new FileOutputStream(filename));
		   // System.out.println("DEBUG");
		    writer.writeObject(json_root_object);
		    //System.out.println("DEBUG");
		    //writer.writeObject(row_json_root_object);
		    //System.out.println("DEBUG");
		    writer.close();
		    //System.out.println("DEBUG");
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeXML(Set<List<String>> set, String fileName, Table table) {
		try {
			StringWriter output = new StringWriter();
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(output);
			
			writer.writeStartDocument();
			writer.writeStartElement("set");
			writer.writeStartElement("set");//Changed from "set"
			
			writer.writeStartElement("list");//Changed from list
			writer.writeAttribute("key", "Table_Name");
			List<String> nameIndex = new LinkedList<String>();
			//nameIndex.add("Name/Index");
			nameIndex.add(table.getTableName());
			//nameIndex.add("" + table.getPrimaryIndex());
			for(String element: nameIndex)
			{
				writer.writeEmptyElement("element");
				writer.writeAttribute("value", element.toString());
			}
			writer.writeEndElement();
			
			writer.writeStartElement("list");//Changed from list
			writer.writeAttribute("key", "Primary_Index");
			List<String> primIndex = new LinkedList<String>();
			//nameIndex.add("Name/Index");
			//primIndex.add(table.getTableName());
			primIndex.add("" + table.getPrimaryIndex());
			for(String element: primIndex)
			{
				writer.writeEmptyElement("element");
				writer.writeAttribute("value", element.toString());
			}
			writer.writeEndElement();
			
			writer.writeStartElement("list");//Changed from list
			writer.writeAttribute("key", "Col_Names");
			List<String> colNames = new LinkedList<String>();
			//colNames.add("ColNames");
			for(int i = 0; i < table.getColumnNames().size(); i++)
			{
				colNames.add(table.getColumnNames().get(i));
			}
			for(String element: colNames)
			{
				writer.writeEmptyElement("element");
				writer.writeAttribute("value", element.toString());
			}
			writer.writeEndElement();
			
			writer.writeStartElement("list");//Changed from list
			writer.writeAttribute("key", "Col_Types");
			List<String> colTypes = new LinkedList<String>();
			//colTypes.add("ColTypes");
			for(int i = 0; i < table.getColumnTypes().size(); i++)
			{
				colTypes.add(table.getColumnTypes().get(i));
			}
			for(String element: colTypes)
			{
				writer.writeEmptyElement("element");
				writer.writeAttribute("value", element.toString());
			}
			writer.writeEndElement();
	
			writer.writeEndElement();
			writer.writeStartElement("set");
			
			for (List<String> list: set) {
				writer.writeStartElement("list");
				//writer.writeAttribute("key", list.get(0));
		    	for (String element: list) {
					writer.writeEmptyElement("element");
					//System.out.println("DEBUGa");
					if(!element.equals(";"))
						writer.writeAttribute("value", element.toString());
					else
						writer.writeEmptyElement(null);
		    	}	
		    	//System.out.println("DEBUGb");
		    	writer.writeEndElement();
		    }
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeEndDocument();
			
			writer.close();
			
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
		    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		    
		    String filename = "data/" + fileName;
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
}
