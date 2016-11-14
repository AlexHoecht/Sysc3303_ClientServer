import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class KillerThread implements Runnable {
	
	// The server that will be killed
	private Server server;
	
	private JFrame killerPopup;
	
	public KillerThread(Server s)
	{
		server = s;
		killerPopup = new JFrame();
	}

	@Override
	public void run() 
	{
		// Last step of the loop is to ask the user if they want to kill the client
		int kill = JOptionPane.showConfirmDialog(killerPopup,"Kill the Server:", "Kill Server", JOptionPane.YES_NO_OPTION);
		
		if(kill == 0)
		{
			try 
			{
				server.kill();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}

}
