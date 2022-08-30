package model;

/** 
 * Defines the protocols for responses.
 * 
 * Additional protocols may be added.
 */
public interface Response {
	/** 
	 * Returns <code>true</code> if the query
	 * was semantically valid and executable.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @return <code>true</code> if the query
	 * was valid, <code>false</code> otherwise
	 **/
    public boolean getSuccess();

	/** 
	 * Returns any message the query produced.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @return the message, or <code>null</code>
	 **/
    public String getMessage();

	/**
	 * Returns any table the query produced.
	 * 
	 * Do not modify the protocol.
	 * 
	 * @return the table, or <code>null</code>
	 **/
    public Table getTable();
}