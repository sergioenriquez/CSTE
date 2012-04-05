package cste.dcp.model;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.net.ftp.*;

public class FtpHandler {
	private static final String TAG = "FTP client";
	public FTPClient mFTPClient = null;
	
	protected String host;
	protected String username;
	protected String password;
	protected int port;

	private String makeTodayKeyFile(){
		return "keys_300312.csv";
	}
	
	public void configureHost(String host, String username, String password, int port){
		this.host = host;
		this.username = username;
		this.password = password;
		this.port = port;
	}
	
	public String getKeysFileContent(){
		String content = ""; 
		if( ftpConnect() ){
			String srcFile = makeTodayKeyFile();
			InputStream is = ftpOpenStream(srcFile);
			if( is != null)
				content = new java.util.Scanner(is,"UTF-8").useDelimiter("\\A").next();
			//else
				//Log.w(TAG, "Could not open FTP file " + srcFile);
			
		}//else
			//Log.w(TAG, "Could not connect to FTP server at " + host);
		
		return content;
	}

	public boolean ftpConnect(){
		try {
			mFTPClient = new FTPClient();
			// connecting to the host
			mFTPClient.connect(host, port);
		
			// now check the reply code, if positive mean connection success
			if (FTPReply.isPositiveCompletion(mFTPClient.getReplyCode())) {
				// login using username & password
				boolean status = mFTPClient.login(username, password);
				/* Set File Transfer Mode*/
				mFTPClient.setFileType(FTP.BINARY_FILE_TYPE);
				mFTPClient.enterLocalPassiveMode();
				return status;
			}
		} catch(Exception e) {
			//Log.d(TAG, "Error: could not connect to: " + e.getMessage());
		}
		return false;
	}
	
	public boolean ftpDisconnect()
	{
	    try {
	        mFTPClient.logout();
	        mFTPClient.disconnect();
	        return true;
	    } catch (Exception e) {
	        //Log.d(TAG, "Error occurred while disconnecting from ftp server.");
	    }
	    return false;
	}
	
	public InputStream ftpOpenStream(String desFilePath){
	    try {
	        return mFTPClient.retrieveFileStream(desFilePath);
	    } catch (Exception e) {
	        //Log.d(TAG, "download failed");
	    }
	    return null;
	}
	
	public boolean ftpChangeDirectory(String directory_path)
	{
		boolean status = false;
	    try {
	    	status = mFTPClient.changeWorkingDirectory(directory_path);
	    } catch(Exception e) {
	        //Log.d(TAG, "Error: could not change directory to " + directory_path);
	    }

	    return status;
	}
	
	public boolean uploadKeyAssignements(String desDirectory, String dstFileName, byte[] fileContent){
		
		boolean status = false;
		if(ftpConnect()){

			status = ftpChangeDirectory(desDirectory);
			
			if (!status){
				status = ftpMakeDirectory(desDirectory);
				
				if (status)
					status = ftpChangeDirectory(desDirectory);
			}
			
			if( status )
				status = ftpUpload(desDirectory, dstFileName, fileContent);
			
			ftpDisconnect();
		}
		
		return status;
	}

	
	public boolean ftpUpload(String desDirectory, String dstFileName, byte[] fileContent){
	    boolean status = false;
	    try {
	        ByteArrayInputStream srcFileStream = new ByteArrayInputStream(fileContent);
	        status = mFTPClient.storeFile(dstFileName, srcFileStream);
	    } catch (Exception e) {
	        //Log.d(TAG, "download failed");
	    }
	    return status;
	}
	
	public boolean ftpDownload(String srcFilePath, String desFilePath){
	    boolean status = false;
	    try {
	        FileOutputStream desFileStream = new FileOutputStream(desFilePath);
	        status = mFTPClient.retrieveFile(srcFilePath, desFileStream);
	        desFileStream.close();
	        return status;
	    } catch (Exception e) {
	        //Log.d(TAG, "download failed");
	    }

	    return status;
	}
	
	public boolean ftpMakeDirectory(String new_dir_path)
	{
	    try {
	        boolean status = mFTPClient.makeDirectory(new_dir_path);
	        return status;
	    } catch(Exception e) {
	        //Log.d(TAG, "Error: could not create new directory named " + new_dir_path);
	    }

	 return false;
	}
}
