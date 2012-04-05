package cste.dcp.model;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class DbManager {
	private String dbURL = "";
	private String dbUserName = "";
	private String dbPassword = "";
	
	public void setDbURL(String dbURL){
		this.dbURL = dbURL;
	}
	
	public String getDbURL(){
		return dbURL;
	}
	
	public void setDbUserName(String dbUserName) {
		this.dbUserName = dbUserName;
    }
    
    public String getDbUserName(){
    	return dbUserName;
    }
    
    public void setDbPassword(String dbPassword){
    	this.dbPassword = dbPassword;
    }
    
    public String getDbPassword(){
    	return dbPassword;
    }
    
    /*
     * Open database connection
     */
    public Connection getConnection(){
	    Connection conn = null;
	    try {
	      conn = DriverManager.getConnection(getDbURL(), getDbUserName(),getDbPassword());
	    }
	    catch (SQLException e) {
		      System.out.println("Could not connect to DB");
		      e.printStackTrace();
	    }
	    return conn;
    }
    
    /*
     * Close open database connection
     */
    public void putConnection(Connection conn){
	    if (conn != null){
	    	try{ 
	    		conn.close(); 
	    	}
			catch (SQLException e) {
		      	System.out.println("Enable to close DB connection");
		      	e.printStackTrace(); }
		    }
    }
    
    public void setDeviceAssignements(int userID, ArrayList<Integer> deviceIDs){
    	Connection conn = getConnection();	
    	if (conn != null){
    		PreparedStatement preparedStatement = null;
    		
    		String strQueryClear = "delete from deviceassignment where UserID = ?";
    		String strQueryAdd = "insert into deviceassignment values (?,?)";
    		
    		
    		try {
				preparedStatement = conn.prepareStatement(strQueryClear);
				preparedStatement.setInt(1,userID);
	    		preparedStatement.execute();
	    		preparedStatement.close(); 

	    		for(int i=0; i<deviceIDs.size();i++){
	    			preparedStatement = conn.prepareStatement(strQueryAdd);
	    			preparedStatement.setInt(1,userID);
		    		preparedStatement.setInt(2,deviceIDs.get(i));
		    		preparedStatement.execute();
		    		preparedStatement.close(); 
	    		}

			} catch (SQLException e) {
				e.printStackTrace();
			} finally{
	        	try{ 
	        		preparedStatement.close(); 
	        	}
	          	catch (SQLException e) {
	          		e.printStackTrace();
          		}
	          	putConnection(conn);	        
	        }//end of finally
    		
    	}
    }
    
    public ArrayList<DeviceBean> getDeviceAssignementForUser(int userID){
    	ArrayList<DeviceBean> list = new ArrayList<DeviceBean>();
    	Connection conn = getConnection();	
	    if (conn != null){
	    	ResultSet rs = null;
	    	PreparedStatement preparedStatement = null;
	    	try{
				String strQuery = "SELECT DeviceID, UID, TypeID , TCK, TCK_ASC , " +
						"( select count(*) from deviceassignment where deviceassignment.UserID = ? and  deviceassignment.DeviceID = devices.DeviceID ) as isAssigned " +
						"FROM devices ";

				preparedStatement = conn.prepareStatement(strQuery);
				preparedStatement.setInt(1,userID);
				rs = preparedStatement.executeQuery();
				
				while(rs.next()){
					DeviceBean deviceBean = new DeviceBean();
					deviceBean.setDeviceID(rs.getInt("DeviceID"));
					deviceBean.setDeviceUID(rs.getBytes("UID"));
					deviceBean.setDeviceType(rs.getInt("TypeID"));
					deviceBean.setTck(rs.getBytes("TCK"));
					deviceBean.setTckAsc(rs.getInt("TCK_ASC"));
					deviceBean.setIsAssigned(rs.getInt("isAssigned") > 0);
					list.add(deviceBean);
				}				
			}//end of try
				catch(SQLException ex){
					ex.printStackTrace();
			}
			finally{
	        	try{ 
	        		rs.close();
	        		preparedStatement.close(); 
	        	}
	          	catch (SQLException e) {
	          		e.printStackTrace();
          		}
	          	putConnection(conn);	        
	        }//end of finally
	    }
    	
    	return list;
    }
    
    public ArrayList<UserBean> getUsersList(int userType){
    	ArrayList<UserBean> list = new ArrayList<UserBean>();
    	Connection conn = getConnection();	
	    if (conn != null){
	    	ResultSet rs = null;
	    	PreparedStatement preparedStatement = null;
	    	try{
				String strQuery = "SELECT UserID, Username, Password, UserType , " +
						"( select count(*) from deviceassignment where deviceassignment.UserID = users.UserID  ) as assignedDevCnt " +
						"FROM users " +
						"WHERE UserType=?";

				preparedStatement = conn.prepareStatement(strQuery);
				preparedStatement.setInt(1,userType);
				rs = preparedStatement.executeQuery();
				
				while(rs.next()){
					UserBean userBean = new UserBean();
					userBean.setUserID(rs.getInt("UserID"));
					userBean.setUserName(rs.getString("Username"));
					userBean.setPassword(rs.getString("Password"));
					userBean.setUserType(rs.getInt("UserType"));
					userBean.setAssignedCnt(rs.getInt("assignedDevCnt"));
					list.add(userBean);
				}				
			}//end of try
				catch(SQLException ex){
					ex.printStackTrace();
			}
			finally{
	        	try{ 
	        		rs.close();
	        		preparedStatement.close(); 
	        	}
	          	catch (SQLException e) {
	          		e.printStackTrace();
          		}
	          	putConnection(conn);	        
	        }//end of finally
	    }
    	
    	return list;
    }
    
    
    /*
     * Retrive data of single user
     */
    public UserBean getUserData(String userName, String password)
	{	
    	if( userName.equals(null))
			userName = "";
		if( password.equals(null))
			password = "";
		UserBean userBean = new UserBean();
		Connection conn = getConnection();	
	    if (conn != null){
	    	ResultSet rs = null;
	    	//Statement stmt = null;
	    	PreparedStatement preparedStatement = null;
			try{
				String strQuery = "SELECT UserID, Username, Password, UserType FROM users WHERE Username=? AND Password=?";

				preparedStatement = conn.prepareStatement(strQuery);
				preparedStatement.setString(1,userName);
				preparedStatement.setString(2,password);
				rs = preparedStatement.executeQuery();
				
				while(rs.next()){
					userBean.setUserID(rs.getInt("UserID"));
					userBean.setUserName(rs.getString("Username"));
					userBean.setPassword(rs.getString("Password"));
					userBean.setUserType(rs.getInt("UserType"));
				}				
			}//end of try
				catch(SQLException ex){
					ex.printStackTrace();
			}
			finally{
	        	try{ 
	        		rs.close();
	        		preparedStatement.close(); 
	        	}
	          	catch (SQLException e) {
	          		e.printStackTrace();
          		}
	          	putConnection(conn);	        
	        }//end of finally
	    }//end of if
	    return userBean;
	}

}
