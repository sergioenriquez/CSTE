<%@ page language="java" contentType="text/html"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title>Login Page</title>
<link rel="stylesheet" type="text/css" href="css/style1.css" />
</head>
<body>

<div class="error">
<% String e = (String) session.getAttribute("error" );
if(e != null)
{
	out.print(e); 
}%>
</div>

<form method="post" action="login">
	<table align="left">
		<tr style="border-bottom:none;">
			<th class="leftLogo" colspan="2" align="center">DCP Administrator Login</th>
		</tr>
		<tr>
			<td class="tdOne">Username : </td>
			<td class="tdTwo"><input type="text" id="userName" name="userName" size="15" maxlength="8" /></td>
		</tr>
		<tr>
			<td class="tdOne">Password : </td>
			<td class="tdTwo"><input type="password" id="password" name="password" size="15" maxlength="20" /></td>
		</tr>
		<tr>
			<td colspan="2" align="right"><input type="submit" value="Login" /></td>
		</tr>
	</table>
</form>

</body>
</html>
