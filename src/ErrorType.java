
/*
 * An enumerated type to store different types of errors that the error generator can generate.
 */
/**
 * @author Andrew
 *
 */
public enum ErrorType 
{
	// I3 ERRORS
	ERROR0("No Error"),
	ERROR1("Lose packet going to Server"), // WORKING
	ERROR2("Lose packet going to Client"), // WORKING
	ERROR3("Delay packet going to Server"), // WORKING
	ERROR4("Delay packet going to Client"), // WORKING
	ERROR5("Duplicate packet going to Server"),// WORKING
	ERROR6("Duplicate packet going to Client"), // WORKING
	   
	// WRONG BLOCK NUMBER
	
	 ERROR7("Send to Server with wrong block #"), // WORKING
	 ERROR8("Send to Client with wrong block #"), // WORKING
	
	// INITIAL CLIENT REQUEST ERRORS
	ERROR9("Send Request to server without filname"), // WORKING
	ERROR10("Send Request to server without mode"), // WORKING
	ERROR11("Send Request to server with invalid mode"), // WORKING
	
	ERROR12("Send Random Ack to Server port 69"), // WORKING
	ERROR13("Send Random Data to Server port 69"), // WORKING
	
	ERROR14("Send Random Ack to Client"), //  WORKING
	ERROR15("Send Random Data to Client"), // WORKING
	ERROR16("Send ERROR packet to Server"), // 
	ERROR17("Send ERROR packet to Client"), // 
	
	// HAVENT tested fully
	///Not sure if will work
	
	ERROR18("Send Packet less than 4 bytes long to Server"), // 
	ERROR19("Send Packet less than 4 bytes long to Client"), //
	
	
	ERROR20("Send data less than 4 bytes to Server"), // 
	ERROR21("Send data less than 4 bytes to Client"), // 
	
	ERROR22("Send ack less than 4 bytes to Server"), // 
	ERROR23("Send ack less than 4 bytes to Client"), // 
	
	ERROR24("Send to Server from wrong port"), ///
	ERROR25("Send to Client from wrong port"), ///
	
	
	
	ERROR26("Send Data with more than 516 length to Server "), ///
	ERROR27("Send Data with more than 516 length to Client "), ///
	ERROR28("Send invalid ERROR packet to Server"), ///
	ERROR29("Send invalid ERROR packet to Client"), ///
	ERROR30("Send Request to server with more than 3 '0' bytes"); 
	
	
	
	
	

	private final String description;

	/**
	 * Constructor for enum with a String description
	 * @param description of the Error
	 */
	private ErrorType(String description)
	{
		this.description = description;
	}

	@Override
	public String toString()
	{
		return description;
	}
}
