<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>
<%@ page import="java.util.ArrayList"%>

<% ArrayList<String> sampleData = (ArrayList<String>) session.getAttribute("sampleData" );
if( sampleData == null)
{
	sampleData = new ArrayList<String>();
}%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
Worker page!

<%for(int i=0; i < sampleData.size(); i++)
{
	String val = sampleData.get(i);
%>
	<p><%= val %></p>

<%}%>

</body>
</html>