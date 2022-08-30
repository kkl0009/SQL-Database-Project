package driver;

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

public class UpdateTable 
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//UPDATE\s+([a-zA-Z][a-zA-Z0-9_]*)\s+SET\s+((?:[a-zA-Z][a-zA-Z0-9_]*\s+=\s+[^, ;]+)(?:\s*,\s*[a-zA-Z][a-zA-Z0-9_]*\s+=\s+[^, ;]+)*)(?:\s+WHERE\s+([a-zA-Z][a-zA-Z0-9_]*)\s+((?:=|<>|<|<=|>|>=))\s+([^, ;]+))?
			"UPDATE\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s+SET\\s+((?:[a-zA-Z][a-zA-Z0-9_]*\\s+=\\s+[^, ;]+)(?:\\s*,\\s*[a-zA-Z][a-zA-Z0-9_]*\\s+=\\s+[^, ;]+)*)(?:\\s+WHERE\\s+([a-zA-Z][a-zA-Z0-9_]*)\\s+((?:=|<>|<|<=|>|>=))\\s+([^, ;]+))?",
			Pattern.CASE_INSENSITIVE
		);
	}
	
	@Override
	public Response execute(String query, Database db) 
	{
		// TODO: Add a case which disallows the primary columns to be changed to have duplicate values
		//create table test (col1 string primary, col2 integer, col3 boolean, col4 integer, col5 string); insert into test values ("ABC", 5, true, -3, "XYZ"), ("CBA", -5, false, 3, "ZYX"), ("yeet", 43, null, null, "oof"); update test set col1 = "SUCC", col2 = -69, col3 = true, col4 = 420, col5 = "CESS" where col4 > 0 
		// TODO: Also, actually store the values in colMutations as their actual data types, not as strings
		
		
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;
		
		
		String tableName = matcher.group(1);
		
		String mutations = matcher.group(2);
		String[] mutArr = mutations.split(",");
		List<String[]> colMutations = new LinkedList<String[]>();
		for(int i = 0; i < mutArr.length; i++)
		{
			colMutations.add(mutArr[i].split("="));
			colMutations.get(i)[0] = colMutations.get(i)[0].strip();
			colMutations.get(i)[1] = colMutations.get(i)[1].strip();
		}
		
		String colParam = matcher.group(3);
		
		String condition = matcher.group(4);
		
		String valParam = matcher.group(5);
		
		boolean optional = true;
		if(colParam == null && condition == null && valParam == null)
			optional = false;
		
		Table table = db.getTables().get(tableName);
		List<String> colNames = table.getColumnNames();
		List<String> colTypes = table.getColumnTypes();
		//List<Object> updatedRows = new LinkedList<Object>();
		int index = colNames.indexOf(colParam);
		Set<Object> keys = table.getState().keySet();
		Object parameter = null;
		List<Integer> indexes = new LinkedList<Integer>();
		
		int[] autos = table.getAutos();
		
		/*if(autos)
		autos = new int[colTypes.size()];
		for(int i = 0; i < autos.length; i++)
			autos[i] = 0;*/
		
		int[] autoBackup;
		if(table.getBackup()[0] == -2)
		{
			autoBackup = new int[autos.length];
			for(int i = 0; i < autos.length; i++)
				autoBackup[i] = 0;
		}
		else
		{
			autoBackup = table.getBackup();
		}
		
		
		
		/*for(int i = 0; i < autos.length; i++)
			System.out.println(autos[i] + " " + autoBackup[i]);*/
		
		if(optional)
		{
			if((valParam.equals("null")) /*&& !colTypes.get(index).equals("auto_integer")*/)
				parameter = null;
			/*else if((valParam.equals("null")) && colTypes.get(index).equals("auto_integer"))
			{
				parameter = autos[index];
				autos[index]++;
				//System.out.println("DEBUG");
			}*/
			else if(colTypes.get(index).equals("integer") || colTypes.get(index).equals("auto_integer"))
			{
				try
				{
					int num = Integer.parseInt(valParam);
					/*if(num >= autos[index])
					{
						autoBackup[index] = autos[index]; //Note
						autos[index] = num + 1;
					}*/
					parameter = num;
				}
				catch(NumberFormatException ex)
				{
					return new SimpleResponse(false, "Expected, but did not find, type Integer for comparison", null);
				}
			}
			else if(colTypes.get(index).equals("boolean"))
			{
				if(valParam.toUpperCase().equals("TRUE"))
					parameter = true;
				else if(valParam.toUpperCase().equals("FALSE"))
					parameter = false;
				else
					return new SimpleResponse(false, "Expected, but did not find, type boolean for comparison",null);
			}
			else if(colTypes.get(index).equals("string"))
			{
				if(valParam.indexOf("\"") >= 0)
				{
					valParam = valParam.substring(valParam.indexOf("\"")+1);
					if(valParam.indexOf("\"") >= 0)
					{
						parameter = valParam.substring(0, valParam.indexOf("\""));
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
		
		// TODO: Solution will involve creating a new list with data from colMutations to be used later
		
		List<Object> newList = new LinkedList<Object>();
		
		for(int i = 0; i < colMutations.size(); i++)
		{
			newList.add(i, colMutations.get(i)[1]);
		}
		
		
		
// ABOVE IS WHERE THE ERROR IS EXPECTED TO OCCUR
		
		for(int k = 0; k < newList.size(); k++)
		{
			int inIndex = colNames.indexOf(colMutations.get(k)[0]);
			
			if((colMutations.get(k)[1].toUpperCase().equals("NULL"))/* && !colTypes.get(inIndex).equals("auto_integer")*/)
				newList.set(k, null);
			/*else if((colMutations.get(k)[1].toUpperCase().equals("NULL")) && colTypes.get(inIndex).equals("auto_integer"))
			{	
				//newList.set(k, autoBackup[inIndex]);
				//System.out.println(newList.get(k));
				//int temp = autos[inIndex];
				if(table.getState().get(colNames.get(inIndex)).get(inIndex) != null)
				{
					System.out.println("DEBUG");
					if((int)table.getState().get(colNames.get(inIndex)).get(inIndex) + 1 == autos[inIndex])
					{
						autos[inIndex] = autoBackup[inIndex];
						autoBackup[inIndex] = 0;
					}
				}
				//System.out.println(autos[0] + " " + autos[1] + " " + autos[2]);
				newList.set(k, null);
			}*/
			else if(colTypes.get(inIndex).equals("integer") || colTypes.get(inIndex).equals("auto_integer"))
			{
				try
				{
					int num = Integer.parseInt(colMutations.get(k)[1]);
					/*if(num >= autos[inIndex] && colTypes.get(inIndex).equals("auto_integer"))
					{
						autoBackup[inIndex] = autos[inIndex]; //Note
						autos[inIndex] = num + 1;
						//System.out.println(autos[inIndex] + " " + autoBackup[inIndex]);
					}*/
					newList.set(k, num);
				}
				catch(NumberFormatException ex)
				{
					return new SimpleResponse(false, "Expected, but did not find, type Integer for comparison", null);
				}
			}
			else if(colTypes.get(inIndex).equals("boolean"))
			{
				if(colMutations.get(k)[1].toUpperCase().equals("TRUE"))
					newList.set(k, true);
				else if(colMutations.get(k)[1].toUpperCase().equals("FALSE"))
					newList.set(k, false);
				else
					return new SimpleResponse(false, "Expected, but did not find, type boolean for comparison",null);
			}
			else if(colTypes.get(inIndex).equals("string"))
			{
				if(colMutations.get(k)[1].indexOf("\"") >= 0)
				{
					colMutations.get(k)[1] = colMutations.get(k)[1].substring(colMutations.get(k)[1].indexOf("\"")+1);
					if(colMutations.get(k)[1].indexOf("\"") >= 0)
					{
						newList.set(k, colMutations.get(k)[1].substring(0, colMutations.get(k)[1].indexOf("\"")));
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
		
		/*for(int i = 0; i < autos.length; i++)
			System.out.println(autos[i] + " " + autoBackup[i]);*/
			
		for(int i = 0; i < colMutations.size(); i++)
		{
			indexes.add(colNames.indexOf(colMutations.get(i)[0]));
		}
		
		Table update = new VolatileTable(
				"_update",
				table.getColumnNames(),
				table.getColumnTypes(),
				table.getPrimaryIndex()
				);
		
		List<List> values = new LinkedList<List>();
		List<Object> removeKeys = new LinkedList<Object>();
		
		//System.out.println(colMutations.get(1)[1]);
		//System.out.println(parameter);
		
		int[] autoAdd = new int[colTypes.size()];
		/*for(int i = 0; i < autoAdd.length; i++)
			autoAdd[i] = autos[i];*/
		
		Set<Object> duplicatePrims = new HashSet<Object>();
		
		for(Object rowKey : keys)
		{
			List<Object> row = table.getState().get(rowKey);
			//System.out.println(row.get(index));
			if(optional)
			{
				try
				{
					if(where(row.get(index), condition, parameter))
					{
						for(int i = 0; i < indexes.size(); i++)
						{
							if(indexes.get(i) == table.getPrimaryIndex())
							{
								if(duplicatePrims.contains(newList.get(i)))
								{
									return new SimpleResponse(false, "The primary column must not contain duplicate values", null);
								}
								else
								{
									duplicatePrims.add(newList.get(i));
								}
								if(newList.get(i) == null && !colTypes.get(indexes.get(i)).equals("auto_integer"))
									return new SimpleResponse(false, "Values in the primary column cannot equal null", null);
							}
							//if(!colTypes.get(indexes.get(i)).equals("auto_integer"))
							row.set(indexes.get(i), newList.get(i));
							/*else if(colTypes.get(indexes.get(i)).equals("auto_integer") && newList.get(i) == null)
							{
								//int bonus = autoAdd[indexes.get(i)] - autos[indexes.get(i)];
								row.set(indexes.get(i), autoBackup[indexes.get(i)]);
								autoBackup[indexes.get(i)]++;
								//System.out.println(autoAdd[0] + " " + autoAdd[1] + " " + autoAdd[2]);
							}*/
							
						}
						values.add(row);
						removeKeys.add(rowKey);

					}
				}
				catch(IllegalArgumentException ex)
				{
					//System.out.println("DEBUG");
				}
			}
			else
			{
				for(int i = 0; i < indexes.size(); i++)
				{
					row.set(indexes.get(i), newList.get(i));
				}
				//Object[] obj = new Object[2];
				//obj[0] = row.get(table.getPrimaryIndex());
				//obj[1] = row;
				values.add(row);
				removeKeys.add(rowKey);
				//table.getState().put(row.get(table.getPrimaryIndex()), row);
				//update.getState().put(row.get(table.getPrimaryIndex()), row);
			}
		}
		
		for(int i = 0; i < removeKeys.size(); i++)
		{
			table.getState().remove(removeKeys.get(i));
		}
		
		
		for(Object rowKey : keys)
		{
			List<Object> tempo = table.getState().get(rowKey);
			for(int i = 0; i < tempo.size(); i++)
			{
				if(colTypes.get(i).equals("auto_integer"))
				{
					if((int)tempo.get(i) >= autos[i])
					{
						//System.out.println(autos[i]);
						autos[i] = (int)tempo.get(i) + 1;
						//removal = i;
						//System.out.println(autos[i]);
					}
				}
			}
		}
		
		
		for(int i = 0; i < values.size(); i++)
		{
			for(int j = 0; j < values.get(i).size(); j++)
			{
				if(colTypes.get(j).equals("auto_integer") && values.get(i).get(j) == null)
				{
					List<Object> tempList = values.get(i);
					tempList.set(j, autos[j]);
					autos[j]++;
					values.set(i, tempList);				
				}
			}
		}
		
		table.setAutos(autos);
		table.setBackup(autoBackup);
		
		for(int i = 0; i < values.size(); i++)
		{
			table.getState().put(values.get(i).get(table.getPrimaryIndex()),values.get(i));
			update.getState().put(values.get(i).get(table.getPrimaryIndex()),values.get(i));
		}
		
		return new SimpleResponse(true, "Successfully updated table \"" + tableName + "\"", update);		
	}
	
	private boolean where(Object left, String condition, Object right)
	{		
		if(left == null && right == null && condition != "<>")
			return false;
		else if(left == null && right == null)
			return true;
		else if((left == null || right == null) && !(right instanceof Boolean) && !condition.equals("<>"))
			return false;
		else if((left == null || right == null) && !(right instanceof Boolean) && condition.equals("<>"))
			return true;
		else if((left == null || right == null) && right instanceof Boolean && condition.equals("="))
			return false;
		else if((left == null || right == null) && right instanceof Boolean && condition.equals("<>"))
			return true;
		else if((left == null || right == null) && right instanceof Boolean)
			throw new IllegalArgumentException();
		else if(right instanceof Integer)
		{		
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
		else if(right instanceof Boolean)
		{		
			Boolean leftB = (boolean)left;
			Boolean rightB = (boolean)right;
			
			if(condition.equals("="))
				return leftB == rightB;
			else if(condition.equals("<>"))
				return leftB != rightB;
			else
				throw new IllegalArgumentException();
		}
		else if(right instanceof String)
		{		
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
