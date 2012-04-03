package org.fern.rest.android.dataObj;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.fern.rest.android.NodeType;
import org.fern.rest.android.dataObj.DataEventReceiver.CommandResult;
import org.fern.rest.android.task.Task;
import org.fern.rest.android.user.User;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.content.Context;
import android.util.Log;

/**
 * Handles the sending or retrieval of task data with the web server
 * @author user
 *
 */
public class WebClient {
	private String lastEtag = null;
	private XPath xp;
	private final String TAG = this.getClass().getSimpleName();
	private static final String HTTP_ACCEPT_TYPES = "application/xml, text/xml; q=0.9, text/html, text/*; q=0.4";
	private final int  TIMEOUT_PERIOD = 10000;
	private CommandResult lastError;

	public WebClient(Context ctx){
		this.xp = XPathFactory.newInstance().newXPath();
	}
	
	/**
	 * If the last request was unsuscesful, this will return the error code with the reason
	 * @return
	 */
	public CommandResult getLastError(){
		return lastError;
	}
	
	/**
	 * This will return the resulting ETAG header from the web server from the last operation performed
	 * @return
	 */
	public String getLastEtag(){
		return lastEtag;
	}

	/**
	 * If the provided task object has no URI, this function will post a request with the server to 
	 * create a new task, receive a reply with the assigned URI, and store in on the database.
	 * If there is an URI, it will post a request to overwrite the resource with the new data.
	 * @param task
	 * Task object with the new data to store
	 * @return
	 * Returns false if request was unsuccessful, call getLastError() to find out what went wrong 
	 */
	public boolean saveTaskForUser(User user, Task task){
		HttpClient httpclient = new DefaultHttpClient();
	    HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT_PERIOD);
        HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUT_PERIOD);        
        
        Credentials creds = new UsernamePasswordCredentials(user.getName(), user.getPassword());    
        ((AbstractHttpClient) httpclient).getCredentialsProvider().setCredentials(
        		new AuthScope( user.getURI().getHost() , user.getURI().getPort()), creds);        
        
        boolean requestSuccess;
        if ( task.getURI() == null )
        	requestSuccess = createTask(user.getURI(),task,httpclient);
        else
        	requestSuccess = editTask(task,httpclient);
        
        httpclient.getConnectionManager().shutdown();
        return requestSuccess;
	}
	
	/**
	 * Sends a request to the webserver to delete a task resource
	 * @param user
	 * @param task
	 * @return
	 */
	public boolean deleteTaskForUser(User user, Task task){
		HttpClient httpclient = new DefaultHttpClient();
	    HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, TIMEOUT_PERIOD);
        HttpConnectionParams.setSoTimeout(httpParameters, TIMEOUT_PERIOD);        
        
        Credentials creds = new UsernamePasswordCredentials(user.getName(), user.getPassword());    
        ((AbstractHttpClient) httpclient).getCredentialsProvider().setCredentials(
        		new AuthScope( user.getURI().getHost() , user.getURI().getPort()), creds);        
        
        boolean requestSuccess;
        HttpResponse response = null;

		HttpDelete httpDelete = new HttpDelete(task.getURI());

        try {
			response = httpclient.execute(httpDelete);
		} catch (ClientProtocolException e) {
			Log.e(TAG,"task xml encoding error");
			lastError = CommandResult.OTHER;
		}catch( UnknownHostException e){
			Log.e(TAG,"Unknown host error");
			lastError = CommandResult.NETWORK_ERROR;
		} catch (IOException e) {
			Log.e(TAG,"IO error");
			lastError = CommandResult.OTHER;
		} catch( Exception ex){
			Log.e(TAG,ex.getMessage());
			lastError = CommandResult.OTHER;
		}
		
		requestSuccess = false;
		if ( response != null){
			int responseCode = response.getStatusLine().getStatusCode();
			switch(responseCode){
			case 200:
			case 201: 
			case 204:// expected response if successful
				requestSuccess =  true;
				break;
			case 401:
				lastError = CommandResult.AUTHENTICATION;
				break;
			case 404:
				lastError = CommandResult.RESOURCENOTFOUND;
				break;
			case 400:
				lastError = CommandResult.BADREQUEST;
				break;
			case 412:
				lastError = CommandResult.PRECONDITIONFAILED;
				break;
			default:
				lastError = CommandResult.OTHER;
				break;
			}
		}

        httpclient.getConnectionManager().shutdown();
        return requestSuccess;
	}
	
	
	private boolean createTask(URI userUri, Task task, HttpClient httpclient){
		HttpResponse response = null;
		StringEntity se = null;
		HttpPost  httppost = new HttpPost (userUri);
		
		String xmlBody = task.toXML();
		
		try {
			se = new StringEntity( xmlBody ,"UTF-8");
			se.setContentType("application/xml");
			httppost.setEntity(se);
			httppost.addHeader("ETag",task.getEtag());
			httppost.setHeader("Content-Type","application/xml;charset=UTF-8");
		} catch (UnsupportedEncodingException e1) {
			Log.e(TAG,"task xml encoding error");
			lastError = CommandResult.OTHER;
			return false;
		}

        try {
			response = httpclient.execute(httppost);
		} catch (ClientProtocolException e) {
			Log.e(TAG,"task xml encoding error");
			lastError = CommandResult.OTHER;
			return false;
		} catch (IOException e) {
			Log.e(TAG,"task xml encoding error");
			lastError = CommandResult.OTHER;
			return false;
		} catch( Exception ex){
			Log.e(TAG,ex.getMessage());
			lastError = CommandResult.OTHER;
			return false;
		}

		int responseCode = response.getStatusLine().getStatusCode();
		switch(responseCode){
		case 200:
			
			return false;
		case 201: // expected response if successful
			String resourceURI = response.getFirstHeader ("Location").getValue();
			task.setURI(resourceURI);
			return true;
		case 401:
			lastError = CommandResult.AUTHENTICATION;
			return false;
		case 404:
			lastError = CommandResult.RESOURCENOTFOUND;
			return false;
		case 400:
			lastError = CommandResult.BADREQUEST;
			return false;
		case 412:
			lastError = CommandResult.PRECONDITIONFAILED;
			return false;
		default:
			lastError = CommandResult.OTHER;
			return false;
		}
	}
	
	private boolean editTask(Task task, HttpClient httpclient){
		HttpResponse response = null;
		StringEntity se = null;
		HttpPut  httpPut = new HttpPut (task.getURI());
		
		try {
			se = new StringEntity( task.toXML() ,"UTF-8");
			se.setContentType("application/xml");
			httpPut.setEntity(se);
			httpPut.addHeader("If-Match",task.getEtag());
			httpPut.setHeader("Content-Type","application/xml;charset=UTF-8");
		} catch (UnsupportedEncodingException e1) {
			Log.e(TAG,"task xml encoding error");
			lastError = CommandResult.OTHER;
			return false;
		}

        try {
			response = httpclient.execute(httpPut);
		} catch (ClientProtocolException e) {
			Log.e(TAG,"task xml encoding error");
			lastError = CommandResult.OTHER;
			return false;
		} catch (IOException e) {
			Log.e(TAG,"task xml encoding error");
			lastError = CommandResult.OTHER;
			return false;
		}

		int responseCode = response.getStatusLine().getStatusCode();
		switch(responseCode){
		case 202: // request accepted but not finished
		case 204: // request accepted and finished
			return true;
		case 401:
			lastError = CommandResult.AUTHENTICATION;
			return false;
		case 404:
			lastError = CommandResult.RESOURCENOTFOUND;
			return false;
		case 400:
			lastError = CommandResult.BADREQUEST;
			return false;
		case 412:
			lastError = CommandResult.PRECONDITIONFAILED;
			return false;
		default:
			lastError = CommandResult.OTHER;
			return false;
		}
	}

	/**
	 * Queries web server for the user task list
	 * @param userURI
	 * @return
	 */
	public List<Task> getTaskListForUser(User user){
	    URI userURI = user.getURI();
		Document doc = processRequest(user, userURI);
		List<Task> taskList = new ArrayList<Task>();
		
		if ( doc == null)
			return null;

		NodeList out = (NodeList) evaluate("//link[@rel='"
                + "http://danieloscarschulte.de/cs/tm/taskDescription" + "']", doc,
                XPathConstants.NODESET);

		List<String> links = new ArrayList<String>();
		
        Node node;
        for (int i = 0; i < out.getLength(); i++) {
            node = out.item(i);
            String href = node.getAttributes().getNamedItem("href").getTextContent();
            links.add(href);
        }
        
        for(String link: links){
        	//URI taskURI = URI.create(link);
        	//Task task = getTaskForUser(user, taskURI);
        	Task task = new Task();
        	task.setURI(link);
        	taskList.add(task);
        }

		return taskList;
	}
	
	private Date parseTimeString(String text){
		String format;
		if ( text.length() == 29)
			format = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
		else
			format = "yyyy-MM-dd'T'HH:mm:ssZ";
		
		SimpleDateFormat inFmt = new SimpleDateFormat(format, Locale.getDefault());
		Date date = new Date();
		
		try {
			date = inFmt.parse(text);
		} catch (ParseException e) {
			Log.e(TAG,"error parsing date");
			return null;
		}
		return date;
	}
	
	/**
	 * Queries server for the task details
	 * @param user
	 * @param taskURI
	 * @return
	 */
	public Task getTaskForUser(User user, URI taskURI){
        Document doc = processRequest(user, taskURI);
        if (doc == null) 
        	return null;

        NodeList out = (NodeList) evaluate("//taskDescription/*", doc, XPathConstants.NODESET);
        Node node;
        Task task = new Task();
        task.setURI(taskURI);
        
        for (int i = 0; i < out.getLength(); i++) {
            node = out.item(i);
            String nodeName = node.getNodeName();
            String text = node.getTextContent();
            switch (NodeType.fromNodeName(nodeName)) {
                case NODE_TASK_ACTIVATION_TIME:
                	task.setActivatedDate( parseTimeString(text));
				break;
                case NODE_TASK_ADDITION_TIME:
                	task.setAdditionDate(parseTimeString(text));
				break;
                case NODE_TASK_EXPIRATION_TIME:
                	task.setExpirationDate(parseTimeString(text));
				break;
                case NODE_TASK_MODIFICATION_TIME:
                	task.setModifiedDate(parseTimeString(text));
				break;
                case NODE_TASK_ESTIMATED_DURATION:
                    // TODO (QueryTaskDescrition) Write parse duration string (see xsd:duration)
                    break;
                case NODE_TASK_NAME:
                    task.setName(text);
                    break;
                case NODE_TASK_PRIORITY:
                    task.setPriority(text);
                    break;
                case NODE_LINK:
                	//To be added once the server supports this
                    break;
                case NODE_TASK_PROCESS_PROGRESS:
                    task.setProcessProgress(Integer.parseInt(text));
                    break;
                case NODE_TASK_PROGRESS:
                    task.setProgress(Integer.parseInt(text));
                    break;
                case NODE_TASK_STATUS:
                    task.setStatus(text);
                    break;
                case NODE_TASK_TYPE:
                    task.setType(text);
                    break;
                case NODE_TASK_DEPENDENT_TASK:
                    Node newNode = (Node) evaluate("link/@href", node,
                            XPathConstants.NODE);
                    if (newNode != null) {
                    	//To be added once the server supports this
                    }
                    break;
                case NODE_TASK_TAG:
                	task.linkTag(text);
                    break;
                case NODE_TASK_DETAIL:
                	task.setDetails(text);
            }
        }
		return task;
	}

	/**
	 * To be added once the server supports this
	 * @param user
	 * @return
	 */
	public boolean deleteUser(User user){
		return false;
	}

	/**
	 * Checks to see if provided credentials are valid and stores them for future use. Needs to be called again
	 * if data for another user is needed.
	 * @param userURI
	 * @param userName
	 * @param password
	 * @return
	 */
	public boolean authenticateUser(URI userURI, String userName, String password){
		return processRequest(userName, password, userURI) != null;
	}
	
	private Document processRequest(User user, URI resource) {
	    return processRequest(user.getName(), user.getPassword(), resource);
	}
	
	private String inputStreamToString(InputStream is) throws IOException {
	    String s = "";
	    String line = "";
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));
		while ((line = rd.readLine()) != null){ 
			s += line; 
		}
	    return s;
	}

	/**
	 * Retrieves a resource from the webserver based on the given URI
	 * @param username
	 * @param password
	 * @param resource
	 * @return
	 */
	private Document processRequest(String username, String password, URI resource) {
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
		switch(responseCode){
		case 200:// expected response if successful
			String response = null;
			try {
				response = inputStreamToString(httpResponse.getEntity().getContent());
			} catch (IllegalStateException e) {
				Log.e(TAG, "Input stream exception", e);
				lastError = CommandResult.OTHER;
			} catch (IOException e) {
				Log.e(TAG, "Input stream exception", e);
				lastError = CommandResult.OTHER;
			}
			lastEtag = httpResponse.getFirstHeader ("ETAG").getValue();
	        Log.i(TAG, "Response length: " + response.length());
	        Log.i(TAG, "Response:" + response);
	        client.getConnectionManager().shutdown();
	        return stringToDocument(response);
		case 201: 
			return null;
		case 401:
			lastError = CommandResult.AUTHENTICATION;
			return null;
		case 404:
			lastError = CommandResult.RESOURCENOTFOUND;
			return null;
		case 400:
			lastError = CommandResult.BADREQUEST;
			return null;
		case 412:
			lastError = CommandResult.PRECONDITIONFAILED;
			return null;
		default:
			lastError = CommandResult.OTHER;
			return null;
		}        
    }
	
	/**
     * Evaluate a given XPath String over a Document or Node. See @see
     * java.xml.xpath for more information on XPath. This works a lot like @see
     * #evaluate(String,Object), except that this one returns any typed that can
     * be found in @see java.xml.xpath.XPathConstants.
     * 
     * @param xpath The XPath string to search for.
     * @param doc The document (or node) to search.
     * @param qn the constant from @see java.xml.xpath.XPathConstants to use.
     * @return
     */
    protected Object evaluate(String xpath, Object doc, QName qn) {
        Object out = null;
        try {
            out = xp.evaluate(xpath, doc, qn);
        } catch (XPathExpressionException e) {
            Log.e(TAG, "XPathExpressionException", e);
        }
        return out;
    }
	
	private Document stringToDocument(String str) {
        Document d = null;
        try {
            d = DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .parse(new InputSource(new StringReader(str)));
        } catch (SAXException e) {
            Log.e(TAG, "SAXException", e);
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);
        } catch (ParserConfigurationException e) {
            Log.e(TAG, "ParserConfigurationExceptoin", e);
        }
        return d;
    }
}
