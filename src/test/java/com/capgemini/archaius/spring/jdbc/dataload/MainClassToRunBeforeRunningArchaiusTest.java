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

public class MainClassToRunBeforeRunningArchaiusTest {

	/* the default framework is embedded */
	private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private String protocol = "jdbc:derby:memory:";
	public Logger LOGGER = LoggerFactory.getLogger(MainClassToRunBeforeRunningArchaiusTest.class);
	
	public void initializedDerby() {

		loadDriver();

		Connection conn = null;

		ArrayList<Statement> statements = new ArrayList<>(); // list of Statements,
		// PreparedStatements
		PreparedStatement psInsert = null;
		Statement s = null;
		ResultSet rs = null;
		String dbName = "jdbcDemoDB"; // the name of the database
		try {

            Properties props = new Properties(); 
			props.put("user", "admin");
			props.put("password", "nimda");

            conn = DriverManager.getConnection(protocol + dbName + ";create=true",props);

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
			s.execute("create table MYSITEPROPERTIES(property_key varchar(40), property_value varchar(40))");
			LOGGER.info("Created table MySiteProperties");

			psInsert = conn
					.prepareStatement("insert into MYSITEPROPERTIES values (?, ?)");
			statements.add(psInsert);

			psInsert.setString(1, "Error404");
			psInsert.setString(2, "Page not found");
			psInsert.executeUpdate();
			LOGGER.info("Inserted Error404");

			psInsert.setString(1, "Error500");
			psInsert.setString(2, "Internal Server Error");
			psInsert.executeUpdate();
			LOGGER.info("Inserted Error500");
			
			psInsert.setString(1, "Error400");
			psInsert.setString(2, "Bad Request");
			psInsert.executeUpdate();
			LOGGER.info("Inserted Error400");
			
			/*
			 * We select the rows and verify the results.
			 */
			rs = s.executeQuery("SELECT property_key, property_value FROM MYSITEPROPERTIES");

			while(rs.next()) {
				
				System.out.print("property_key : "+rs.getString(1));
				LOGGER.info("  and property_value : "+rs.getString(2));
			}

			/*
			 * We commit the transaction. Any changes will be persisted to the
			 * database now.
			 */
			conn.commit();
			LOGGER.info("Committed the transaction");

			/*if (framework.equals("embedded")) {
				try {
					// the shutdown=true attribute shuts down Derby
					DriverManager.getConnection("jdbc:derby:;shutdown=true");

				} catch (SQLException se) {
					if (((se.getErrorCode() == 50000) && ("XJ015".equals(se
							.getSQLState())))) {
						LOGGER.info("Derby shut down normally");
					} else {
						LOGGER.error("Derby did not shut down normally");
						printSQLException(se);
					}
				}
			}*/
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
