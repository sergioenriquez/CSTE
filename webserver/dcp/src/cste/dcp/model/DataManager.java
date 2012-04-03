package cste.dcp.model;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class DataManager {
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
    
    
    /*
     * Retrive data of single user
     */
    public UserBean getUserData(String userName, String password)
	{	
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
