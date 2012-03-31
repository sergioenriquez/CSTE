package cste.android.core;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

public class WebHandler {
	private final String TAG = "Web Client";
	private static final String HTTP_ACCEPT_TYPES = "application/xml, text/xml; q=0.9, text/html, text/*; q=0.4";
	private final int  TIMEOUT_PERIOD = 1000;
	public CommandResult lastError;
	
	public String requestResource(String username, String password, URI resource) {
		HttpGet request = new HttpGet(resource);
        request.addHeader("Accept", HTTP_ACCEPT_TYPES);

        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT_PERIOD);
        HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUT_PERIOD);        
        HttpClient client = new DefaultHttpClient(httpParameters);
        Credentials creds = new UsernamePasswordCredentials(username, password);    
        
        ((AbstractHttpClient) client).getCredentialsProvider().setCredentials(
        		new AuthScope( resource.getHost() , resource.getPort()), creds);        

        HttpResponse httpResponse = null;

        Log.i(TAG, "Sending request to: " + request.getURI().toString());

        try {
        	httpResponse = client.execute(request);
        } 
        catch(HttpResponseException e){
        	if ( e.getMessage().equalsIgnoreCase("unauthorized") )
        		lastError = CommandResult.AUTHENTICATION;
        	else
        		lastError = CommandResult.RESOURCENOTFOUND;
        }
        catch (UnknownHostException e) {
            lastError = CommandResult.HOSTNOTFOUND;
        }
        catch (ClientProtocolException e) {
        	Log.e(TAG, "ClientProtocolException", e);
        	lastError = CommandResult.OTHER;
        } 
        catch( HttpHostConnectException e){
        	lastError = CommandResult.HOSTNOTFOUND;
        }
        catch (ConnectTimeoutException e) {
            Log.e(TAG, "Timeout exception", e);
            lastError = CommandResult.NETWORK_ERROR;
        }catch(Exception e){
        	Log.e(TAG, "Unknown exception", e);
            lastError = CommandResult.OTHER;
        }
        
        if (httpResponse == null){
        	client.getConnectionManager().shutdown();
        	return null;
        }
        
        int responseCode = httpResponse.getStatusLine().getStatusCode();
        String response = "";
        
		switch(responseCode){
		case 200:// expected response if successful
			try {
				response =  new java.util.Scanner( httpResponse.getEntity().getContent() ,"UTF-8").useDelimiter("\\A").next();
			} catch (IllegalStateException e) {
				Log.e(TAG, "Input stream exception", e);
				lastError = CommandResult.OTHER;
			} catch (IOException e) {
				Log.e(TAG, "Input stream exception", e);
				lastError = CommandResult.OTHER;
			}
		case 401:
			lastError = CommandResult.AUTHENTICATION;
		case 404:
			lastError = CommandResult.RESOURCENOTFOUND;
		case 400:
			lastError = CommandResult.BADREQUEST;
		case 412:
			lastError = CommandResult.PRECONDITIONFAILED;
		default:
			lastError = CommandResult.OTHER;
		}     
		return response;
    }
	
	public static enum CommandResult{
		SUCCESS,
		NETWORK_ERROR,
		AUTHENTICATION,
		HOSTNOTFOUND,
		RESOURCENOTFOUND,
		BADREQUEST,
		PRECONDITIONFAILED,
		OTHER
	}

}
