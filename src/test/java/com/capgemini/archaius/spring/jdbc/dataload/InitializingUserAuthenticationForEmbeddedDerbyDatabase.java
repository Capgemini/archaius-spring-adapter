package com.capgemini.archaius.spring.jdbc.dataload;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Sanjay Kumar.
 */
public class InitializingUserAuthenticationForEmbeddedDerbyDatabase {

	private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private String protocol = "jdbc:derby:memory:";
	public Logger LOGGER = LoggerFactory.getLogger(InitializingUserAuthenticationForEmbeddedDerbyDatabase.class);
	
	public void initializedDerby() {
		
		loadDriver();

		Connection conn = null;

		String dbName = "jdbcDemoDB"; // the name of the database

		String connectionURL = protocol + dbName + ";create=true";

		// Start the database and set up users, then close database
		try {
			LOGGER.info("Trying to connect to url : {}", connectionURL);
			conn = DriverManager.getConnection(connectionURL);
			LOGGER.info("Connected to database: {} ", connectionURL);

			turnOnBuiltInUsers(conn);

			// shut down the database
			conn.close();
			LOGGER.info("Closed connection");

			/*
			 * In embedded mode, an application should shut down Derby. Shutdown
			 * throws the XJ015 exception to confirm success.
			 */
		/*	boolean gotSQLExc = false;
			try {
				DriverManager.getConnection("jdbc:derby;shutdown=true");
			} catch (SQLException se) {
				if (se.getSQLState().equals("XJ015")) {
					gotSQLExc = true;
				}
			}
			if (!gotSQLExc) {
				LOGGER.info("Database did not shut down normally");
			} else {
				LOGGER.info("Database shut down normally");
			}*/

			// force garbage collection to unload the EmbeddedDriver
			// so Derby can be restarted
			System.gc();
		} catch (Throwable e) {
			errorPrint(e);
			System.exit(1);
		}
	}

	private void loadDriver() {

		try {
			Class.forName(driver).newInstance();
			LOGGER.info("Loaded the appropriate driver");
		} catch (ClassNotFoundException cnfe) {
			LOGGER.error("\nUnable to load the JDBC driver " + driver);
			LOGGER.error("Please check your CLASSPATH.");
			cnfe.printStackTrace(System.err);
		} catch (InstantiationException ie) {
			LOGGER.error("\nUnable to instantiate the JDBC driver "
					+ driver);
			ie.printStackTrace(System.err);
		} catch (IllegalAccessException iae) {
			LOGGER.error("\nNot allowed to access the JDBC driver "
					+ driver);
			iae.printStackTrace(System.err);
		}
	}

	/**
	 * Turn on built-in user authentication and user authorization.
	 * 
	 * @param conn
	 *            a connection to the database.
	 */
	private void turnOnBuiltInUsers(Connection conn) throws SQLException {
		LOGGER.info("Turning on authentication.");
		Statement s = conn.createStatement();

		// Setting and Confirming requireAuthentication
		s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY("
				+ "'derby.connection.requireAuthentication', 'true')");
		ResultSet rs = s
				.executeQuery("VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY("
						+ "'derby.connection.requireAuthentication')");
		rs.next();
		LOGGER.info("Value of requireAuthentication is "
				+ rs.getString(1));
		// Setting authentication scheme to Derby
		s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY("
				+ "'derby.authentication.provider', 'BUILTIN')");

		// Creating some sample users
		s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY("
				+ "'derby.user.admin', 'nimda')");
		s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY("
				+ "'derby.user.guest', 'guest')");
		s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY("
				+ "'derby.user.test', 'test')");

		// Setting default connection mode to no access
		// (user authorization)
		s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY("
				+ "'derby.database.defaultConnectionMode', 'noAccess')");
		// Confirming default connection mode
		rs = s.executeQuery("VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY("
				+ "'derby.database.defaultConnectionMode')");
		rs.next();
		LOGGER.info("Value of defaultConnectionMode is "
				+ rs.getString(1));

		// Defining read-write users
		s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY("
				+ "'derby.database.fullAccessUsers', 'admin')");

		// Defining read-only users
		s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY("
				+ "'derby.database.readOnlyAccessUsers', 'guest,test')");

		// Confirming full-access users
		rs = s.executeQuery("VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY("
				+ "'derby.database.fullAccessUsers')");
		rs.next();
		LOGGER.info("Value of fullAccessUsers is " + rs.getString(1));

		// Confirming read-only users
		rs = s.executeQuery("VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY("
				+ "'derby.database.readOnlyAccessUsers')");
		rs.next();
		System.out
				.println("Value of readOnlyAccessUsers is " + rs.getString(1));

		// We would set the following property to TRUE only
		// when we were ready to deploy.
		s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY("
				+ "'derby.database.propertiesOnly', 'false')");
		s.close();
	}

	/**
	 * Exception reporting methods with special handling of SQLExceptions
	 */
	private void errorPrint(Throwable e) {
		if (e instanceof SQLException)
			SQLExceptionPrint((SQLException) e);
		else {
			LOGGER.info("A non-SQL error occurred.");
			e.printStackTrace();
		}
	}

	// Iterates through a stack of SQLExceptions
	private void SQLExceptionPrint(SQLException sqle) {
		while (sqle != null) {
			LOGGER.info("\n---SQLException Caught---\n");
			LOGGER.info("SQLState:   " + (sqle).getSQLState());
			LOGGER.info("Severity: " + (sqle).getErrorCode());
			LOGGER.info("Message:  " + (sqle).getMessage());
			sqle.printStackTrace();
			sqle = sqle.getNextException();
		}
	} // END SQLExceptionPrint
}
