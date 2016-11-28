import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.util.Arrays;

/**
 * Server
 * SYSC 3303 
 * Fall Semester 2016
 * 
 * @author Andrew Ward      100898624
 * @author Alex Hoecht      100933730
 * @author Connor Emery     100980809
 * @author Robert Graham    100981086
 * @author Saleem Karkabi   100944655 
 */
public class Server  
{
	// Server's well known port
	private static final int PORT = 69;
	
	private int initialPort;
	// Name of the directory where server files will be stored
	private static final String DIRECTORY_NAME = "Server Directory";
	
	private DatagramSocket socket;
	private DatagramPacket receivePacket, sendPacket;
	
	// Server Directory
	private File directory;
	// Path of the current directory used to transfer files to and from
	private String directoryPath;
	// Error message to respond to an invalid request with if applicable
	private String errorMessage;
	
	// The client connection thread
	// TODO Currently the server can spawn only one client connection manager thread at a time. Eventually this will have to be a list of threads.
	private Thread t;
	// Boolean denoting whether a client connection manager is running. 
	private boolean hasThreadStarted;
	// Boolean denoting whether the server has been requested to shut down
	public boolean killServer;
	
	/*
	 * The main constructor for the Server class.
	 * When the Server is initialized, it receives at port 69 and creates the Directory
	 */
	public Server()
	{
		hasThreadStarted = false;
		killServer = false;
		
		// Initialize datagram socket
		try
		{
			socket = new DatagramSocket(PORT);	
		}
		catch(SocketException se)
		{
			se.printStackTrace();
			System.exit(1);
		}
		
		directory = new File(DIRECTORY_NAME);
		// Create the directory if it doesn't already exist
		if(!directory.exists())
		{
			System.out.println("Creating server directory");
			try
			{
				directory.mkdir();
			}
			catch(SecurityException se)
			{
				System.exit(1);
			}
		}
	}
	
