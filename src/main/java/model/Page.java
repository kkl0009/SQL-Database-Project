package model;

import java.util.List;
import java.nio.file.Path;

/** 
 * Defines the protocols for a records page.
 * 
 * Do not modify at all.
 */
public interface Page {
	/**
	 * Returns the file path of this page, only
	 * assigned in the constructor.
	 * 
	 * @return the file path of this page
	 */
	public Path getPath();
	
	/**
	 * Returns the field types of this page, only
	 * assigned in the constructor.
	 * 
	 * @return the field types of this page
	 */
	public List<String> getFieldTypes();
	
	/**
	 * Returns the key index of this page, only
	 * assigned in the constructor.
	 * 
	 * @return the key index of this page
	 */
	public int getKeyIndex();
	
	/**
	 * Returns the length of this page. The length is
	 * the number of records allocated in the file.
	 * 
	 * @return the length of this page
	 */
	public int length();
	
	/**
	 * Updates the length of this page. The length is
	 * the number of records allocated in the file.
	 * 
	 * Do not automatically update the length during
	 * record mutations. It is only updated when this
	 * method is called manually from another class.
	 * 
	 * For all methods taking an index as a parameter,
	 * the index must between 0 and the length,
	 * exclusive of the length.
	 * 
	 * @param length the new length of this page
	 */
	public void length(int length);
	
	/**
	 * Returns the size of this page. The size is
	 * the number of records stored.
	 * 
	 * @return the size of this page
	 */
	public int size();
	
	/**
	 * Updates the size of this page. The size is
	 * the number of records stored.
	 * 
	 * Do not automatically update the size during
	 * record mutations. It is only updated when this
	 * method is called manually from another class.
	 * 
	 * @param size the new size of this page
	 * 
	 * @throws IllegalArgumentException
	 * if the size exceeds the length
	 */
	public void size(int size);

	/**
	 * Stores a record at the given index.
	 * 
	 * @param index the index of the record
	 * @param record the record to store
	 * 
	 * @throws IndexOutOfBoundsException
	 * if the index is invalid
	 * 
	 * @throws IllegalArgumentException
	 * if the record is null,
	 * if the record has a null value at the key index, or
	 * if the record size doesn't match the number of fields
	 */
	public void write(int index, List<Object> record);
	
	/**
	 * Marks the record at the given index as null.
	 * 
	 * @param index the index of the record
	 * 
	 * @throws IndexOutOfBoundsException
	 * if the index is invalid
	 */
	public void writeNull(int index);
	
	/**
	 * Marks the record at the given index as removed.
	 * 
	 * If your collision resolution technique marks
	 * removed entries with a non-null sentinel,
	 * support this operation by overriding it.
	 * 
	 * @param index the index of the record
	 * 
	 * @throws IndexOutOfBoundsException
	 * if the index is invalid
	 */
	public default void writeRemoved(int index) {
		throw new UnsupportedOperationException();
	};
	
	/**
	 * Returns the record stored at the given index.
	 * 
	 * @param index the index of the record
	 * @return the record
	 * 
	 * @throws IndexOutOfBoundsException
	 * if the index is invalid
	 * 
	 * @throws IllegalStateException if there is no record
	 */
	public List<Object> read(int index);

	/**
	 * Returns <code>true</code> if there is a
	 * record stored at the given index.
	 * 
	 * @param index the index of the record
	 * @return <code>true</code> if there is a record
	 * or <code>false</code> if there is no record
	 * 
	 * @throws IndexOutOfBoundsException
	 * if the index is invalid
	 */
	public boolean isRecord(int index);
	
	/**
	 * Returns <code>true</code> if the record at
	 * the given index is null.
	 * 
	 * @param index the index of the record
	 * @return <code>true</code> if the record is null
	 * or <code>false</code> if the record is not null
	 * 
	 * @throws IndexOutOfBoundsException
	 * if the index is invalid
	 */
	public boolean isNull(int index);
	
	/**
	 * Returns <code>true</code> if the record at
	 * the given index is removed.
	 * 
	 * If your collision resolution technique marks
	 * removed entries with a non-null sentinel,
	 * support this operation by overriding it.
	 * 
	 * @param index the index of the record
	 * @return <code>true</code> if the record is removed
	 * or <code>false</code> if the record is not removed
	 * 
	 * @throws IndexOutOfBoundsException
	 * if the index is invalid
	 */
	public default boolean isRemoved(int index) {
		throw new UnsupportedOperationException();
	};
}