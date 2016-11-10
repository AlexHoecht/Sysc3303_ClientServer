import java.io.*;
import java.net.*;
import java.util.Arrays;

public class SubServer implements Runnable {
	
//////////////////////////////////////////////////
	private static String filePath;
	// Datagram socket timeout in milliseconds
	private static int TIMEOUT = 1000;
//////////////////////////////////////////////////
	
	// Datagrams to be used in the SubServer
	private DatagramSocket subServerSocket;
	private DatagramPacket receivePacket, sendPacket;
	
	// Size of the Packet being sent back to the client
	byte[] sendPacketSize;
	byte[] receivePacketSize;
	
	// Byte arrays to store the data being sent and received
	private byte[] data = new byte[516];
	private byte[] ack = new byte[4];
	private byte[] opNum = new byte[4];
	
	// Integer representation of the packet number
	private int packetCounter = 0;
	
	// The file being accessed by the client request
	private File receivedFile;
	private File serverDir;
	
	// The file being transferred
	private String fileName;
	
	// Boolean statements that help with file transfer
	private boolean errorOut = false;
	private boolean firstBlock = true;
	
	/*
	 * The main constructor for the Server class.
	 * When the Server is initialized, it receives at port 69 and creates the Directory
	 * @param	target - The desired port to send packets to
	 * 			d - The data received from the request packet
	 * 			fN - The filename of the file being transferred
	 * 			requestOP - The opcode of the sent request
	 * 			s - The server directory
	 * 			fP - The predefined file path of the file
	 */
	public SubServer ( int target, byte[] d, String fN, byte[] requestOp, File s, String fP)
	{
		// Save the name of the requested file
		fileName = fN;
		// Save the Server Directory
		serverDir = s;
		// Save the filepath
		filePath = fP;

		// Create Socket
		try
		{
			subServerSocket = new DatagramSocket();
			subServerSocket.setSoTimeout(TIMEOUT);
		}
		catch(SocketException se)
		{
			se.printStackTrace();
			System.exit(1);
		}
		
		// Create packet that will be sent back to the client
		// If we received a Read request, we will send DATA packets and receive ACK packets
		if(requestOp[1] == 3)
		{
			sendPacketSize = new byte[516];
			receivePacketSize = new byte[4];
		}
		// If we received a Write request, we will send ACK packets and receive DATA packets
		else
		{
			sendPacketSize = new byte[4];
			receivePacketSize = new byte[516];
		}
		
		// Creating Datagram Packets!
		try 
		{
			// Create the packet of designated size, and points to the address and port of
			// the client
			sendPacket = new DatagramPacket (sendPacketSize, sendPacketSize.length-1,
													InetAddress.getLocalHost(), target);
		} 
		catch (UnknownHostException e) 
		{
			e.printStackTrace();
		}

		// Create the packet of designated size, and points to the address and port of
		// the client
		receivePacket = new DatagramPacket (receivePacketSize, receivePacketSize.length);
		
		// Save data received
		data = d;
		ack = requestOp;
	}
	
	
	@Override
	public void run() 
	{		
		// If write request
		if (data[1] == 2)
		{
			// File creation
			System.out.println(fileName + " is being created");
			fileCreation(fileName);
			
			// If the directory does not have enough space to write the file
			if(serverDir.getUsableSpace() < 512)
	        {
				// Send error
	            sendPacket.setData(createErrorPacket(new byte[] {0, 3}));
	            errorOut = true;
	        }
			// If the directory has enough space to write the file
			if (!errorOut)
			{
				// Write
				sendAck(2);
				waitForData();
			}
		}
		
		// If read request
		else if(data[1] == 1)
		{
			try 
			{
				handleRead(filePath+ "\\"+fileName);
			}
			catch (IOException e) 
			{
				e.printStackTrace();
				System.exit(0);
			}
		}
		else
		{
			// ERROR REQUEST REACHED
		}
		// End of loop
		subServerSocket.close();		
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	FILE CREATION
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void fileCreation(String f)
	{
    	// Create the file to be added to the directory
    	receivedFile = new File(serverDir,f);
    	
    	// If the file doesnt already exist
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
    		
    		// If the file was successfully addes
    		if(fileAdded)
    		{
    			System.out.println("Received file created in the server directory.");
    			System.out.println("File: " + receivedFile.toString());
    		}
    	}
    	// File already exists ERROR
    	else
    	{
    		sendPacket.setData(createErrorPacket(new byte[] {0, 6}));
    		errorOut = true;
    		try 
    		{
    			subServerSocket.send(sendPacket);
    			System.out.println("File which was requested to write already exist\n" );
    			for (int i = 0; i < sendPacket.getData().length; i++)
    			{
    				System.out.print(sendPacket.getData()[i] + ", ");
 				}
 			} 
    		catch (IOException e) 
    		{
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
            return;	
    	}
	}
	
	
	/*
	 * A method that when called, properly formats the passed data and then writes the data into the passed file
	 */
	public void appendToFile(File f, byte[] byteData)
	{
		try
		{
			String stringData = new String(cutEnd(byteData));
			for(int i = 0; i< byteData.length; i++){
				System.out.print(byteData[i]);
			}
	
			System.out.println(f.getAbsolutePath().toString());
			
			FileWriter fw = new FileWriter(f.getAbsolutePath(),!firstBlock);
			firstBlock = false;
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(stringData);
			bw.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	SUBSERVER BEHAVIOUR
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/*
	 * A method that handles how the SubServer reads data to the Client directory
	 */
	public void handleRead(String readFile) throws IOException
	{
		// First check to see if the requested file exists
		// Create the file to be added to the directory
		File tempFile = new File(readFile);
		
		if(!tempFile.isFile())
	    {
	        sendPacket.setData(createErrorPacket(new byte[] {0, 1}));
	        subServerSocket.send(sendPacket);
	        return;
	    }
		
		else
		{
			// Sending read data from file to client
			System.out.println("Sending data to: " + sendPacket.getPort());
			int packNum = 0;
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(readFile));
			
			//bytes from file
			byte[] fdata = new byte[512];
				
			//op code and block # + fdata
		    byte[] pack = new byte[516];
		
		    // used for cycling through file
		    int n;
		
		    // a and b used for printing packet number without negatives
		    int a;
		    int b;
		
		    // data requests are op code 0 3
		    pack[0] = 0;
		    pack[1] = 3; 
		
		    // while loop cycles through data in file 512 bytes at a time
		    while ((n = in.read(fdata)) != -1)
		    {
		    	//setting bytes for packet number converting from int to 2 bytes
		    	pack[3] = (byte) (packNum & 0xFF);
		    	pack[2] = (byte) ((packNum >> 8) & 0xFF); 
		    	packNum ++;
		    
		    	// if end of data from file is null then the remaining part of the file was under 512 bytes
		    	if (fdata[511] == 0x00)
		    	{
		    		// resized array to match the remaining bytes in file (from 512 to < 512)
		    	    byte[] lastData = cutEnd(fdata);
		    	    System.out.println(lastData[3]);
			
		    		System.out.println("data not 512 bytes");
		    		System.out.println("Size of this is array is: " + lastData.length);
			
		    		// copies file data behind opcode and packet number
		    		System.arraycopy(lastData, 0, pack, 4, lastData.length);
			
		    		// resizes final array from 516 to 4 + remaining data from file
		    		//byte[] lastPack = resize(pack);
			
		    		// creates final packet
		    		sendPacket.setData(pack);
		    		a = pack[2];
		        	b = pack[3];
		        	a &= 0xFF;
		        	b &= 0xFF;
		    	}
		    	else
		    	{
		    		System.out.println("\n" + fdata[511] + "\n");
		    		// if file is sending 512 bytes for data
		    		System.arraycopy(fdata, 0, pack, 4, fdata.length);
		    		sendPacket.setData(pack);
		    	
		    		a = pack[2];
		    		b = pack[3];
		    		a &= 0xFF;
		    		b &= 0xFF;
		    	
		    	//System.out.println(a + ", " + b);
		
		    	for (int i = 0; i < pack.length; i++){
		    		System.out.print(" " + pack[i]);
		    	}
		    	}
		    	for (int i = 0; i < sendPacket.getData().length; i++){
		    		System.out.print(" " + sendPacket.getData()[i]);
		    	}
		    	System.out.println( "\n \n" + sendPacket.getData()[1] + " 2nd byte of data being sent");
		    	
		    	// Loop until an acknowledge with the proper block number is received
		    	// If not received within the timeout length, retransmit the last packet
		    	Boolean socketTimedOut = true;
		    	while(socketTimedOut)
		    	{
		    		socketTimedOut = false;
			    	try
					{
						subServerSocket.send(sendPacket);
					} 
					
					catch (IOException e)
					{
						e.printStackTrace();
						System.exit(0);
					}
			    	re(fdata);
			    	sendPacket.setData(re(sendPacket.getData()));
			    	
			    	System.out.println("Reaching receive");
			    	try
			    	{
			    		receive();
			    	}
			    	catch(SocketTimeoutException e)
			    	{
			    		socketTimedOut = true;
			    	}
			    	System.out.println( "\n \n" + receivePacket.getData()[1] + " 2nd byte of data being sent");
		    	}
		    }
		    System.out.println("Leaving Send Data");
			in.close();
			}
		}


	/*
	 * A method that handles what the SubServer does when a write request is received.
	 */
	public void handleWrite(byte[] f)
	{
		// Increase the packet number each write
		packetCounter++;
		
		// Set the opNum to the first 4 bytes of the DATA packet
		System.arraycopy(f, 0, opNum, 0, 4);
		
		// Set the packet number
		opNum[3] = (byte) packetCounter;
		byte[] writeData = cutEnd(resize(receivePacket.getData()));
	
		
		if(!receivedFile.canWrite())
	    { 
			sendPacket.setData(createErrorPacket(new byte[] {0, 2}));
	        try
	        {
	        	subServerSocket.send(sendPacket);
			} 
	        catch (IOException e) 
	        {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        return;
	     }
		// Write data to file
		appendToFile(receivedFile, writeData);
		
		// Send back an ACK
		sendAck(opNum);
	}


	/*
	 * A method that handles what the Server does when it is expecting a DATA packet
	 */
	public void waitForData()
	{
		System.out.println("Server is waiting to receive a data packet");
		
		// Wait to receive the packet
		Boolean socketTimedOut = true;
		while(socketTimedOut)
		{
			socketTimedOut = false;
			try
			{
				receive();
			}
			catch(SocketTimeoutException e)
			{
				socketTimedOut = true;
			}
		}
		
		socketTimedOut = true;
		// While the data is 512 bytes
		// Loop until data with the proper block number is received
    	// If not received within the timeout length, retransmit the last packet
		while(receivePacket.getData()[515] != (byte) 0 && socketTimedOut)
		{
			// If there is not enough usable space in directory
			if(serverDir.getUsableSpace() < 512)
	        {
	            sendPacket.setData(createErrorPacket(new byte[] {0, 3}));
	            errorOut = true;
	            System.out.println("Out of file space error has been sent to client");
	            return;
	        }
			// Write data
			handleWrite(receivePacket.getData());
			System.out.println("data packet has been writen to file");
			
			// Wait to receive another packet
			socketTimedOut = false;
			try
			{
				receive();
			}
			catch(SocketTimeoutException e)
			{
				socketTimedOut = true;
			}
		}
		
		// When the data is less then 512 bytes
		handleWrite(receivePacket.getData());
		firstBlock = true;
		System.out.println("final data packet has been writen to file");
		// Reset the packet counter
		packetCounter = 0;
	}

	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//	SENDING AND RECEIVING
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * A method that properly formats the Server's ACK packets and sets them as the sendPacket
	 * @param	code - The received opcode from the request (SHOULD BE 2!!!!!!!)
	 */
	public void sendAck (int code)
	{
		//Printing out the information of the packet being sent
		System.out.println("Server sent ACK packet");
		System.out.println("To Host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());
		System.out.print("Packet: ");
		
		for(int k = 0; k<ack.length;k++)
		{
			System.out.print(" " + ack[k]);
		}
		// sendPacket data is set to the ACK packet
		sendPacket.setData(ack);
		
		System.out.println();
		try
		{
			subServerSocket.send(sendPacket);
		} 
		
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	
	/*
	 * An alternate method that properly formats the Server's ACK packets and sets them as the sendPacket
	 * @param	code - The byte array that contains the ACK
	 */
	public void sendAck (byte[] code)
	{
		sendPacket.setData(code);
		System.out.println("Server sent ACK packet");
		System.out.println("To Host: " + sendPacket.getAddress());
		System.out.println("Destination host port: " + sendPacket.getPort());
		System.out.print("Response Packet: ");
		
		for(int k = 0; k<ack.length;k++)
		{
			System.out.print(" " + ack[k]);
		}
		
		System.out.println();
		
		try 
		{
			subServerSocket.send(sendPacket);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	/*
	 * A method that sends the Datagram packets
	 */
	public void sendPacket()
	{
		try
		{
			subServerSocket.send(receivePacket);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	/*
	 * A method that waits until a Datagram packet is received
	 */
	public void receive() throws SocketTimeoutException
	{
		do
		{
			try 
			{
				subServerSocket.receive(receivePacket);
			}
			catch(SocketTimeoutException e)
			{
				throw e;
			}
			catch (IOException e) 
			{
				e.printStackTrace();
				System.exit(0);
			}
		}
		// Ignore acknowledges that have a block number less than what was expected
		// i.e. delayed ack packets
		while(sendPacket.getData()[1] == 4 && (sendPacket.getData()[3] < receivePacket.getData()[3] || sendPacket.getData()[2] < receivePacket.getData()[2]));
	}
	
	// wipes array replacing all elements with null
	public byte [] re (byte[] data)
	{
		for (int i = 0; i < data.length; i++)
	{
			data[i] = 0x00;
		}
		return data;
	}


	// resizes arrays to remove null at the end
    public byte[] resize (byte[] data)
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
    
    
    public byte[] cutEnd (byte[] data)
    {
    	int i;
    	for(i = 4; i < data.length; i++)
	{
    		if(data[i] == 0x00)
		{
    			break;
    			
    		}
    	}
    	
    	data = Arrays.copyOf(data, i);
    	
    	return data;
    }
    
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //	ERRORS
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    public byte[] createErrorPacket(byte[] errorCode) throws IllegalArgumentException
	{
		byte[] opcode = {0, 5};
	    byte[] zero = {0};
	    
	    // Error message
	    String errMsgStr;
	    if(errorCode[0] != 0) throw new IllegalArgumentException("First byte in errorCode must be 0");
	    switch(errorCode[1])
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
	    byte[] errMsgByt = errMsgStr.getBytes();
	    
	    // Concatenate opcode, error code, error message, and zero byte
	    byte[] data = new byte[opcode.length + errorCode.length + errMsgByt.length + zero.length];
	    System.arraycopy(opcode, 0, data, 0, opcode.length);
	    System.arraycopy(errorCode, 0, data, opcode.length, errorCode.length);
	    System.arraycopy(errMsgByt, 0, data, opcode.length + errorCode.length, errMsgByt.length);
	    System.arraycopy(zero, 0, data, opcode.length + errorCode.length + errMsgByt.length, zero.length);
	    
	    return data;
	}
    

}