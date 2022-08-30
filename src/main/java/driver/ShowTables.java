package driver;

/**
 * This class was written by Kollin Labowski
 * It was created as part of Module 1 of the CS 210 Project
 * It displays a table which contains the name and number of rows for each of the other tables stored in the database
 */

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Database;
import model.*;
import structure.*;

public class ShowTables 
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//SHOW\s+TABLES
			"SHOW\\s+TABLES",
			Pattern.CASE_INSENSITIVE
		);
	}
	
	@Override
	public Response execute(String query, Database db) 
	{
		//Check if regular expression works
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;

		//Reads data from the database
		HashMap<String, Table> allTables = db.getTables();
		int numTables = allTables.size();
		
		//Creates the table
		Table table = new VolatileTable(
				"_tables",
				List.of("table_name", "row_count"),
				List.of("string", "integer"),
				0
			);
		
		//Adds the necessary rows to the tables
		Object[] arrTables = (allTables.values().toArray());
		List<String> names = new LinkedList<String>();
		List<Integer> rows = new LinkedList<Integer>();
		
		// If there is a problem where rows are being incorrectly computed,
		// try to divide table.getState().size() by the # of columns.
		// If this fix works, also implement it to the DropTable driver
		
		for(int i = 0; i < arrTables.length; i++)
		{
			names.add(((Table)arrTables[i]).getTableName());
			rows.add(((Table)arrTables[i]).getState().size());
		}
		
		for(int i = 0; i < names.size(); i++)
		{
			List<Object> list = new LinkedList<>();
			list.add(names.get(i));
			list.add(rows.get(i));
			table.getState().put(names.get(i), list);
		}
		
		return new SimpleResponse(true, "Now showing " + numTables + " tables in the database", table);
	}

}
