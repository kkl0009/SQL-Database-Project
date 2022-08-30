package core;


import driver.*;
import model.*;
import structure.*;

import java.util.List;
import java.util.LinkedList;
import java.io.Closeable;
import java.io.IOException;

/** 
 * This class implements a database
 * management system.
 * 
 * Additional protocols may be added.
 */
public class Database
	implements Closeable
{
	private Driver[] drivers;
	private HashMap<String, Table> tables;
	
	//private boolean transaction;

	/**
	 * Initialize the database with a set of tables
	 * and a sequence of drivers.
	 * 
	 * Do not modify the protocol.
	 */
	public Database() {
		setTables(new VolatileHashMap<>());
		//transaction = false;
		// TODO: Initialize available drivers in sequence.
		this.drivers = new Driver[]{
			new Echo(), 
			new Range(),
			new SquaresBelow(),
			new CreateTable(),
			new DropTable(),
			new ShowTables(),
			new DumpTable(),
			new InsertReplace(),
			new AlterTableAdd(),
			new AlterTableDrop(),
			new AlterTableRenameCol(),
			new AlterTableRenameTable(),
			new UpdateTable(),
			new DeleteFrom(),
			new Select(),
			new Export(),
			new Import(),
			new Unrecognized()
		};
	}
	
	/**
	 * Returns the tables of this database.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @return the tables
	 */
	public HashMap<String, Table> getTables() {
		return tables;
	}
	
	/**
	 * Assigns the tables of this database.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @param tables the tables
	 */
	public void setTables(HashMap<String, Table> tables) {
		this.tables = tables;
	}

	/**
	 * Interprets a script and returns a list of
	 * responses to each query when executed.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @param script the script
	 * @return the list of responses to each query
	 */
	public List<Response> interpret(String script) {
		String query = script;

		String[] inputs = query.split(";");
		
		List<Response> responses = new LinkedList<Response>();
		//responses.add(drivers[0].execute(query, this));
		
		for(int i = 0; i < inputs.length; i++)
		{
			for(int j = 0; j < drivers.length; j++)
			{
				Response drivResp = drivers[j].execute(inputs[i], this);
				if(drivResp != null)
				{
					responses.add(drivResp);
					break;
				}
			}
		}

		return responses;
	}
	
	
	/**
	 * Execute any required tasks when
	 * the database is closed.
	 * 
	 * Do not modify the protocol.
	 */
	@Override
	public void close() throws IOException {
		
	}
}
