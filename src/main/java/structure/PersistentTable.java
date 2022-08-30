package structure;

import model.*;

import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PersistentTable
	implements Table
{
	/*
	 * TODO: For Module 8, copy this class and implement
	 * the requirements to support persistent tables.
	 * 
	 * Until then, this class supports volatile tables.
	 */

	private Page storage;
	private final List<String> types = Arrays.asList("string", "string");
	
	public PersistentTable(
		String table_name,
		List<String> column_names,
		List<String> column_types,
		int primary_index
	) {
		storage = new SimplePage(Paths.get("data", "data.txt"), types, 0);
		storage.length(1 + column_names.size());
		storage.write(0, Arrays.asList(table_name, "" + primary_index));
		storage.size(1);
		for(int i = 0; i < column_names.size(); i++)
		{
			storage.write(i, Arrays.asList("", ""));
		}
		
		setTableName(table_name);
		setColumnNames(column_names);
		setColumnTypes(column_types);
		setPrimaryIndex(primary_index);
		
		setState(new VolatileHashMap<>());
	}
	
	private String table_name;
	
	@Override
	public String getTableName() 
	{
		return (String)storage.read(0).get(0);	
	}

	@Override
	public void setTableName(String table_name) 
	{
		List<Object> record = storage.read(0);
		record.set(0, table_name);
		storage.write(0, record);
		//this.table_name = table_name;
	}
	
	private List<String> column_names;
	
	@Override
	public List<String> getColumnNames() 
	{
		List<String> colNames = new LinkedList<>();
		int i = 1;
		while(storage.isRecord(i))
		{
			colNames.add((String)storage.read(i).get(0));
		}
		
		return colNames;
	}

	@Override
	public void setColumnNames(List<String> column_names) 
	{
		for(int i = 0; i < column_names.size(); i++)
		{
			List<Object> record = storage.read(i + 1);
			record.set(0, column_names.get(i));
			storage.write(i + 1, record);
		}
		//this.column_names = column_names;
	}
	
	private List<String> column_types;
	
	@Override
	public List<String> getColumnTypes() 
	{
		List<String> colTypes = new LinkedList<>();
		int i = 1;
		while(storage.isRecord(i))
		{
			colTypes.add((String)storage.read(i).get(1));
		}
		
		return colTypes;
	}

	@Override
	public void setColumnTypes(List<String> column_types) 
	{
		for(int i = 0; i < column_types.size(); i++)
		{
			List<Object> record = storage.read(i + 1);
			record.set(1, column_types.get(i));
			storage.write(i + 1, record);
		}
		//this.column_types = column_types;
	}
	
	private int primary_index;
	
	@Override
	public int getPrimaryIndex() 
	{
		return Integer.parseInt((String)storage.read(0).get(1));
	}

	@Override
	public void setPrimaryIndex(int primary_index) 
	{
		List<Object> record = storage.read(0);
		record.set(1, primary_index);
		storage.write(0, record);
		//this.primary_index = primary_index;
	}

	private Map<Object, List<Object>> state;
	
	@Override
	public Map<Object, List<Object>> getState() {
		return state;
	}

	@Override
	public void setState(Map<Object, List<Object>> state) {
		this.state = state;
	}
	
	@Override
	public String toString() {
		return String.format(
			"<state=%s, schema={table_name=%s, column_names=%s, column_types=%s, primary_index=%s}>",
			getState().toString(),
			getTableName(),
			getColumnNames(),
			getColumnTypes(),
			getPrimaryIndex()
		);
	}

	@Override
	public int[] getAutos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setAutos(int[] autos) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int[] getBackup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBackup(int[] backupAutos) {
		// TODO Auto-generated method stub
		
	}
}