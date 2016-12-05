/**
 * Project ErrorSimulator class
 * SYSC 3303 L2
 * Andrew Ward, Alex Hoecht, Connor Emery, Robert Graham, Saleem Karkabi
 * 100898624,   100933730,   100980809,    100981086,     100944655
 * Fall Semester 2016
 * 
 * ErrorSimulator Class
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

/**
* An Error simulator to generate errors on specific packets and pass them on
**/

public class ErrorSimulator 
{

	private DatagramPacket sendPacket, receivePacket , delayedPacket, errorPacket, wrongPortPacket, tempPacket;
	private int clientPort, serverPort; // client, server ports
	private InetAddress clientIP, serverIP; // variables to store the addresses
	private DatagramSocket receiveSocket, clientSocket, serverSocket, randomSocket; // DatagramSockets
	private boolean serverWait; // used to track which socket to wait at
	private int currentPacketNumber; // used to count the packet number
	private String inputStringError; // used for ERROR input from user
	private Error errorTest; // the actual error object
	private int counter; // Used in the Lost packet to client (breaks lost packet loop)
	private int servercounter; // used in the Lost packet to server (breaks lost packet loop)
	private int delayTime;
	private boolean serverLock = false;
	private boolean goingServer = false;
	private int errorNumberServer;
	private int errorNumberClient;
	private InetSocketAddress address;
	
	private int delayPacketcounter;
	
	private int c;
	
	private ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(4);
	
    
    Callable callabledelayedTask = new Callable()
    {

        @Override
        public String call() throws Exception
        {
       	  //serverSocket.send(sendPacket);
       	  System.out.println("You are genius");
          if( errorTest.getErrorType() == ErrorType.ERROR3){
        	  clientSocket.send(delayedPacket);
          }else if(errorTest.getErrorType() == ErrorType.ERROR4){
        	   serverSocket.send(delayedPacket);
          }
       	  System.out.println("LOOK HERE: " + new String(delayedPacket.getData()));
             return "GoodBye! See you at another invocation...";
        }
    };
	
	
	
