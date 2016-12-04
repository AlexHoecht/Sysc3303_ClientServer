/**
 * Project Client class
 * SYSC 3303 L2
 * Andrew Ward, Alex Hoecht, Connor Emery, Robert Graham, Saleem Karkabi
 * 100898624,   100933730,   100980809,    100981086,     100944655
 * Fall Semester 2016
 * 
 * Client Class
 */
import java.net.*;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import java.awt.Component;
import java.io.*;

public class Client 
{
	// Datagrams to be used in the client
	private DatagramSocket sendReceiveSocket;
	private DatagramPacket sendPacket, receivePacket;
	
	// User input file name and transfer mode
	private String fileName;	
	private String mode;
	private String inputString;	// Last input from the user
	private String directoryPath;	// Path of the current directory used to transfer files to and from
	private InetSocketAddress address;
	
	String ipAddress = "";
	
	// Size of the Packet being sent back to the client
	private byte[] sendPacketSize;
	private byte[] receivePacketSize;
	// The data of the request packet
	private byte[] message;
	// The opcode and block number of the packet being transferred
	private byte[] opNum = new byte[4];
	
	// if true stop the current data transfer
    private boolean haltCurrentTransfer;
	// False overwrites the current file, true appends additional data
	private boolean fileWriterAppend;
	// If the Client has timed out
	private boolean timeout = false;
	
	// The client directory
	private File clientDir;
	// The place holder for the file being transferred
	private File receivedFile;
	
	// The port that the Datagram Packet will be sent to
	private int portNum;
	
	// Frame used to facilitate UI
	private JFrame popupWindow;
	
	// User input mode of the TFTP
	// TEST or NORMAL mode
	private int tORn = 0;
	// QUIET or VERBOSE outputs
	private int qORv = 0;
	// Type of request for the file transfer
	private String request;
	// Has the user set all nessessary inputs
	private boolean allValidInputs;
	
	

