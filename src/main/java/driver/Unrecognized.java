package driver;

import core.Database;
import model.*;
import structure.*;

/** 
 * Do not remove this driver.
 * 
 * Example:
 * 	NONSENSE QUERY ASDF
 * 
 * Response:
 * 	failure flag
 * 	message "Unrecognized query"
 * 	no result table
 */
public class Unrecognized
	implements Driver
{
	@Override
	public Response execute(String query, Database db) {
		return new SimpleResponse(false, "Unrecognized query", null);
	}
}
