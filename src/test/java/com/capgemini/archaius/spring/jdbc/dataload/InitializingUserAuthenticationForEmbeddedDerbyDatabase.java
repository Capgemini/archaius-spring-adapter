package com.capgemini.archaius.spring.jdbc.dataload;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class InitializingUserAuthenticationForEmbeddedDerbyDatabase {

	private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private String protocol = "jdbc:derby:";

	public static void main(String[] arrs) {
		InitializingUserAuthenticationForEmbeddedDerbyDatabase derbyDatabase = new InitializingUserAuthenticationForEmbeddedDerbyDatabase();
		derbyDatabase.initializedDerby();
	}

	public void initializedDerby() {
		loadDriver();

		Connection conn = null;

		String dbName = "jdbcDemoDB"; // the name of the database

		String connectionURL = protocol + dbName + ";create=true";

		// Start the database and set up users, then close database
		try {
			System.out.println("Trying to connect to " + connectionURL);
			conn = DriverManager.getConnection(connectionURL);
			System.out.println("Connected to database " + connectionURL);

			turnOnBuiltInUsers(conn);

			// shut down the database
			conn.close();
			System.out.println("Closed connection");

			/*
			 * In embedded mode, an application should shut down Derby. Shutdown
			 * throws the XJ015 exception to confirm success.
			 */
			boolean gotSQLExc = false;
			try {
				DriverManager.getConnection("jdbc:derby:;shutdown=true");
			} catch (SQLException se) {
				if (se.getSQLState().equals("XJ015")) {
					gotSQLExc = true;
				}
			}
			if (!gotSQLExc) {
				System.out.println("Database did not shut down normally");
			} else {
				System.out.println("Database shut down normally");
			}

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
			System.out.println("Loaded the appropriate driver");
		} catch (ClassNotFoundException cnfe) {
			System.err.println("\nUnable to load the JDBC driver " + driver);
			System.err.println("Please check your CLASSPATH.");
			cnfe.printStackTrace(System.err);
		} catch (InstantiationException ie) {
			System.err.println("\nUnable to instantiate the JDBC driver "
					+ driver);
			ie.printStackTrace(System.err);
		} catch (IllegalAccessException iae) {
			System.err.println("\nNot allowed to access the JDBC driver "
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
	public static void turnOnBuiltInUsers(Connection conn) throws SQLException {
		System.out.println("Turning on authentication.");
		Statement s = conn.createStatement();

		// Setting and Confirming requireAuthentication
		s.executeUpdate("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY("
				+ "'derby.connection.requireAuthentication', 'true')");
		ResultSet rs = s
				.executeQuery("VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY("
						+ "'derby.connection.requireAuthentication')");
		rs.next();
		System.out.println("Value of requireAuthentication is "
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
		System.out.println("Value of defaultConnectionMode is "
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
		System.out.println("Value of fullAccessUsers is " + rs.getString(1));

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
	static void errorPrint(Throwable e) {
		if (e instanceof SQLException)
			SQLExceptionPrint((SQLException) e);
		else {
			System.out.println("A non-SQL error occurred.");
			e.printStackTrace();
		}
	}

	// Iterates through a stack of SQLExceptions
	static void SQLExceptionPrint(SQLException sqle) {
		while (sqle != null) {
			System.out.println("\n---SQLException Caught---\n");
			System.out.println("SQLState:   " + (sqle).getSQLState());
			System.out.println("Severity: " + (sqle).getErrorCode());
			System.out.println("Message:  " + (sqle).getMessage());
			sqle.printStackTrace();
			sqle = sqle.getNextException();
		}
	} // END SQLExceptionPrint
}
