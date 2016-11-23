import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ServerKiller implements Runnable
{
	// The server that will be killed
	private Server server;
	
	private JFrame killerPopup;
	
	public ServerKiller(Server server)
	{
		this.server = server;
		killerPopup = new JFrame();
	}

	@Override
	public void run() 
	{
		while(true)
		{
			// Last step of the loop is to ask the user if they want to kill the client			
			if(JOptionPane.showConfirmDialog(killerPopup,"Kill the Server:", "Kill Server", JOptionPane.YES_NO_OPTION) == 0)
			{
				server.killServer = true;
				break;
			}
		}
	}
}
