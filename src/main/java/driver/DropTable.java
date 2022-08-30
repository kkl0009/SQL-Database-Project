package driver;

import java.util.LinkedList;
import java.util.List;
/**
 * Class created by Kollin Labowski as part of Module 1
 * 
 * This class is used as a driver which allows the user to remove specified tables if they exist
 * If the input table name is not connected to a table in the database, this will return an error message
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Database;
import model.*;
import structure.*;

public class DropTable 
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//DROP\s+TABLE\s+([a-zA-Z][a-zA-Z0-9_]*)
			"DROP\\s+TABLE\\s+([a-zA-Z][a-zA-Z0-9_]*)",
			Pattern.CASE_INSENSITIVE
		);
	}

	public Response execute(String query, Database db) 
	{
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;

		String tableName = matcher.group(1);
		
		HashMap<String, Table> allTables = db.getTables();
		
		if(!allTables.containsKey(tableName))
			return new SimpleResponse(false, "A table with the name \"" + tableName + "\" does not exist", null);
		
		Table selection = allTables.get(tableName);
		
		allTables.remove(tableName);
		
		db.setTables(allTables);
		
		int rows = selection.getState().size();
		
		// TODO: Add a successful response that includes the name of the table and its # of ROWS
		return new SimpleResponse(true, "Successfully dropped table \"" + tableName + "\", which had " + rows + " rows", selection);
	}

}
