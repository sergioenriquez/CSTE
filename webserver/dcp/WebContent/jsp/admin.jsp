<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.ArrayList"%>
<%@ page import="cste.dcp.model.UserBean" %>
<%@ page import="cste.dcp.model.DeviceBean" %>


<jsp:useBean id="userBean" class="cste.dcp.model.UserBean" scope="session" />

<jsp:useBean id="workers" class="java.util.ArrayList" scope="session" />
<jsp:useBean id="devices" class="java.util.ArrayList" scope="session" />

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>

<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Device Assignemt Page</title>
</head>

<body>

<%
Integer selectedWorkerID = (Integer)session.getAttribute("selectedWorkerID");
if( selectedWorkerID == null) 
	selectedWorkerID = 0;
UserBean selectionUser = new UserBean();
%>

<h2> Administration Page </h2>
<b> User logged in: <%= userBean.getUserName() %> </b>

<form method="post" action="Admin">
	<input type="hidden" name="action" value="setUser">
	<table border="1">
		<tr>
			<th> UserID </th>
			<th> Username </th>
			<th> Assigned Devices </th>
			<th> Selected </th>
		</tr>
		
		<%
		

		for(int i=0; i < workers.size(); i++)
		{
			UserBean user = new UserBean();
			user = (UserBean) workers.get(i);
		 	boolean isSelected = user.getUserID() == selectedWorkerID;
			if( isSelected )
				selectionUser = user;
			 %>
	        <tr>
	        	<td><%= user.getUserID() %></td>
	            <td><%= user.getUserName() %></td>
	            <td><%= user.getAssignedCnt() %></td>
	            <td ><input type="radio" name="selectedWorker" value="<%= user.getUserID() %>" <% if(isSelected){ %> checked <%} %> /></td>
	        </tr>
	   <%}%>
	
	</table>
	<input type="submit" value="Select User" />
</form>

</br>
<b> Devices assigned to: <%= selectionUser.getUserName() %> (<%= selectionUser.getAssignedCnt() %>)</b>

<form method="post" action="Admin">
	<input type="hidden" name="action" value="setDevices">
	<input type="hidden" name="selectedWorker" value="<%= selectedWorkerID %>">
	
	<table border="1">
		<tr>
			<th> Device ID </th>
			<th> UID </th>
			<th> Type ID </th>
			<th> TCK </th>
			<th> TCK Assension </th>
			<th> Assigned </th>
		</tr>
		
		<%
	
		for(int i=0; i < devices.size(); i++)
		{
			DeviceBean device = new DeviceBean();
			device = (DeviceBean) devices.get(i);
		%>
	        <tr>
	        	<td><%= device.getDeviceID() %></td>
	            <td><%= device.getDeviceUID() %></td>
	            <td><%= device.getDeviceType() %></td>
	            <td><%= device.getTck() %></td>
	            <td><%= device.getTckAsc() %></td>
	            <td><input type="checkbox" name="assignedDevs" value="<%= device.getDeviceID() %>" <% if(device.getIsAssigned()){ %> checked <%} %>/></td>
	        </tr>
	   <%}%>
	
	</table>
	<input type="submit" value="Submit Assignement" />
</form>

</body>

</html>