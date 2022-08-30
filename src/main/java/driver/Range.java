package driver;

import model.*;
import structure.*;

import java.util.regex.*;

import core.Database;

import java.util.List;
import java.util.LinkedList;

/** 
 * Do not remove this driver.
 * 
 * Examples:
 * 	RANGE 5
 * 	RANGE 3 AS x
 * 
 * Response 1:
 * 	success flag
 * 	no message
 * 	result table
 * 		primary integer column "number"
 *		rows [0]; [1]; [2]; [3]; [4]
 * 
 * Response 2:
 * 	success flag
 * 	no message
 * 	result table
 * 		primary integer column "x"
 *		rows [0]; [1]; [2]
 */
public class Range
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			"RANGE\\s+([0-9]+)(?:\\s+AS\\s+(\\w+))?",
			Pattern.CASE_INSENSITIVE
		);
	}

	@Override
	public Response execute(String query, Database db) {
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;
		
		int upper = Integer.parseInt(matcher.group(1));
		String name = matcher.group(2) != null ? matcher.group(2) : "number";
		
		Table table = new VolatileTable(
			"_range",
			List.of(name),
			List.of("integer"),
			0
		);
		
		for (int i = 0; i < upper; i++) {
			List<Object> row = new LinkedList<>();
			row.add(i);
			table.getState().put(i, row);
		}
				
		return new SimpleResponse(true, null, table);
	}
}
