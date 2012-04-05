package cste.dcp.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cste.dcp.model.DbManager;
import cste.dcp.model.DeviceBean;
import cste.dcp.model.FtpHandler;
import cste.dcp.model.UserBean;
import static cste.dcp.model.Utility.*;

import java.io.Serializable;



/**
 * Servlet implementation class AdminServlet
 */
public class AdminServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DbManager dataManager;
	private boolean dbOK = false;
	private String strError = null;
	
	private UserBean userBean;
	private ArrayList<UserBean> userList;
	private ArrayList<DeviceBean> deviceList;
	
	private static final int ADMIN_TYPE = 1;
	private static final int WORKER_TYPE = 2;
	private FtpHandler ftpHandler;
	
	private String ftpHost;
       
	public void init(ServletConfig config) throws ServletException{
    	super.init(config);
    	ftpHost = config.getInitParameter("ftpHost");
    	ftpHandler = new FtpHandler();
    	ftpHandler.configureHost(
    			config.getInitParameter("ftpHost"), 
    			config.getInitParameter("ftpUsername"), 
    			config.getInitParameter("ftpPassword"), 21);
    	
		dataManager = new DbManager();
	    dataManager.setDbURL(config.getInitParameter("dbURL"));
	    dataManager.setDbUserName(config.getInitParameter("dbUserName"));
	    dataManager.setDbPassword(config.getInitParameter("dbPassword"));
		try{
			Class.forName(config.getInitParameter("jdbcDriver"));
		}
		catch (Exception ex){
			System.out.println("Initialize connector string");
			ex.printStackTrace();
		}
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {	
		HttpSession session = request.getSession(true);

		userBean = (UserBean)session.getAttribute("userBean");
		if( userBean == null || userBean.getUserType() != ADMIN_TYPE){
			RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
			dispatcher.forward( request, response);	
		}else{
			ArrayList<UserBean> workerList = dataManager.getUsersList(WORKER_TYPE);
			session.setAttribute("workers", workerList);
			
			RequestDispatcher dispatcher = request.getRequestDispatcher("/admin.jsp");
			dispatcher.forward( request, response);	
		}	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String formAction = request.getParameter("action");

		if( formAction.equals("setUser") )
			processSetUserRequest(request,response);
		else if(formAction.equals("setDevices"))
			processSetAssignementsRequest(request,response);
		else if(formAction.equals("authenticateHnad"))
			processHnadAuthenticationRequest(request,response);
		else{
			//error
			RequestDispatcher dispatcher = request.getRequestDispatcher("/admin.jsp");
			dispatcher.forward( request, response);	
		}
	}
	
	protected void processSetUserRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		HttpSession session = request.getSession(true);
		String selectedWorkerID = request.getParameter("selectedWorker");
		if( selectedWorkerID != null){
			int workerID = Integer.parseInt(selectedWorkerID);
			
			ArrayList<DeviceBean> assignedDevices = dataManager.getDeviceAssignementForUser(workerID);
			session.setAttribute("devices", assignedDevices );
			session.setAttribute("selectedWorkerID", workerID);
		}
		
		RequestDispatcher dispatcher = request.getRequestDispatcher("/admin.jsp");
		dispatcher.forward( request, response);	
	}
	
	protected void processSetAssignementsRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		HttpSession session = request.getSession(true);
		String selectedWorkerID = request.getParameter("selectedWorker");
		if( selectedWorkerID != null){
		
			int workerID = Integer.parseInt(selectedWorkerID);
			
			String[] assignedDevs = request.getParameterValues("assignedDevs");
			ArrayList<Integer> assignements = new ArrayList<Integer>();
			if( assignements != null){
				for(int i=0;i<assignedDevs.length;i++){
					int val = Integer.parseInt(assignedDevs[i]);
					assignements.add(val);
				}
			}
			dataManager.setDeviceAssignements(workerID, assignements);
			
			ArrayList<UserBean> workerList = dataManager.getUsersList(WORKER_TYPE);
			session.setAttribute("workers", workerList);
	
			ArrayList<DeviceBean> assignedDevices = dataManager.getDeviceAssignementForUser(workerID);
			session.setAttribute("devices", assignedDevices );
			
			UserBean workerBean = null;
			for(int i=0; i<workerList.size();i++){
				if( workerList.get(i).getUserID() == workerID )
					workerBean = workerList.get(i);
			}
			
			if(workerBean != null)
				if ( !storeFtpKeyAssignementFile(workerBean,assignedDevices) );
					session.setAttribute("error", "FTP upload error");
		}
		
		RequestDispatcher dispatcher = request.getRequestDispatcher("/admin.jsp");
		dispatcher.forward( request, response);	
	}

	protected void processHnadAuthenticationRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		HttpSession session = request.getSession(true);
		String userName = request.getParameter("userName");
		String password = request.getParameter("password");
		
		UserBean hnadUser = dataManager.getUserData(userName, password);
		PrintWriter toClient = response.getWriter();
		if ( hnadUser.getUserName() != null){
			String ftpServer = ftpHost;
			String ftpUsername = "x" + hnadUser.getUserName();
			String ftpPassword = "pass"; // TODO one time pasword generation

//			session.setAttribute("host", ftpServer);
//			session.setAttribute("username", ftpUsername);
//			session.setAttribute("password", ftpPassword); 

			toClient.print(ftpServer);
			toClient.print(",");
			toClient.print(ftpUsername);
			toClient.print(",");
			toClient.print(ftpPassword);
		}else
			toClient.println("error");
			session.setAttribute("error", "Invalid username or password");
//		
//		RequestDispatcher dispatcher = request.getRequestDispatcher("/admin.jsp");
//		dispatcher.forward( request, response);	
		
		toClient.close();
	}
	
	protected boolean storeFtpKeyAssignementFile(UserBean workerBean, ArrayList<DeviceBean> assignedDevices) throws UnsupportedEncodingException{
		String destinationFolder = "x" + workerBean.getUserName();
		String filename = destinationFolder + ".csv";
		StringBuilder fileContentStr = new StringBuilder();
		
		for(int i=0; i<assignedDevices.size();i++){
			
			if( assignedDevices.get(i).getIsAssigned() ){
				fileContentStr.append(assignedDevices.get(i).getDeviceUID());
				fileContentStr.append(",");
				byte []devTypeHex = new byte[]{(byte)assignedDevices.get(i).getDeviceType()};
				fileContentStr.append(hexToStr(devTypeHex));
				fileContentStr.append(",");
				fileContentStr.append(assignedDevices.get(i).getTck());
				fileContentStr.append(",");
				fileContentStr.append(assignedDevices.get(i).getTckAsc());//assension
				fileContentStr.append("\r");
			}
		}
		
		byte []fileContent = fileContentStr.toString().getBytes("UTF-8");
		return ftpHandler.uploadKeyAssignements(destinationFolder, filename, fileContent);
	}

}
