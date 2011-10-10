package cste.dcp;

import cste.dcp.interfaces.DcpDbHandler;

public class DcpDbHandlerImpl implements DcpDbHandler{
	static final String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
    static final String PROTOCOL = "jdbc:derby:";
    static final String DBNAME = "derbyDB"; // the name of the database
    
    static final String CREATE_DEVICES_TABLE_QUERY = 	
    	"CREATE TABLE Devices ("+
		"DeviceUID VARCHAR (8) FOR BIT DATA NOT NULL,"+
		"DeviceType VARCHAR(1) FOR BIT DATA NOT NULL,"+
		"LastErrorCode VARCHAR(1) FOR BIT DATA,"+
		"LastStatusData VARCHAR(55) FOR BIT DATA,"+
		"ChangeTime datetime NOT NULL,"+	
		"TckAscNum INTEGER DEFAULT 0)";
    
    static final String CREATE_EVENTLOG_TABLE_QUERY = 	
    	"CREATE TABLE Devices ("+
		"DeviceUID VARCHAR (8) FOR BIT DATA NOT NULL,"+
		"EventType VARCHAR(1) FOR BIT DATA NOT NULL,"+
		"EventTime datetime,"+
		"DeviceStatus VARCHAR(51) FOR BIT DATA NOT NULL)";

	@Override
	public boolean initialize() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean addDeviceRecord(NetDevice device) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeDeviceRecord(NetDevice device) {
		// TODO Auto-generated method stub
		return false;
	}

}
