package driver;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Database;
import model.Driver;
import model.Response;
import model.Table;
import structure.SimpleResponse;

public class AlterTableAdd 
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//ALTER\s+TABLE\s+([a-zA-Z][a-zA-Z0-9_]*)\s+ADD\s+COLUMN\s+([a-zA-Z][a-zA-Z0-9_]*)\s+((?:STRING|INTEGER|BOOLEAN))(?:\s+((?:FIRST|BEFORE\s+[a-zA-Z][a-zA-Z0-9_]*|AFTER\s+[a-zA-Z][a-zA-Z0-9_]*|LAST)))?
			"ALTER\\s+TABLE\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s+ADD\\s+COLUMN\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s+((?:STRING|INTEGER|BOOLEAN))(?:\\s+((?:FIRST|BEFORE\\s+[a-zA-Z][a-zA-Z0-9_]*|AFTER\\s+[a-zA-Z][a-zA-Z0-9_]*|LAST)))?",
			Pattern.CASE_INSENSITIVE
		);
	}
	
	@Override
	public Response execute(String query, Database db) 
	{
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;
		
		String tableName = matcher.group(1);
		String colName = matcher.group(2);
		String colType = matcher.group(3);
		String location = matcher.group(4);
		
		boolean optional = true;
		if(location == null)
			optional = false;
		
		Table table = db.getTables().get(tableName);
		List<String> colNames = table.getColumnNames();
		
		if(colNames.contains(colName))
			return new SimpleResponse(false, "A column named \"" + colName + "\" already exists in the table", null);
		
		//colNames.add(null);
		int index = -1;
		if(optional)
		{
			if(location.toUpperCase().equals("FIRST"))
				index = 0;
			else if(location.length() > 7 && location.toUpperCase().substring(0,6).equals("BEFORE"))
			{
				String befCol = location.substring(7).strip();
				index = colNames.indexOf(befCol);
				if(index < 0)
					return new SimpleResponse(false, "Column \"" + befCol + "\" does not exist in table \"" + tableName + "\"", null);
			}
			else if(location.length() > 6 && location.toUpperCase().substring(0,5).equals("AFTER"))
			{
				String aftCol = location.substring(6).strip();
				index = colNames.indexOf(aftCol) + 1;
				if(index == 0)
					return new SimpleResponse(false, "Column \"" + aftCol + "\" does not exist in table \"" + tableName + "\"", null);
			}
			else if(location.toUpperCase().equals("LAST"))
				index = colNames.size();
			else
				return new SimpleResponse(false, "Invalid location specification for new column", null);
		
			//System.out.println("Index: " + index);
			//System.out.println("Location: " + location);
		}
		else
		{
			index = colNames.size();
		}
		
		table.getColumnNames().add(index, colName);
		table.getColumnTypes().add(index, colType);
		
		//colNames.remove(null);
		
		Set<Object> keys = table.getState().keySet();
		
		for(Object rowKey : keys)
		{
			List<Object> row = table.getState().get(rowKey);
			row.add(index, null);
		}
		
		if(index <= table.getPrimaryIndex())
			table.setPrimaryIndex(table.getPrimaryIndex() + 1);
		
		int[] autos = table.getAutos();
		int[] temp = new int[autos.length + 1];
		int counter = 0;
		for(int i = 0; counter < autos.length; i++)
		{
			if(i != index)
			{
				temp[i] = autos[counter];
				counter++;
			}
			else
			{
				temp[i] = 0;
			}
		}
		table.setAutos(temp);
		
		return new SimpleResponse(true, "Successfully added column \"" + colName + "\" to table \"" + tableName + "\"", table);
	}

}
