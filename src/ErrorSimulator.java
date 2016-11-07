/**
 * Project ErrorSimulator class
 * SYSC 3303 L2
 * Andrew Ward, Alex Hoecht, Connor Emery, Robert Graham, Saleem Karkabi
 * 100898624,   100933730,   100980809,    100981086,     100944655
 * Fall Semester 2016
 * 
 * ErrorSimulator Class
 */

import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
* Error Simulator currently acts as a relay to send and receive Datagram packets from the Client and Server
**/

public class ErrorSimulator 
{
	// Datagrams to be used
	private DatagramSocket sendSocket, receiveSocket, sendReceiveSocket;
	private DatagramPacket sendPacketServer, sendPacketClient;
	private DatagramPacket receivePacketServer, receivePacketClient;
	
	public ErrorSimulator()
	{
		try
		{
			// Datagram socket to send and receive UDP packets
			receiveSocket = new DatagramSocket(23);
			sendReceiveSocket = new DatagramSocket();
		}
		catch(SocketException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/*
	 * Algorithm for Error Simulator:
	 * Repeat forever:
	 * 	- Creates a the packet that the error simulator will receive Client packets
	 * 	- Receives a Client packet
	 * 	- Processes the packet and prints the relevant data about it
	 * 	- Repacks the data back into a packet to be sent to the Server
	 * 	- Receives a Sever packet
	 * 	- Processes the packet and prints the relevant data about it
	 * 	- Repacks the data back into a packet to be sent to the Client
	 */
	public void ErrorSimulatorAlgorithm()
	{
		// The received request type
		String request = "";
		while(true)
		{
			// Byte array to contain client request
			byte[] message = new byte[50];
			// Byte array to respond to request
			byte[] data = new byte[4];
			
			// Packet to be received from client 
			int msglength = message.length;
			receivePacketClient = new DatagramPacket(message,msglength);
			System.out.println("Error Simulator is waiting for a packet");
			
			//Waiting on packet from client 
			try
			{
				//Receive the packet
				receiveSocket.receive(receivePacketClient);
			}			
			catch(IOException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("Error Simulator has received a packet");
			
			// Determine request
			if(message[1] == 0)
			{
				request = "Invalid";
			}
			if(message[1] == 1)
			{
				request = "Read";
			}
			if(message[1] == 2)
			{
				request = "Write";
			}
			
			System.out.println(request + " Request received");
			
			// Information to find in the packet
			String fileName = "";
			String mode2 = "";
			String Stringinfo = "";
			byte[] Bytesinfo = new byte[2];
			
			/*
			* Parsing packet to retrieve information
			*/
			if(!request.equals("Invalid"))
			{
				byte[] file = new byte[1];
				byte[] mode = new byte[1];
				int count = 0;
				for(int i = 2; i < msglength; i++)
				{				
					if(message[i] == 0)
					{
						count++;
						if (count == 1)
						{
							file = Arrays.copyOfRange(message, 2, i);
						}
						if(count == 2)
						{
							mode = Arrays.copyOfRange(message, 3 + file.length, i);
							break;
						}
					}	
				}
				//Printing Received Packet
				fileName = new String(file);
				System.out.println("File Name: " + fileName);
				
				mode2 = new String(mode);
				System.out.println("Mode: " + mode2);
				
				int msgLength = 4 +fileName.length() + mode2.length();
				Stringinfo = new String(message,0,msgLength);
				System.out.println("Information as String: " + Stringinfo);
				Bytesinfo = Arrays.copyOfRange(message, 0, msgLength);
				System.out.println("Information as Bytes: "+ Arrays.toString(Bytesinfo) + "\n");
			}
			
			//Sending packet to server
			try
			{
				sendPacketServer = new DatagramPacket(message,message.length,
						InetAddress.getLocalHost(),69);
			}
			
			catch(UnknownHostException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
			
			System.out.println("Error Simulator is sending a packet");
			System.out.println("Sending " + request + " Request");
			
			//Sending packet information 
			if(!request.equals("Invalid"))
			{
				System.out.println("File Name: " + fileName);
				System.out.println("Mode: " + mode2);
				System.out.println("Information as String: " + Stringinfo);
				int msgLength = fileName.length() + mode2.length() + 4;
				Bytesinfo = Arrays.copyOfRange(message, 0, msgLength);
				System.out.println("Information as Bytes: "+ Arrays.toString(Bytesinfo) + "\n");
			}
			
			//Packet to Server
			try
			{
				sendReceiveSocket.send(sendPacketServer);
			}
			catch(IOException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
			
			//Receive Packet from Server
			receivePacketServer = new DatagramPacket(data,data.length);
			
			//Receive Packet
			try
			{
				sendReceiveSocket.receive(receivePacketServer);
			}
			catch(IOException e)
			{
				e.printStackTrace();
				System.exit(1);
			}
			
			//Print Received packet 
			System.out.println("Error Simulator received a packet");
			System.out.println("From Server " + receivePacketServer.getAddress());
			System.out.println("With port: " + receivePacketServer.getPort());
			
			int len = receivePacketServer.getLength();
			System.out.println("Length: " + len);
			
			System.out.print("Info: ");
			
			for(int k = 0; k<data.length;k++)
			{
				System.out.print(" " + data[k]);
			}
			
			System.out.println("\n");
			
			//Send packet for Client 
			sendPacketClient = new DatagramPacket(data,data.length,receivePacketClient.getAddress(),receivePacketClient.getPort());
			
			//Create Socket
			try
			{
				sendSocket = new DatagramSocket();
			}
			
			catch(SocketException se)
			{
				se.printStackTrace();
				System.exit(1);
			}
			
			// Printing information to be sent to client
			System.out.println("Error Simulator Host sent packet");
			System.out.println("To Host: " + sendPacketClient.getAddress());
			System.out.println("Destination host port: " + sendPacketClient.getPort());
			System.out.print("Response Packet: ");
			
			for(int k = 0; k<data.length;k++)
			{
				System.out.print(" " + data[k]);
			}
			
			System.out.println("\n");
			
			// Send Packet to Client 
		    try 
		    {
		        sendSocket.send(sendPacketClient);
		    }
		    catch(IOException e)
		    {
		        e.printStackTrace();
		        System.exit(1);
		    }
		}
	}
	
	/*
	 * The MAIN FUNCTION for the Error Simulator
	 */
	public static void main(String args[])
	{
		ErrorSimulator errorsimulator = new ErrorSimulator();
		errorsimulator.ErrorSimulatorAlgorithm();
	}
}