package authentication.ports;

import java.util.Optional;
import authentication.domain.model.*;
import authentication.domain.exceptions.*;

public interface AuthenticationService {

	public String register(Username username, String password)
			throws UserAlreadyExistsException, SomethingWentWrongException;

	public String authenticate(Username username, String password)
			throws UserNotFoundException, WrongCredentialsException;

	public String refresh(String jwt)
			throws PasswordAuthenticationRequiredException, SessionExpiredException, InvalidTokenException;

	public Username validate(String jwt) throws SessionExpiredException, InvalidTokenException;

	public void forceAuthentication(String jwt, Username username)
			throws UserNotFoundException, SessionExpiredException, InvalidTokenException;

	public Optional<String> healthCheckError();
}
