CSTE Authentication Server setup

Install Apache Tomcat, MySQL, Eclipse EE, and FTP server (like Filezilla )

Setup Eclipse to use local PC Tomcat installation
Import cste.sql file into MySQl installation to load tables
Import DCP webserver project into Eclipse EE
Run DCP project website on Eclipse

Create FTP Admin account with read/write access
Create user accounts on FTP server that match the ones on the user table on the SQL server
set the home directoy for the FTP users

Update the Tomcat server's DCP init-params on the web.xml file to have the right usernames,passwords,addresses