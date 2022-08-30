package examples.binary;

import java.nio.*;
import java.nio.file.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.io.*;
import java.util.*;

public class Bytes2 {
	public static void main(String[] args) {
		Set<List<String>> a = new HashSet<>();
		a.add(Arrays.asList("beta", "zeta", "eta", "theta"));
		a.add(Arrays.asList("epsilon", "upsilon"));
		a.add(Arrays.asList("phi", "psi", "chi"));
		write(a);
		System.out.println(a);
		
		Set<List<String>> b = read();
		System.out.println(b);
		
		System.out.println(a.equals(b));
		System.out.println(a == b);
	}
	
	public static void write(Set<List<String>> set) {
		try (
			FileChannel channel = FileChannel.open(
				Paths.get("data", "bytes2.bin"),
				StandardOpenOption.CREATE,
				StandardOpenOption.TRUNCATE_EXISTING,
				StandardOpenOption.SPARSE,
				StandardOpenOption.READ,
				StandardOpenOption.WRITE
			)
		) {
			final int set_size = set.size();
			final short max_list_size = 4,
				max_string_length = 7;
			
			MappedByteBuffer buf = channel.map(
				FileChannel.MapMode.READ_WRITE, 
				0,
				4 + 2 + 2 + set_size*(max_list_size*(1 + max_string_length))
			);
			
			buf.putInt(set_size);
			buf.putShort(max_list_size);
			buf.putShort(max_string_length);
			
		    for (List<String> list: set) {
		    	final int strings = list.size();
		    	for (int j = 0; j < strings; j++) {
			    	final byte[] chars = list.get(j).getBytes(StandardCharsets.UTF_8);
			    	buf.put((byte) chars.length);
			    	buf.put(chars);
//			    	buf.put(new byte[max_string_length - chars.length]);
			    	buf.position(buf.position() + max_string_length - chars.length);
		    	}
		    	if (strings < max_list_size) {
		    		buf.put((byte) -1);
		    		buf.position(buf.position() - 1 + (max_list_size-list.size()) * (1 + max_string_length));
		    	}
		    }
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Set<List<String>> read() {
		Set<List<String>> set = null;
		try (
			FileChannel channel = FileChannel.open(
				Paths.get("data", "bytes2.bin"),
				StandardOpenOption.READ
			)
		) {
			MappedByteBuffer buf = channel.map(
				FileChannel.MapMode.READ_ONLY, 
				0, 
				4 + 2 + 2
			);
			
			final int set_size = buf.getInt();
			final short max_list_size = buf.getShort(),
				max_string_length = buf.getShort();
			
			buf = channel.map(
				FileChannel.MapMode.READ_ONLY, 
				4 + 2 + 2, 
				set_size*(max_list_size*(1 + max_string_length))
			);
			
			set = new HashSet<>();
		    for (int i = 0; i < set_size; i++) {
		    	final List<String> list = new LinkedList<>();
		    	for (int j = 0; j < max_list_size; j++) {
		    		final int len = buf.get();
		    		if (len == -1) break;
		    		
					final byte[] chars = new byte[len];
					buf.get(chars);
					buf.position(buf.position() + max_string_length - chars.length);
		    		list.add(new String(chars, StandardCharsets.UTF_8));
	    		}
		    	if (list.size() < max_list_size) {
		    		buf.position(buf.position() - 1 + (max_list_size-list.size()) * (1 + max_string_length));
		    	}
		    	set.add(list);
		    }
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return set;
	}
}
