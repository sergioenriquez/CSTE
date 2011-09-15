package cste.dcp;

public class KmfClientThread implements Runnable{
	static int kmfServerPort = 0;
	static String kmfServerAddress = "";
	
	static public boolean configure(int port, String address)
	{
		kmfServerPort = port;
		kmfServerAddress = address;
		
		
		return true;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	private void sendNewRecordRequest()
	{
		
	}

}
