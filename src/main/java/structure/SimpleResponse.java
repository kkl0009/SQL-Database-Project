package structure;

import model.*;

public class SimpleResponse
	implements Response
{
	private boolean success;
	private String message;
	private Table table;
	
    public SimpleResponse(boolean success, String message, Table table) {
    	this.success = success;
    	this.message = message;
    	this.table = table;
    }

    @Override
    public boolean getSuccess() {
    	return success;
    }

    @Override
    public String getMessage() {
    	return message;
    }

    @Override
    public Table getTable() {
    	return table;
    }
}