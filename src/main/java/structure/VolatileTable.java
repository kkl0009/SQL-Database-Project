package structure;

import model.*;

import java.util.List;
import java.util.Map;

public class VolatileTable
	implements Table
{
	/*
	 * TODO: For Module 8, copy this class and implement
	 * the requirements to support persistent tables.
	 * 
	 * Until then, this class supports volatile tables.
	 */
	private int[] autos = {-2};
	private int[] backupAutos = {-2};
	public VolatileTable(
		String table_name,
		List<String> column_names,
		List<String> column_types,
		int primary_index
	) {
		setTableName(table_name);
		setColumnNames(column_names);
		setColumnTypes(column_types);
		setPrimaryIndex(primary_index);
		
		setState(new VolatileHashMap<>());
	}
	
	private String table_name;
	
	@Override
	public String getTableName() {
		return table_name;
	}

	@Override
	public void setTableName(String table_name) {
		this.table_name = table_name;
	}
	
	private List<String> column_names;
	
	@Override
	public List<String> getColumnNames() {
		return column_names;
	}

	@Override
	public void setColumnNames(List<String> column_names) {
		this.column_names = column_names;
	}
	
	private List<String> column_types;
	
	@Override
	public List<String> getColumnTypes() {
		return column_types;
	}

	@Override
	public void setColumnTypes(List<String> column_types) {
		this.column_types = column_types;
	}
	
	private int primary_index;
	
	@Override
	public int getPrimaryIndex() {
		return primary_index;
	}

	@Override
	public void setPrimaryIndex(int primary_index) {
		this.primary_index = primary_index;
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
	
	public int[] getAutos()
	{
		return autos;
	}
	
	public void setAutos(int[] autos)
	{
		this.autos = autos;
	}
	
	public int[] getBackup()
	{
		return backupAutos;
	}
	
	public void setBackup(int[] backupAutos)
	{
		this.backupAutos = backupAutos;
	}
}