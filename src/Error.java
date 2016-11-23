/**
 * Used to keep track of an error type, and a packet to generate the error on
 * @author Andrew
 *
 */
public class Error {
	
	private ErrorType errorType; // ErrorType enum



	private int packetNumber;
	
	
	/**
	 * Construct an Error with given error type and packet number
	 * @param errorType
	 * @param packetNumber
	 */
	public Error(ErrorType errorType, int packetNumber)
	{
		this.errorType = errorType;
		this.packetNumber = packetNumber;
	}
	
	/**
	 * @return the ErrorType
	 */
	public ErrorType getErrorType()
	{
		return errorType;
	}
	
	public void setErrorType(ErrorType errorType) {
		this.errorType = errorType;
	}
	
	
	/**
	 * @return the packetNumber for the error to be generated on
	 */
	public int getPacketNumber()
	{
		return packetNumber;
	}
	
	

}
