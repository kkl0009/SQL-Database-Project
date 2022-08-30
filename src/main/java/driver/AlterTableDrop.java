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

public class AlterTableDrop 
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//ALTER\s+TABLE\s+([a-zA-Z][a-zA-Z0-9_]*)\s+DROP\s+COLUMN\s+([a-zA-Z][a-zA-Z0-9_]*)
			"ALTER\\s+TABLE\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s+DROP\\s+COLUMN\\s+([a-zA-Z][a-zA-Z0-9_]*)",
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
		
		Table table = db.getTables().get(tableName);
		List<String> colNames = table.getColumnNames();
		
		if(!colNames.contains(colName))
			return new SimpleResponse(false, "There is no column in table \"" + tableName + "\" named \"" + colName + "\"", null);
		
		if(colNames.get(table.getPrimaryIndex()).equals(colName))
			return new SimpleResponse(false, "Column \"" + colName + "\" is the primary column and cannot be dropped", null);
		
		int index = colNames.indexOf(colName);
		
		table.getColumnNames().remove(index);
		table.getColumnTypes().remove(index);
		
		Set<Object> keys = table.getState().keySet();
		
		for(Object rowKey : keys)
		{
			List<Object> row = table.getState().get(rowKey);
			row.remove(index);
		}
		
		if(index < table.getPrimaryIndex())
		{
			table.setPrimaryIndex(table.getPrimaryIndex() - 1);
		}
		
		int[] autos = table.getAutos();
		int[] temp = new int[autos.length - 1];
		int counter = 0;
		for(int i = 0; counter < temp.length; i++)
		{
			if(i != index)
			{
				temp[counter] = autos[i];
				counter++;
			}
		}
		
		table.setAutos(temp);
		
		return new SimpleResponse(true, "Successfully dropped column \"" + colName + "\" from table \"" + tableName + "\"", table);
	}
}
