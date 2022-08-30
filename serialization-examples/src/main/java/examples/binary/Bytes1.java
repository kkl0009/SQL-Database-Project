package examples.binary;

import java.nio.*;
import java.nio.file.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.io.*;
import java.util.*;

public class Bytes1 {
	public static void main(String[] args) {
		Map<String, Integer> a = new HashMap<>();
		a.put("alpha", 1);
		a.put("beta",  2);
		a.put("gamma", 3);
		a.put("delta", 4);
		a.put("tau",   19);
		a.put("pi",    16);
		write(a);
		System.out.println(a);
		
		Map<String, Integer> b = read();
		System.out.println(b);
		
		System.out.println(a.equals(b));
		System.out.println(a == b);
	}
	
	public static void write(Map<String, Integer> map) {
		try (
			FileChannel channel = FileChannel.open(
				Paths.get("data", "bytes1.bin"),
				StandardOpenOption.CREATE,
				StandardOpenOption.READ,
				StandardOpenOption.WRITE
			)
		) {
			final int map_size = map.size(),
				max_string_length = 8;
			
			MappedByteBuffer buf = channel.map(
				FileChannel.MapMode.READ_WRITE,
				0,
				4 + 4 + map_size*(4 + max_string_length + 4)
			);
			
			buf.putInt(map_size);
			buf.putInt(max_string_length);
			
		    for (String key: map.keySet()) {
		    	final byte[] chars = key.getBytes(StandardCharsets.UTF_8);
		    	buf.putInt(chars.length);
		    	buf.put(chars);
		    	buf.put(new byte[max_string_length - chars.length]);
		    	buf.putInt(map.get(key));
		    }
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Map<String, Integer> read() {
		Map<String, Integer> result = null;
		try (
			FileChannel channel = FileChannel.open(
				Paths.get("data", "bytes1.bin"),
				StandardOpenOption.READ
			)
		) {
			MappedByteBuffer buf = channel.map(
				FileChannel.MapMode.READ_ONLY,
				0, 
				4 + 4
			);
			
			final int map_size = buf.getInt(),
				max_string_length = buf.getInt();
			
			buf = channel.map(
				FileChannel.MapMode.READ_ONLY, 
				4 + 4, 
				map_size*(4 + max_string_length + 4)
			);
			
			result = new HashMap<>();
			for (int i = 0; i < map_size; i++) {
				final byte[] chars = new byte[buf.getInt()];
				buf.get(chars);
				buf.position(buf.position() + max_string_length - chars.length);
				result.put(
		    		new String(chars, StandardCharsets.UTF_8), 
		    		buf.getInt()
		    	);
		    }
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
