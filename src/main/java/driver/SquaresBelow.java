package driver;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import core.Database;
import model.*;
import structure.*;

/** 
 * For Lab 1, implement this driver.
 * 
 * Examples:
 * 	SQUARES BELOW 20
 * 	SQUARES BELOW 30 AS a
 * 	SQUARES BELOW 15 AS a, b
 * 
 * Response 1:
 *  success flag
 *  message "There were 5 results."
 * 	result table
 * 		primary integer column "x", integer column "x_squared"
 *		rows [0, 0]; [1, 1]; [2, 4]; [3, 9]; [4, 16]
 *
 * Response 2:
 *  success flag
 *  message "There were 6 results."
 * 	result table
 * 		primary integer column "a", integer column "a_squared"
 *		rows [0, 0]; [1, 1]; [2, 4]; [3, 9]; [4, 16]; [5, 25]
 *
 * Response 3:
 *  success flag
 *  message "There were 4 results."
 * 	result table
 * 		primary integer column "a", integer column "b"
 *		rows [0, 0]; [1, 1]; [2, 4]; [3, 9]
 */
@Deprecated
public class SquaresBelow
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			//SQUARES\s+BELOW\s+([0-9]+)(?:\s+AS\s+([a-z][a-z0-9_]*)(?:\s*,\s*([a-z][a-z0-9_]*))?)?
			"SQUARES\\s+BELOW\\s+([0-9]+)(?:\\s+AS\\s+([a-z][a-z0-9_]*)(?:\\s*,\\s*([a-z][a-z0-9_]*))?)?",
			Pattern.CASE_INSENSITIVE
		);
	}
		
	@Override
	public SimpleResponse execute(String query, Database db) 
	{		
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;

		int upper = Integer.parseInt(matcher.group(1));
		String name = matcher.group(2) != null ? matcher.group(2) : "x";
		String sname = matcher.group(3) != null ? matcher.group(3) : name + "_squared";
		
		if(name.equals(sname))
			return new SimpleResponse(false, "Column names cannot be the same", null);
		
		Table table = new VolatileTable(
			"_squares",
			List.of(name, sname),
			List.of("integer", "integer"),
			0
		);
		
		for (int i = 0; i * i < upper; i++) {
			List<Object> row = new LinkedList<>();
			row.add(i);
			row.add(i * i);
			table.getState().put(i, row);
		}
		
		return new SimpleResponse(true, null, table);
	}
}
