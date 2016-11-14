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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

/**
* An Error simulator to generate errors on specific packets and pass them on
**/

public class ErrorSimulator 
{

	private DatagramPacket sendPacket, receivePacket , delayedPacket;
	private int clientPort, serverPort; // client, server ports
	private InetAddress clientIP, serverIP; // variables to store the addresses
	private DatagramSocket receiveSocket, clientSocket, serverSocket; // DatagramSockets
	private boolean serverWait; // used to track which socket to wait at
	private int currentPacketNumber; // used to count the packet number
	private String inputStringError; // used for ERROR input from user
	private Error errorTest; // the actual error object
	private int counter; // Used in the Lost packet to client (breaks lost packet loop)
	private int servercounter; // used in the Lost packet to server (breaks lost packet loop)
	
	private int delayPacketcounter;
	
	private int c;
	
	
	
	
	/**
	 * Constructor for the ErrorSimulator
	 */
	public ErrorSimulator()
	{
		serverWait = true;
		try
		{
			receiveSocket = new DatagramSocket(23); // construct DatagramSocket and bind to port 23
			serverSocket = new DatagramSocket(); // construct server DatagramSocket
			clientSocket = new DatagramSocket(); // construct Client DatagramSocket
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
	 * Generates the following Errors:
	 * EEROR0: No error regular WRQ/RRQ
	 * ERROR1: Lose packet going to Server
	 * ERROR2: Lose packet going to Client
	 * ERROR3: Delay packet going to Server
	 * ERROR4: Delay packet going to Client
	 * ERROR5: Duplicate packet going to Server
	 * ERRPR6: Duplicate packet going to Client
	 * 
	 * change ENUM in main to test different errors. // need to add UI to choose ERROR
	 */
	public void ErrorSimulatorAlgorithm()
	{

		boolean running = true;

		while(running)
		{
			
			
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		// START OF SENDING TO SERVER INTIAL CLIENT CONNECTION
		///////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////			
			
		boolean error = true;
		Scanner in = new Scanner(System.in);
		BufferedReader bufferReadError = new BufferedReader(new InputStreamReader(System.in));
		while(error)
		{
			System.out.println("\nGenerates the following errors\n");
			System.out.println("\nNOERROR: No error regular WRQ/RRQ\n");
			System.out.println("\nERROR1: Lose packet going to Server\n");
			System.out.println("\nERROR2: Lose packet going to Client\n");
			System.out.println("\nERROR3: Delay packet going to Server\n");
			System.out.println("\nERROR4: Delay packet going to Client\n");
			System.out.println("\nERROR5: Duplicate packet going to Server\n");
			System.out.println("\nERROR6: Duplicate packet going to Client\n");
			System.out.println("\nWhat error would you like to test\n");
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
				errorTest = new Error(ErrorType.ERROR3, i);
				error = false;
			}
			else if(inputStringError.equals("ERROR4"))
			{
				System.out.println("\nWhat Packet Number would you like to Delay a Packet to the Client on?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR4, i);
				error = false;
			}
			else if(inputStringError.equals("ERROR5"))
			{
				System.out.println("\nWhat Packet Number would you like to Duplicate a Packet to the Server on?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR5, i);
				error = false;
			}
			else if(inputStringError.equals("ERROR6"))
			{
				System.out.println("\nWhat Packet Number would you like to Duplicate a Packet to the Client on?\n");
				int i = in.nextInt();
				errorTest = new Error(ErrorType.ERROR6, i);
				error = false;
			}
			else
			{
				System.out.println("/nInvalid Error Option/n");
			}

			
		}
		
		byte[] data = new byte[516];
		currentPacketNumber = 0;
		c = 0;
		counter = 0;
		servercounter = 0;
		delayPacketcounter = 0;
		receivePacket = new DatagramPacket(data, data.length);
		delayedPacket = new DatagramPacket(data,data.length);
		
	
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
		
		sendPacket = new DatagramPacket(data, receivePacket.getLength(), serverIP, 69);
		
		
		printSendingToServer(sendPacket); // print the packet being sent to the server
		
		try
		{ 
			// if the error type is ERROR1. Lose the packet (don't send packet)
			if(errorTest.getErrorType() == ErrorType.ERROR1 && errorTest.getPacketNumber() == 0)
			{
				System.out.println("\nLost packet to Server\n");
			}
			// if the error type is ERROR3 Delay the packet(thread sleep?)
			else if(errorTest.getErrorType() == ErrorType.ERROR3 && errorTest.getPacketNumber() == 0)
			{
				
					delayedPacket = new DatagramPacket(data,data.length);
					System.out.println("\nDelaying packet to Server\n");
					delayedPacket.setData(receivePacket.getData());
				
			}
			else if(errorTest.getErrorType() == ErrorType.ERROR5 && errorTest.getPacketNumber() == 0)
			{
					System.out.println("\nDuplicating packet to server\n");
					serverSocket.send(sendPacket); // send packet once
					System.out.println("\nPacket duplicated\n");
					serverSocket.send(sendPacket); // send packet twice
				
			}
			else
			{
				serverSocket.send(sendPacket); // otherwise continue with normal transfer to Server
			}
			
		}
		catch(IOException e)
		{
			e.printStackTrace();
			System.exit(1);
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
		
		
		//Loop forever to pass on packets keeping track of packet number 
		for(currentPacketNumber = 1;; currentPacketNumber++)
		{
			if(serverWait)
			{
				data = new byte[516];
				receivePacket = new DatagramPacket(data, data.length);
				
				
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
				
				printSendingToClient(sendPacket); // print the packet being sent to client
				
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
							System.out.println("\nDelaying packet to Client....\n");
							printReceivedFromServer(receivePacket);
							delayedPacket.setData(receivePacket.getData());
							delayedPacket.setPort(receivePacket.getPort());
							delayedPacket.setAddress(receivePacket.getAddress());
							printSendingToClient(delayedPacket);
							currentPacketNumber--;
							serverWait = false;
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
					else if(delayPacketcounter != 0)
					{
						if(c == 7)
						{
							clientSocket.send(delayedPacket);
						}
						c++;
					}
					else // No errors detected transfer regularly
					{
							clientSocket.send(sendPacket);
							serverWait = false;
					}
					if(sendPacket.getData()[515] == (byte) (0) && sendPacket.getData()[1] == 3)
					{
						try
						{
							clientSocket.receive(receivePacket); // receive a packet from the server
						}
						catch(IOException e)
						{
							e.printStackTrace();
							System.exit(1);
						}
						sendPacket = new DatagramPacket(data, receivePacket.getLength(), clientIP, clientPort);
						try
						{
							serverSocket.send(sendPacket); // receive a packet from the server
						}
						catch(IOException e)
						{
							e.printStackTrace();
							System.exit(1);
						}
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

				
				sendPacket = new DatagramPacket(data, receivePacket.getLength(), serverIP, serverPort);
				printSendingToServer(sendPacket); // print the packet being sent to the server
				
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
					// if the error type is ERROR5 Delay the packet(thread sleep?)
					else if(errorTest.getErrorType() == ErrorType.ERROR3 && errorTest.getPacketNumber() == currentPacketNumber)
					{
							if(delayPacketcounter == 0)
							{
							System.out.println("\nDelaying packet to Server\n");
							delayedPacket = receivePacket;
							serverWait = true;
							delayPacketcounter++;
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
					else
					{
						serverSocket.send(sendPacket); // otherwise continue with normal transfer to Server
						serverWait = true;
					}
					
				}
				catch(IOException e)
				{
					e.printStackTrace();
					System.exit(1);
				}
				if(sendPacket.getData()[515] == (byte) (0) && sendPacket.getData()[1] == 3)
				{
					try
					{
						serverSocket.receive(receivePacket); // receive a packet from the server
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
					}
					catch(IOException e)
					{
						e.printStackTrace();
						System.exit(1);
					}
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
		DatagramPacket errorPacket;
		
		System.out.println("\n Generating Error case: " + error.getErrorType().toString() + "\n");
		
		byte[] data = new byte[516];
		byte[] newdata = new byte[516];
		
		switch(error.getErrorType())
		{
		case ERROR0: // No errors, send original packet
		case ERROR1: // Lose packet going to Server	
		case ERROR2: // Lose packet going to Client
		case ERROR3: // Delay packet going to Server
		case ERROR4: // Delay packet going to Client
		case ERROR5: // Duplicate packet going to Server
		case ERROR6: // Duplicate packet going to Client
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
		System.out.println("Bytes: " + Arrays.toString(data) + "\n");
		System.out.println("String: " + new String(data) + "\n");
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
