package com.capgemini.archaius.spring.jdbc.dataload;

import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Sanjay Kumar.
 */
public class DeleteTestDataAndSchemaForArchaiusTest {
	/* the default framework is embedded */
	private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
	public Logger LOGGER = LoggerFactory.getLogger(DeleteTestDataAndSchemaForArchaiusTest.class);

	public void deleteDatabase() {
		/* load the desired JDBC driver */
		loadDriver();
		
		try {

			LOGGER.info("Going to issue the shutdown of DB");
			DriverManager.getConnection("jdbc:derby:memory:jdbcDemoDB;user=admin;password=nimda;shutdown=true");
		/*	
			LOGGER.info("going to drop table");
			DriverManager
					.getConnection("jdbc:derby:memory:jdbcDemoDB;user=admin;password=nimda;drop=true");
			LOGGER.info("dropped table");*/

		} catch (SQLException se) {
			if (((se.getErrorCode() == 50000) && ("XJ015".equals(se
					.getSQLState())))) {
				// we got the expected exception
				LOGGER.info("Derby shut down normally");
				
				// Note that for single database shutdown, the expected
				// SQL state is "08006", and the error code is 45000.
			}if (((se.getErrorCode() == 45000) && ("08006".equals(se
					.getSQLState())))){
				// we got the expected exception
				LOGGER.info("Derby shut down normally");
				LOGGER.info("going to drop table");
				try {
					DriverManager
							.getConnection("jdbc:derby:memory:jdbcDemoDB;user=admin;password=nimda;drop=true");
				} catch (SQLException e) {
					if (((e.getErrorCode() == 45000) && ("08006".equals(e
							.getSQLState())))){
						// we got the expected exception
						LOGGER.info("Derby dropped table normally");
					}
					else {
						// if the error code or SQLState is different, we have
						// an unexpected exception (shutdown failed)
						LOGGER.error("Derby did not shut down normally");
						printSQLException(se);
					}
				}
				LOGGER.info("Derby dropped table normally");
			}else {
				// if the error code or SQLState is different, we have
				// an unexpected exception (shutdown failed)
				LOGGER.error("Derby did not shut down normally");
				printSQLException(se);
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
