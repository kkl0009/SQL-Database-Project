package driver;

/*
 * Class written by Kollin Labowski for Module 3 of CS 210
 * Completed on 2/28/2020
 */
import java.util.HashSet;
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

public class Select 
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//SELECT\s+(?:(\*|(?:[a-zA-Z][a-zA-Z0-9_]*(?:\s+AS\s+[a-zA-Z][a-zA-Z0-9_]*)?(?:\s*,\s*[a-zA-Z][a-zA-Z0-9_]*(?:\s+AS\s+[a-zA-Z][a-zA-Z0-9_]*)?)*)))\s+FROM\s+([a-zA-Z][a-zA-Z0-9_]*)(?:\s+JOIN\s+([a-zA-Z][a-zA-Z0-9_]*)\s+ON\s+([a-zA-Z][a-zA-Z0-9_]*)\s*=\s*([a-zA-Z][a-zA-Z0-9_]*))?(?:\s+WHERE\s+([a-zA-Z][a-zA-Z0-9_]*)\s*(=|<>|<|>|<=|>=|IS)\s*(NOT)?\s*([^; ,]+))?
			"SELECT\\s+(?:(\\*|(?:[a-zA-Z][a-zA-Z0-9_]*(?:\\s+AS\\s+[a-zA-Z][a-zA-Z0-9_]*)?(?:\\s*,\\s*[a-zA-Z][a-zA-Z0-9_]*(?:\\s+AS\\s+[a-zA-Z][a-zA-Z0-9_]*)?)*)))\\s+FROM\\s+([a-zA-Z][a-zA-Z0-9_]*)(?:\\s+JOIN\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s+ON\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s*=\\s*([a-zA-Z][a-zA-Z0-9_]*))?(?:\\s+WHERE\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s*(=|<>|<|>|<=|>=|IS)\\s*(NOT)?\\s*([^; ,]+))?",
			Pattern.CASE_INSENSITIVE
		);
	}
	
	@Override
	public Response execute(String query, Database db) 
	{
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;
		
		String colSelections = matcher.group(1);
		
		String tableName = matcher.group(2);
		
		// TODO: Check whether the next 3 strings are null
		
		String rightTableName = matcher.group(3);
		
		String leftColumn = matcher.group(4);
		
		String rightColumn = matcher.group(5);
		
		String leftCol = matcher.group(6);
		
		String condition = matcher.group(7);
		
		String not = matcher.group(8);
		
		String rightVal = matcher.group(9);
		
		if(!db.getTables().containsKey(tableName))
			return new SimpleResponse(false, "Table name \"" + tableName + "\" does not exist in the database", null);
		
		Table table = db.getTables().get(tableName);
		
		Table rightTable = db.getTables().get(rightTableName);
		
		boolean whereCase = true;
		if(leftCol == null && condition == null && rightVal == null)
			whereCase = false;
		
		String[] origCols;
		String[] aliases;
		int[] indexes;
		
		List<String> colNames = table.getColumnNames();
		List<String> colTypes = table.getColumnTypes();
		int primIndex = table.getPrimaryIndex();
		
		List<String> rightColNames = rightTable.getColumnNames();
		List<String> rightColTypes = rightTable.getColumnTypes();
		int rightPrimIndex = rightTable.getPrimaryIndex();
		boolean leftPrimary = false;
		
		if(!colNames.contains(leftColumn))
			return new SimpleResponse(false, "The column \"" + leftColumn + "\" does not exist in table \"" + tableName + "\"", null);
		if(!rightTable.getColumnNames().contains(rightColumn))
			return new SimpleResponse(false, "The column \"" + rightColumn + "\" does not exist in table \"" + rightTableName + "\"", null);
		if(leftColumn.equals(colNames.get(primIndex)))
			leftPrimary = true;
		else if(rightColumn.equals(rightColNames.get(rightPrimIndex)))
			leftPrimary = false;
		else
			return new SimpleResponse(false, "At least one of your selected columns to join must be a primary column", null);
		
		String leftColType = colTypes.get(colNames.indexOf(leftColumn));
		String rightColType = rightColTypes.get(rightColNames.indexOf(rightColumn));
		
		if(!leftColType.equals(rightColType))
			return new SimpleResponse(false, "Your columns to join the tables on must be of the same type", null);
		
		if(leftCol != null)
		{
			if(!colNames.contains(leftCol.strip()))
				return new SimpleResponse(false, "The column \"" + leftCol + "\" does not exist in table \"" + tableName + "\"", null);
		}
		
		if(!colSelections.strip().equals("*"))
		{
			String[] commaSplit = colSelections.split(",");
			for(int i = 0; i < commaSplit.length; i++)
				commaSplit[i] = commaSplit[i].strip();
		
			origCols = new String[commaSplit.length];
			aliases = new String[origCols.length];
			indexes = new int[aliases.length];
			
			Set<String> dupNames = new HashSet<String>();
		
			boolean primFound = false;
			
			for(int i = 0; i < commaSplit.length; i++)
			{
				if(commaSplit[i].indexOf(" AS ") >= 0)
				{
					String[] temp = commaSplit[i].split(" AS ");
					origCols[i] = temp[0].strip();
					aliases[i] = temp[1].strip();
				}
				else if(commaSplit[i].indexOf(" as ") >= 0)
				{
					String[] temp = commaSplit[i].split(" as ");
					origCols[i] = temp[0].strip();
					aliases[i] = temp[1].strip();
				}
				else if(commaSplit[i].indexOf(" As ") >= 0)
				{
					String[] temp = commaSplit[i].split(" As ");
					origCols[i] = temp[0].strip();
					aliases[i] = temp[1].strip();
				}
				else if(commaSplit[i].indexOf(" aS ") >= 0)
				{
					String[] temp = commaSplit[i].split(" aS ");
					origCols[i] = temp[0].strip();
					aliases[i] = temp[1].strip();
				}
				else
				{
					origCols[i] = commaSplit[i].strip();
					aliases[i] = origCols[i];
				}
				
				indexes[i] = colNames.indexOf(origCols[i]);
				
				if(dupNames.contains(aliases[i]))
					return new SimpleResponse(false, "You cannot have multiple columns under the same name", null);
				dupNames.add(aliases[i]);
				
				if(!colNames.contains(origCols[i]))
					return new SimpleResponse(false, "The column \"" + origCols[i] + "\" does not exist in table \"" + tableName + "\"", null);
				
				if(colNames.indexOf(origCols[i]) == table.getPrimaryIndex() && !primFound)
				{
					primFound = true;
					primIndex = i;
				}
			}
			if(!primFound)
				return new SimpleResponse(false, "One of your selected columns must be the primary column", null);
		}
		else
		{
			origCols = new String[colNames.size()];
			aliases = new String[origCols.length];
			indexes = new int[aliases.length];
			for(int i = 0; i < colNames.size(); i++)
			{
				origCols[i] = colNames.get(i);
				aliases[i] = colNames.get(i);
				indexes[i] = i;
			}
		}
		
		List<String> selectedCols = new LinkedList<String>();
		for(int i = 0; i < aliases.length; i++)
		{
			selectedCols.add(aliases[i]);
		}
		
		List<String> selectedTypes = new LinkedList<String>();
		for(int i = 0; i < origCols.length; i++)
		{
			selectedTypes.add(colTypes.get(colNames.indexOf(origCols[i])));
		}
		
		Table select = new VolatileTable(
				"_select",
				selectedCols,
				selectedTypes,
				primIndex
				);
				
		Object parameter = null;
		int index = colNames.indexOf(leftCol);
		if(whereCase)
		{
			if(rightVal.equals("null"))
				parameter = null;
			else if(colTypes.get(index).equals("integer") || colTypes.get(index).equals("auto_integer"))
			{
				try
				{
					//System.out.println(rightVal);
					int num = Integer.parseInt(rightVal);
					parameter = num;
				}
				catch(NumberFormatException ex)
				{
					return new SimpleResponse(false, "Expected, but did not find, type Integer for comparison", null);
				}
			}
			else if(colTypes.get(index).equals("boolean"))
			{
				if(rightVal.toUpperCase().equals("TRUE"))
					parameter = true;
				else if(rightVal.toUpperCase().equals("FALSE"))
					parameter = false;
				else
					return new SimpleResponse(false, "Expected, but did not find, type boolean for comparison",null);
			}
			else if(colTypes.get(index).equals("string"))
			{
				if(rightVal.indexOf("\"") >= 0)
				{
					rightVal = rightVal.substring(rightVal.indexOf("\"")+1);
					if(rightVal.indexOf("\"") >= 0)
					{
						parameter = rightVal.substring(0, rightVal.indexOf("\""));
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
		
		List<List> rowsToAdd = new LinkedList<List>();
		if(whereCase)
		{
			Set<Object> keys = table.getState().keySet();
			for(Object rowKey : keys)
			{
				try
				{
					List<Object> temp = table.getState().get(rowKey);
					List<Object> adding = new LinkedList<Object>();
					//System.out.println(where(temp.get(index), condition, parameter, colTypes.get(index)));
					if(where(temp.get(index), condition, parameter, colTypes.get(index), not))
					{
						//for(int i = 0; i < origCols.length; i++)
							//adding.add(null);
						for(int i = 0; i < origCols.length; i++)
						{
							adding.add(i, temp.get(indexes[i]));//FLAG
						}
						rowsToAdd.add(adding);
					}
				}
				catch(IllegalArgumentException ex)
				{	
					return new SimpleResponse(false, "The arguments passed into the WHERE statement were invalid", null);
				}
			}
		}
		else
		{
			Set<Object> keys = table.getState().keySet();
			for(Object rowKey : keys)
			{
				List<Object> temp = table.getState().get(rowKey);
				List<Object> adding = new LinkedList<Object>();
				//for(int i = 0; i < origCols.length; i++)
					//adding.add(null);
				for(int i = 0; i < origCols.length; i++)
				{
					adding.add(i, temp.get(indexes[i]));//FLAG
				}
				//for(int i = 0; i < adding.size(); i++)
				//{
					//if(adding.get(i) == null)
						//adding.remove(null);
				//}
				rowsToAdd.add(adding);
				//System.out.println(rowsToAdd);
			}
		}
		
		for(int i = 0; i < rowsToAdd.size(); i++)
		{
			select.getState().put(rowsToAdd.get(i).get(primIndex), rowsToAdd.get(i));
		}
		
		db.getTables().put("_select", select);
		
		return new SimpleResponse(true, "Now displayed selected columns as table \"_select\"", select);
	}
	
	private boolean where(Object left, String condition, Object right, String type, String not)
	{
		type = type.toUpperCase();
		if(type.equals("AUTO_INTEGER")) 
			type = "INTEGER";
		
		if(not != null && !condition.toUpperCase().equals("IS"))
			throw new IllegalArgumentException();
		else if(condition.toUpperCase().equals("IS") && not == null)
		{
			if(right == null)
				return left == null;
			else
				throw new IllegalArgumentException();
		}
		else if(condition.toUpperCase().equals("IS") && not != null)
		{
			if(right == null)
				return left != null;
			else
				throw new IllegalArgumentException();
		}
		else if(left == null && right == null && !condition.equals("<>"))
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
			else
				throw new IllegalArgumentException();
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
			else
				throw new IllegalArgumentException();
		}
		throw new IllegalArgumentException();
	}

}
