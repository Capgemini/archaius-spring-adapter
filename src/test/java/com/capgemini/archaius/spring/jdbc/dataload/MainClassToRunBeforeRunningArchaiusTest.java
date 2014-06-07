package com.capgemini.archaius.spring.jdbc.dataload;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;

public class MainClassToRunBeforeRunningArchaiusTest {


	/* the default framework is embedded */
	private String framework = "embedded";
	private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	private String protocol = "jdbc:derby:";

	public static void main(String [] arrs){
		MainClassToRunBeforeRunningArchaiusTest dpt=new MainClassToRunBeforeRunningArchaiusTest();
		dpt.initializedDerby();
	}
	
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

            Properties props = new Properties(); // connection properties
			// providing a user name and password is optional in the embedded
			// and derbyclient frameworks
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
			System.out.println("Created table MySiteProperties");


			/*
			 * It is recommended to use PreparedStatements when you are
			 * repeating execution of an SQL statement. PreparedStatements also
			 * allows you to parameterize variables. By using PreparedStatements
			 * you may increase performance (because the Derby engine does not
			 * have to recompile the SQL statement each time it is executed) and
			 * improve security (because of Java type checking).
			 */
			// parameter 1 is num (int), parameter 2 is addr (varchar)
			psInsert = conn
					.prepareStatement("insert into MYSITEPROPERTIES values (?, ?)");
			statements.add(psInsert);

			psInsert.setString(1, "Error404");
			psInsert.setString(2, "Page not found");
			psInsert.executeUpdate();
			System.out.println("Inserted Error404");

			psInsert.setString(1, "Error500");
			psInsert.setString(2, "Internal Server Error");
			psInsert.executeUpdate();
			System.out.println("Inserted Error500");
			
			psInsert.setString(1, "Error400");
			psInsert.setString(2, "Bad Request");
			psInsert.executeUpdate();
			System.out.println("Inserted Error400");
			
			psInsert.setString(1, "Error401");
			psInsert.setString(2, "Unauthorized");
			psInsert.executeUpdate();
			System.out.println("Inserted Error500");
			
			psInsert.setString(1, "Error407");
			psInsert.setString(2, "Proxy Authentication Required");
			psInsert.executeUpdate();
			System.out.println("Inserted Error407");

			
			/*
			 * We select the rows and verify the results.
			 */
			rs = s.executeQuery("SELECT property_key, property_value FROM MYSITEPROPERTIES");

			/*
			 * we expect the first returned column to be an integer (num), and
			 * second to be a String (addr). Rows are sorted by street number
			 * (num).
			 * 
			 * Normally, it is best to use a pattern of while(rs.next()) { // do
			 * something with the result set } to process all returned rows, but
			 * we are only expecting two rows this time, and want the
			 * verification code to be easy to comprehend, so we use a different
			 * pattern.
			 */

			while(rs.next()) {
				
				System.out.print("property_key : "+rs.getString(1));
				System.out.println("property_value"+rs.getString(2));
			}

			// delete the table
			//s.execute("drop table MySiteProperties");
			//System.out.println("Dropped table MySiteProperties");

			/*
			 * We commit the transaction. Any changes will be persisted to the
			 * database now.
			 */
			conn.commit();
			System.out.println("Committed the transaction");

			/*
			 * In embedded mode, an application should shut down the database.
			 * If the application fails to shut down the database, Derby will
			 * not perform a checkpoint when the JVM shuts down. This means that
			 * it will take longer to boot (connect to) the database the next
			 * time, because Derby needs to perform a recovery operation.
			 * 
			 * It is also possible to shut down the Derby system/engine, which
			 * automatically shuts down all booted databases.
			 * 
			 * Explicitly shutting down the database or the Derby engine with
			 * the connection URL is preferred. This style of shutdown will
			 * always throw an SQLException.
			 * 
			 * Not shutting down when in a client environment, see method
			 * Javadoc.
			 */

			if (framework.equals("embedded")) {
				try {
					// the shutdown=true attribute shuts down Derby
					DriverManager.getConnection("jdbc:derby:;shutdown=true");

					// To shut down a specific database only, but keep the
					// engine running (for example for connecting to other
					// databases), specify a database in the connection URL:
					// DriverManager.getConnection("jdbc:derby:" + dbName +
					// ";shutdown=true");
				} catch (SQLException se) {
					if (((se.getErrorCode() == 50000) && ("XJ015".equals(se
							.getSQLState())))) {
						// we got the expected exception
						System.out.println("Derby shut down normally");
						// Note that for single database shutdown, the expected
						// SQL state is "08006", and the error code is 45000.
					} else {
						// if the error code or SQLState is different, we have
						// an unexpected exception (shutdown failed)
						System.err.println("Derby did not shut down normally");
						printSQLException(se);
					}
				}
			}
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
