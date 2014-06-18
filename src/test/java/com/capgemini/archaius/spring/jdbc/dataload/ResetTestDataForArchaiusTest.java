package com.capgemini.archaius.spring.jdbc.dataload;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class reset the test data for Archaius to initial value.
 * Error500 = Internal Server Error
 * Error404 = Page not found 
 * Error400 = Bad Request
 * 
 * @author Sanjay Kumar
 *
 */
public class ResetTestDataForArchaiusTest {

	/* the default framework is embedded */
	private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private String protocol = "jdbc:derby:memory:";
	public Logger LOGGER = LoggerFactory.getLogger(ResetTestDataForArchaiusTest.class);
	
	public void initializedDerby() {
		loadDriver();

		Connection conn = null;

		ArrayList<Statement> statements = new ArrayList<>(); // list of Statements,
		// PreparedStatements
		PreparedStatement psUpdate = null;
		Statement s = null;
		ResultSet rs = null;

		try {
			Properties props = new Properties(); 
			props.put("user", "admin");
			props.put("password", "nimda");

			String dbName = "jdbcDemoDB"; // the name of the database

			conn = DriverManager.getConnection(protocol + dbName
					+ ";create=false",props);

			LOGGER.info("Connected to and created database " + dbName);

			// We want to control transactions manually. Autocommit is on by
			// default in JDBC.
			conn.setAutoCommit(false);

			/*
			 * Creating a statement object that we can use for running various
			 * SQL statements commands against the database.
			 */
			s = conn.createStatement();
			statements.add(s);

			LOGGER.info("Created table MySiteProperties");
		
			// Let's update some rows as well...
            psUpdate = conn.prepareStatement(
                        "update MYSITEPROPERTIES set property_key=?, property_value=? where property_key=?");
            statements.add(psUpdate);
            
            psUpdate.setString(1, "Error500");
            psUpdate.setString(2, "Internal Server Error");
            psUpdate.setString(3, "Error500");
            psUpdate.executeUpdate();
            LOGGER.info("Updated Error500");

            psUpdate.setString(1, "Error404");
            psUpdate.setString(2, "Page not found");
            psUpdate.setString(3, "Error404");
            psUpdate.executeUpdate();
            LOGGER.info("Updated Error404");

            psUpdate.setString(1, "Error400");
            psUpdate.setString(2, "Bad Request");
            psUpdate.setString(3, "Error400");
            psUpdate.executeUpdate();
            LOGGER.info("Updated Error400");
            
            conn.commit();
			
			/*
			 * We select the rows and verify the results.
			 */
			rs = s.executeQuery("SELECT property_key, property_value FROM MYSITEPROPERTIES");

			while(rs.next()) {
				
				System.out.print("property_key : "+rs.getString(1));
				LOGGER.info("  and  property_value : "+rs.getString(2));
			}

			/*
			 * We commit the transaction. Any changes will be persisted to the
			 * database now.
			 */
			conn.commit();
			LOGGER.info("Committed the transaction");

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

	public void printSQLException(SQLException e) {
		// Unwraps the entire exception chain to unveil the real cause of the
		// Exception.
		while (e != null) {
			LOGGER.error("\n----- SQLException -----");
			LOGGER.error("  SQL State:  " + e.getSQLState());
			LOGGER.error("  Error Code: " + e.getErrorCode());
			LOGGER.error("  Message:    " + e.getMessage());
			// for stack traces, refer to derby.log or uncomment this:
			// e.printStackTrace(System.err);
			e = e.getNextException();
		}
	}
	
}
