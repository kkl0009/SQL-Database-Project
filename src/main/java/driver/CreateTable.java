package driver;

/**
 * This class was written by Kollin Labowski
 * 
 * The purpose of this class is to create a driver which will allow the user to make a table that is to their specifications
 * The user has the ability to input a name for the table, as well as several column definitions
 * Each column definition  consists of a column name and a data type (String, Integer, or Boolean)
 * The user can also specify which of the columns should be designated as the primary column (exactly 1 must be identified as such)
 * 
 */

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Database;
import model.*;
import structure.*;
import java.util.Set;

public class CreateTable 
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//CREATE\s+TABLE\s+([a-zA-Z][a-zA-Z0-9_]*)\s+\(\s*([a-zA-Z][a-zA-Z0-9_]*\s+(?:STRING|INTEGER|BOOLEAN|AUTO_INTEGER)(?:\s+PRIMARY)?(?:\s*,\s*[a-zA-Z][a-zA-Z0-9_]*\s+(?:STRING|INTEGER|BOOLEAN|AUTO_INTEGER)(?:\s+PRIMARY)?)*)\s*\)
			"CREATE\\s+TABLE\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s+\\(\\s*([a-zA-Z][a-zA-Z0-9_]*\\s+(?:STRING|INTEGER|BOOLEAN|AUTO_INTEGER)(?:\\s+PRIMARY)?(?:\\s*,\\s*[a-zA-Z][a-zA-Z0-9_]*\\s+(?:STRING|INTEGER|BOOLEAN|AUTO_INTEGER)(?:\\s+PRIMARY)?)*)\\s*\\)",
			Pattern.CASE_INSENSITIVE
		);
	}

	@Override
	public Response execute(String query, Database db) 
	{
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;
		
		//Grabbing the data from the regular expression capture groups
		String tableName = matcher.group(1);
		String[] colDefs = matcher.group(2).split(",");
		
		if(tableName.length() > 15)
			return new SimpleResponse(false, "Table names can be no longer than 15 characters", null);
		
		//Checking if name is a duplicate
		if(db.getTables().containsKey(tableName))
			return new SimpleResponse(false, "There is already a table named \"" + tableName + "\", name your table something else", null);
		
		//Checking whether there are more than 15 columns
		if(colDefs.length > 15)
			return new SimpleResponse(false, "You cannot define more than 15 columns", null);
		
		//Loops to collect each part of the data
		Set<String> dupNames = new HashSet<String>();
		List<String> colNames = new LinkedList<String>();
		List<String> dataTypes = new LinkedList<String>();
		int primaryIndex = -1;
		boolean primaryFound = false;
		for(int i = 0; i < colDefs.length; i++)
		{
			colDefs[i] = colDefs[i].strip();
			
			if(colDefs[i].indexOf(" ") < 0)
				return new SimpleResponse(false, "You must have whitespace between the parts of your column definitions", null);
			
			String colName = colDefs[i].substring(0, colDefs[i].indexOf(" "));
			colName = colName.strip();
			
			if(colName.length() > 15)
				return new SimpleResponse(false, "Column names can be no longer than 15 characters", null);
		
			if(dupNames.contains(colName))
				return new SimpleResponse(false, "You cannot have duplicate column names", null);
			else
				dupNames.add(colName);
			
			colNames.add(colName); 
			colDefs[i] = colDefs[i].substring(colDefs[i].indexOf(" ") + 1);
			
			String dataType = "";
			if(colDefs[i].toUpperCase().indexOf("AUTO_INTEGER") >= 0)
				dataType = "auto_integer";
			else if(colDefs[i].toUpperCase().indexOf("STRING") >= 0)
				dataType = "string";
			else if(colDefs[i].toUpperCase().indexOf("BOOLEAN") >= 0)
				dataType = "boolean";
			else if(colDefs[i].toUpperCase().indexOf("INTEGER") >= 0)
				dataType = "integer";
			else
				return new SimpleResponse(false, "Invalid data type in one or more column definitions", null);
			
			dataTypes.add(dataType);
			
			if(colDefs[i].toUpperCase().indexOf("PRIMARY") > 0)
			{
				if(!primaryFound)
				{
					primaryFound = true;
					primaryIndex = i;
				}
				else
					return new SimpleResponse(false, "You cannot have more than 1 primary column", null);
			}
		}
		if(!primaryFound)
			return new SimpleResponse(false, "You must designate a primary column", null);
		
		//Creating the table from the collected data
		Table table = new VolatileTable(
			tableName,
			colNames,
			dataTypes,
			primaryIndex
		);
		
		db.getTables().put(tableName, table);
 		
		//Returning the completed table
		return new SimpleResponse(true, "Created a table named \"" + tableName + "\" with " + colNames.size() + " columns", table);
	}

}
