/**
 * Project Server class
 * SYSC 3303 
 * Andrew Ward, Alex Hoecht, Connor Emery, Robert Graham, Saleem Karkabi
 * 100898624,   100933730,   100980809,    100981086,     100944655
 * Fall Semester 2016
 * 
 * Server Class
 */

import java.io.*;
import java.net.*;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;


public class Server 
{
	// The Datagrams used by the server to receive
	private DatagramSocket receiveSocket;
	private DatagramPacket receivePacket;
	
	// The Server Directory
	private File serverDir;
	// Path of the current directory used to transfer files to and from
	private String directoryPath;
	
	// The Client Connection Thread
	private Thread t;
	// The path for the file being transferred
	private boolean hasThreadStarted = false;
	
	// Frame for the pop up windows to use
	private JFrame popupWindow = new JFrame();
	
	/*
	 * The main constructor for the Server class.
	 * When the Server is initialized, it receives at port 69 and creates the Directory
	 */
	public Server()
	{
		try
		{
			// DatagramSocket created to receive(port 69)
			receiveSocket = new DatagramSocket(69);	
		}
		catch(SocketException se)
		{
			se.printStackTrace();
			System.exit(1);
		}
		
		// Create the Server Directory
		serverDir = new File("Server Directory");
		// If the directory doesnt already exist, create it
		if(!serverDir.exists())
		{
			try
			{
				//If the directory is successfully created
				serverDir.mkdir();
			}
			catch(SecurityException se)
			{
				System.exit(1);
			}
		}
	}
	
	
	/*
	 * The behaviour of the Server is as follows:
	 *  Forever:
	 * 		- A packet is created to receive DatagramPackets into
	 * 		- The Server receives a packet and prints all important information about it
	 * 		- A Client connection thread (SubServer) is then created to handle the file transfer
	 */
	public void serverAlgorithm()
	{
		// Sets the default file path for the Server Directory 
		directoryPath = serverDir.getAbsolutePath().replace('\\',  '/');
		System.out.println("Directory " + directoryPath + "\n");
		
		Thread k = new Thread(new KillerThread(this));
		k.start();
		
		// Loop Here!!
		while(true)
		{
			// byte arrays created to pack and unpacked data
			byte[] msg = new byte[50];
			byte[] data = new byte[4];
			
			// That packet that will receive the Packet from the Client
			receivePacket = new DatagramPacket(msg,msg.length);
			System.out.println("Server is waiting for a packet");
			
			try
			{		
				//Slow the program down to simulate wait time
				try
				{
					Thread.sleep(5000);
				}
				catch(InterruptedException e)
				{
					e.printStackTrace();
					System.exit(1);
				}
				
				//Receive the packet
				receiveSocket.receive(receivePacket);
			}
			catch(IOException e)
			{
				System.out.println("Error\n" + e);
				e.printStackTrace();
				System.exit(1);
			}
		
			System.out.println("Server has received a packet");
			
			String request = "";
			
			try
			{
				//Invalid request receive
				if(msg[1] == 0)
				{
					throw new NoSuchFieldException();
				}
				//Read request receive
				if (msg[1] == 1)
				{
					request = "Read";
					data[1] = 3;
				}
				//Write request receive
				if(msg[1] == 2)
				{
					request = "Write";
					data[1] = 4;
				}
				
			}
			
			catch(NoSuchFieldException e)
			{
				JOptionPane.showMessageDialog(popupWindow, "Invalid Request received...! \n" + "Quitting");
				System.out.println("Invalid Request..... Quitting");
				System.exit(1);
			}
			
			//Parsing the packet received for valid format
			System.out.println(request + " Request received");
			byte[] file = new byte[1];
			byte[] mode = new byte[1];
			byte[] msgBytes = new byte[1];
			int count = 0;
			
			for(int i = 2; i < msg.length; i++)
			{		
				if(msg[i] == 0)
				{
					count++;
					if (count == 1)
					{
						file = Arrays.copyOfRange(msg, 2, i);
					}
					if(count == 2)
					{
						mode = Arrays.copyOfRange(msg, 3 + file.length, i);
						break;
					}
				}	
			}
		
			//Printing the information of the received packet
			String fileName = new String(file);
			System.out.println("File Name: " + fileName);
			
			String mode2 = new String(mode);
			System.out.println("Mode: " + mode2);
			
			int len = receivePacket.getData().length;
			System.out.println("Length: " + len);
			
			String infoString = new String(msg,0,len);
			System.out.println("Information as String: " + infoString);

			msgBytes = Arrays.copyOfRange(msg, 0, len);
			System.out.println("Information as Bytes: "+ Arrays.toString(msgBytes) + "\n");
			
			
			System.out.println();
			
			// CREATE THE CLIENT CONNECTION THREAD
			t = new Thread (new SubServer(receivePacket.getPort(), receivePacket.getData(),fileName,data, serverDir, directoryPath));
		    hasThreadStarted = true;
			t.start();
		}

	}
	
	public void kill() throws InterruptedException
	{
		System.out.println("Server has terminated");
		if(hasThreadStarted)
		{
			t.join();
		}
		System.exit(0);
	}
	
	/*
     * The MAIN FUNCTION of the Server class
     */
	public static void main(String[] args)
	{
		Server server = new Server();
		
		server.serverAlgorithm();
	}
}
