package driver;

/**
 * This class was written by Kollin Labowski
 * It is a driver which allows the user to view an input table
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Database;
import model.Driver;
import model.Response;
import structure.SimpleResponse;

public class DumpTable
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//DUMP\s+TABLE\s+([a-zA-Z_][a-zA-Z0-9_]*)
			"DUMP\\s+TABLE\\s+([a-zA-Z_][a-zA-Z0-9_]*)",
			Pattern.CASE_INSENSITIVE
		);
	}

	@Override
	public Response execute(String query, Database db) 
	{
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;
		
		String name = matcher.group(1); 
		if(!db.getTables().containsKey(name))
			return new SimpleResponse(false, "A table named \"" + name + "\" does not exist in the database", null);
		else
			return new SimpleResponse(true, "Now displaying table \"" + name +"\"", db.getTables().get(name));
	}

}
