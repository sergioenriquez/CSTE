package cste.dcp.web;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import cste.dcp.model.DataManager;
import cste.dcp.model.DeviceBean;
import cste.dcp.model.UserBean;

/**
 * Servlet implementation class AdminServlet
 */
public class AdminServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private DataManager dataManager;
	private boolean dbOK = false;
	private String strError = null;
	
	private UserBean userBean;
	private ArrayList<UserBean> userList;
	private ArrayList<DeviceBean> deviceList;
	
	private static final int ADMIN_TYPE = 1;
	private static final int WORKER_TYPE = 2;
       
	public void init(ServletConfig config) throws ServletException{
    	super.init(config);
		dataManager = new DataManager();
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
		String selectedWorkerID = request.getParameter("selectedWorker");

		HttpSession session = request.getSession(true);
		if( selectedWorkerID != null)
		{
			int workerID = Integer.parseInt(selectedWorkerID);
			if( formAction.equals("setUser")){
				ArrayList<DeviceBean> assignedDevices = dataManager.getDeviceAssignementForUser(workerID);
				session.setAttribute("devices", assignedDevices );
				session.setAttribute("selectedWorkerID", workerID);
			}else{
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
			}

		}

		RequestDispatcher dispatcher = request.getRequestDispatcher("/admin.jsp");
		dispatcher.forward( request, response);	
	}

}
