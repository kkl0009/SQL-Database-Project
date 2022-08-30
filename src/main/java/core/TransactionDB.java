package core;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

import driver.*;
import model.Driver;
import model.HashMap;
import model.Response;
import model.Table;
import structure.VolatileHashMap;

public class TransactionDB extends Database
{
	private Driver[] drivers;
	private HashMap<String, Table> tables;
	
	public TransactionDB() {
		setTables(new VolatileHashMap<>());
		
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
	

	public HashMap<String, Table> getTables() {
		return tables;
	}
	

	public void setTables(HashMap<String, Table> tables) {
		this.tables = tables;
	}

	
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
