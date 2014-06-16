package com.capgemini.archaius.spring.jdbc.dataload;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

/**
 * This class update the test data for Archaius to new value.
 * Error500 = New Internal Server Error
 * Error404 = New Page not found 
 * Error400 = New Bad Request
 * 
 * @author Sanjay Kumar
 *
 */
public class UpdateTestDataForArchaiusTest {


	/* the default framework is embedded */
	private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private String protocol = "jdbc:derby:";

	public void initializedDerby() {
		loadDriver();

		Connection conn = null;

		ArrayList<Statement> statements = new ArrayList<>(); // list of Statements,
		// PreparedStatements
		PreparedStatement psUpdate = null;
		Statement s = null;
		ResultSet rs = null;

		try {
			Properties props = new Properties(); // connection properties
			props.put("user", "admin");
			props.put("password", "nimda");

			
			String dbName = "jdbcDemoDB"; // the name of the database

			/*
			 * This connection specifies create=true in the connection URL to
			 * cause the database to be created when connecting for the first
			 * time. To remove the database, remove the directory derbyDB (the
			 * same as the database name) and its contents.
			 * 
			 * The directory derbyDB will be created under the directory that
			 * the system property derby.system.home points to, or the current
			 * directory (user.dir) if derby.system.home is not set.
			 */
			conn = DriverManager.getConnection(protocol + dbName
					+ ";create=false",props);

			System.out.println("Connected to and created database " + dbName);

			// We want to control transactions manually. Autocommit is on by
			// default in JDBC.
			conn.setAutoCommit(false);

			/*
			 * Creating a statement object that we can use for running various
			 * SQL statements commands against the database.
			 */
			s = conn.createStatement();
			statements.add(s);

			// We create a table...
			//s.execute("create table MYSITEPROPERTIES(property_key varchar(40), property_value varchar(40))");
			System.out.println("Created table MySiteProperties");

		
			// Let's update some rows as well...
            psUpdate = conn.prepareStatement(
                        "update MYSITEPROPERTIES set property_key=?, property_value=? where property_key=?");
            statements.add(psUpdate);

            psUpdate.setString(1, "Error500");
            psUpdate.setString(2, "New Internal Server Error");
            psUpdate.setString(3, "Error500");
            psUpdate.executeUpdate();
            System.out.println("Updated Error500");

            psUpdate.setString(1, "Error404");
            psUpdate.setString(2, "New Page not found");
            psUpdate.setString(3, "Error404");
            psUpdate.executeUpdate();
            System.out.println("Updated Error404");

            psUpdate.setString(1, "Error400");
            psUpdate.setString(2, "New Bad Request");
            psUpdate.setString(3, "Error400");
            psUpdate.executeUpdate();
            System.out.println("Updated Error400");

            conn.commit();
			/*
			 * We select the rows and verify the results.
			 */
			rs = s.executeQuery("SELECT property_key, property_value FROM MYSITEPROPERTIES");

			while(rs.next()) {
				
				System.out.print("property_key : "+rs.getString(1));
				System.out.println(" and property_value : "+rs.getString(2));
			}

			/*
			 * We commit the transaction. Any changes will be persisted to the
			 * database now.
			 */
			conn.commit();
			System.out.println("Committed the transaction");

		} catch (SQLException sqle) {
			printSQLException(sqle);
		} finally {
			// release all open resources to avoid unnecessary memory usage

			// ResultSet
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
			} catch (SQLException sqle) {
				printSQLException(sqle);
			}

			// Statements and PreparedStatements
			int i = 0;
			while (!statements.isEmpty()) {
				// PreparedStatement extend Statement
				Statement st = (Statement) statements.remove(i);
				try {
					if (st != null) {
						st.close();
						st = null;
					}
				} catch (SQLException sqle) {
					printSQLException(sqle);
				}
			}

			// Connection
			try {
				if (conn != null) {
					conn.close();
					conn = null;
				}
			} catch (SQLException sqle) {
				printSQLException(sqle);
			}
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

	public static void printSQLException(SQLException e) {
		// Unwraps the entire exception chain to unveil the real cause of the
		// Exception.
		while (e != null) {
			System.err.println("\n----- SQLException -----");
			System.err.println("  SQL State:  " + e.getSQLState());
			System.err.println("  Error Code: " + e.getErrorCode());
			System.err.println("  Message:    " + e.getMessage());
			// for stack traces, refer to derby.log or uncomment this:
			// e.printStackTrace(System.err);
			e = e.getNextException();
		}
	}
	
}
