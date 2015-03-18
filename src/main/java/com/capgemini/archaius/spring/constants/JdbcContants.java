package com.capgemini.archaius.spring.constants;


/**
 * Class which hold jdbc related contact values.
 * @author skumar81 
 */
public final class JdbcContants {
	
	private JdbcContants(){}
    
    public static final String DB_DRIVER ="driverClassName";
	public static final String DB_URL ="dbURL";
	public static final String USERNAME="username";
	public static final String PASSWORD="password";
	public static final String SQL_QUERY="sqlQuery";
	public static final String KEY_COLUMN_NAME="keyColumnName";
	public static final String VALUE_COLUMN_NAME="valueColumnName";
	public static final int EXPECTED_JDBC_PARAM_COUNT=7;
	public static final String  IGNORE_RESOURCE_NOT_FOUND="ignoreResourceNotFound";
	public static final String  IGNORE_DELETE_FROM_SOURCE="ignoreDeletesFromSource";
	public static final String  INITIAL_DELAY_MILLIS="initialDelayMillis";
	public static final String  DELAY_MILLIS="delayMillis";	
}