	/**
	 * The behaviour of the Server is as follows:
	 *  Forever:
	 * 		- A packet is created to receive DatagramPackets into
	 * 		- The Server receives a packet and prints all important information about it
	 * 		- Validates the request and returns an error packet to the request sender if invalid
	 * 			- Restart the loop
	 * 		- A Client connection thread (SubServer) is then created to handle the file transfer
	 */
	public void serverAlgorithm()
	{
		// Sets the default file path for the Server Directory 
		directoryPath = directory.getAbsolutePath().replace('\\',  '/');
		System.out.println("Directory " + directoryPath + "\n");
		
		Thread serverKiller = new Thread(new ServerKiller(this));
		serverKiller.start();
		
		while(!killServer)
		{
			// Byte arrays created to pack and unpacked data
			byte[] msg = new byte[517];
			byte[] data = new byte[4];
			
			// That packet that will receive the Packet from the Client
			receivePacket = new DatagramPacket(msg, msg.length);
			System.out.println("Waiting for a packet...\n");
			
			try
			{
				// Receive the packet
				socket.receive(receivePacket);
			}
			catch(IOException e)
			{
				System.out.println("Error\n" + e);
				e.printStackTrace();
				System.exit(1);
			}
		
			System.out.println("Packet received\n");
			
			String request = "";
			
			// Check the packet for validity
			errorMessage = isValidRequest(msg);
			
			// If there is a problem with the request
			if(errorMessage != null)
			{
				// Send an error packet back to where the request originated
				byte[] errorData = generateErrorData(4, errorMessage);
				System.out.println("Invalid request format: " + errorMessage);
				System.out.println("Sending error packet to client");
				sendPacket = new DatagramPacket(errorData, errorData.length, receivePacket.getAddress(), receivePacket.getPort());
				try
				{
					socket.send(sendPacket);
				}
				catch (IOException e)
				{
					System.out.println("Error\n" + e);
					e.printStackTrace();
					System.exit(1);
				}
			}
			else // No problems with the request
			{
				// Read request
				if (msg[1] == 1)
				{
					request = "Read";
					data[1] = 3;
				}
				// Write request
				else if(msg[1] == 2)
				{
					request = "Write";
					data[1] = 4;
				}
				
				System.out.println("Request type\t" + request);
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
				
				String fileName = new String(file);
				
					t = new Thread (new SubServer(receivePacket.getPort(), receivePacket.getData(), fileName, data, directory, directoryPath));
					t.start();
					initialPort = receivePacket.getPort();
					hasThreadStarted = true;
			
			
				// Output received packet information
				
				System.out.println("File Name\t" + fileName);
				
				String mode2 = new String(mode);
				System.out.println("Transfer mode\t" + mode2);
				
				// TODO Is this necessary?
				int len = receivePacket.getData().length;
				System.out.println("Length\t\t" + len + "\n");
				
				String infoString = new String(msg,0,len).trim();
				System.out.println("Information as String:\t" + infoString);
	
				msgBytes = Arrays.copyOfRange(msg, 0, len);
				System.out.println("Information as Bytes:\t"+ Arrays.toString(truncZeroesFromReq(msgBytes)) + "\n");
				
				// Create client connection manager
				System.out.println("Spawning new client connection manager\n");	
			}
		}
		try
		{
			kill();
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Checks whether the specified byte array is a valid TFTP request and finds the problem with it if applicable..
	 * 
	 * @param request TFTP request byte array.
	 * @return If request is invalid, a string describing the error with the request. If request is valid, null.
	 */
	private String isValidRequest(byte[] request)
    {
        // Request must be in the form: 0, 1 || 2, filename, 0, netascii || octet, 0
        if(request.length < 11) return "Request components missing";
        if(request[0] != 0 || !(request[1] == 1 || request[1] == 2)) System.out.println("Invalid opcode");;

        // Used to store file name and transfer mode
        byte[] tempByteArr = new byte[516];
        // Used to iterate through the request
        int i = 2;
        
        // Iterate through filename to null char
        if(request[2] == 0) return "No file name";
        else
        {
            for(; request[i + 1] != 0; i++)
            {
                if(i >= request.length - 1) return "Missing zero after file name";
                tempByteArr[i - 2] = request[i];
            }
        }
        tempByteArr[i - 2] = request[i];
        
        // Check if valid filename
        String fileName = (new String(tempByteArr)).trim();
        try
        {
        	(new File(fileName)).getCanonicalPath();
        }
        catch(IOException e)
        {
        	return "Invalid file name";
        }
        
        // Clear the temporary array to make way for the transfer mode
        for(int j = 0; j < tempByteArr.length; j++)
        {
            tempByteArr[j] = 0;
        }
        
        // Iterate through transfer mode to the null character
        if(i > request.length - 8) return "Invalid or no mode";
        else
        {
            for(i = 3 + fileName.length(); request[i] != 0; i++)
            {
                if(i >= request.length - 1) return "Missing zero after mode";
                tempByteArr[i - 3 - fileName.length()] = request[i];
            }
        }
        
        // Check if valid transfer mode
        String mode = (new String(tempByteArr)).trim().toLowerCase();
        if(!(mode.equals("netascii") || mode.equals("octet"))) return "Invalid mode";
        
        // Valid packet
        return null;
    }
	
	/**
     * Generates error packet data depending on the opcode and message. Any invalid error code is defaulted to code 0 "Error not defined".
     * 
     * @param errorCode A number from 0 to 7 inclusive specifying the type of error.
     * @param errorMessageString A message for additional error information.
     * @return A byte array to be put into a datagram packet.
     */
    public byte[] generateErrorData(int errorCode, String errorMessageString)
    {
        byte[] errorDataBytes = new byte[5 + errorMessageString.length()];
        byte[] errorMessageBytes = errorMessageString.getBytes();
        
        // Default error code if necessary
        if(errorCode < 0 || errorCode > 7)
        {
            errorCode = 0;
        }
        
        // Initialise opcode, error cods, and zero bytes
        errorDataBytes[0] = 0;
        errorDataBytes[1] = 5;
        errorDataBytes[2] = 0;
        errorDataBytes[3] = (byte) errorCode;
        errorDataBytes[4 + errorMessageString.length()] = 0;
        // Initialise error error message
        for(int i = 0; i < errorMessageString.length(); i++)
        {
            errorDataBytes[i + 4] = errorMessageBytes[i];
        }
        
        return errorDataBytes;
    }
    
    /**
     * Truncates all extra zero from a TFTP request packet. Must be a valid packet or errors will occur.
     * @param requestArray TFTP request as a byte array.
     * @return The request byte array with all extra zeroes removed.
     */
    private byte[] truncZeroesFromReq(byte[] requestArray)
    {
    	int i;
    	for(i = requestArray.length - 1; requestArray[i] == 0 && i > 0; i--){}
    	byte[] truncated = new byte[i + 2];
    	for(i = 0; i < truncated.length && i < requestArray.length; i++)
    	{
    	    truncated[i] = requestArray[i];
    	}
    	return truncated;
    }
    
	public void kill() throws InterruptedException
	{
		if(hasThreadStarted)
		{
			System.out.println("Waiting for client connection managers to terminate...");
			t.join();
		}
		System.out.println("Terminating");
		System.exit(0);
	}
	
	/**
     * Creates a new server and executes it's server algorithm
     */
	public static void main(String[] args)
	{
		Server server = new Server();
		server.serverAlgorithm();
	}
}
