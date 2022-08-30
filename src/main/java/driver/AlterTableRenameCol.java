package driver;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Database;
import model.Driver;
import model.Response;
import model.Table;
import structure.SimpleResponse;

public class AlterTableRenameCol 
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//ALTER\s+TABLE\s+([a-zA-Z][a-zA-Z0-9_]*)\s+RENAME\s+COLUMN\s+([a-zA-Z][a-zA-Z0-9_]*)\s+TO\s+([a-zA-Z][a-zA-Z0-9_]*)
			"ALTER\\s+TABLE\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s+RENAME\\s+COLUMN\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s+TO\\s+([a-zA-Z][a-zA-Z0-9_]*)",
			Pattern.CASE_INSENSITIVE
		);
	}
	
	@Override
	public Response execute(String query, Database db) 
	{
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;
		
		String tableName = matcher.group(1);
		String oldName = matcher.group(2);
		String newName = matcher.group(3);
		
		Table table = db.getTables().get(tableName);
		List<String> colNames = table.getColumnNames();
		int index = colNames.indexOf(oldName);
		
		if(index < 0)
			return new SimpleResponse(false, "A column named \"" + oldName + "\" does not exist in table \"" + tableName + "\"", null);
		
		if(colNames.contains(newName))
			return new SimpleResponse(false, "The name \"" + newName + "\" was already used for a column in table \"" + tableName + "\"", null);
		
		table.getColumnNames().set(index, newName);
	
		return new SimpleResponse(true, "Renamed column \"" + oldName + "\" to \"" + newName + "\" in table \"" + tableName + "\"", table);
	}

}
