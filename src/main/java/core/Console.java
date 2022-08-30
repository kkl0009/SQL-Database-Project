package core;
//create table table1 (col1 string primary, col2 integer, col3 boolean); insert into table1 values ("ABC", -1231, false), ("DEF", 321331, true), ("GHI", 0, null) , ("JKL", 23, false), ("MNO", null, false); create table table2 (col1 boolean, col2 integer, col3 string, col4 integer primary); insert into table2 values (null, null, null, 0), (true, 1, "reallyreallylongstring", 1), (false, 1, "TEST", 2), (true, null, "normal", 3), (false, 3, "str", 4) , (true, 4323442, "word", 5); create table table3 (col1_is_longer integer, col2 string primary, col3 string); insert into table3 values (123, "A", "Z"), (456, "B", null), (789, "C", "Y"), (101112, "D", "X"), (null, "E", "W"); dump table table1; dump table table2; dump table table3

import model.*;
import structure.VolatileHashMap;
import structure.VolatileTable;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import java.io.PrintStream;
import java.io.IOException;

/*
 * KNOWN ISSUE WITH PROGRAM WHERE THE ROWS ARE NOT CORRECTLY INSERTED INTO THE TABLES (EVERY OTHER ROW FOR SOME REASON)
 */


/** 
 * This class implements a user console for
 * interacting with a database.
 * 
 * Additional protocols may be added.
 */
