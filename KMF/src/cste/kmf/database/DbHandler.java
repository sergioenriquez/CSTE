package cste.kmf.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import cste.kmf.KmfDeviceRecord;
import cste.kmf.KmfDeviceRecord.InvalidRecordExeption;

public class DbHandler {
	static final String GET_RECORD_QUERY = "SELECT RekeyKey,DeviceType,RekeyAscNum from Devices where DeviceUID = (?)";
	static final String STORE_RECORD_QUERY = "INSERT INTO Devices values (?, ?, ?, ?)";
	static final String CREATE_DB_QUERY = 	"CREATE TABLE Devices ("+
											"DeviceUID VARCHAR (8) FOR BIT DATA,"+
											"RekeyKey VARCHAR(16) FOR BIT DATA NOT NULL,"+
											"DeviceType VARCHAR(1) FOR BIT DATA NOT NULL," +
											"RekeyAscNum INTEGER DEFAULT 0)";
	
    static Connection conn = null;
    static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    static final String PROTOCOL = "jdbc:derby:";
    static final String DBNAME = "derbyDB"; // the name of the database
    
    public static  boolean deleteDeviceRecord(byte[] uid) {
    	//TODO
    	return false;
    }
   
    public static  boolean addDeviceRecord(KmfDeviceRecord record) {
    	//TODO handle the case where it already exists, in whic case update
    	PreparedStatement psInsert = null;
    	try {
			psInsert = conn.prepareStatement(STORE_RECORD_QUERY);
			psInsert.setBytes(1, record.getUID());
	    	psInsert.setBytes(2, record.getRekeyKey());
	    	psInsert.setBytes(3, new byte[]{record.getDeviceType()}); // need to cast as byte array
	    	psInsert.setInt(4, record.getAscCount());
	    	psInsert.executeUpdate();
		} catch (SQLException e) {
			System.err.println("SQL query error!");
			return false;
		}
		return true;
    }
    
    public static KmfDeviceRecord getDeviceRecord(byte[] deviceUID){
    	byte[] rekeyKey = null;
		byte type = 0;
		int rekeyAscNum = 0;
		
    	try {
			PreparedStatement psSelect = conn.prepareStatement(GET_RECORD_QUERY);
			psSelect.setBytes(1, deviceUID);
			ResultSet rs = psSelect.executeQuery();
			
			if (!rs.next())
				return null; // no record was found with this UID

			rekeyKey = rs.getBytes(1);
			type = rs.getBytes(2)[0];
			rekeyAscNum = rs.getInt(3);
    	
    	} catch (SQLException e) {
    		System.err.println("SQL query error!");
			return null;
		}
    	
    	KmfDeviceRecord r;
    	try {
			r = new KmfDeviceRecord(type,deviceUID,rekeyKey,rekeyAscNum);
		} catch (InvalidRecordExeption e) {
			System.err.println("Invalid record was retrieved from DB!");
			return null;
		}
    	return r;
    }

    public static void init() {

    	if ( !loadDriver() )
    		return;
    	
    	Properties props = new Properties();
    	Statement s = null;
    	try {
			conn = DriverManager.getConnection(PROTOCOL + DBNAME + ";create=true", props);
			conn.setAutoCommit(false);
			s = conn.createStatement();
		} catch (SQLException e) {
			System.err.println("Unable to create database connection");
            return;
		}
    	
    	System.out.println("Connected to and created database " + DBNAME);

    	try {
			s.execute(CREATE_DB_QUERY);
		} catch (SQLException e) {
			System.err.println("SQL syntax error on create Devices table");
		}
    }
    
    static boolean loadDriver() {
        try {
            Class.forName(DRIVER).newInstance();
            System.out.println("Loaded the appropriate derby driver");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("\nUnable to load the JDBC driver " + DRIVER);
            System.err.println("Please check your CLASSPATH.");
            return false;
        } catch (InstantiationException ie) {
            System.err.println("\nUnable to instantiate the JDBC driver " + DRIVER);
            return false;
        } catch (IllegalAccessException iae) {
            System.err.println("\nNot allowed to access the JDBC driver " + DRIVER);
            return false;
        }
        return true;
    }

}
