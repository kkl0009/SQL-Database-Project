package driver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Database;
import model.Driver;
import model.Response;
import model.Table;
import structure.SimpleResponse;

public class AlterTableRenameTable 
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//ALTER\s+TABLE\s+([a-zA-Z][a-zA-Z0-9_]*)\s+RENAME\s+TO\s+([a-zA-Z][a-zA-Z0-9_]*)
			"ALTER\\s+TABLE\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s+RENAME\\s+TO\\s+([a-zA-Z][a-zA-Z0-9_]*)",
			Pattern.CASE_INSENSITIVE
		);
	}
	
	@Override
	public Response execute(String query, Database db) 
	{
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;
		
		String oldName = matcher.group(1);
		String newName = matcher.group(2);
		
		if(!db.getTables().containsKey(oldName))
			return new SimpleResponse(false, "Table \"" + oldName + "\" does not exist in the database", null);
			
		if(db.getTables().containsKey(newName))
			return new SimpleResponse(false, "A table named \"" + newName + "\" already exists in the database", null);
		
		Table table = db.getTables().get(oldName);
		table.setTableName(newName);
		db.getTables().remove(oldName);
		db.getTables().put(newName,table);
		
		return new SimpleResponse(true, "Successfully renamed table \"" + oldName + "\" to \"" + newName + "\"", table);
	}
}
