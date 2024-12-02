package authentication.ports;

import java.util.Optional;
import authentication.domain.model.*;

public interface AuthenticationService {

	public String register(Username username, String password);

	/**
	 * Returns a token or an empty optional in case the authentication failed.
	 */
	public Optional<String> authenticate(Username username, String password);

	/**
	 * Returns the new token or an empty optional in case the given one was not
	 * valid.
	 */
	public Optional<String> refresh(String jwt);

	/**
	 * If valid returns the authenticated user username otherwise an empty optional.
	 */
	public Optional<Username> validate(String jwt);

	public void forceAuthentication(Username username);
}
