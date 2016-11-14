
/*
 * An enumerated type to store different types of errors that the error generator can generate.
 */
/**
 * @author Andrew
 *
 */
public enum ErrorType 
{
	
	ERROR0("No Error"),
	ERROR1("Lose packet going to Server"),
	ERROR2("Lose packet going to Client"),
	ERROR3("Delay packet going to Server"),
	ERROR4("Delay packet going to Client"),
	ERROR5("Duplicate packet going to Server"),
	ERROR6("Duplicate packet going to Client");
	
	

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
