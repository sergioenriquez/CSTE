package cste.android.core;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.StringWriter;

import org.apache.commons.net.ftp.*;

import android.util.Log;

public class FtpHandler {
	private static final String TAG = "FTP client";
	public FTPClient mFTPClient = null;
	
	protected String host;
	protected String username;
	protected String password;
	protected int port;

	private String makeTodayKeyFile(){
		return "xsergio.csv";
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
			else
				Log.w(TAG, "Could not open FTP file " + srcFile);
			
		}else
			Log.w(TAG, "Could not connect to FTP server at " + host);
		
		return content;
	}

	public boolean ftpConnect(){
		try {
			mFTPClient = new FTPClient();
			mFTPClient.setDefaultTimeout(1000);
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
			Log.d(TAG, "Error: could not connect to: " + e.getMessage());
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
	        Log.d(TAG, "Error occurred while disconnecting from ftp server.");
	    }
	    return false;
	}
	
	public InputStream ftpOpenStream(String desFilePath){
	    try {
	        return mFTPClient.retrieveFileStream(desFilePath);
	    } catch (Exception e) {
	        Log.d(TAG, "download failed");
	    }
	    return null;
	}
	
	public boolean ftpDownload(String srcFilePath, String desFilePath){
	    boolean status = false;
	    try {
	        FileOutputStream desFileStream = new FileOutputStream(desFilePath);
	        status = mFTPClient.retrieveFile(srcFilePath, desFileStream);
	        desFileStream.close();
	        return status;
	    } catch (Exception e) {
	        Log.d(TAG, "download failed");
	    }

	    return status;
	}
}
