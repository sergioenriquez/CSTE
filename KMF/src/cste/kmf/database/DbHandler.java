package cste.kmf.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import cste.kmf.KmfDeviceRecord;
import cste.kmf.KmfDeviceRecord.InvalidRecordExeption;

public class DbHandler {
	static final String GET_RECORD_QUERY = "SELECT RekeyKey,DeviceType,RekeyCounter,LongTermKey from Devices where DeviceUID = (?)";
	static final String GET_ALL_RECORDS_QUERY = "SELECT DeviceUID,RekeyKey,DeviceType,RekeyCounter,LongTermKey from Devices";
	static final String STORE_RECORD_QUERY = "INSERT INTO Devices values (?, ?, ?, ?, ?)";
	static final String DELETE_RECORD_QUERY = "DELETE FROM Devices where DeviceUID = (?)";
	static final String UPDATE_RECORD_QUERY = "UPDATE Devices SET RekeyKey = ?, LongTermKey = ?, DeviceType = ?, RekeyCounter = ? WHERE DeviceUID = (?)";
	static final String CREATE_DB_QUERY = 	"CREATE TABLE Devices ("+
											"DeviceUID VARCHAR (8) FOR BIT DATA,"+
											"RekeyKey VARCHAR(16) FOR BIT DATA NOT NULL,"+
											"LongTermKey VARCHAR(16) FOR BIT DATA NOT NULL,"+
											"DeviceType VARCHAR(1) FOR BIT DATA NOT NULL," +
											"RekeyCounter INTEGER DEFAULT 0)";
	
    static Connection conn = null;
    static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    static final String PROTOCOL = "jdbc:derby:";
    static final String DBNAME = "derbyDB"; // the name of the database
    
    public static List<KmfDeviceRecord> getRecords(){
    	List<KmfDeviceRecord> recordList = new ArrayList<KmfDeviceRecord>();
    	
    	byte[] deviceUID = null;
    	byte[] rekeyKey = null;
    	byte[] devLTK = null;
		byte type = 0;
		int rekeyAscNum = 0;
    	
    	Statement s = null;
    	ResultSet r = null;
		try {
			s = conn.createStatement();
			s.execute(GET_ALL_RECORDS_QUERY);
			r = s.getResultSet();
			
			while(r.next()){
				deviceUID = r.getBytes(1);
				rekeyKey = r.getBytes(2);
				type = r.getBytes(3)[0];
				rekeyAscNum = r.getInt(4);
				devLTK = r.getBytes(5);
				KmfDeviceRecord k = new KmfDeviceRecord(type,deviceUID,rekeyKey,rekeyAscNum,devLTK);
				recordList.add(k);
			}

		} catch (SQLException e) {
			System.err.println("SQL query error!");
		} catch (InvalidRecordExeption e) {
			System.err.println("SQL query error!");
		}
    	
    	return recordList;
    }
    
    public static  boolean deleteDeviceRecord(byte[] uid) {
    	PreparedStatement psDelete = null;
    	try {
    		psDelete = conn.prepareStatement(DELETE_RECORD_QUERY);
    		psDelete.setBytes(1, uid);
    		psDelete.executeUpdate();
		} catch (SQLException e) {
			System.err.println("SQL query error!");
			return false;
		}
		return true;
    }
   
    public static  boolean addDeviceRecord(KmfDeviceRecord record) {
    	// check if record exists in which case update it instead
    	KmfDeviceRecord existingRecord = getDeviceRecord(record.getUID());
    	if ( existingRecord != null){
    		PreparedStatement psUpdate = null;
    		try {
    			psUpdate = conn.prepareStatement(UPDATE_RECORD_QUERY);
    			psUpdate.setBytes(1, record.getRekeyKey());
    			psUpdate.setBytes(2, record.getLTK());
    			psUpdate.setBytes(3, new byte[]{record.getDeviceType()}); // need to cast as byte array
    			psUpdate.setInt(4, record.getRekeyCtr());
    			psUpdate.setBytes(5, record.getUID());
    			psUpdate.executeUpdate();
    		} catch (SQLException e) {
    			System.err.println("SQL query error!");
    			return false;
    		}
    		return true;
    	}
    	
    /*
     * 	static final String CREATE_DB_QUERY = 	"CREATE TABLE Devices ("+
											"DeviceUID VARCHAR (8) FOR BIT DATA,"+
											"RekeyKey VARCHAR(16) FOR BIT DATA NOT NULL,"+
											"LongTermKey VARCHAR(16) FOR BIT DATA NOT NULL,"+
											"DeviceType VARCHAR(1) FOR BIT DATA NOT NULL," +
											"RekeyCounter INTEGER DEFAULT 0)";
    	*/
    	PreparedStatement psInsert = null;
    	try {
			psInsert = conn.prepareStatement(STORE_RECORD_QUERY);
			psInsert.setBytes(1, record.getUID());
	    	psInsert.setBytes(2, record.getRekeyKey());
	    	psInsert.setBytes(3, record.getLTK());
	    	psInsert.setBytes(4, new byte[]{record.getDeviceType()}); // need to cast as byte array
	    	psInsert.setInt(5, record.getRekeyCtr());
	    	psInsert.executeUpdate();
		} catch (SQLException e) {
			System.err.println("SQL query error!");
			return false;
		}
		return true;
    }
    
    public static KmfDeviceRecord getDeviceRecord(byte[] deviceUID){
    	byte[] rekeyKey = null;
    	byte[] devLTK = null;
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
			devLTK = rs.getBytes(4);
    	
    	} catch (SQLException e) {
    		System.err.println("SQL query error!");
			return null;
		}
    	
    	KmfDeviceRecord r;
    	try {
			r = new KmfDeviceRecord(type,deviceUID,rekeyKey,rekeyAscNum,devLTK);
		} catch (InvalidRecordExeption e) {
			System.err.println("Invalid record was retrieved from DB!");
			return null;
		}
    	return r;
    }

    public static boolean init() {

    	if ( !loadDriver() )
    		return false;
    	
    	Properties props = new Properties();
    	Statement s = null;
    	try {
			conn = DriverManager.getConnection(PROTOCOL + DBNAME + ";create=true", props);
			conn.setAutoCommit(true);
			s = conn.createStatement();
		} catch (SQLException e) {
			System.err.println("Unable to connect to the database file");
            return false;
		}
    	
    	System.out.println("Connected to the database succesfully");

    	try {
			s.execute(CREATE_DB_QUERY);
		} catch (SQLException e) {
			System.out.println("Device Records table already exists.");
		}
		
		return true;
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
