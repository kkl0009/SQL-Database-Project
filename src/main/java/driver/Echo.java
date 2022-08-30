package driver;

import model.*;
import structure.*;

import java.util.regex.*;

import core.Database;

/** 
 * Do not remove this driver.
 * 
 * Example:
 * 	ECHO "Hello, world!"
 * 
 * Response:
 * 	success flag
 * 	message "Hello, world!"
 * 	no result table
 */
public class Echo
	implements Driver
{
	private static final Pattern pattern;
	static {
		pattern = Pattern.compile(
			"ECHO\\s+\"([^\"]*)\"",
			Pattern.CASE_INSENSITIVE
		);
	}

	@Override
	public Response execute(String query, Database db) {
		Matcher matcher = pattern.matcher(query.strip());
		if (!matcher.matches()) return null;

		String text = matcher.group(1);
		
		return new SimpleResponse(true, text, null);
	}
}
