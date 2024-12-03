package authentication.ports.usersservice;

public class UsersServiceException extends Exception {
	public UsersServiceException(Exception innerException) {
		super(innerException);
	}
}
