package authentication.domain.exceptions;

public class SomethingWentWrongException extends Exception {

	public SomethingWentWrongException(String message) {
		super(message);
	}

	public SomethingWentWrongException(Exception innerException) {
		super(innerException);
	}

	public SomethingWentWrongException(String message, Exception innerException) {
		super(message, innerException);
	}
}