	/*
	 * The main constructor for class Client
	 * When the client is initialized it creates both a Datagram socket and a 'Client Directory'
	 * to aid in file transfer.
	 */
	public Client()
	{
		// create DatagramSocket to both send and receive packets
		try
		{
			sendReceiveSocket = new DatagramSocket();
			//sendReceiveSocket.setSendBufferSize(1000);
			sendReceiveSocket.setSoTimeout(150);
		}
		catch(SocketException se)
		{
			se.printStackTrace();
			System.exit(1);
		}
		
		// The mode of each request will always be octet
		mode = "ocTEt".toLowerCase();
	}
	
	
	/*
	 * The ClientAlgorithm() method defines the core behavior of the TFTP Client.
	 * The Client's current state is initialized by user input (test/normal and quiet/verbose)
	 * The Client loops HERE until killed:
	 *  - The Client is told by user input which type of request packet it should form.
	 *  - The user tells the Client which file it should be transferring.
	 *  - The Client packages the desired bytes into the sendPacket DatagramPacket.
	 *  - The request packet is then sent to a specific port (test mode = port 23, normal mode = port 69).
	 *  - The Client then waits for the DatagramSocket to receive a packet
	 *  - If the Client sent a read request the received packet will receive a DATA packet
	 *  - If the Client sent a write request the received packet will receive a ACK packet
	 */
	public void ClientAlgorithm()
	{
		// Reset for next data transfer
		haltCurrentTransfer = false;
		
		// Overwrite at the beginning of each transfer
		fileWriterAppend = false;
		
		// Prompt the user to set the Directory
		int directory = JOptionPane.showConfirmDialog(popupWindow,"Do you want to use the default directory for the client and server?", "Directory", JOptionPane.YES_NO_OPTION);
		
		// CREATING THE CLIENT DIRECTORY
		clientDir = new File("Client Directory");
		// If default is selected
		if(directory == 0)
		{
			// Set the file path to where Client Directory is created
			directoryPath = clientDir.getAbsolutePath().replace('\\',  '/');		
			// If the directory doesn't already exist, create it
			if(!clientDir.exists())
			{
				try
				{
					//If the directory is successfully created
					clientDir.mkdir();
				}
				catch(SecurityException se)
				{
					System.exit(1);
				}
			}
		}
		else
		{
			// Prompt user to set directory path
			directoryPath = JOptionPane.showInputDialog(null,"Specify file path:", "Directory", JOptionPane.QUESTION_MESSAGE);
		}
		
		// Print the chosen file path for the user to see
		System.out.println("Directory " + directoryPath + "\n");
		
		// HERE!!!!!! The Client will stay in this loop until killed
		while(true)
		{
			while(true)
			{
				while(!allValidInputs)
				{
					UserInputs();
				}
				
				setInputs();
			
				// Print the file path for the user
				System.out.println("Transferring file " + directoryPath + "/" + fileName + "\n");
					
				// Create an empty byte array for the request packet
				message = new byte[4 + fileName.length() + mode.length()];
				message[0] = 0;
			
				// If the user wants to read the file from the server
				if(request == "read")
				{
					// If we give a Read request, we will send ACK packets and receive DATA packets
					sendPacketSize = new byte[517];
					receivePacketSize = new byte[517];
					message[1] = 1;		// 01 is the opcode for read
				}
				
				// If the user wants to write the file to the server
				else if(request == "write")
				{
					// If we give a Write request, we will send DATA packets and receive ACK packets
				    sendPacketSize = new byte[517];
					receivePacketSize = new byte[517];
					message[1] = 2;		// 02 is the opcode for write
				}
				
				// If the user wants an invalid request
				else
				{
					message[1] = 0;		// 00 is the opcode for error
				}
					
				
				// ERROR HANDLING
				if((!new File(clientDir, fileName).exists()) && (request == "write"))
				{
					// File does not exist
					System.out.println("Error: File " + fileName + " does not exist.\n");
					JOptionPane.showMessageDialog(popupWindow, "ERROR: Specified file does not exist \n" + "Please create and try again");
					break;
				}
				else if(new File(clientDir, fileName).exists() && request == "read")
				{
					// File already exists
					System.out.println("Error: File " + fileName + " already exists.\n");
					JOptionPane.showMessageDialog(popupWindow, "ERROR: File already exists! \n" + "No overwrite function");
					break;
				}
				else
				{
					//break;
				}
				// End of Error Handling
		
				// CREATING THE REQUESET PACKET!
				// Saving the file being transferred
				File transferFile = new File(clientDir,fileName);
				
				// Data in packets must be in byte form
				byte[] fileNameToBytes = fileName.getBytes();
				int os1 = fileNameToBytes.length;
		  
				byte[] modeToBytes = mode.getBytes();
				int os2 = modeToBytes.length;
				
				// Copying data into 'message' byte array
				System.arraycopy(fileNameToBytes, 0, message, 2, os1);
				message[os1 + 2] = 0;

				System.arraycopy(modeToBytes, 0, message, os1 + 3, os2);
				int os3 = os1 + os2 + 3;
				message[os3] = 0;
				
				// CREATING THE SEND DATAGRAMPACKET
				try
				{	
					// If we are operating in Test mode, Send to the Error Simulator
					if(tORn == 1)
					{
						sendPacket = new DatagramPacket(message,message.length,InetAddress.getLocalHost(),23);
					}
					// If we are operating in Normal mode, Send to the Server 
					else
					{
						
						sendPacket = new DatagramPacket(message,message.length,address.getAddress(),69);
					}
				}
				catch(UnknownHostException e)
				{
					e.printStackTrace();
					System.exit(1);
				}
				
				// Before we send, check access
				if(request == "write" && readOnly(transferFile))
				{
					byte[] buf = {0,5,0,2};
					sendPacket.setData(buf, 0, 4);
					System.out.println(extractErrorData(sendPacket.getData()));
					JOptionPane.showMessageDialog(popupWindow, "Access Violation: This file is Read Only!");
					break;
				}

				// Send the packet
				send(sendPacket);

				// If we are operating in verbose mode, Print what we sent
				if(qORv == 1)
				{
					System.out.println("Sending: " + request + " Request");
					System.out.println("Host: " + sendPacket.getAddress());
					System.out.println("Destination Port: " + sendPacket.getPort());
					System.out.println("File Name: " + fileName);
					System.out.println("Mode: " + mode);
		
					int length1 = os3 + 1;
					System.out.println("Length: " + length1);
					String info = new String(message,0,length1);
					System.out.println("String : " + info);
					System.out.println("Bytes : " + Arrays.toString(message));
					System.out.println("");
				}

				
				// RECEIVING PACKETS!
				// We initialize the DatagramPacket that we receive into
				receivePacket = new DatagramPacket(receivePacketSize, receivePacketSize.length);
				System.out.println(receivePacketSize);
					
				
				receive();
				
				// End this transfer if Client times out
				if(timeout)
				{
					timeout = false;
					break;
				}
				

		
				// If we are operating in verbose mode, Print what we receive
				if(qORv == 1)
				{
					// Receive Packet info
					System.out.println("Client received packet");
					System.out.println("Sent from Host: " + receivePacket.getAddress());
					System.out.println(" using port: " + receivePacket.getPort());
					int length2 = receivePacket.getLength();
					System.out.println("Length: " + length2);
					System.out.println("Packet: ");

					for(int k = 0; k < receivePacket.getData().length; k++)
					{
						System.out.print(" " + receivePacket.getData()[k]);
					}
					System.out.println();
					System.out.println("\n");
				}
		
				// If we have received an ACK packet
				if(receivePacket.getData()[1] == 0x04)
				{
					try 
					{
						// Start writing data to the server directory
						sendData();
					} 
					catch (FileNotFoundException e) 
					{
						e.printStackTrace();
					}
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
				
				// If we have received a DATA packet
				else if(receivePacket.getData()[1] == 0x03)
				{
					portNum = receivePacket.getPort();
					sendPacket.setPort(portNum);
					sendPacket.setAddress(address.getAddress());
					// Begin to receive data to read into a file
					waitForData();
				}
				else
				{
				//////////////////////////////////////////////////////////////
				//	CLIENT BEHAVIOUR IF IT RECEIVES AN ERROR FROM THE SERVER//
				//////////////////////////////////////////////////////////////
				JOptionPane.showMessageDialog(popupWindow, "Server produced an error! \n" + "Please try again");
				}
				break;
			}
			System.out.println(receivePacket.getData().length);
			// Last step of the loop is to ask the user if they want to kill the client
			int kill = JOptionPane.showConfirmDialog(popupWindow,"Would you like to continue?", "Kill Client", JOptionPane.YES_NO_OPTION);
			if(kill != 0)
			{
				System.exit(0);
			}
			else
			{
				System.out.println(" ");
				allValidInputs = false;
			}
		//END OF LOOP!
			
		}
	// END OF CLIENT ALGORITHM!!!!!!!!!!!!!!!!!!
		
	}


	


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	CREATING AND WRITING TO A FILE METHODS
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	 * A method creates a new file in the client directory
	 * @param	f - The byte array representation of the name of the file being created
	 */
	public void fileCreation(byte[] f)
	{
		// Convert the received file name back into a string
		String fileName = new String(f);
		// Create the file to be added to the directory
		receivedFile = new File(clientDir,fileName);
		
		// If the file does not exist in the client directory
		if(!receivedFile.exists())
		{
			boolean fileAdded = false;
			try
			{
				// Create if the file doesn't already exist
				receivedFile.createNewFile();
				fileAdded = true;
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
				System.exit(1);
			}
			// If the file was successfully added to the directory
			if(fileAdded)
			{
				if(qORv == 1)
				{
					System.out.println("Received file created in the client directory.");
					System.out.println("File: " + receivedFile.toString());
				}
			}
		}
		// If file already exists in client directory
		else
		{
			int overWrite = JOptionPane.showConfirmDialog(popupWindow,"WARNING: \n" + "File already exists, do you want to append to flie?", "File Exists", JOptionPane.YES_NO_OPTION);
			// If we dont want to append the file
			if(overWrite != 0)
			{
				haltCurrentTransfer = true;
			}
		}
		
	}
	
	/*
	 * A method that when called, properly formats the passed data and then writes the data into the passed file
	 * @param	f - The file to be written into
	 * 			byteData - The data being written
	 */
	public void appendToFile(File f, byte[] byteData)
	{
		if (receivePacket.getData()[4] != 0x00)
		try
		{
			// Properly formatting data to be written
			String stringData = new String(resize(byteData));
			System.out.println( "\n" + stringData + "\n");
			
			for (int i = 0; i < resize(byteData).length; i++)
	    	{
	    		System.out.print(resize(byteData)[i] + ", ");
	    	}
			System.out.println(" ");
			// Write data to file
			FileWriter fw = new FileWriter(f.getAbsolutePath(), fileWriterAppend);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(stringData);
			// Close writers
			bw.close();

			System.out.println("Write successful!");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	CLIENT BEHAVIOR FOR READING AND WRITTING
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	 * A method that handles how the Client sends
	 */
	public void sendData() throws FileNotFoundException, IOException
	{
		// Set port to send and receive to
		portNum = receivePacket.getPort();
	    sendPacket.setPort(portNum);
	    sendPacket.setAddress(address.getAddress());
	    
	    System.out.println("Sending data to: " + sendPacket.getPort() + "\n");
	    
	    // The first packet number
	    int packNum = 1;
	    // used for cycling through file
	    int n;
	    // a and b used for printing packet number without negatives
	    int a;
	    int b;
	    boolean mul512 = true;
	
	    // the data of the Datagram packet
	    byte[] fdata = new byte[512];
	    // the full Datagram packet
	    byte[] pack = new byte[516];
	
	    // The packet is a DATA packet (opcode: 03)
	    pack[0] = 0;
	    pack[1] = 3; 
	
	    // Allows the Client to read from the specified file
	    BufferedInputStream in = new BufferedInputStream(new FileInputStream(directoryPath + "\\" + fileName));
	    
	    // while loop cycles through data in file 512 bytes at a time
	    while (((n = in.read(fdata)) != -1) && !haltCurrentTransfer)
	    {
	    	pack[1] = 3;
	    	// setting bytes for packet number converting from int to 2 bytes
	    	pack[3] = (byte) (packNum & 0xFF);
	    	pack[2] = (byte) ((packNum >> 8) & 0xFF); 
	    	packNum ++;
	    
	    	// if end of data from file is null then the remaining part of the file was under 512 bytes
	    	if (fdata[511] == 0x00)
	    	{
	    		mul512 = false;
	    		// resized array to match the remaining bytes in file (from 512 to < 512)
	    	    byte[] lastData = resize(fdata);
	    	    //System.out.println(lastData[3]);
		
	    		System.out.println("data not 512 bytes");
	    		System.out.println("Size of this is array is: " + lastData.length);
		
	    		// copies file data behind opcode and packet number
	    		System.arraycopy(lastData, 0, pack, 4, lastData.length);
		
	    		// resizes final array from 516 to 4 + remaining data from file
	    		byte[] lastPack = resize(pack);
		
	    		// // create the final Datagram Packet out of byte array lastPack
	    		createPack(lastPack);
	    		
	    		// setting bytes for packet number
	    		a = lastPack[2];
	        	b = lastPack[3];
	        	a &= 0xFF;
	        	b &= 0xFF;
	        	
	        	if(qORv == 1)
		    	{
		    		System.out.println("The packet being sent contains: ");
		    		for (int i = 0; i < lastPack.length; i++)
		    		{
			    		System.out.print( lastPack[i] + " ");
			    	}
		    		System.out.println("");
		    	}
	    	}
	    	
	    	// If the data from the file is equal to 512 bytes, meaning there is more data to transfer
	    	else
	    	{
	    		// Store data in pack (offset from the start by 4)
	    		System.arraycopy(fdata, 0, pack, 4, fdata.length);
	    		// create a Datagram Packet out of byte array pack
	    		
	    		createPack(pack);
	    	
	    		// setting bytes for packet number
	    		a = pack[2];
	    		b = pack[3];
	    		a &= 0xFF;
	    		b &= 0xFF;
	    		
	    		if(qORv == 1)
		    	{
		    		System.out.println("The packet being sent contains: ");
		    		for (int i = 0; i < pack.length; i++)
		    		{
			    		System.out.print(" " + pack[i]);
			    	}
		    	}
	    	}
	
	    	// Send the current sendPacket
	    	send(sendPacket);
	    	
	    	// Clear all bytes in fdata
	    	
	
	    	// Wait for next packet
	    	receive();
	    	re(fdata);
	    	re(pack);
	    	//sendPacket.setData(re(sendPacket.getData()));
	    	
	    	// End this transfer if Client times out
			if(timeout)
			{
				timeout = false;
				break;
			}
	    	
	    }
	    // All the data has been received, End loop
	    if (mul512){
	    	byte[] lastPack512 = {0, 3, (byte) ((packNum >> 8) & 0xFF), (byte) (packNum & 0xFF), 0};
	    	sendPacket.setData(lastPack512);
	    	send(sendPacket);
	    	
	    }
	    System.out.println("DONE WRITE");
	    in.close();
	}


	/*
	 * A method that handles what the client does when a read request is sent and a DATA packet is received.
	 */
	public void handleRead() 
	{
		// Save the first 4 bytes of the DATA packet as 'opNum'
		System.arraycopy(receivePacket.getData(), 0, opNum, 0, 4);
		
		
		
		// Cut the first 4 bytes off the DATA packet
		byte[] writeData = cutOP(receivePacket.getData());
		
		// Write data to file
		appendToFile(receivedFile, writeData);
		// After the first write we want to append the data to the file
		fileWriterAppend = true;
		
		// When finished writing, Send ACK
		sendAck(opNum);
	}


	/*
	 * A method that handles what the Client does when it receives a DATA packet
	 */
	public void waitForData()
	{
		// While the DATA packet contains 512 bytes
		byte[] temp = fileName.getBytes();
		// Check to see if the file has been created before
		fileCreation(temp);
		while((receivePacket.getData()[515] != (byte) 0) && !haltCurrentTransfer)
		{
			// If the client receives an error packet
			if(receivePacket.getData()[1] == 5)
			{
				// stop file transfer
				haltCurrentTransfer = true;
				System.out.println("Error: " + extractErrorData(receivePacket.getData()));
			}
			
			// Write to file
			handleRead();
			
			// Send ACK
			//sendAck(opNum);
			
			if(receivePacket.getData()[515] != (byte) 0)
			{
				receivePacket.setData(re(receivePacket.getData()));
				
				receive();
				System.out.println("Leaving receive");
				// End this transfer if Client times out
				if(timeout)
				{
					timeout = false;
					break;
				}
			
			}
			
		}
		System.out.println("The final data packet has been writen to the file!");
		// Last write to file
		handleRead();	
	}


	/*
	 * A method that when called, it initializes the send DatagramPacket to the proper address and port
	 * @param	packet - the byte array to be used in the creation of the sendPacket of the Client
	 * 
	 */
	public void createPack(byte[] packet)
	{
		
			sendPacket = new DatagramPacket(packet,packet.length,address.getAddress(),portNum);
	
    	
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	SENDING AND RECEIVING PACKETS
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	 * A method that properly formats the Client's ACK packets and sets them as the sendPacket
	 * @param	code - The data to be used to form the ACK packet
	 */
	public void sendAck (byte[] code)
	{
		// ACK opcode = 04
		code[1] = (byte) 4;
		// We want to send to the SubServer thread
		portNum = receivePacket.getPort();
		sendPacket.setPort(portNum);
		sendPacket.setAddress(address.getAddress());
		sendPacket.setData(code);
		
		if(qORv == 1)
		{
			System.out.println("Client sent ACK packet " );
			System.out.println("To Host: " + sendPacket.getAddress());
			System.out.println("Destination host port: " + sendPacket.getPort());
			System.out.print("Response Packet: ");
			
			for(int k = 0; k<opNum.length;k++)
			{
				System.out.print(" " + opNum[k]);
			}
			System.out.println();
		}
		
			send(sendPacket);
			System.out.println("");

	}
	


	/*
	 * A method that simply sends the passed packet through the Datagram Socket
	 * @param	sP - The datagram packet being sent
	 */
	public void send(DatagramPacket sP)
	{		
		try
		{
			// Send
			sendReceiveSocket.send(sP);
		}
		
		
		catch(IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Client has sent packet!!" +"\n");
	}
	
	/*
	 * A method that simply receives an incoming Datagram Packet
	 */
	public void receive ()
	{
		int n = 0;
		byte a = sendPacket.getData()[3];
		a +=1;
		byte b = sendPacket.getData()[2];
		b +=1;
		
		while (n<10){
			if(qORv == 1)                          
			{
			// Where we are receiving the packet

				System.out.println("Client is receiving at " + sendReceiveSocket.getLocalPort());
			}
			try
			{
				sendReceiveSocket.receive(receivePacket);
				// when the client is sending ACKS
				if (sendPacket.getData()[1] ==  3 || sendPacket.getData()[1] == 4){
				if (!checkSource()){
					System.out.println("ERROR detected and handled");
					continue;
					
				}
				}
				
				if ((a == receivePacket.getData()[3]|| b == receivePacket.getData()[2] )&& sendPacket.getData()[1] == 4 && receivePacket.getData()[516] == 0){
					System.out.println("We have received an Data Packet");
					return;
				} 
				
				// When the client is sending DataPackets
				else if ((sendPacket.getData()[3] == receivePacket.getData()[3] && sendPacket.getData()[2] == receivePacket.getData()[2] && sendPacket.getData()[1] == 3)){
					System.out.println("We have received a ACK packet");
					return;
				}
				else if ((sendPacket.getData()[1] == 1  && receivePacket.getData()[3] == 1)){
					System.out.println("We have received the first response \n");
					return;
				}
				else if(sendPacket.getData()[1] == 2  && receivePacket.getData()[3] == 0){
					System.out.println("We have received the first response \n");
					return;
				}
				
				
				// If received an error
				else if(receivePacket.getData()[1] == 5)
				{
					System.out.println("Error packet");
					System.out.println(extractErrorData(receivePacket.getData()));
					
					// Right after a read request
					if(sendPacket.getData()[1] == 1)
					{
						// Determine error code
						switch(receivePacket.getData()[3])
						{
							case 1	:	haltCurrentTransfer = true;
										System.out.println("Unrecoverable error, quitting");
										break;
							case 2	:	haltCurrentTransfer = true;
										System.out.println("Unrecoverable error, quitting");
										break;
							case 3	:	send(sendPacket);
										System.out.println("Resending last packet");
										break;
							case 4	:	send(sendPacket);
										System.out.println("Resending last packet");
										break;
							case 5	:	haltCurrentTransfer = true;
										System.out.println("Unrecoverable error, quitting");
										break;
							case 6	:	send(sendPacket);
										System.out.println("Resending last packet");
										break;
							default	:	System.out.println("Invalid error packet");
										System.out.println("Resending last packet");
										send(sendPacket);
						}
					}
					
					// Right after a write request
					else if(sendPacket.getData()[1] == 2)
					{
						// Determine error code
						switch(receivePacket.getData()[3])
						{
							case 1	:	send(sendPacket);
										System.out.println("Resending last packet");
										break;
							case 2	:	haltCurrentTransfer = true;
										System.out.println("Unrecoverable error, quitting");
										break;
							case 3	:	haltCurrentTransfer = true;
										System.out.println("Unrecoverable error, quitting");
										break;
							case 4	:	send(sendPacket);
										System.out.println("Resending last packet");
										break;
							case 5	:	haltCurrentTransfer = true;
										System.out.println("Unrecoverable error, quitting");
										break;
							case 6	:	haltCurrentTransfer = true;
										System.out.println("Unrecoverable error, quitting");
										break;
							default	:	System.out.println("Invalid error packet");
										System.out.println("Resending last packet");
										send(sendPacket);
						}
					}
					
					// Some time during a transfer while reading
					else if(sendPacket.getData()[1] == 4)
					{
						// Determine error code
						switch(receivePacket.getData()[3])
						{
							case 1	:	send(sendPacket);
										System.out.println("Resending last packet");
										break;
							case 2	:	send(sendPacket);
										System.out.println("Resending last packet");
										break;
							case 3	:	send(sendPacket);
										System.out.println("Resending last packet");
										break;
							case 4	:	send(sendPacket);
										System.out.println("Resending last packet");
										break;
							case 5	:	haltCurrentTransfer = true;
										System.out.println("Unrecoverable error, quitting");
										break;
							case 6	:	send(sendPacket);
										System.out.println("Resending last packet");
										break;
							default	:	System.out.println("Invalid error packet");
										System.out.println("Resending last packet");
										send(sendPacket);
						}
					}
					
					// Some time during a transfer while writing
					else
					{
						// Determine error code
						switch(receivePacket.getData()[3])
						{
							case 1	:	send(sendPacket);
										System.out.println("Resending last packet");
										break;
							case 2	:	send(sendPacket);
										System.out.println("Resending last packet");
										break;
							case 3	:	haltCurrentTransfer = true;
										System.out.println("Unrecoverable error, quitting");
										break;
							case 4	:	send(sendPacket);
										System.out.println("Resending last packet");
										break;
							case 5	:	haltCurrentTransfer = true;
										System.out.println("Unrecoverable error, quitting");
										break;
							case 6	:	send(sendPacket);
										System.out.println("Resending last packet");
										break;
							default	:	System.out.println("Invalid error packet");
										System.out.println("Resending last packet");
										send(sendPacket);
						}
					}
					if(haltCurrentTransfer)
					{
						break;
					}
				}
				
				System.out.println(receivePacket.getData()[2] + " " + receivePacket.getData()[3] + " sent " + sendPacket.getData()[2] + " " + sendPacket.getData()[3]);
				System.out.println("Last Packet received was not what was exected it was\n");
				
				for (int i = 0; i < receivePacket.getData().length; i++)
		    	{
		    		System.out.print(receivePacket.getData()[i] + ", ");
		    	}

			}
			catch(IOException e)
			{
				System.out.println("Client didn't get a response");
				//if(sendPacket.getData()[1] == 3)
				System.out.println("resending");
				send(sendPacket);
				n++;
			}
			
			
		}
		System.out.println("Client didn't get a response and has now made 10 attempts. Client is ending transfer.");
		timeout = true;
	}
	
	public boolean checkSource(){
		boolean isValid = true;
		if (portNum != receivePacket.getPort()){
			System.out.println("Packet Received came from the wrong port");
			isValid = false;
			sendError("Packet Received from unknown port", 5);
		}
		if (receivePacket.getData()[0] != 0  ){
		
			isValid = false;
			System.out.println("Invalid OP code first byte not 0x00");
			sendError("Invalid OP code first byte not 0x00", 4);
		}
		if (!(receivePacket.getData()[1] == 3 || receivePacket.getData()[1] == 4|| receivePacket.getData()[1] == 5) ){
			isValid = false;
			System.out.println("Invalid OP code not Data, Ack or ERROR");
			sendError("Invalid OP code not Data, Ack or ERROR", 4);
		}
		if (receivePacket.getData()[516] !=0 ){
			isValid = false;
			receivePacket.getData()[516] = 0;
			System.out.println("TFTP ERROR Datagram was more than 516 bytes");
			sendError("TFTP ERROR Datagram was more than 516 bytes", 4);
		}
		
		return isValid ;
		}
	public void sendError(String message, int code){

			byte [] errorMes = new byte[4 + message.length()];
			errorMes[1] = 5;
			errorMes[3] = (byte) code;
			System.arraycopy(message.getBytes(), 0, errorMes, 4, message.length());
			DatagramPacket sE = null;
			try {
				sE = new DatagramPacket(errorMes, errorMes.length, InetAddress.getLocalHost(), receivePacket.getPort());
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			for(int p = 0; p < 4; p++){
				System.out.println(sE.getData()[p]);
			}
			
		    System.out.println(sE.getPort());
			System.out.println( "Sending "+ message + " Error");
			try {
				sendReceiveSocket.send(sE);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
			
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	MANIPULATING BYTE ARRAYS
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	 * wipes array replacing all elements with null
	 * @param	data - the byte array to be refreshed
	 */
    public byte [] re (byte[] data)
    {
    	for (int i = 0; i < data.length; i++)
    	{
    		data[i] = 0x00;
    	}
    	return data;
    }
	
    /*
     * resizes array to remove any zeros at the end of the array
     * @param	data - the byte array to be resized
     */
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
    
    /*
     * removes opcode and packnum from datagram packet
     * @param	data - the byte array to be resized
     */
    public byte[] cutOP (byte[] data)
    {
    	byte[] temp = new byte[data.length-4];
    	int j = 0;
    	for(int i = 4; i < data.length; i++)
    	{
    		temp[j] = data[i];
    		j++;
    	}
    	return temp;
    } 
    
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//	User Interface
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private void UserInputs()
    {
		String[] tORn = {"test", "normal"};
		String[] qORv = {"verbose","quiet"};
		String[] wORrORe = {"write", "read", "error"};

	
		// USER INPUT 1: Test mode (Uses ErrorSimulator) or Normal mode (Doesn't uses the ErrorSimulator)
		String mode = (String) JOptionPane.showInputDialog(popupWindow,"Choose a Mode:", "Mode", JOptionPane.QUESTION_MESSAGE, null, tORn, tORn[0]);
		
		if(mode == "normal" ){
			ipAddress = JOptionPane.showInputDialog(null,"Specify ipAddress:", "IpAddress", JOptionPane.QUESTION_MESSAGE);
		}
	
		// USER INPUT 2: Quiet mode (Minimal information displayed) or Verbose mode (Displays detailed information)
		String sound = (String) JOptionPane.showInputDialog(popupWindow,"Choose Output Type:", "Output", JOptionPane.QUESTION_MESSAGE, null, qORv, qORv[0]);
	
		//USER INPUT 3: What file will we be transferring?
		String f = JOptionPane.showInputDialog(null,"Specify File:", "File", JOptionPane.QUESTION_MESSAGE);
	
		//USER INPUT 4: What are we doing to the user specified file
		String requestType = (String) JOptionPane.showInputDialog(popupWindow,"Choose Request Type:", "Request", JOptionPane.QUESTION_MESSAGE, null, wORrORe, wORrORe[0]);
	
		if(mode != null && sound != null && f != null && requestType != null)
		{
			System.out.println("ALL VALID INPUTS RECEIVED :)");
			if(mode == "test"){this.tORn = 1;}
			if(mode == "normal"){this.tORn = 2;}
			if(sound == "quiet"){this.qORv = 2;}
			if(sound == "verbose"){this.qORv = 1;}
			if(requestType == "write"){this.request = "write";}
			if(requestType == "read"){this.request = "read";}
			if(requestType == "error"){this.request = "error";}
			this.fileName = f;
			
			if(mode == "test"){try {
				this.address = new InetSocketAddress (InetAddress.getLocalHost(),23);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}}
			
			if(mode == "normal"){
				this.address = new InetSocketAddress (ipAddress,69);
			}
			allValidInputs = true;
		}
		else
		{
			if(mode == null)
			{
				JOptionPane.showMessageDialog(popupWindow, "Mode not selected!");
			}
			if(sound == null)
			{
				JOptionPane.showMessageDialog(popupWindow, "Output Type not selected!");
			}
			if(f == null)
			{
				JOptionPane.showMessageDialog(popupWindow, "File not selected!");
			}
			if(requestType == null)
			{
				JOptionPane.showMessageDialog(popupWindow, "Request not selected!");
			}
		}
	}

    private void setInputs()
    {
    	if(tORn == 1)
    	{
    		System.out.println("We are now in test mode!");
    		// The request packets will be sent to the Error Simulator
    		portNum = 23;		
    	}

    	// If the user inputs 2
    	else if(tORn == 2)
    	{
    		System.out.println("We are now in normal mode!\n");
    		// The request packets will be sent to the Server
    		portNum = 69;		
    		}

    	// If the user input was invalid
    	else
    	{
    		System.out.println("Invalid option");
    	}

    	if(qORv == 1)
    	{
    		System.out.println("We are now in quiet mode\n");
    	}

    	// If the user inputs 2
    	else if(qORv == 2)
    	{
    		System.out.println("We are now in verbose mode\n");
    	}

    	// If the user input is invalid
    	else
    	{
    		System.out.println("Invalid option");
    	}
    }
    
    
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	ERRORS!!!!!!!!!!!!!!!!!!!!!!!!
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Extracts error data from an error packet datagram. Includes the error type and message if applicable.
     * 
     * @param A byte array of error data from a datagram packet.
     * @return A string containing the error type and additional error information from the data array.
     */
    public String extractErrorData(byte[] errorDataBytes)
    {
      String errorCodeString;

      switch(errorDataBytes[3])
        {
            case 1 : errorCodeString = "File not found"; break;
            case 2 : errorCodeString = "Access violation"; break;
            case 3 : errorCodeString = "Disk full or allocation exceeded"; break;
            case 4 : errorCodeString = "Illegal TFTP operation"; break;
            case 5 : errorCodeString = "Unknown transfer ID"; break;
            case 6 : errorCodeString = "File already exists"; break;
            case 7 : errorCodeString = "No such user"; break;
            default: errorCodeString = "Not defined"; break;
        }
        
      return errorCodeString + ": " + new String(errorDataBytes).trim();
    }
    
    /**
  	 * 
  	 * @param file
  	 * @return
  	 */
  	private boolean readOnly(File file)
  	{
  		if(file.canWrite())
  		{
  			return false;
  		}
  		return true;
  	}
    
 
  	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	MAIN
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
	/*
     * The MAIN FUNCTION of the Client class
     */
	public static void main(String[] args) 
	{
		Client c = new Client();
		c.ClientAlgorithm();
	}
}