public class Console {
	/**
	 * This is the entry point for execution
	 * with user input/output.
	 * 
	 * Do not modify the protocol.
	 */
	public static void main(String[] args) {
		boolean quit = false;
		boolean transaction = false;
		try (
			final Database db = new Database();
			final TransactionDB transDB = new TransactionDB();
			final Scanner in = new Scanner(System.in);
			final PrintStream out = System.out;
		) 
		{
			while (!quit)
			{
				out.print(">> "); // TODO: known issue with IntelliJ IDEA
				String text = in.nextLine();
			
				String[] inputs = text.split("; ");
			
				List<Response> responses = null;
				List<Response> tranResponses = null;
			
				for(String str : inputs)
				{
					if(str.strip().toUpperCase().equals("EXIT"))
					{
						quit = true;
						break;
					}
					else if(str.strip().equalsIgnoreCase("BEGIN") || str.strip().equalsIgnoreCase("BEGIN;"))
					{
						if(transaction)
						{
							out.println("Success: false");
							out.println("Message: You are already in a transaction");
							break;
						}
						
						
						transaction = true;
						
						HashMap<String, Table> temp = new VolatileHashMap<>();
						
						for(String name : db.getTables().keySet())
						{
							Table thisTable = db.getTables().get(name);
							Table newTable = new VolatileTable(thisTable.getTableName(), thisTable.getColumnNames(), thisTable.getColumnTypes(), thisTable.getPrimaryIndex());
							for(Object key : thisTable.getState().keySet())
							{
								List<Object> newRow = new LinkedList<>(thisTable.getState().get(key));
								newTable.getState().put(key, newRow);
							}
							temp.put(name, newTable);
						}
						
						transDB.setTables(temp);
			
						out.println("Success: true");
						out.println("Message: Started a new transaction");
						break;
					}
					else if(str.strip().equalsIgnoreCase("COMMIT") || str.strip().equalsIgnoreCase("COMMIT;"))
					{
						if(!transaction)
						{
							out.println("Success: false");
							out.println("Message: You are not in a transaction");
							break;
						}
						
						
						db.setTables(new VolatileHashMap<>(transDB.getTables()));
						transDB.setTables(null);
						
						
						transaction = false;
						out.println("Success: true");
						out.println("Message: Committed all changes to the database");
						break;
					}
					else if(str.strip().equalsIgnoreCase("ROLLBACK") || str.strip().equalsIgnoreCase("ROLLBACK;"))
					{
						if(!transaction)
						{
							out.println("Success: false");
							out.println("Message: You are not in a transaction");
							break;
						}
						
						transDB.setTables(null);
						
						transaction = false;
						out.println("Success: true");
						out.println("Message: All transaction changes were discarded");
						break;
					}
					
					if(!transaction)
						responses = db.interpret(str);
					else
						tranResponses = transDB.interpret(str);
					
					
					
					if(!transaction)
					{
						out.println("Success: " + responses.get(0).getSuccess());
						out.println("Message: " + responses.get(0).getMessage());
					}
					else
					{
						out.println("Success: " + tranResponses.get(0).getSuccess());
						out.println("Message: " + tranResponses.get(0).getMessage());
					}
					
					// TODO: For Module 4, pretty-print the table.
					
		//THIS MUST BE UPDATED TO WORK WITH TRANSACTIONS AS WELL		
					
					final int MAX_STRING = 16;
					final int MAX_INT = 12;
					final int MAX_BOOLEAN = 10;
					
					Table table = null;
					boolean tableCreate = false;
					
					if(!transaction)
					{
						if(responses.get(0).getTable() != null)
						{				
							table = responses.get(0).getTable();
							tableCreate = true;
						}
					}
					else
					{
						if(tranResponses.get(0).getTable() != null)
						{
							table = tranResponses.get(0).getTable();
							tableCreate = true;
						}
					}
					if(tableCreate)
					{
						out.println();
						out.println("[ " + table.getTableName() + " ]");
						//List<String> newColNames = new LinkedList<String>();
						
						int[] colLengths = new int[table.getColumnNames().size()];
						
						int totalLength = 0;
						for(int i = 0; i  < table.getColumnNames().size(); i++)
						{
							String type = table.getColumnTypes().get(i);
							if(type.equals("boolean"))
								totalLength += MAX_BOOLEAN + 2;
							else if(type.equals("integer"))
									totalLength += MAX_INT + 2;
							else if(type.equals("string"))
									totalLength += MAX_STRING + 2;
						}
						
						out.print("=");
						for(int i = 0; i < totalLength; i++)
							out.print("=");
						
						out.println();
						
						for(int i = 0; i < table.getColumnNames().size(); i++)
						{
							String type = table.getColumnTypes().get(i);
							String colName = table.getColumnNames().get(i);
							int colLength = 0;
							if(type.equals("boolean"))
								colLength = MAX_BOOLEAN;
							else if(type.equals("integer"))
								colLength = MAX_INT;
							else if(type.equals("string"))
								colLength = MAX_STRING;
							
							if(colName.length() > colLength - 2)
							{
								colName = colName.substring(0, 8);
								colName = colName + "...";
							}
							if(table.getPrimaryIndex() == i)
								colName += "*";
							//create table yeet (col1 integer primary, col2 string, col3 boolean)
							
							out.print("| ");
							if(!type.equals("integer"))
								out.printf("%-"+ colLength + "s", colName + " ");
							else
								out.printf("%"+ colLength + "s", colName + " ");
							
							colLengths[i] = colLength;
						}
						out.print("|");
						
						out.println();
						
						out.print("-");
						for(int i = 0; i < totalLength; i++)
							out.print("-");
						
						
						Set<Object> keys = table.getState().keySet();
						for(Object key : keys)
						{
							List<Object> row = table.getState().get(key);
							out.println();
							for(int i = 0; i < row.size(); i++)
							{
								out.print("| ");
								if(row.get(i) == null)
								{
									out.printf("%-"+ colLengths[i] + "s", " ");
								}
								else
								{
									String type = table.getColumnTypes().get(i);
									if(type.equals("string") && ((String)row.get(i)).length() > colLengths[i])
									{
										String temp = ((String)row.get(i)).substring(0, 11);
										temp = "\"" + temp + "...";
										row.set(i, temp);
									}
									else if(type.equals("string") && ((String)row.get(i)).length() == 0)
									{
										row.set(i, "\"\"");
									}
									else if(type.equals("string") && ((String)row.get(i)).charAt(0) != '"')
									{
										String temp = (String)row.get(i);
										temp = "\"" + temp + "\"";
										row.set(i, temp);
									}
								
								
									if(row.get(i) != null)
									{
										if(!type.equals("integer"))
											out.printf("%-"+ colLengths[i] + "s", row.get(i) + " ");
										else
											out.printf("%"+ colLengths[i] + "s", row.get(i) + " ");
									}
									else
									{
										out.printf("%-"+ colLengths[i] + "s", " ");
									}
								}
							}
							out.print("|");
						}
						
						out.println();
						
						if(table.getState().size() > 0)
						{
							out.print("=");
							for(int i = 0; i < totalLength; i++)
								out.print("=");
						}
						
							out.println();	
					}
				}
							
				//responses = db.interpret(text);
				
				if(quit)
					break;
			}		
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
}