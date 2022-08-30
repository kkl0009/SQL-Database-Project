package model;

import java.util.Map;
import java.util.List;

/** 
 * Defines a table as a map
 * from field values of the primary column
 * to rows of field values,
 * plus encapsulated schema properties.
 * 
 * Additional protocols may be added.
 */
public interface Table {	
	/** 
	 * Returns the name of this table.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @return the table name
	 **/
	public String getTableName();
	
	/** 
	 * Updates the name of this table.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @param table_name the table name
	 **/
	public void setTableName(String table_name);
	
	/** 
	 * Returns the column names of this table.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @return the column names
	 **/
	public List<String> getColumnNames();
	
	/** 
	 * Updates the column names of this table.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @param column_names the new column names
	 **/
	public void setColumnNames(List<String> column_names);
	
	/** 
	 * Returns the column types of this table.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @return the column types
	 **/
	public List<String> getColumnTypes();
	
	/** 
	 * Updates the column types of this table.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @param column_types the new column types
	 **/
	public void setColumnTypes(List<String> column_types);
	
	/** 
	 * Returns the primary index of this table.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @return the primary index
	 **/
	public int getPrimaryIndex();
	
	/** 
	 * Updates the primary index of this table.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @param primary_index the new primary index
	 **/
	public void setPrimaryIndex(int primary_index);
	
	/** 
	 * Returns the state of this table.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @return the state map
	 **/
	public Map<Object, List<Object>> getState();
	
	/** 
	 * Assigns the state of this table.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @param state the new state
	 **/
	public void setState(Map<Object, List<Object>> state);
	
	public int[] getAutos();
	
	public void setAutos(int[] autos);
	
	public int[] getBackup();
	
	public void setBackup(int[] backupAutos);
}
