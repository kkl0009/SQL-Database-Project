package driver;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Database;
import model.Driver;
import model.Response;
import model.Table;
import structure.SimpleResponse;
import structure.VolatileTable;

public class DeleteFrom 
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//DELETE\s+FROM\s+([a-zA-Z][a-zA-Z0-9_]*)(?:\s+WHERE\s+([a-zA-Z][a-zA-Z0-9_]*)\s+(=|<>|>|>=|<|<=)\s+([^;,]+))?
			"DELETE\\s+FROM\\s+([a-zA-Z][a-zA-Z0-9_]*)(?:\\s+WHERE\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s+(=|<>|>|>=|<|<=)\\s+([^;,]+))?",
			Pattern.CASE_INSENSITIVE
		);
	}

	@Override
	public Response execute(String query, Database db) 
	{
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;
		
		String tableName = matcher.group(1);
		
		String col = matcher.group(2);
		
		String condition = matcher.group(3);
		
		String val = matcher.group(4);
		
		boolean optional = true;
		if(col == null && condition == null && val == null)
			optional = false;
		
		Table table = db.getTables().get(tableName);
		List<String> colNames = table.getColumnNames();
		List<String> colTypes = table.getColumnTypes();
		//List<Object> updatedRows = new LinkedList<Object>();
		int index = colNames.indexOf(col);
		Set<Object> keys = table.getState().keySet();
		Object parameter = null;
		//List<Integer> indexes = new LinkedList<Integer>();
		
		if(optional)
		{
			if(val.equals("null"))
				parameter = null;
			else if(colTypes.get(index).equals("integer") || colTypes.get(index).equals("auto_integer"))
			{
				try
				{
					int num = Integer.parseInt(val);
					parameter = num;
				}
				catch(NumberFormatException ex)
				{
					return new SimpleResponse(false, "Expected, but did not find, type Integer for comparison", null);
				}
			}
			else if(colTypes.get(index).equals("boolean"))
			{
				if(val.toUpperCase().equals("TRUE"))
					parameter = true;
				else if(val.toUpperCase().equals("FALSE"))
					parameter = false;
				else
					return new SimpleResponse(false, "Expected, but did not find, type boolean for comparison",null);
			}
			else if(colTypes.get(index).equals("string"))
			{
				if(val.indexOf("\"") >= 0)
				{
					val = val.substring(val.indexOf("\"")+1);
					if(val.indexOf("\"") >= 0)
					{
						parameter = val.substring(0, val.indexOf("\""));
					}
					else
						return new SimpleResponse(false, "Expected, but did not find, type String for comparison", null);
				}
				else
					return new SimpleResponse(false, "Expected, but did not find, type String for comparison", null);
			}
			else
				return new SimpleResponse(false, "The type of given column is not one of the supported types", null);
		}
		
		Table delete = new VolatileTable(
				"_delete",
				table.getColumnNames(),
				table.getColumnTypes(),
				table.getPrimaryIndex()
				);
		
		List<List> values = new LinkedList<List>();
		List<Object> removeKeys = new LinkedList<Object>();
		
		for(Object rowKey : keys)
		{
			List<Object> row = table.getState().get(rowKey);
			if(optional)
			{
				try
				{
					if(where(row.get(index), condition, parameter, colTypes.get(index)))
					{
						values.add(row);
						removeKeys.add(rowKey);
					}
				}
				catch(IllegalArgumentException ex)
				{
				}
			}
			else
			{
				values.add(row);
				removeKeys.add(rowKey);
			}
		}
		
		for(int i = 0; i < removeKeys.size(); i++)
		{
			table.getState().remove(removeKeys.get(i));
		}
		
		for(int i = 0; i < values.size(); i++)
		{
			delete.getState().put(values.get(i).get(table.getPrimaryIndex()),values.get(i));
		}
		
		return new SimpleResponse(true, "Successfully deleted " + delete.getState().size() + " rows from table \"" + tableName + "\"", delete);
	}
	
	private boolean where(Object left, String condition, Object right, String type)
	{
		type = type.toUpperCase();
		if(type.equals("AUTO_INTEGER")) 
			type = "INTEGER";
		
		if(left == null && right == null && condition != "<>")
			return false;
		else if(left == null && right == null)
			return true;
		else if((left == null || right == null) && !type.equals("BOOLEAN") && !condition.equals("<>"))
			return false;
		else if((left == null || right == null) && !type.equals("BOOLEAN") && condition.equals("<>"))
			return true;
		else if((left == null || right == null) && type.equals("BOOLEAN") && condition.equals("="))
			return false;
		else if((left == null || right == null) && type.equals("BOOLEAN") && condition.equals("<>"))
			return true;
		else if((left == null || right == null) && type.equals("BOOLEAN"))
			throw new IllegalArgumentException();
		else if(type.equals("INTEGER"))
		{
			if(!(right instanceof Integer))
				throw new IllegalArgumentException();
			
			Integer leftI = (int)left;
			Integer rightI = (int)right;
			
			if(condition.equals("="))
				return leftI == rightI;
			else if(condition.equals("<>"))
				return leftI != rightI;
			else if(condition.equals("<"))
				return leftI < rightI;
			else if(condition.equals("<="))
				return leftI <= rightI;
			else if(condition.equals(">"))
				return leftI > rightI;
			else if(condition.equals(">="))
				return leftI >= rightI;
		}
		else if(type.equals("BOOLEAN"))
		{
			if(!(right instanceof Boolean))
				throw new IllegalArgumentException();
			
			Boolean leftB = (boolean)left;
			Boolean rightB = (boolean)right;
			
			if(condition.equals("="))
				return leftB == rightB;
			else if(condition.equals("<>"))
				return leftB != rightB;
			else
				throw new IllegalArgumentException();
		}
		else if(type.equals("STRING"))
		{
			if(!(right instanceof String))
				throw new IllegalArgumentException();
			
			String leftS = (String)left;
			String rightS = (String)right;
			
			if(condition.equals("="))
				return leftS.equals(rightS);
			else if(condition.equals("<>"))
				return !leftS.equals(rightS);
			else if(condition.equals("<"))
				return leftS.compareTo(rightS) < 0;
			else if(condition.equals("<="))
				return leftS.compareTo(rightS) <= 0;
			else if(condition.equals(">"))
				return leftS.compareTo(rightS) > 0;
			else if(condition.equals(">="))
				return leftS.compareTo(rightS) >= 0;	
		}
		throw new IllegalArgumentException();
	}

}
