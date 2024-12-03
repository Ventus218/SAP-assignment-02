package authentication.ports.usersservice;

import authentication.domain.model.Username;

public interface UsersService {

	public void registerUser(Username username) throws UserAlreadyRegisteredException, UsersServiceException;
}
