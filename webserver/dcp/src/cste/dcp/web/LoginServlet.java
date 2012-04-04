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
import cste.dcp.model.UserBean;

/**
 * Servlet implementation class LoginServlet
 */
public class LoginServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	private UserBean userBean;
	private ArrayList<UserBean> userList;
	
	private DataManager dataManager;
	private boolean dbOK = false;
	private String strError = null;
	
	private static final int ADMIN_TYPE = 1;
	private static final int WORKER_TYPE = 2;

	/***
	 * 
	 */
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
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException{
    	HttpSession session = request.getSession(true);

    	userBean = (UserBean)session.getAttribute("userBean");
		
		if(userBean != null && userBean.getUserType() != 0){
			if( userBean.getUserType() == ADMIN_TYPE ){
				RequestDispatcher dispatcher = request.getRequestDispatcher("/admin.jsp");
				dispatcher.forward( request, response);	
			}else if (userBean.getUserType() == WORKER_TYPE){
				RequestDispatcher dispatcher = request.getRequestDispatcher("/worker.jsp");
				dispatcher.forward( request, response);	
			}else{
				session.removeAttribute("userBean");
				strError = "User type has no access";
				session.setAttribute("error", strError);
				RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
				dispatcher.forward( request, response);	
			}
		}else{
			strError = "You are not logged in!";
			session.setAttribute("error", strError);
			RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
			dispatcher.forward( request, response);	
		}
    }

    /**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/* 
		 * Get user data fro submited form
		 */		
		String userName = request.getParameter("userName");
		String password = request.getParameter("password");
		
		if( userName.equals(null))
			userName = "";
		if( password.equals(null))
			password = "";
		
		dbOK = false;
		if(validate(userName, password)){
			userBean = new UserBean();
			userBean = dataManager.getUserData(userName, password);
			
			try{
				if( userBean.getUserName() != null )
					dbOK = true;
			}
			catch(NullPointerException npe){
				System.out.println("Error on DB return");
				npe.printStackTrace();
			}
		}

		HttpSession session = request.getSession(true);
		session.setAttribute("authenticated",true);
		
		if(dbOK){
			session.setAttribute("userBean", userBean);		
			
			if(userBean.getUserType() == ADMIN_TYPE){
				ArrayList<UserBean> workerList = dataManager.getUsersList(WORKER_TYPE);
				session.setAttribute("workers", workerList);
				
				RequestDispatcher dispatcher = request.getRequestDispatcher("/admin.jsp");
				dispatcher.forward( request, response);		
			}
			else if(userBean.getUserType() == WORKER_TYPE){
				RequestDispatcher dispatcher = request.getRequestDispatcher("/worker.jsp");
				dispatcher.forward( request, response);	
			}
			else{
				strError = "User type has no access";
				session.setAttribute("error", strError);
				RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
				dispatcher.forward( request, response);	
			}
		}
		else{
			//Error after DB login checkout, redirect back to index.jsp
			strError = "Invalid username or password.";
			session.setAttribute("error", strError);
			RequestDispatcher dispatcher = request.getRequestDispatcher("/index.jsp");
			dispatcher.forward( request, response);	
		}
	}
	
	public boolean validate(String userName, String password){
		if(userName.length() >45 || password.length() > 45){
			return false;
		}
		return true;
	}  

}
