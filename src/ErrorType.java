
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
	ERROR1("Lose packet going to Server"), 
	ERROR2("Lose packet going to Client"), 
	ERROR3("Delay packet going to Server"), 
	ERROR4("Delay packet going to Client"), 
	ERROR5("Duplicate packet going to Server"), 
	ERROR6("Duplicate packet going to Client"), 
	
	ERROR7("Send to Server with wrong block #"), //
	ERROR8("Send to Client with wrong block #"), // 
	
	// INITIAL CLIENT REQUEST ERRORS
	ERROR9("Send Request to server without filname"), //
	ERROR10("Send Request to server without mode"), //
	ERROR11("Send Request to server with invalid mode"), //
	ERROR12("Ack sent to Server port 69"),// can happen whenever
	ERROR13("Data sent to Server port 69"),// can happen whenever
	ERROR14("Error sent to Server port 69"), // can happen whenever
	ERROR15("Send Request to server with more than 3 '0' bytes"), 
	
	
	// Wrong expected packets
	ERROR19("Send Ack to Server when it is expecting Data Write"),
	ERROR20("Send Data to Server when it is expecting Ack Read"),
	ERROR21("Send Ack to Client when it is expecting Data Read"),
	ERROR22("Send Data to Client when it is expecting Ack Write"),
	
	// Send Data/Ack packets < 4 bytes
	ERROR23("Send Data less than 4 bytes long to Server Write"),
	ERROR24("Send Ack less than 4 bytes long to Server Read"),
	ERROR25("Send Data less than 4 bytes long to Client Read"),
	ERROR26("Send Ack less than 4 bytes long to Client WRite"),
	
	ERROR27("Send to server from wrong port"), 
	ERROR28("Send to Client from wrong port"), 
	
	ERROR29("Send Data with more than 516 length"),
	ERROR30("Seend invalid ERROR packet");
	
	

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
