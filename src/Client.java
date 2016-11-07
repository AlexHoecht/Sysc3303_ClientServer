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
	
	// Size of the Packet being sent back to the client
	private byte[] sendPacketSize;
	private byte[] receivePacketSize;
	// The data of the request packet
	private byte[] message;
	private byte[] opNum = new byte[4];
	
	// User input mode of the TFTP
	private boolean quiet;
	private boolean normal;
	// if true stop the current data transfer
    	private boolean haltCurrentTransfer;
	// False overwrites the current file, true appends additional data
	private boolean fileWriterAppend;
	
	// The client directory
	private File clientDir;
	// The place holder for the file being transferred
	private File receivedFile;
	
	// The port that the Datagram Packet will be sent to
	private int portNum;

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
			sendReceiveSocket.setSendBufferSize(1000);
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
		// We want our Client to run until we say so
		boolean running = true;		
		while (running)
		{
			// Reset for next data transfer
			haltCurrentTransfer = false;
			
			// Overwrite at the beginning of each transfer
			fileWriterAppend = false;
			
			// The user input request
			String request = null;
	
			//keeps track between test or normal mode
			int tOrN = 0;		
			//keeps track between quiet and verbose 
			int qOrV = 0;
			
			//User input stream
			BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
			
			// CREATING THE CLIENT DIRECTORY
			clientDir = new File("Client Directory");
			// Set the file path to where Client Directory is created
			directoryPath = clientDir.getAbsolutePath().replace('\\',  '/');
			// Print file path for the user to see
			System.out.println("Directory " + directoryPath + "\n");
			
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
			
			// USER INPUT 1: Test mode (Uses ErrorSimulator) or Normal mode (Doesn't uses the ErrorSimulator)
			while(tOrN == 0)
			{
				System.out.print("Would you like to enter test mode, or normal mode? (1 for test 2 for normal): ");
				try
				{
					// Input
					inputString = bufferRead.readLine();
				}
				catch(IOException ex)
				{
					ex.printStackTrace();
					System.exit(0);
				}
				
				// If the user inputs 1
				if(inputString.equals("1"))
				{
					System.out.println("We are now in test mode!\n");
					tOrN = 1;
					normal = false;
					portNum = 23;		// The request packets will be sent to the Error Simulator
				}
				
				// If the user inputs 2
				else if(inputString.equals("2"))
				{
					System.out.println("We are now in normal mode!\n");
					tOrN = 2;
					normal = true;
					portNum = 69;		// The request packets will be sent to the Server
				}
				
				// If the user input was invalid
				else
				{
					System.out.println
					("Invalid option");
				}
			}
        
			// USER INPUT 2: Quiet mode (Minimal information displayed) or Verbose mode (Displays detailed information)
			while(qOrV == 0)
			{
				System.out.print("Would you like to enter quiet mode, or verbose mode? (1 for quiet 2 for verbose): ");
				try
				{
					// Input
					inputString = bufferRead.readLine();
				}
				catch(IOException ex)
				{
					ex.printStackTrace();
					System.exit(0);
				}
				
				// If the user inputs 1
				if(inputString.equals("1"))
				{
					System.out.println("We are now in quiet mode\n");
					qOrV = 1;
					quiet = true;
				}
				
				// If the user inputs 2
				else if(inputString.equals("2"))
				{
					System.out.println("We are now in verbose mode\n");
					qOrV = 2;
					quiet = false;
				}
				
				// If the user input is invalid
				else
				{
					System.out.println("Invalid option");
				}
			}
	
			// HERE!!!!!! The Client will stay in this loop until killed
			while(true)
			{
				// This loop handles an invalid input, file not found, and other errors that may occur during input
				while(true)
				{
					// USER INPUT 3: What file will we be transferring?
					System.out.print("What file are we going to be transferring? ");
					try
					{
						// input
						inputString = bufferRead.readLine();
					}
					catch(IOException ex)
					{
						ex.printStackTrace();
			       	}
					
					// The user input file name
					fileName = inputString;
					// Print the file path for the user
					System.out.println("Transferring file " + directoryPath + "/" + fileName + "\n");
					
					// Create an empty byte array for the request packet
					message = new byte[4 + fileName.length() + mode.length()];
					message[0] = 0;
			
					// USER INPUT 4: What are we doing to the user specified file
					System.out.print("Would you like to read a file (read) or write to a file (write)? ");
					try
					{
						// input
						inputString = bufferRead.readLine();
					}
					catch(IOException ex)
					{
						ex.printStackTrace();
			       	}

					// If the user wants to read the file from the server
					if(inputString.equals("read"))
					{
						// If we give a Read request, we will send ACK packets and receive DATA packets
						sendPacketSize = new byte[4];
						receivePacketSize = new byte[516];
						message[1] = 1;		// 01 is the opcode for read
						request = "Read";
					}
					
					// If the user wants to write the file to the server
					else if(inputString.equals("write"))
					{
						// If we give a Write request, we will send DATA packets and receive ACK packets
					    	sendPacketSize = new byte[516];
						receivePacketSize = new byte[4];
						message[1] = 2;		// 02 is the opcode for write
						request = "Write"; 
					}
					
					// If the user wants an invalid request
					else
					{
						message[1] = 0;		// 00 is the opcode for error
						request = "Error"; 	// #11 invalid request
					}
					
					// ERROR HANDLING
					if((!new File(clientDir, fileName).exists()) && (request == "Write"))
					{
						// File does not exist
						System.out.println("Error: File " + fileName + " does not exist.\n");
					}
					else if(new File(clientDir, fileName).exists() && request == "Read")
					{
						// File already exists
						System.out.println("Error: File " + fileName + " already exists.\n");
					}
					else
					{
						break;
					}
					// End of Error Handling loop
				}
		
				// CREATING THE REQUESET PACKET!
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
					if(normal == false)
					{
						sendPacket = new DatagramPacket(message,message.length,InetAddress.getLocalHost(),23);
					}
					// If we are operating in Normal mode, Send to the Server 
					else
					{
						sendPacket = new DatagramPacket(message,message.length,InetAddress.getLocalHost(),69);
					}
				}
				catch(UnknownHostException e)
				{
					e.printStackTrace();
					System.exit(1);
				}

				// Send the packet
				send(sendPacket);

				// If we are operating in verbose mode, Print what we sent
				if(quiet == false)
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
				}

				
				// RECEIVING PACKETS!
				// We initialize the DatagramPacket that we receive into
				receivePacket = new DatagramPacket(receivePacketSize, receivePacketSize.length);
					
				receive();
				

		
				// If we are operating in verbose mode, Print what we receive
				if(quiet == false)
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
					// Begin to receive data to read into a file
					waitForData();
				}
				else
				{
				//////////////////////////////////////////////////////////////
				//	CLIENT BEHAVIOUR IF IT RECEIVES AN ERROR FROM THE SERVER//
				//////////////////////////////////////////////////////////////
				}
				
				// Last step of the loop is to ask the user if they want to kill the client
				System.out.println("Would you like to kill the client? (k to kill, any other key to keep running)");
				try
				{
					inputString = bufferRead.readLine();
		        
					// If the user wanted to kill the client
					if(inputString.equals("k"))
					{
						System.exit(0);
					}
				}
				catch(IOException ex)
				{
		        ex.printStackTrace();
		        }
				//END OF LOOP!
			}
			// END OF CLIENT ALGORITHM
		}
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
				if(quiet == false)
				{
					System.out.println("Received file created in the client directory.");
					System.out.println("File: " + receivedFile.toString());
				}
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
		try
		{
			// Properly formatting data to be written
			String stringData = new String(resize(byteData));
			
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
	    
	    System.out.println("Sending data to: " + sendPacket.getPort());
	    
	    // The first packet number
	    int packNum = 0;
	    // used for cycling through file
	    int n;
	    // a and b used for printing packet number without negatives
	    int a;
	    int b;
	
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
	    	// setting bytes for packet number converting from int to 2 bytes
	    	pack[3] = (byte) (packNum & 0xFF);
	    	pack[2] = (byte) ((packNum >> 8) & 0xFF); 
	    	packNum ++;
	    
	    	// if end of data from file is null then the remaining part of the file was under 512 bytes
	    	if (fdata[511] == 0x00)
	    	{
	    		// resized array to match the remaining bytes in file (from 512 to < 512)
	    	    byte[] lastData = resize(fdata);
	    	    System.out.println(lastData[3]);
		
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
	        	
	        	if(quiet == false)
		    	{
		    		System.out.println("The packet being sent contains: ");
		    		for (int i = 0; i < lastPack.length; i++)
		    		{
			    		System.out.print(" " + lastPack[i]);
			    	}
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
	    		
	    		if(quiet == false)
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
	    	re(fdata);
	
	    	// Wait for next packet
	    	receive();   	
	    }
	    // All the data has been received, End loop
	    in.close();
	}


	/*
	 * A method that handles what the client does when a read request is sent and a DATA packet is received.
	 */
	public void handleRead() 
	{
		// Save the first 4 bytes of the DATA packet as 'opNum'
		System.arraycopy(receivePacket.getData(), 0, opNum, 0, 4);
		
		byte[] temp = fileName.getBytes();
		// Check to see if the file has been created before
		fileCreation(temp);
		
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
			System.out.println("Data has been writen to the file!");
			
			// Send ACK
			sendAck(opNum);
			
			if(receivePacket.getData()[515] != (byte) 0)
			{
				receivePacket.setData(re(receivePacket.getData()));
				receive();
			
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
		try
		{
			sendPacket = new DatagramPacket(packet,packet.length,InetAddress.getLocalHost(),portNum);
		}
		catch(UnknownHostException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
    	
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
		sendPacket.setPort(receivePacket.getPort());
		sendPacket.setData(code);
		
		if(quiet == false)
		{
			System.out.println("Server sent ACK packet");
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
		
		System.out.println("Client has sent packet!");
	}
	
	/*
	 * A method that simply receives an incoming Datagram Packet
	 */
	public void receive ()
	{
		int n = 0;
		while (n<5){
			if(quiet == false)
			{
			// Where we are receiving the packet
				System.out.println("Client is receiving at " + sendReceiveSocket.getLocalPort());
			}
			try
			{
				sendReceiveSocket.receive(receivePacket);
				return;
			}
			catch(IOException e)
			{
				System.out.println("Client didn't get a response");
			}
			System.out.println("We have received");
		}
		System.out.println("Client didn't get a response and has now made 5 attempts. Client is ending transfer.");
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
    	for(i = 4; i < data.length; i++)
    	{
    		if(data[i] == 0x00)
    		{
    			break;
    			
    		}
    	}
    	
    	data = Arrays.copyOf(data, i+1);
    	return data;
    }
    
    /*
     * removes opcode and packnum from datagram packet
     * @param	data - the byte array to be resized
     */
    public byte[] cutOP (byte[] data)
    {
    	byte[] temp = new byte[data.length];
    	int j = 0;
    	for(int i = 4; i < data.length; i++)
    	{
    		temp[j] = data[i];
    		j++;
    	}
    	return temp;
    } 
    
    
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	ERRORS!!!!!!!!!!!!!!!!!!!!!!!!
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    /*
     * Extract the error code from an error packet. Does not extract error message!!!
     * @param data Error packet as a byte array.
     * @return Error packet as a string.
     */
    private String extractErrorData(byte[] data)
    {
    	String errMsgStr;
    	// Determine error type
    	switch(data[3])
        {
            case 1 : errMsgStr = "File not found";
                     break;
            case 2 : errMsgStr = "Access violation";
                     break;
            case 3 : errMsgStr = "Disk full or allocation exceeded";
                     break;
            case 4 : errMsgStr = "Illegal TFTP operation";
                     break;
            case 5 : errMsgStr = "Unknown transfer ID";
                     break;
            case 6 : errMsgStr = "File already exists";
                     break;
            case 7 : errMsgStr = "No such user";
                     break;
            default: errMsgStr = "Not defined";
                     break;
        }
    	return errMsgStr;
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
