package cste.dcp.model;

public class UserBean {
	private int 	userID;		//Username
    private String 	username;   //Password    
    private String 	password;   //First Name
    private int 	userType;    //Last Name
    private int 	assignedCnt;
    private boolean isSelected;

    public UserBean() {}

    public void setUserID(int val) {userID = val;}
    public int getUserID() { return userID;}
    
    public void setUserName(String str) {username = str;}
    public String getUserName() { return username;}

    public void setPassword(String str) {password = str;}
    public String getPassword() {return password;}    

    public void setUserType(int val) {userType = val;}
    public int getUserType() { return userType;}
    
    public void setAssignedCnt(int val) {assignedCnt = val;}
    public int getAssignedCnt() { return assignedCnt;}
    
    public void setIsSelected(boolean val) {isSelected = val;}
    public boolean getIsSelected() { return isSelected;}
}
