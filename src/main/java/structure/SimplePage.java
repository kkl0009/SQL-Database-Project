package structure;

import model.*;

import java.util.LinkedList;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.MappedByteBuffer;

/*
 * This class was written by Kollin Labowski on May 2, 2020
 * It was created to fulfull the requirements of Module 7 for CS 210
 */


public class SimplePage
	implements Page
{
	/*
	 * TODO: For Module 7, implement the requirements.
	 * 
	 * Until then, this class is unused.
	 */
	
	private Path path;
	private List<String> field_types;
	private int key_index;
	private int length;
	private int size;
	//private FileChannel channel;
	private final int headerSize = 8;
	private final int bitMaskSize = 2;
	private int rowCount;
	private int maxRowSize;
	private final int maxStringSize = 256; //Remember it is 256 bytes for 255 max characters and additional length integer
	private final int maxIntSize = 4; //Max 32 bits (4 bytes)
	private final int booleanSize = 1; //Max 8 bits (1 byte)
	
	/*
	 * TODO Implementation Notes:
	 * May not work either because missing key index implementation
	 * or (more likely) because FileChannel (and MappedByteBuffer) are
	 * not fields, making all the reads fail
	 */
	
	public SimplePage(Path path, List<String> field_types, int key_index) 
	{
		//TODO: Determine if the length/size really should be initialized to zero
		//or if instead you will have to do more operations in the constructor.
		//Also, see how to initialize length appropriately instead of constant
		this.path = path;
		this.field_types = field_types;
		this.key_index = key_index;
		length = 10;
		size = 0;
		
		try(FileChannel channel = FileChannel.open(
				path,
				StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.READ,
				StandardOpenOption.WRITE)
				)
		{
			rowCount = 0; //User can set manually later
			
			int maxRowSize = 0;
			for(int i = 0; i < field_types.size(); i++)
			{
				if(field_types.get(i).equalsIgnoreCase("string"))
					maxRowSize += (1 + maxStringSize);
				else if(field_types.get(i).equalsIgnoreCase("integer"))
					maxRowSize += maxIntSize;
				else if(field_types.get(i).equalsIgnoreCase("boolean"))
					maxRowSize += booleanSize;
				else
					System.out.println("ERROR");
			}
			
			this.maxRowSize = maxRowSize;
			
			MappedByteBuffer buf = channel.map(
					FileChannel.MapMode.READ_WRITE,
					0,
					headerSize + rowCount*(bitMaskSize + maxRowSize) + 1//Double check whether or not there should be an additional + 4 after maxRowSize
				);
			
			length = rowCount;
			
			buf.putInt(size);
			buf.putInt(length);
			//this.channel = channel;
			
			
			/*
			 * TODO: May need to update this code eventually
			 */
			for(int i = 0; i < rowCount; i++)
			{
				int tempIndex = (headerSize + 1) + i * (bitMaskSize + maxRowSize);
				buf.putInt(tempIndex, 0);
			}
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}

	@Override
	public Path getPath() 
	{
		return path;
	}

	@Override
	public List<String> getFieldTypes() 
	{
		return field_types;
	}

	@Override
	public int getKeyIndex() 
	{
		return key_index;
	}
	
	@Override
	public int length() 
	{
		return length;
	}

	@Override
	public void length(int length) 
	{
		/* 
		 * TODO: Actually change the size of the file and move existing data accordingly,
		 * right now it only shows the field, and does not calculate from the row amount
		 * Do the same for the size(int size) method
		 */
		try(FileChannel channel = FileChannel.open(
				path,
				StandardOpenOption.CREATE,
				StandardOpenOption.READ,
				StandardOpenOption.WRITE)
				)
		{
			MappedByteBuffer buf = channel.map(
					FileChannel.MapMode.READ_WRITE,
					0, 
					headerSize + length * (bitMaskSize + maxRowSize) + 1
				);
			
			//System.out.println(length);
			
			buf.putInt(4, length);
			
			for(int i = 0; i < rowCount; i++)
			{
				int tempIndex = (headerSize + 1) + i * (bitMaskSize + maxRowSize);
				if(buf.getInt(tempIndex) == 0)
					buf.putInt(tempIndex, 0);
			}
			this.length = length;
			rowCount = length;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public int size() 
	{
		return size;
	}

	@Override
	public void size(int size) 
	{
		try(FileChannel channel = FileChannel.open(
				path,
				StandardOpenOption.CREATE,
				StandardOpenOption.READ,
				StandardOpenOption.WRITE)
				)
		{
			MappedByteBuffer buf = channel.map(
					FileChannel.MapMode.READ_WRITE,
					0, 
					headerSize
				);
			
			if(size >= length)
				throw new IllegalArgumentException();
			
			//System.out.println(length);
			
			buf.putInt(size);
			this.size = size;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void write(int index, List<Object> entry) 
	{
		//System.out.println("WRITE");
		
		int mask = 0;
		int newIndex = -1;	
		
		if(entry == null || entry.size() != field_types.size() || entry.get(key_index) == null)
			throw new IllegalArgumentException();
		
		if(index >= length)
			throw new IndexOutOfBoundsException();
		
		for(int i = 0; i < entry.size(); i++)
		{
			if(entry.get(i) != null)
				mask += Math.pow(2, entry.size() - 1 - i);
		}
		
		
		try(FileChannel channel = FileChannel.open(
				path,
				StandardOpenOption.CREATE,
				StandardOpenOption.READ,
				StandardOpenOption.WRITE)
				)
		{
			MappedByteBuffer buf = channel.map(
					FileChannel.MapMode.READ_WRITE,
					0, 
					headerSize + rowCount * (bitMaskSize + maxRowSize) + 1
				);
			
			//System.out.println("FileSize : " + (headerSize + rowCount * (bitMaskSize + maxRowSize)));
			
			newIndex = (headerSize + 1) + index * (bitMaskSize + maxRowSize);
			
			buf.putShort(newIndex, (short)mask);
			
			newIndex += bitMaskSize;
			
			//System.out.println("write: " + newIndex);
			
			for(int i = 0; i < entry.size(); i++)
			{
				if(entry.get(i) != null)
				{	
					if(field_types.get(i).equalsIgnoreCase("string"))
					{
						String strEntry = (String)entry.get(i);
						buf.put(newIndex, (byte)(strEntry.length() - 128));
						//System.out.println(strEntry.length().byteValue());
						newIndex++;
						
						byte[] chars = ((String)entry.get(i)).getBytes(StandardCharsets.UTF_8);
						for(int j = 0; j < chars.length; j++)
						{
							buf.put(newIndex + j, chars[j]);
						}
						newIndex += maxStringSize - 1; //POSSIBLE ERROR, MAY NEED TO INVESTIGATE
						//System.out.println("STRING");
					}
					else if(field_types.get(i).equalsIgnoreCase("integer"))
					{
						buf.putInt(newIndex, (int) entry.get(i));
						newIndex += maxIntSize;
						//System.out.println("INTEGER");
						//System.out.println(entry.get(i));
					}
					else if(field_types.get(i).equalsIgnoreCase("boolean"))
					{
						//System.out.println(entry.get(i));
						if((boolean)entry.get(i))
							buf.put(newIndex, (byte)1);
						else
							buf.put(newIndex, (byte)0);
						newIndex += booleanSize;
						//System.out.println("BOOLEAN");
					}
				}
				else
				{
					if(field_types.get(i).equalsIgnoreCase("string"))
					{
						newIndex += maxStringSize;
					}
					else if(field_types.get(i).equalsIgnoreCase("integer"))
					{
						newIndex += maxIntSize;
					}
					else if(field_types.get(i).equalsIgnoreCase("boolean"))
					{
						newIndex += booleanSize;
					}
				}
				//System.out.println("write: " + newIndex);
			}			
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void writeNull(int index) 
	{
		if(index >= length)
			throw new IndexOutOfBoundsException();
		
		int newIndex = (headerSize + 1) + index * (bitMaskSize + maxRowSize);
		try(FileChannel channel = FileChannel.open(
				path,
				StandardOpenOption.CREATE,
				StandardOpenOption.READ,
				StandardOpenOption.WRITE)
				)
		{
			MappedByteBuffer buf = channel.map(
					FileChannel.MapMode.READ_WRITE,
					0, 
					headerSize + rowCount * (bitMaskSize + maxRowSize) + 1
				);
			buf.putInt(newIndex, 0);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public List<Object> read(int index) 
	{
		//System.out.println("READ");
		if(index >= length)
			throw new IndexOutOfBoundsException();
			
		List<Object> readList = new LinkedList<>();	
		
		try (
				FileChannel channel = FileChannel.open(
					path,
					StandardOpenOption.READ
				)
			) 
		{
				MappedByteBuffer buf = channel.map(
					FileChannel.MapMode.READ_ONLY,
					0, 
					headerSize + rowCount * (bitMaskSize + maxRowSize) + 1
				);
				//System.out.println("FileSize : " + (headerSize + rowCount * (bitMaskSize + maxRowSize)));
						
				int newIndex = (headerSize + 1) + index * (bitMaskSize + maxRowSize);
				
				int mask = buf.getShort(newIndex);
				
				if(mask <= 0)
					throw new IllegalStateException();
				
				byte[] bitMask = new byte[field_types.size()];
				
				newIndex += bitMaskSize;
								
				int count = field_types.size() - 1;
				while(mask != 0)
				{
					if(mask % 2 == 0)
					{
						bitMask[count] = 0;
					}
					else
					{
						bitMask[count] = 1;
					}
					mask /= 2;
					count--;
				}
				
				for(int i = 0; i < field_types.size(); i++)
				{
					if(bitMask[i] == 1)
					{	
						if(field_types.get(i).equalsIgnoreCase("string"))
						{
							int strLength = buf.get(newIndex) + 128;
							newIndex++;
							byte[] chars = new byte[strLength];
							
							for(int j = 0; j < chars.length; j++)
							{
								chars[j] = buf.get(newIndex + j);
							}
							
							readList.add(new String(chars, StandardCharsets.UTF_8));
							
							newIndex += maxStringSize - 1;//POSSIBLE ERROR HERE
						}
						else if(field_types.get(i).equalsIgnoreCase("integer"))
						{
							readList.add(buf.getInt(newIndex));
							newIndex += maxIntSize;
						}
						else if(field_types.get(i).equalsIgnoreCase("boolean"))
						{
							byte bool = buf.get(newIndex);
							if(bool == 1)
								readList.add(true);
							else
								readList.add(false);
							newIndex += booleanSize;
						}
					}
					else
					{
						readList.add(null);
						if(field_types.get(i).equalsIgnoreCase("string"))
						{
							newIndex += maxStringSize;
						}
						else if(field_types.get(i).equalsIgnoreCase("integer"))
						{
							newIndex += maxIntSize;
						}
						else if(field_types.get(i).equalsIgnoreCase("boolean"))
						{
							newIndex += booleanSize;
						}
					}
					//System.out.println("read: " + newIndex);
				}
		}
		catch(IOException e)
		{
			//System.out.println("ERROR");
			e.printStackTrace();
		}
		return readList;
	}

	@Override
	public boolean isRecord(int index) 
	{
		if(index >= length)
			throw new IndexOutOfBoundsException();
		
		try(FileChannel channel = FileChannel.open(
				path,
				StandardOpenOption.CREATE,
				StandardOpenOption.READ,
				StandardOpenOption.WRITE)
				)
		{
			MappedByteBuffer buf = channel.map(
					FileChannel.MapMode.READ_WRITE,
					0, 
					headerSize + length * (bitMaskSize + maxRowSize) + 1
				);
			
			//System.out.println(length);
			
			int tempIndex = (headerSize + 1) + index * (bitMaskSize + maxRowSize);
			if(buf.getInt(tempIndex) <= 0)
				return false;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean isNull(int index) 
	{
		if(index >= length)
			throw new IndexOutOfBoundsException();
	
		try(FileChannel channel = FileChannel.open(
			path,
			StandardOpenOption.CREATE,
			StandardOpenOption.READ,
			StandardOpenOption.WRITE)
			)
		{
			MappedByteBuffer buf = channel.map(
				FileChannel.MapMode.READ_WRITE,
				0, 
				headerSize + length * (bitMaskSize + maxRowSize) + 1
			);
		
		//System.out.println(length);
		
			int tempIndex = (headerSize + 1) + index * (bitMaskSize + maxRowSize);
			if(buf.getInt(tempIndex) <= 0)
			return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
	
	public void writeRemoved(int index) 
	{
		if(index >= length)
			throw new IndexOutOfBoundsException();
		
		int newIndex = (headerSize + 1) + index * (bitMaskSize + maxRowSize);
		try(FileChannel channel = FileChannel.open(
				path,
				StandardOpenOption.CREATE,
				StandardOpenOption.READ,
				StandardOpenOption.WRITE)
				)
		{
			MappedByteBuffer buf = channel.map(
					FileChannel.MapMode.READ_WRITE,
					0, 
					headerSize + rowCount * (bitMaskSize + maxRowSize) + 1
				);
			buf.putInt(newIndex, -1);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean isRemoved(int index) 
	{
		if(index >= length)
			throw new IndexOutOfBoundsException();
	
		try(FileChannel channel = FileChannel.open(
			path,
			StandardOpenOption.CREATE,
			StandardOpenOption.READ,
			StandardOpenOption.WRITE)
			)
		{
			MappedByteBuffer buf = channel.map(
				FileChannel.MapMode.READ_WRITE,
				0, 
				headerSize + length * (bitMaskSize + maxRowSize) + 1
			);
		
		//System.out.println(length);
		
			int tempIndex = (headerSize + 1) + index * (bitMaskSize + maxRowSize);
			if(buf.getInt(tempIndex) < 0)
				return true;
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return false;
	}
}
