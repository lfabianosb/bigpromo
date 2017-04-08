package voegol.exception;

public class FlightNotFoundException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public FlightNotFoundException(Exception e) {
		super(e);
	}
	
	public FlightNotFoundException(String message) {
		super(message);
	}
}