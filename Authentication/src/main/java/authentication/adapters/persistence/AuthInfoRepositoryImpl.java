package authentication.adapters.persistence;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import authentication.ports.persistence.AuthInfoRepository;
import authentication.ports.persistence.exceptions.*;
import authentication.domain.model.*;
import static authentication.adapters.persistence.entities.AuthInfo.*;

@Service
class AuthInfoRepositoryImpl implements AuthInfoRepository {

	@Autowired
	AuthInfoCrudRepository repo;

	public void insert(AuthInfo authInfo) throws AuthInfoAlreadyExistsException {
		var entity = fromDomainModel(authInfo);
		if (repo.findById(entity.username()).isPresent()) {
			throw new AuthInfoAlreadyExistsException();
		}
		repo.save(entity);
	}

	public Optional<AuthInfo> retrieve(Username username) {
		return repo.findById(username.value()).map(authInfo -> authInfo.toDomainModel());
	}

	public void forcePasswordAuthentication(Username username) throws AuthInfoNotExistsException {
		var authInfoOpt = repo.findById(username.value());
		if (authInfoOpt.isEmpty()) {
			throw new AuthInfoNotExistsException();
		}
		var authInfo = authInfoOpt.get();
		authInfo.setCanRenew(false);
		repo.save(authInfo);
	}

	public void allowTokenRenewal(Username username) throws AuthInfoNotExistsException {
		var authInfoOpt = repo.findById(username.value());
		if (authInfoOpt.isEmpty()) {
			throw new AuthInfoNotExistsException();
		}
		var authInfo = authInfoOpt.get();
		authInfo.setCanRenew(true);
		repo.save(authInfo);
	}
}
