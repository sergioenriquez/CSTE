package cste.kmf.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Properties;


public class DbHandler {
    static Connection conn = null;
    static final private String driver = "org.apache.derby.jdbc.EmbeddedDriver";
    static final private String protocol = "jdbc:derby:";
    static final private String dbName = "derbyDB"; // the name of the database
    
    public static void addDeviceRecord(byte[] deviceUID, byte[] deviceRekeyKeym, byte type) {
    	PreparedStatement psInsert = null;
    	try {
			psInsert = conn.prepareStatement("INSERT INTO Devices values (?, ?, ?)");
			psInsert.setBytes(1, deviceUID);
	    	psInsert.setBytes(2, deviceRekeyKeym);
	    	psInsert.setByte(3, type);
	    	psInsert.executeUpdate();
		} catch (SQLException e) {
			System.err.println("Unable to store deveice record in database");
		}
    }

    public static void init() {

    	if ( !loadDriver() )
    		return;
    	
    	Properties props = new Properties();
    	Statement s = null;
    	try {
			conn = DriverManager.getConnection(protocol + dbName + ";create=true", props);
			conn.setAutoCommit(false);
			s = conn.createStatement();
		} catch (SQLException e) {
			System.err.println("Unable to create database connection");
            return;
		}
    	
    	System.out.println("Connected to and created database " + dbName);

    	try {
			s.execute("CREATE TABLE Devices ("+
					"DeviceUID CHAR(8) PRIMARY KEY,"+
					"RekeyKey CHAR(16) NOT NULL,"+
					"DeviceType CHAR(1) NOT NULL," +
					"RekeyAscNum INTEGER DEFAULT 0)");
			
		} catch (SQLException e) {
			System.err.println("SQL syntax error on create Devices table");
		}
    }
    
    static private boolean loadDriver() {
        try {
            Class.forName(driver).newInstance();
            System.out.println("Loaded the appropriate derby driver");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("\nUnable to load the JDBC driver " + driver);
            System.err.println("Please check your CLASSPATH.");
            return false;
        } catch (InstantiationException ie) {
            System.err.println("\nUnable to instantiate the JDBC driver " + driver);
            return false;
        } catch (IllegalAccessException iae) {
            System.err.println("\nNot allowed to access the JDBC driver " + driver);
            return false;
        }
        return true;
    }

}
