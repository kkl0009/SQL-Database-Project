package driver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import core.Database;
import model.Driver;
import model.Response;
import model.Table;
import structure.SimpleResponse;
import structure.VolatileTable;

public class Import
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//IMPORT\s+([a-zA-Z][a-zA-Z0-9]*.(?:XML|JSON))(?:\s+TO\s+([a-zA-Z][a-zA-Z0-9_]*))?
			"IMPORT\\s+([a-zA-Z][a-zA-Z0-9]*.(?:XML|JSON))(?:\\s+TO\\s+([a-zA-Z][a-zA-Z0-9_]*))?",
			Pattern.CASE_INSENSITIVE
		);
	}

	
	@Override
	public Response execute(String query, Database db) 
	{
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;
		
		String fileName = matcher.group(1);
		
		String tableName = matcher.group(2);
		
		String fileType = "";
		
		if(fileName.toUpperCase().charAt(fileName.length()-1) == 'N')
		{
			fileType = "JSON";
		}
		else if(fileName.toUpperCase().charAt(fileName.length()-1) == 'L')
		{
			fileType = "XML";
		}
		else
		{
			return new SimpleResponse(false, "Invalid file type input", null);
		}
		
		File file = new File("data/" + fileName);
		if(!file.exists())
			return new SimpleResponse(false, "No file named " + fileName + " exists. Please select a different file", null);
		
		Set<List<String>> tableSchema = null;
		if(fileType.equals("JSON"))
		{
			tableSchema = readJSON(fileName);
			/*for(List<String> nullCheck : tableSchema)
			{
				for(int i = 0; i < nullCheck.size(); i++)
				{
					if(nullCheck.get(i) == null)
						nullCheck.set(i, ";");
				}
			}
			
			int rowLength = -1;
			for(List<String> row: tableSchema)
			{
				if(row.get(0).equals(";Col_Names"))
				{
					rowLength = row.size() - 1;
					//System.out.println(rowLength);
					break;
				}
			}
			
			if(rowLength == -1)
				return new SimpleResponse(false, "Unexpected error in JSON importing, possible programmer error", null);
			
			Set<List<String>> newSet = new HashSet<List<String>>();
			for(List<String> row: tableSchema)
			{
				if(row.get(0).equals(";Data"))
				{
					int numRows = (row.size() - 1) / rowLength;
					//System.out.println(numRows);
					int counter = 0;
					List<String> values = new LinkedList<>();
					for(int i = 1; i < row.size(); i++)
					{
						values.add(row.get(i));
					}
					for(int i = 0; i < numRows; i++)
					{
						List<String> newRow = new LinkedList<String>();
						for(int j = 0; j < rowLength; j++)
						{
							newRow.add(values.get(counter));
							counter++;
						}
						newSet.add(newRow);
					}
					break;
				}
			}
			
			for(List<String> newRow : newSet)
				tableSchema.add(newRow);
			
			List<String> marker = new LinkedList<String>();
			for(List<String> delete : tableSchema)
			{
				if(delete.get(0).equals(";Data"))
				{
					marker = delete;
					break;
				}
			}
			
			tableSchema.remove(marker);*/
			//System.out.println(tableSchema);
		}
		else if(fileType.equals("XML"))
		{
			//Remember to set the new table = to the finalTable variable when calculations complete :)
			tableSchema = readXML(fileName);
			//System.out.println(tableSchema);
		}
		
		//System.out.println(tableSchema);
		
		int primaryIndex = -1;
		List<String> colNames = null;
		List<String> colTypes = null;
		List<String> nameList = null;
		List<String> indexList = null;
			
		//int a = 0;
		for(List<String> data : tableSchema)
		{
			if(data.get(0).equals(";Table_Name"))
			{
				if(tableName == null)
				{
					//data.remove(0);
					tableName = data.get(1);
				}
				//primaryIndex = Integer.parseInt(data.get(2));
				nameList = data;
			}
			else if(data.get(0).equals(";Col_Names"))
			{
				colNames = data;
				//colNames.remove(0);
			}
			else if(data.get(0).equals(";Col_Types"))
			{
				colTypes = data;
				//colTypes.remove(0);
			}
			else if(data.get(0).equals(";Primary_Index"))
			{
				//data.remove(0);
				//System.out.println("debug");
				indexList = data;
				primaryIndex = Integer.parseInt(data.get(1));				
				//primaryIndex = a;
			}
		}
			
		
		if(colTypes == null || colNames == null || primaryIndex == -1)
			return new SimpleResponse(false, "Imported file is not in the appropriate format to be converted to a table", null);
			
		tableSchema.remove(colNames);
		tableSchema.remove(colTypes);
		tableSchema.remove(indexList);
		tableSchema.remove(nameList);
		//tableSchema.remove(nameIndex);
		colNames.remove(0);
		colTypes.remove(0);
			
		//System.out.println(tableSchema);
		//System.out.println(colNames);
			
		if(db.getTables().containsKey(tableName))
		{
			boolean numFound = false;
			int number = 0;
			while(!numFound)
			{
				number++;
				if(!db.getTables().containsKey(tableName + "_" + number))
					numFound = true;
			}
			tableName = tableName + "_" + number;
		}
			
		Table table = new VolatileTable(
				tableName,
				colNames,
				colTypes,
				primaryIndex
				);
			
		List<List<Object>> allRows = new LinkedList<List<Object>>();
			
		for(List<String> data : tableSchema)
		{
			List<Object> newData = new LinkedList<Object>();
			for(int i = 0; i < data.size(); i++)
			{
				if(data.get(i).equals(";"))
				{
					newData.add(null);
				}
				else if(colTypes.get(i).equalsIgnoreCase("INTEGER"))
				{
					newData.add(Integer.parseInt(data.get(i)));
				}
				else if(colTypes.get(i).equalsIgnoreCase("BOOLEAN"))
				{
					if(data.get(i).equals("true"))
					{
						newData.add(true);
					}
					else if(data.get(i).equals("false"))
					{
						newData.add(false);
					}
					else
						return new SimpleResponse(false, "An error occured while translating a String to a boolean", null);
				}
				else if(colTypes.get(i).equalsIgnoreCase("STRING"))
				{
					newData.add("\"" + data.get(i) + "\"");
				}				
			}
			allRows.add(newData);
		}
			
		for(int i = 0; i < allRows.size(); i++)
		{
			table.getState().put(allRows.get(i).get(primaryIndex), allRows.get(i));
		}
				
		db.getTables().put(table.getTableName(), table);
		
		return new SimpleResponse(true, "Successfully imported " + fileName + " to table " + tableName, table);
	}

	public Set<List<String>> readJSON(String fileName) {
		Set<List<String>> set = null;
		try {
			String filename = "data/" + fileName;
			JsonReader reader = Json.createReader(new FileInputStream(filename));
		    JsonObject json_object = reader.readObject();
		    reader.close();
		    
		    JsonObject schema = json_object.getJsonObject("Schema");
		    JsonObject rowObject = json_object.getJsonObject("Data");
		    
		    set = new HashSet<>();
		    for (String key: schema.keySet()) {
		    	JsonArray json_array = schema.getJsonArray(key);
		    	List<String> list = new LinkedList<>();
		    	list.add(";" + key);
		    	//System.out.println(list);
		    	for (int i = 0; i < json_array.size(); i++) {
		    		if(!json_array.isNull(i))
		    			list.add(json_array.getString(i));
		    		else
		    			list.add(";");
		    	}
		    	set.add(list);
		    }
		    
		    for (String key: rowObject.keySet()) {
		    	JsonArray json_array = rowObject.getJsonArray(key);
		    	List<String> list = new LinkedList<>();
		    	//list.add(";" + key);
		    	//System.out.println(list);
		    	for (int i = 0; i < json_array.size(); i++) {
		    		if(!json_array.isNull(i))
		    			list.add(json_array.getString(i));
		    		else
		    			list.add(";");
		    	}
		    	set.add(list);
		    }
		    
		    //System.out.println(set);
		    
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		return set;
	}
	
	public Set<List<String>> readXML(String fileName) {
		try {
			String filename = "data/" + fileName;
			XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileReader(filename));
			
			Set<List<String>> master = new HashSet<>();
			Set<List<String>> result = null;
			List<String> list = null;
			String value = null;
			boolean first = false;
			//int counter = reader.START_DOCUMENT;
			while (reader.hasNext()) {
				reader.next();
				
				if (reader.getEventType() == XMLStreamReader.START_ELEMENT) {
					//for(int i = 0; i < 1000; i++)
					if (reader.getLocalName().equalsIgnoreCase("set")) {
						result = new HashSet<>();
					}
					else if (reader.getLocalName().equalsIgnoreCase("list")) {
						//System.out.println(reader.getAttributeName(XMLStreamReader.START_ELEMENT));
						//System.out.println(reader.getAttributeValue(null, "key"));
						list = new LinkedList<>();
						if(!first && reader.getAttributeValue(null, "key") != null)
							list.add(";" + reader.getAttributeValue(null, "key"));
					}
					else if (reader.getLocalName().equalsIgnoreCase("element")) {
						value = reader.getAttributeValue(null, "value");
					}
				}
				
				
				if (reader.getEventType() == XMLStreamReader.END_ELEMENT) {
					if (reader.getLocalName().equalsIgnoreCase("set")) {
						
						for(List<String> listo : result)
						{
							master.add(listo);
						}
						
						if(first)
							return master;
						first = true;
					}
					else if (reader.getLocalName().equalsIgnoreCase("list")) {
						result.add(list);
						list = null;
					}
					else if (reader.getLocalName().equalsIgnoreCase("element")) {
						if(value != null)
							list.add(value);
						else
							list.add(";");
						value = null;
					}
				}
				//counter++;
				//System.out.println(XMLStreamReader.END_DOCUMENT);
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