	/**
	 * Constructor for the ErrorSimulator
	 */
	public ErrorSimulator()
	{
		
		try
		{
			receiveSocket = new DatagramSocket(23); // construct DatagramSocket and bind to port 23
			serverSocket = new DatagramSocket(); // construct server DatagramSocket
			clientSocket = new DatagramSocket(); // construct Client DatagramSocket
			randomSocket = new DatagramSocket(1025);
			serverIP = InetAddress.getLocalHost();
		}
		catch(SocketException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (UnknownHostException e)
		{
			e.printStackTrace();
		}
		System.out.println("Error simulator is running");
	}
	
	/**
	 * @param error ErrorType pass ERROR0 if no errors (regular WRQ/RRQ)
	 * Algorithm for Error Simulator:
	 * Repeat forever:
	 * 	- Receives a Client packet
	 * 	- Processes the packet and prints the relevant data about it
	 * 	- Repacks the data back into a packet to be sent to the Server
	 * 	- Receives a Server packet
	 * 	- Processes the packet and prints the relevant data about it
	 * 	- Repacks the data back into a packet to be sent to the Client
	 * 
	 */
	public void ErrorSimulatorAlgorithm()
	{

		boolean running = true;
		serverWait = true;
		String ipAddress = JOptionPane.showInputDialog(null,"Specify ipAddress:", "IpAddress", JOptionPane.QUESTION_MESSAGE);
		address = new InetSocketAddress (ipAddress, serverPort);

		while(running)
		{
			
			
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// START OF SENDING TO SERVER INTIAL CLIENT CONNECTION
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////			
			
		boolean error = true;
		BufferedReader bufferReadError = new BufferedReader(new InputStreamReader(System.in));
		while(error)
		{
			System.out.println("\nGenerates the following errors:");
			System.out.println("NOERROR: No error regular WRQ/RRQ");
			System.out.println("ERROR1: Lose packet going to Server");
			System.out.println("ERROR2: Lose packet going to Client");
			System.out.println("ERROR3: Delay packet going to Server");
			System.out.println("ERROR4: Delay packet going to Client");
			System.out.println("ERROR5: Duplicate packet going to Server");
			System.out.println("ERROR6: Duplicate packet going to Client");
			System.out.println("ERROR7: Send to Server with wrong block#");
			System.out.println("ERROR8: Send to Client with wrong block# ");
			System.out.println("ERROR9: Send to Server without filename");
			System.out.println("ERROR10: Send Request to server without mode");
			System.out.println("ERROR11: Send Request to server with invalid mode");
			System.out.println("ERROR12: Send Random Ack to Server");
			System.out.println("ERROR13: Send Random Data to Server");
			System.out.println("ERROR14: Send Random Ack to Client");
			System.out.println("ERROR15: Send Random Data to Client");
			System.out.println("ERROR16: Send Error Packet to Server");
			System.out.println("ERROR17: Send Error Packet to Client");
			System.out.println("ERROR18: Send Packet with less than 4 bytes long to Server");
			System.out.println("ERROR19: Send Packet with less than 4 bytes long to Client");
			System.out.println("ERROR20: Send data less than 4 bytes to Server");
			System.out.println("ERROR21: Send data less than 4 bytes to Client");
			System.out.println("ERROR22: Send ack less than 4 bytes to Server");
			System.out.println("ERROR23: Send ack less than 4 bytes to Client");
			System.out.println("ERROR24: Send to Server from wrong port");
			System.out.println("ERROR25: Send to Client from wrong port");
			System.out.println("ERROR26: Send Data with more than 516 length to Server");
			System.out.println("ERROR27: Send Data with more than 516 length to Client");
			System.out.println("ERROR28: Send invalid Error Packet to Server");
			System.out.println("ERROR29: Send invalid Error Packet to Client");
			

			System.out.println("\nWhat error would you like to test\n");
			
			Scanner in = new Scanner(System.in);
			try
			{
				inputStringError = bufferReadError.readLine();
			}
			catch(IOException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
			
			if(inputStringError.equals("NOERROR"))
			{
				System.out.println("\nContinuing with Regular WRQ/RRQ\n");
				errorTest = new Error(ErrorType.ERROR0,5);
				error = false;
			}
			else if(inputStringError.equals("ERROR1"))
			{
				System.out.println("\nWhat Packet Number would you like to Lose a Packet to the Server on?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR1, i);
				error = false;
				goingServer = true;
			}
			else if(inputStringError.equals("ERROR2"))
			{
				System.out.println("\nWhat Packet Number would you like to Lose a Packet to the Client on?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR2, i);
				error = false;
			}
			else if(inputStringError.equals("ERROR3"))
			{
				System.out.println("\nWhat Packet Number would you like to Delay a Packet to the Server on?\n");
				int i = in.nextInt();
				System.out.println("\nHow long would you like to delay the packet for? (MILLISECONDS) \n");
				delayTime = in.nextInt();
				errorTest = new Error(ErrorType.ERROR3, i);
				goingServer = true;
				error = false;
			}
			else if(inputStringError.equals("ERROR4"))
			{
				System.out.println("\nWhat Packet Number would you like to Delay a Packet to the Client on?\n");
				int i = in.nextInt();
				System.out.println("\nHow long would you like to delay the packet for? (MILLISECONDS) \n");
				delayTime = in.nextInt();
				errorTest = new Error(ErrorType.ERROR4, i);
				error = false;
			}
			else if(inputStringError.equals("ERROR5"))
			{
				System.out.println("\nWhat Packet Number would you like to Duplicate a Packet to the Server on?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR5, i);
				goingServer = true;
				error = false;
			}
			else if(inputStringError.equals("ERROR6"))
			{
				System.out.println("\nWhat Packet Number would you like to Duplicate a Packet to the Client on?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR6, i);
				error = false;
			}
			
			// Wrong Block number
			else if(inputStringError.equals("ERROR7"))
			{
				boolean isizero = true;
				while(isizero)
				{
					System.out.println("\nWhat Packet Number would you like to Send to Server with wrong block #?\n");
					int i = in.nextInt();
					if(i == 0)
					{
						System.out.println("\n0 is an invalid packetnumber for this error\n");
					}
					else
					{
						errorTest = new Error(ErrorType.ERROR7, i);
						error = false;
						goingServer = true;
						isizero = false;
					}
				}
			}
			else if(inputStringError.equals("ERROR8"))
			{
				boolean isizero = true;
				while(isizero)
				{
					System.out.println("\nWhat Packet Number would you like to Send to Client with wrong block #?\n");
					int i = in.nextInt();
					if(i == 0)
					{
						System.out.println("\n0 is an invalid packetnumber for this error\n");
					}
					else
					{
						errorTest = new Error(ErrorType.ERROR8, i);
						error = false;
						isizero = false;
					}
				}
			}
			else if(inputStringError.equals("ERROR9"))
			{
				System.out.println("\nError sim will send request to server without filename\n");
				errorTest = new Error(ErrorType.ERROR9, 0);
				error = false;
			}
			else if(inputStringError.equals("ERROR10"))
			{
				System.out.println("\nError sim will send request to server without mode\n");
				errorTest = new Error(ErrorType.ERROR10, 0);
				error = false;
			}
			else if(inputStringError.equals("ERROR11"))
			{
				System.out.println("\nError sim will send request to server with invalid mode\n");
				errorTest = new Error(ErrorType.ERROR11, 0);
				error = false;
			}
			else if(inputStringError.equals("ERROR12"))
			{
				System.out.println("\nWhat Packet number to send random Ack to Server ");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR12,i);
				goingServer = true;
				error=false;
			}
			else if(inputStringError.equals("ERROR13"))
			{
				System.out.println("\nWhat Packet number to send random Data to Server ");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR13,i);
				goingServer = true;
				error=false;
			}
			else if(inputStringError.equals("ERROR14"))
			{
				boolean isizero = true;
				while(isizero)
				{
					System.out.println("\nWhat Packet Number would you like to Send random Ack to Client?\n");
					int i = in.nextInt();
					if(i == 0)
					{
						System.out.println("\n0 is an invalid packetnumber for this error\n");
					}
					else
					{
						errorTest = new Error(ErrorType.ERROR14, i);
						error = false;
						isizero = false;
					}
				}
			}
			else if(inputStringError.equals("ERROR15"))
			{
				boolean isizero = true;
				while(isizero)
				{
					System.out.println("\nWhat Packet Number would you like to Send random Data to Client?\n");
					int i = in.nextInt();
					if(i == 0)
					{
						System.out.println("\n0 is an invalid packetnumber for this error\n");
					}
					else
					{
						errorTest = new Error(ErrorType.ERROR15, i);
						error = false;
						isizero = false;
					}
				}
			}
			else if(inputStringError.equals("ERROR16"))
			{
				System.out.println("\nWhat Packet would you like to send ErrorPacket to Server on? ");
				int i = in.nextInt();
				
				errorTest = new Error(ErrorType.ERROR16,i);
				goingServer = true;
				error=false;
				
				boolean errorPacketloop = true;
				while(errorPacketloop)
				{
					System.out.println("\nWhat Error Number would you like to send? ");
					errorNumberServer = in.nextInt();
				
					if(errorNumberServer > 7 || errorNumberServer < 0)
					{
						System.out.println("\n Invalid Error Number");
					}
					else
					{
						errorPacketloop = false;
					}
				}
				
			}
			else if(inputStringError.equals("ERROR17"))
			{
				System.out.println("\nWhat Packet would you like to send ErrorPacket to Client on? ");
				int i = in.nextInt();
				
				errorTest = new Error(ErrorType.ERROR17,i);
				error=false;
				
				boolean errorPacketloop = true;
				while(errorPacketloop)
				{
					System.out.println("\nWhat Error Number would you like to send? ");
					errorNumberClient = in.nextInt();
				
					if(errorNumberClient > 7 || errorNumberClient < 0)
					{
						System.out.println("\n Invalid Error Number");
					}
					else
					{
						errorPacketloop = false;
					}
				}
				
			}
			else if(inputStringError.equals("ERROR18"))
			{
				System.out.println("\nWhat Packet Number would you like to send Packet with less than 4 bytes to Server?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR18, i);
				error = false;
				goingServer = true;
			}
			else if(inputStringError.equals("ERROR19"))
			{
				System.out.println("\nWhat Packet Number would you like to send Packet with less than 4 bytes to Client?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR19, i);
				error = false;
			}
			else if(inputStringError.equals("ERROR20"))
			{
				System.out.println("\nWhat Packet Number would you like to send data with less than 4 bytes to Server?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR20, i);
				error = false;
				goingServer = true;
			}
			else if(inputStringError.equals("ERROR21"))
			{
				System.out.println("\nWhat Packet Number would you like to send data with less than 4 bytes to Server?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR21, i);
				error = false;
			}
			else if(inputStringError.equals("ERROR22"))
			{
				System.out.println("\nWhat Packet Number would you like to send Ack with less than 4 bytes to Server?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR22, i);
				error = false;
				goingServer = true;
			}
			else if(inputStringError.equals("ERROR23"))
			{
				System.out.println("\nWhat Packet Number would you like to send Ack with less than 4 bytes to Client?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR23, i);
				error = false;
		
			}
			else if(inputStringError.equals("ERROR24"))
			{
				System.out.println("\nWhat Packet Number would you like to send Server with wrong Port?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR24, i);
				error = false;
				goingServer = true;
		
			}
			else if(inputStringError.equals("ERROR25"))
			{
				System.out.println("\nWhat Packet Number would you like to send Server with wrong Port?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR25, i);
				error = false;
		
			}
			else if(inputStringError.equals("ERROR26"))
			{
				System.out.println("\nWhat Packet to Send Data with more than 516 length to Server (Write)?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR26, i);
				error = false;
				goingServer = true;
		
			}
			else if(inputStringError.equals("ERROR27"))
			{
				System.out.println("\nWhat packet to Send Data with more than 516 length to Client (Read)?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR27, i);
				error = false;
			}
			else if(inputStringError.equals("ERROR28"))
			{
				System.out.println("\nWhat packet to Send invalid ERROR packet to Server?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR28, i);
				error = false;
				goingServer = true;
			}
			else if(inputStringError.equals("ERROR29"))
			{
				System.out.println("\nWhat packet to Send invalid ERROR packet to Client?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR29, i);
				error = false;
				goingServer = true;
			}
			
			else
			{
				System.out.println("/nInvalid Error Option/n");
			}

			
		}
			
		boolean transferloop = true;
		byte[] data = new byte[516];
		currentPacketNumber = 0;
		c = 0;
		counter = 0;
		servercounter = 0;
		delayPacketcounter = 0;
		receivePacket = new DatagramPacket(data, data.length);
		delayedPacket = new DatagramPacket(data,data.length);
		tempPacket = new DatagramPacket(data,data.length);
		
		while(transferloop)
		{
			
		try
		{
			receiveSocket.receive(receivePacket); // receive packet from client
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		printReceivedFromClient(receivePacket); // print the packet received from client
		clientPort = receivePacket.getPort(); // store the client port
		clientIP = receivePacket.getAddress(); // store the client address
	
		sendPacket = new DatagramPacket(data, receivePacket.getLength(), address.getAddress(), 69);
		
		
		
		try
		{ 
			// if the error type is ERROR1. Lose the packet (don't send packet)
			if(errorTest.getErrorType() == ErrorType.ERROR1 && errorTest.getPacketNumber() == 0)
			{
				
				System.out.println("\nLost packet to Server\n");
				errorTest.setErrorType(ErrorType.ERROR0);
				
				
			  System.out.println("here!!");
			}
			// if the error type is ERROR3 Delay the packet(thread sleep?)
			else if(errorTest.getErrorType() == ErrorType.ERROR3 && errorTest.getPacketNumber() == 0)
			{
				
				System.out.println("\nDelayed packet to Server\n");
				delayedPacket = receivePacket;
				delayPacketcounter++;
				ScheduledFuture sf = scheduledPool.schedule(callabledelayedTask, delayTime, TimeUnit.MILLISECONDS);
				errorTest.setErrorType(ErrorType.ERROR0);
				
				
			}
			else if(errorTest.getErrorType() == ErrorType.ERROR5 && errorTest.getPacketNumber() == 0)
			{
					System.out.println("\nDuplicating packet to server\n");
					serverSocket.send(sendPacket); // send packet once
					System.out.println("\nPacket duplicated\n");
					serverSocket.send(sendPacket); // send packet twice
					errorTest.setErrorType(ErrorType.ERROR0);
				
			}
			else if(errorTest.getErrorType() == ErrorType.ERROR9 && errorTest.getPacketNumber() == 0)
			{
				System.out.println("\nSending to Server without filename\n");
				generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
				serverSocket.send(sendPacket);
				printSendingToServer(sendPacket);
				break;
				
			}
			else if(errorTest.getErrorType() == ErrorType.ERROR10 && errorTest.getPacketNumber() == 0)
			{
				System.out.println("\nSending to Server without mode\n");
				generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
				serverSocket.send(sendPacket);
				printSendingToServer(sendPacket);
				break;
			}
			else if(errorTest.getErrorType() == ErrorType.ERROR11 && errorTest.getPacketNumber() == 0)
			{
				System.out.println("\nSending to Server with invalid mode\n");
				generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
				serverSocket.send(sendPacket);
				printSendingToServer(sendPacket);
				break;
			}
			else if(errorTest.getErrorType() == ErrorType.ERROR12 && errorTest.getPacketNumber() == 0)
			{
				System.out.println("\n Sending Random Ack to server Port 69");
				generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
				serverSocket.send(sendPacket);
				printSendingToServer(sendPacket);
				break;
			}
			else if(errorTest.getErrorType() == ErrorType.ERROR13 && errorTest.getPacketNumber() == 0)
			{
				System.out.println("\n Sending Random Data to server Port 69");
				generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
				serverSocket.send(sendPacket);
				printSendingToServer(sendPacket);
				break;
			}
			else if(errorTest.getErrorType() == ErrorType.ERROR18 && errorTest.getPacketNumber() == currentPacketNumber)
			{
				System.out.println("\n Sending Packet to Server with less than 4 bytes");
				generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
				serverSocket.send(sendPacket);
				printSendingToServer(sendPacket);
	
			}
			else
			{
				printSendingToServer(sendPacket);
				serverSocket.send(sendPacket); // otherwise continue with normal transfer to Server
				transferloop = false;
			}
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		
		}
		System.out.println("Initial Client connection");
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// END OF SENDING TO SERVER INTIAL CLIENT CONNECTION
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// START OF RECEIVING FROM SERVER AND SENDING TO CLIENT
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		
		boolean lock = false;
		//Loop forever to pass on packets keeping track of packet number 
		for(currentPacketNumber = 1;; currentPacketNumber++)
		{
			
			if(serverWait)
			{
				data = new byte[516];
				receivePacket = new DatagramPacket(data, data.length);
				try {
					sendPacket.setAddress(InetAddress.getLocalHost());
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				try
				{
					serverSocket.receive(receivePacket); // receive a packet from the server
				}
				catch(IOException e)
				{
					e.printStackTrace();
					System.exit(1);
				}
				printReceivedFromServer(receivePacket); // print packet received from server
				
				serverPort = receivePacket.getPort(); // store the server port
				
				// create a new DatagramPacket to be sent to the client
				sendPacket = new DatagramPacket(data, receivePacket.getLength(), clientIP, clientPort);
				
				if(sendPacket.getData()[1] == 4 && !lock && !goingServer){
					currentPacketNumber--;
					lock = true;
					
				}
				
				
				
				try
				{
					//If packet is ERROR2 dont send the packet and set the flag if it was an ACK packet
					if(currentPacketNumber == errorTest.getPacketNumber() && errorTest.getErrorType() == ErrorType.ERROR2)
					{
						if(counter == 0)
						{
						System.out.println("\nLost packet to Client\n");
						currentPacketNumber--;
						counter++;
						setFlag(sendPacket);
						continue;
						}

					}
					else if(errorTest.getPacketNumber() == currentPacketNumber && errorTest.getErrorType() == ErrorType.ERROR4)
					{
						if(delayPacketcounter == 0)
						{
						System.out.println("\nDelayed packet to Client\n");
						currentPacketNumber--;
						ScheduledFuture sf = scheduledPool.schedule(callabledelayedTask, delayTime, TimeUnit.MILLISECONDS);
						delayedPacket = receivePacket;
						setFlag(sendPacket);
						delayPacketcounter++;
						continue;
						}
							
					}
						// if ERROR6. duplicate the packet being sent to Client
					else if(errorTest.getPacketNumber() == currentPacketNumber && errorTest.getErrorType() == ErrorType.ERROR6)
					{
							System.out.println("Duplicating packet to Client\n");
							clientSocket.send(sendPacket); // send packet once
							clientSocket.send(sendPacket); // send packet twice
							printSendingToClient(sendPacket);
							serverWait = false;
					
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR8 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						clientSocket.send(sendPacket);
						printSendingToClient(sendPacket);
						serverWait = false;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR14 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending Random Ack to Client");
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						clientSocket.send(sendPacket);
						printSendingToClient(sendPacket);
						serverWait = false;
						
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR15 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending Random Ack to Client");
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						clientSocket.send(sendPacket);
						printSendingToClient(sendPacket);
						serverWait = false;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR17 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending ErrorPacket to Client");
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						clientSocket.send(sendPacket);
						printSendingToClient(sendPacket);
						serverWait = false;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR19 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending Packet to Client with less than 4 bytes");
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						clientSocket.send(sendPacket);
						printSendingToClient(sendPacket);
						serverWait = false;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR21 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending Data to Client with less than 4 bytes");
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						clientSocket.send(sendPacket);
						printSendingToClient(sendPacket);
						serverWait = false;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR23 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending Ack to Client with less than 4 bytes");
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						clientSocket.send(sendPacket);
						printSendingToClient(sendPacket);
						serverWait = false;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR25 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending to Client from wrong port");
						byte[] temp = new byte[516];
						for (int i = 0; i < 516; i++){
							temp[i] = sendPacket.getData()[i];
						}
						tempPacket.setData(temp);
						tempPacket.setPort(sendPacket.getPort());
						tempPacket.setAddress(sendPacket.getAddress());
						
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						randomSocket.send(sendPacket);
						printSendingToClient(sendPacket);
						wrongPortPacket = new DatagramPacket(data, receivePacket.getLength(), clientIP, clientPort);
						randomSocket.receive(wrongPortPacket);
						System.out.println("Sending Error to imaginary source");
						printReceivedFromClient(wrongPortPacket);
						System.out.println("Now Sending from Correct Port");
						
						
						clientSocket.send(tempPacket);
						printSendingToClient(tempPacket);
						serverWait = false;

					}		
					
					else if(errorTest.getErrorType() == ErrorType.ERROR27 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending to Client datagram > 516");
						byte[] big = new byte[517];
						big[1] = sendPacket.getData()[1];
						big[2] = sendPacket.getData()[2];
						big[3] = sendPacket.getData()[3];
						big[515] = sendPacket.getData()[515];
						big[516] = 5;
						DatagramPacket tooBig = new DatagramPacket(big, big.length, sendPacket.getAddress(),sendPacket.getPort());
						clientSocket.send(tooBig);
						printSendingToClient(tooBig);
						serverWait = false;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR29 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending to Client invalid ERROR Packet");
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						clientSocket.send(sendPacket);
						printSendingToClient(sendPacket);
						serverWait = false;
					}
					else // No errors detected transfer regularly
					{
							clientSocket.send(sendPacket);
							printSendingToClient(sendPacket); // print the packet being sent to client
							serverWait = false;
					}
					
					if(sendPacket.getData()[515] == (byte) (0) && sendPacket.getData()[1] == 3)
					{
						try
						{
							clientSocket.receive(receivePacket); // receive a packet from the client
							printReceivedFromClient(receivePacket);
						}
						catch(IOException e)
						{
							e.printStackTrace();
							System.exit(1);
						}
						sendPacket = new DatagramPacket(data, receivePacket.getLength(), serverIP, serverPort);
						
						try
						{
							serverSocket.send(sendPacket); 
							System.out.println("/n" + sendPacket.getPort() + "\n");
							printSendingToServer(sendPacket);
							
						}
						catch(IOException e)
						{
							e.printStackTrace();
							System.exit(1);
						}
						receivePacket.setData(re(receivePacket.getData()));
						sendPacket.setData(re(sendPacket.getData()));
						break;
					}
				}
				
				catch(IOException e)
				{
					e.printStackTrace();
					System.exit(1);
				}
			}
			
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			// END OF RECEIVING FROM SERVER AND SENDING TO CLIENT
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			// START OF RECEIVING FROM CLIENT AND SENDING TO SERVER
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			
			
			if(!serverWait)
			{
				data = new byte[516];
				receivePacket = new DatagramPacket(data, data.length);
				
				
				
				try
				{
					clientSocket.receive(receivePacket); // wait until a datagram is received on the clientSocket
					// receiveSocket.close(); close? only doing one transfer at a time.
				}
				catch (IOException e)
				{
					e.printStackTrace();
					System.exit(1);
				}
				printReceivedFromClient(receivePacket); // print the packet received from client
				sendPacket = new DatagramPacket(data, receivePacket.getLength(), address.getAddress(), serverPort);
				
				 
				
				try
				{ 
					// if the error type is ERROR1. Lose the packet (don't send packet)
					if(errorTest.getErrorType() == ErrorType.ERROR1 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						if(servercounter == 0)
						{
						System.out.println("\nLost packet to Server\n");
						currentPacketNumber--;
						servercounter++;
						setFlag(sendPacket);
						continue;
						}
					}
					// if the error type is ERROR3 Delay the packet(thread sleep?)
					else if(errorTest.getErrorType() == ErrorType.ERROR3 && errorTest.getPacketNumber() == currentPacketNumber)
					{
							if(delayPacketcounter == 0)
							{
								System.out.println("\nDelayed packet to Server\n");
								currentPacketNumber--;
								ScheduledFuture sf = scheduledPool.schedule(callabledelayedTask, delayTime, TimeUnit.MILLISECONDS);
								delayedPacket = sendPacket;
								
								delayPacketcounter++;
								setFlag(receivePacket);
								continue;
							}
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR5 && errorTest.getPacketNumber() == currentPacketNumber)
					{

							System.out.println("\nDuplicating packet to server\n");
							serverSocket.send(sendPacket); // send packet once
							printSendingToServer(sendPacket);
							serverSocket.send(sendPacket); // send packet twice
							serverWait = true;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR7 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						serverSocket.send(sendPacket);
						printSendingToServer(sendPacket);
						serverWait = true;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR12 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending Random Ack to server Port 69");
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						serverSocket.send(sendPacket);
						printSendingToServer(sendPacket);
						serverWait = true;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR13 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending Random Data to server Port 69");
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						serverSocket.send(sendPacket);
						printSendingToServer(sendPacket);
						serverWait = true;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR16 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending ErrorPacket to Server");
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						serverSocket.send(sendPacket);
						printSendingToServer(sendPacket);
						serverWait = true;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR18 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending Packet to Server with less than 4 bytes");
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						serverSocket.send(sendPacket);
						printSendingToServer(sendPacket);
						serverWait = true;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR20 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending Data to Server with less than 4 bytes");
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						serverSocket.send(sendPacket);
						printSendingToServer(sendPacket);
						serverWait = true;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR22 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending Ack to Server with less than 4 bytes");
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						serverSocket.send(sendPacket);
						printSendingToServer(sendPacket);
						serverWait = true;
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR24 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending to Server from wrong port");
						byte[] temp = new byte[516];
						for (int i = 0; i < 516; i++){
							temp[i] = sendPacket.getData()[i];
						}
						tempPacket.setData(temp);
						tempPacket.setPort(sendPacket.getPort());
						tempPacket.setAddress(sendPacket.getAddress());
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						
						randomSocket.send(sendPacket);
						
						printSendingToServer(sendPacket);
						wrongPortPacket = new DatagramPacket(data, receivePacket.getLength(), serverIP, serverPort);
						randomSocket.receive(wrongPortPacket);
						System.out.println("Sending Error to imaginary Client");
						printReceivedFromServer(wrongPortPacket);
						
						System.out.println("Now Sending from Correct Port");
						serverWait = true;
						serverSocket.send(tempPacket);
						printSendingToServer(tempPacket);
						
						
					}
					else if(errorTest.getErrorType() == ErrorType.ERROR26 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending to Server datagram > 516");
						
						byte[] big = new byte[517];
						big[1] = sendPacket.getData()[1];
						big[2] = sendPacket.getData()[2];
						big[3] = sendPacket.getData()[3];
						big[515] = sendPacket.getData()[515];
						big[516] = 5;
						DatagramPacket tooBig = new DatagramPacket(big, big.length, sendPacket.getAddress(),sendPacket.getPort());
						
					
						serverSocket.send(tooBig);
						printSendingToServer(tooBig);
						serverWait = true;
						
					}					
					else if(errorTest.getErrorType() == ErrorType.ERROR28 && errorTest.getPacketNumber() == currentPacketNumber)
					{
						System.out.println("\n Sending to Server invalid ERROR packet");
						generateErrorPacket(errorTest,sendPacket,sendPacket.getAddress());
						serverSocket.send(sendPacket);
						printSendingToServer(sendPacket);
						serverWait = true;
					}
					else
					{
						serverSocket.send(sendPacket); // otherwise continue with normal transfer to Server
						serverWait = true;
						printSendingToServer(sendPacket); // print the packet being sent to the server
					}
					
				}
				catch(IOException e)
				{
					e.printStackTrace();
					System.exit(1);
				}
				if(sendPacket.getData()[1] == 3 && sendPacket.getData()[515] == (byte) (0)  )
				{
					
					try
					{
						serverSocket.receive(receivePacket); // receive a packet from the server
						printReceivedFromServer(receivePacket);
						System.out.println("");
	                   
					}
					catch(IOException e)
					{
						e.printStackTrace();
						System.exit(1);
					}
					sendPacket = new DatagramPacket(data, receivePacket.getLength(), clientIP, clientPort);
					try
					{
						clientSocket.send(sendPacket); // receive a packet from the server
						printSendingToClient(sendPacket);
					}
					catch(IOException e)
					{
						e.printStackTrace();
						System.exit(1);
					}
					receivePacket.setData(re(receivePacket.getData()));
					sendPacket.setData(re(sendPacket.getData()));
					break;
				}
			}
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			// END OF RECEIVING FROM CLIENT AND SENDING TO SERVER
			///////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////
		}
		// end of transfer loop
		//System.out.println("\nDID THIS WORK????!!!\n");
	}
		// end of while loop
	}
	// end of Error Simulator algorithm
	
	
	
	/**
	 * 
	 * Generates an error packet on a given packet with an error type
	 * @param error the error type and packet number to generate an error on
	 * @param OGPacket The packet to have an error generated on
	 * @return a packet with the appropriate error
	 */
	private DatagramPacket generateErrorPacket(Error error, DatagramPacket OGPacket, InetAddress address)
	{
		
		System.out.println("\n Generating Error case: " + error.getErrorType().toString() + "\n");
		
		byte[] data = new byte[516];
		
		switch(error.getErrorType())
		{
		// I3 ERRORS
		case ERROR0: // No errors, send original packet
		case ERROR1: // Lose packet going to Server	
		case ERROR2: // Lose packet going to Client
		case ERROR3: // Delay packet going to Server
		case ERROR4: // Delay packet going to Client
		case ERROR5: // Duplicate packet going to Server
		case ERROR6: // Duplicate packet going to Client
			
			
		/////////////////////////////////////////////////////////////////////////
		// WRONG BLOCK NUMBER
		/////////////////////////////////////////////////////////////////////////
		case ERROR7: // Send Data to Server with wrong block # Write
			OGPacket.getData()[3]  = 7;
;
			return OGPacket;
		case ERROR8:
			OGPacket.getData()[3] = 7;
			return OGPacket;
			
		/////////////////////////////////////////////////////////////////////////
		// INITIAL CLIENT REQUEST ERRORS
		/////////////////////////////////////////////////////////////////////////
			
		case ERROR9: // Send Request to server without filename
			data = OGPacket.getData();
			
			for(int i = 2; data[i] != 0; i++){
				OGPacket.getData()[i] = 0;
			}
			
			return OGPacket;
		
		case ERROR10: //Send Request to server without mode
			data = OGPacket.getData();
			int modePos;
			
			int i;
			for(i = 2; data[i] != 0; i++){
				modePos = i;
			}
			modePos = i + 1;
			System.out.println(modePos + "\n");
			for(int j = modePos; data[j] != 0; j++){
				OGPacket.getData()[j] = 0;
			}
			return OGPacket;
			
		case ERROR11: //Send Request to server without mode
			data = OGPacket.getData();
			modePos = 0;

			for(i = 2; data[i] != 0; i++){
				modePos = i;
			}
			modePos = i + 1;
			OGPacket.getData()[modePos++] = 7;
			return OGPacket;
		case ERROR12: // Send random Ack to server
			OGPacket.setData(re(OGPacket.getData()));
			OGPacket.getData()[0]  = 0;
			OGPacket.getData()[1]  = 4;
			OGPacket.getData()[2]  = 0;
			OGPacket.getData()[3]  = 4;
			
			return OGPacket;
		case ERROR13: // Send random Data to server
			OGPacket.getData()[0]  = 0;
			OGPacket.getData()[1]  = 3;
			OGPacket.getData()[2]  = 0;
			OGPacket.getData()[3]  = 7;
			
			for(i = 4; i<516;i++)
			{
				OGPacket.getData()[i]  = 7;
			}
			
			return OGPacket;
		case ERROR14: // Send Random Ack to CLient
			OGPacket.setData(re(OGPacket.getData()));
			OGPacket.getData()[0]  = 0;
			OGPacket.getData()[1]  = 4;
			OGPacket.getData()[2]  = 0;
			OGPacket.getData()[3]  = 7;
			
			
			
			return OGPacket;
			
		case ERROR15: // Send Random Data to Client
			OGPacket.getData()[0]  = 0;
			OGPacket.getData()[1]  = 3;
			OGPacket.getData()[2]  = 0;
			OGPacket.getData()[3]  = 7;
			
			for(i = 4; i<516;i++)
			{
				OGPacket.getData()[i]  = 7;
			}
			
			return OGPacket;
			
		case ERROR16: //Send Error Packet to Server
			OGPacket.setData(re(OGPacket.getData()));
			byte b = (byte)errorNumberServer;
			
			OGPacket.getData()[0]  = 0;
			OGPacket.getData()[1]  = 5;
			OGPacket.getData()[2]  = 0; 
			OGPacket.getData()[3]  = b;
			
			String errorMsg = "Error sent from Error Simulator";
			byte[] errorByte = errorMsg.getBytes();
			
			for(int j = 5; j < errorByte.length; j++){
				OGPacket.getData()[j] = errorByte[j];
			}
			
			
			
			return OGPacket;
			
		case ERROR17: // Send Error Packet to Client
			OGPacket.setData(re(OGPacket.getData()));
			byte c = (byte)errorNumberClient;
			
			OGPacket.getData()[0]  = 0;
			OGPacket.getData()[1]  = 5;
			OGPacket.getData()[2]  = 0; 
			OGPacket.getData()[3]  = c;
			
			String errMsg = "Error sent from Error Simulator";
			byte[] errByte = errMsg.getBytes();
			
			for(int j = 5; j < errByte.length; j++){
				OGPacket.getData()[j] = errByte[j];
			}
			
			
			
			
			return OGPacket;
			
		case ERROR18: // Send Packet with less than 4 bytes Server

			OGPacket.setLength(3);
			
			OGPacket.getData()[0]  = 0;
			OGPacket.getData()[1]  = 0;
			OGPacket.getData()[2]  = 0; 

			return OGPacket;
		
		case ERROR19: // Send Packet with less than 4 bytes to Client
			OGPacket.setLength(3);
			
			OGPacket.getData()[0]  = 0;
			OGPacket.getData()[1]  = 0;
			OGPacket.getData()[2]  = 0; 

			return OGPacket;
			
		case ERROR20: // Send Data to Server with less than 4 bytes
			OGPacket.setLength(3);
			
			OGPacket.getData()[0]  = 0;
			OGPacket.getData()[1]  = 3;
			OGPacket.getData()[2]  = 0; 

			return OGPacket;
		case ERROR21: // Send Data to Server with less than 4 bytes
			OGPacket.setLength(3);
			
			OGPacket.getData()[0]  = 0;
			OGPacket.getData()[1]  = 3;
			OGPacket.getData()[2]  = 0; 

			return OGPacket;
			
		case ERROR22:  // Send Ack to Server with less than 4 bytes
			OGPacket.setLength(3);
			
			OGPacket.getData()[0]  = 0;
			OGPacket.getData()[1]  = 4;
			OGPacket.getData()[2]  = 0; 

			return OGPacket;
		case ERROR23: // Send Ack to CLient with less than 4 bytes
			
			OGPacket.setLength(3);
			
			OGPacket.getData()[0]  = 0;
			OGPacket.getData()[1]  = 3;
			OGPacket.getData()[2]  = 0; 

			return OGPacket;
			
		case ERROR24: // Send to Server from wrong port
		case ERROR25: // Send to Client from wrong port
			return OGPacket;
		
			
		case ERROR26:
		case ERROR27: // Send Data with more than 516 length to Client
			return OGPacket;
			
			
		case ERROR28: // Send invalid ERROR packet to Server
			OGPacket.getData()[0]  = 0;
			OGPacket.getData()[1]  = 5;
			OGPacket.getData()[2]  = 0; 
			OGPacket.getData()[3]  = 8;
			return OGPacket;
			
		case ERROR29: // Send invalid ERROR packet to Client
			OGPacket.getData()[0]  = 0;
			OGPacket.getData()[1]  = 5;
			OGPacket.getData()[2]  = 0; 
			OGPacket.getData()[3]  = 8;
			return OGPacket;
		case ERROR30:
			
			
			
		}
		
		
		return OGPacket;
		
	}
	
	
	/**
	 * @param packet
	 * prints the packet: Address, port, length, bytes, string
	 */
	private void printPacket(DatagramPacket packet)
	{
		byte[] data = new byte[packet.getLength()];
		System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());
		System.out.println("Address: " + packet.getAddress() + "\n");
		System.out.println("Port: " + packet.getPort() + "\n");
		System.out.println("Length: " + packet.getLength() + "\n");
		System.out.println("Bytes: " + byteArrToString(data) + "\n");
		//System.out.println("String: " + new String(data) + "\n");
	}
	
	/**
	 * @param packet
	 * prints packet received from server
	 */
	private void printReceivedFromServer(DatagramPacket packet)
	{
		System.out.print("\nReceived from Server....\n");
		printPacket(packet);
	}
	
	/**
	 * @param packet
	 * prints packet received from client
	 */
	private void printReceivedFromClient(DatagramPacket packet)
	{
		System.out.print("\nReceived from Client....\n");
		printPacket(packet);
	}
	
	/**
	 * @param packet
	 * prints packet sent to Server
	 */
	private void printSendingToServer(DatagramPacket packet) {
		System.out.print("\nSending to Server...\n");
		printPacket(packet);
	}
	
	/**
	 * @param packet
	 * prints packet sent to Client
	 */
	private void printSendingToClient(DatagramPacket packet) {
		System.out.print("\nSending to Client...\n");
		printPacket(packet);
	}
	
	/**Sets flag if DatagramPacket is an ACK packet
	 * @param packet DatagramPacket being lost
	 */
	private void setFlag(DatagramPacket packet)
	{
		if(packet.getData()[1] == 4)
		{
			serverWait = flipFlag(serverWait);
		}
		// if data leave flag as is
	}
	
	/**
	 * Inverts a boolean
	 * @param b
	 * @return 
	 */
	private boolean flipFlag(boolean b)
	{
		return !b;
	}
	
	 public byte [] re (byte[] data)
	    {
	    	for (int i = 0; i < data.length; i++)
	    	{
	    		data[i] = 0x00;
	    	}
	    	return data;
	    }
	 
	   public byte[] resize (byte[] data)
	    {
	    	int i;
	    	for(i = 3; i < data.length; i++)
	    	{
	    		if(data[i] == 0x00)
	    		{
	    			break;
	    			
	    		}
	    	}
	    	
	    	data = Arrays.copyOf(data, i);
	    	return data;
	    }
	   
	   /**
	     * Improved version of Arrays.toString. Returns the byte in the array as a string representation of unsigned integers.
	     * @param array - The byte array to be stringefied.
	     */
	    public String byteArrToString(byte[] array)
	    {
	        String str = "[ ";
	        if(array[1] == 4)
	        {
	        	for(int i = 0; i < 4; i++)
	        	{
	        		str += (0xFF & array[i]) + " ";
	        	}
	        }
	        else
	        {
		        for(int i = 0; i < array.length; i++)
		        {
		            str += (0xFF & array[i]) + " ";
		        }
	        }
	        return str += "]";
	    }

	
	/**
	 * Main method to run error simulator
	 * @param args
	 */
	public static void main(String args[])
	{
		ErrorSimulator es = new ErrorSimulator();
		es.ErrorSimulatorAlgorithm();
		
	}
	
}


