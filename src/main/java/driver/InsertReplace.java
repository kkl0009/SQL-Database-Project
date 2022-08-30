package driver;

/**
 * This class was created by Kollin Labowski for Module 2 of CS 210
 * This class allows the user to either insert a particular set of values into a row or replace an existing row of values
 * The user has the ability to specify which columns to alter, as well as which values should go to each column
 */

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;

import core.Database;
import model.Driver;
import model.Response;
import model.Table;
import structure.SimpleResponse;
import structure.VolatileTable;

public class InsertReplace 
	implements Driver
{

	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//(INSERT|REPLACE)\s+INTO\s+([a-zA-Z][a-zA-Z0-9_]*)\s+(?:\(\s*([a-zA-Z][a-zA-Z0-9_]*\s*(?:,\s*[a-zA-Z][a-zA-Z0-9_]*\s*)*)\)\s+)?VALUES\s+((?:\(\s*(?:[^,;()]+\s*(?:,\s*[^,;()]+\s*)*)\))(?:\s*,\s*\(\s*(?:[^,;()]+\s*(?:,\s*[^,;()]+\s*)*)\))*)
			"(INSERT|REPLACE)\\s+INTO\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s+(?:\\(\\s*([a-zA-Z][a-zA-Z0-9_]*\\s*(?:,\\s*[a-zA-Z][a-zA-Z0-9_]*\\s*)*)\\)\\s+)?VALUES\\s+((?:\\(\\s*(?:[^,;()]+\\s*(?:,\\s*[^,;()]+\\s*)*)\\))(?:\\s*,\\s*\\(\\s*(?:[^,;()]+\\s*(?:,\\s*[^,;()]+\\s*)*)\\))*)",
			Pattern.CASE_INSENSITIVE
		);
	}
	
	@Override
	public Response execute(String query, Database db) 
	{
		// TODO: Fix bug that causes some earlier row additions to add even if a later one fails
		
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;
		
		//Reading in data from the user input
		////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		String selection = matcher.group(1).toUpperCase();
		String tableName = matcher.group(2);
		
		if(!db.getTables().containsKey(tableName))
			return new SimpleResponse(false, "A table named \"" + tableName + "\" does not exist in the database", null);
		
		String cols = matcher.group(3);
		String[] colSelection = null;
		if(cols != null)
			colSelection = cols.split(",");
		
		int everyPrimIndex = -1;
		//System.out.println("DEBUG");
		
		String strVals = matcher.group(4);
		
		String[] rowValues = strVals.split("\\)");
		//rowValues.remove(0);
		
		for(int i = 0; i < rowValues.length; i++)
		{
			//System.out.println(strVals);
			rowValues[i] = rowValues[i].substring(rowValues[i].indexOf("(") + 1);
			//System.out.println(rowValues[i]);
		}
		
		List<List> everyRow = new LinkedList<List>();
		
		boolean primExists = false;
		Table table = db.getTables().get(tableName);
		List<String> names = table.getColumnNames();
		Set<String> dupCols = new HashSet<String>();
		String primaryName = names.get(table.getPrimaryIndex());
		
		List<Object> allRows = new LinkedList<Object>();
		
		Table insertTable = new VolatileTable(
				"_insert",
				table.getColumnNames(),
				table.getColumnTypes(),
				table.getPrimaryIndex()
				);
		
		Table replaceTable = new VolatileTable(
				"_replace",
				table.getColumnNames(),
				table.getColumnTypes(),
				table.getPrimaryIndex()
				);
		
		//Finding references for use when creating rows later
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		boolean primaryFound = false;
		
		List<String> inputCols = new LinkedList<String>();
		List<Integer> references = new ArrayList<Integer>(names.size());
		
		if(cols != null)
		{
			for(int j = 0; j < table.getColumnTypes().size(); j++)
			{
				references.add(-1);
			}
		
			for(int i = 0; i < colSelection.length; i++)
			{	
				String input = colSelection[i].strip();
				if(dupCols.contains(input))
					return new SimpleResponse(false, "You input one or more duplicate column names", null);
				dupCols.add(input);
				if(input.equals(primaryName))
					primaryFound = true;
				inputCols.add(input);
				references.set(i, names.indexOf(input));
			}
			if(!primaryFound)
				return new SimpleResponse(false, "One of your input columns must be the table's primary column (\"" + primaryName + "\")", null);
		}
		else
		{
			inputCols = names;
			for(int i = 0; i < inputCols.size(); i++)
				references.add(i);
		}
		
		List<String> types = table.getColumnTypes();
		
		int autos[];
		if(table.getAutos()[0] == -2)
		{
			autos = new int[types.size()];
			for(int i = 0; i < autos.length; i++)
			{
				if(types.get(i).equals("auto_integer"))
					autos[i] = 0;
				else
					autos[i] = -1;
			}
		}
		else
		{
			autos = table.getAutos();
		}
		
		for(int q = 0; q < rowValues.length; q++)
		{	
			String[] values = rowValues[q].split(",");
		
			//Gathering values for use in row creation
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
			List<String> inputValues = new LinkedList<String>();
		
			for(int i = 0; i < values.length; i++)
			{
				inputValues.add(values[i].strip());
			}
		
			if(inputValues.size() != inputCols.size())
				return new SimpleResponse(false, "You did not input the same number of column names and values", null);
		
			//Relevant Lists
			// inputCols | inputValues | references
		
			int rowLength = inputValues.size();
			List<Object> row = new ArrayList<Object>();
		
			for(int i = 0; i < references.size(); i++)
			{
				row.add(null);
			}
		
			//Loops through the data to create the row to insert/replace
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
			
		
			for(int i = 0; i < rowLength; i++)
			{
				int refer = references.get(i);
				String chosen = inputCols.get(i);
				String val = inputValues.get(i);
				
				if(val.toUpperCase().equals("NULL") && !types.get(refer).equals("auto_integer"))
					row.set(refer, null);
				else if(val.toUpperCase().equals("NULL") && types.get(refer).equals("auto_integer"))
				{
					row.set(refer, autos[refer]);
					autos[refer]++;
				}
				else if(types.get(refer).equals("string"))
				{
					if(val.indexOf("\"") >= 0)
					{
						val = val.substring(val.indexOf("\"")+1);
						if(val.indexOf("\"") >= 0)
						{
							val = val.substring(0, val.indexOf("\""));
							if(val.length() < 256)
								row.set(refer, val);
							else
								return new SimpleResponse(false, "Input Strings cannot be more than 255 characters long", null);
						}
						else
							return new SimpleResponse(false, "Expected type String in column \"" + chosen + "\", but did not find expected format", null);
					}
					else
						return new SimpleResponse(false, "Expected type String in column \"" + chosen + "\", but did not find expected format", null);
				}
				else if(types.get(refer).equals("integer") || types.get(refer).equals("auto_integer"))
				{
					if(val.charAt(0) == '0' && val.length() > 1)
						return new SimpleResponse(false, "Integers cannot have leading zeroes", null);
					try
					{
						int num = Integer.parseInt(val);
						if(num >= autos[refer] && types.get(refer).equals("auto_integer"))
							autos[refer] = num + 1;
						row.set(refer, num);
					}
					catch(NumberFormatException ex)
					{
						return new SimpleResponse(false, "Expected type Integer in column \"" + chosen + "\", but did not find expected format", null);
					}
				}
				else if(types.get(refer).equals("boolean"))
				{
					if(val.toUpperCase().equals("TRUE"))
						row.set(refer, true);
					else if(val.toUpperCase().equals("FALSE"))
						row.set(refer, false);
					else
						return new SimpleResponse(false, "Expected type Boolean in column \"" + chosen + "\", but did not find expected format", null);
				}
				else if(refer >= 0)
					return new SimpleResponse(false, "A type of one column in table \"" + tableName + "\" is not valid", null);
			}
		
			//Cutting off unneeded data from the end of the list
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
			List<Object> tempo = new ArrayList<Object>();
			for(int i = 0; i < references.size(); i++)
			{
				tempo.add(row.get(i));
			}
		
			row = tempo;
		
			//Searching for the primary value to use as the key for the row (NOTE: May be simplified if earlier code used)
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
			
			Set<Object> keys = table.getState().keySet();
			int ind = table.getPrimaryIndex();
		
			if(keys.contains(row.get(table.getPrimaryIndex())))
			{
				primExists = true;
			}
		
			if(row.get(ind) == null)
				return new SimpleResponse(false, "Values in the primary column cannot be equal to null", null);
		
			//Performs the actual insert or replace function and creates a new table to output the necessary information
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////
			allRows.add(row);
		
			everyPrimIndex = ind;
			
			if(selection.equals("INSERT"))
			{
				if(primExists)
					return new SimpleResponse(false, "You cannot have duplicate values in the primary column", null);
				//table.getState().put(row.get(ind), row);
			
				everyRow.add(row);
			
				//insertTable.getState().put(row.get(ind), row);
			
				//return new SimpleResponse(true, "Inserted 1 row into table \"" + tableName + "\"", insertTable);
			}
			else if(selection.equals("REPLACE"))
			{
				if(!primExists)//Consider removing this if it becomes necessary
					return new SimpleResponse(false, "A row with the key \"" + row.get(ind) + "\" does not already exist", null);
				//table.getState().put(row.get(ind), row);
			
				everyRow.add(row);
			
				//replaceTable.getState().put(row.get(ind), row);
			
				//return new SimpleResponse(true, "Replaced 1 row in table \"" + tableName + "\"", replaceTable);
			}
			else
				return new SimpleResponse(false, "Please begin your input with a valid keyword \"REPLACE\" or \"INSERT\"", null);
		}
		
		table.setAutos(autos);
		
		if(selection.equals("INSERT"))
		{
			for(int i = 0; i < everyRow.size(); i++)
			{
				table.getState().put(everyRow.get(i).get(everyPrimIndex), everyRow.get(i));
				insertTable.getState().put(everyRow.get(i).get(everyPrimIndex), everyRow.get(i));
			}
			return new SimpleResponse(true, "Inserted " + allRows.size() + " row(s) into table \"" + tableName + "\"", insertTable);
		}
		else
		{
			for(int i = 0; i < everyRow.size(); i++)
			{
				table.getState().put(everyRow.get(i).get(everyPrimIndex), everyRow.get(i));
				replaceTable.getState().put(everyRow.get(i).get(everyPrimIndex), everyRow.get(i));
			}
			return new SimpleResponse(true, "Replaced " + allRows.size() + " row(s) in table \"" + tableName + "\"", replaceTable);
		}
	}
}
