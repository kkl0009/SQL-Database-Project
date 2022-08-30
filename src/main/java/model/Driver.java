package model;

import core.Database;

/** 
 * Defines the protocols for query drivers.
 * 
 * Additional protocols may be added.
 */
public interface Driver {
	/** 
	 * Executes the given query on the given
	 * database and returns a response
	 * indicating the result of the query.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @return the response of the query
	 **/
	public Response execute(String query, Database db);
}
