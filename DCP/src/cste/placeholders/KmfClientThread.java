package cste.placeholders;

/***
 * deprecated
 * @author Sergio Enriquez
 *
 */
public class KmfClientThread implements Runnable{
	private static final String TAG = KmfClientThread.class.getName();
	static int kmfServerPort = 0;
	static String kmfServerAddress = "";
	
	static public boolean configure(int port, String address){
		kmfServerPort = port;
		kmfServerAddress = address;
		return true;
	}
	
	@Override
	public void run() {

		
	}
	
	private void sendNewRecordRequest(){
		
	}

}
