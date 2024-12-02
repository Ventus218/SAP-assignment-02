package authentication.ports.persistence;

import java.util.Optional;
import authentication.domain.model.*;
import authentication.ports.persistence.exceptions.*;

public interface AuthInfoRepository {

	public void insert(AuthInfo authInfo) throws AuthInfoAlreadyExistsException;

	public Optional<AuthInfo> retrieve(Username username);

	public void forcePasswordAuthentication(Username username) throws AuthInfoNotExistsException;

	public void allowTokenRenewal(Username username) throws AuthInfoNotExistsException;
}
