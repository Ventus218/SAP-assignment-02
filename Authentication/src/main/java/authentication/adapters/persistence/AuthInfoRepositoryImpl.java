package authentication.adapters.persistence;

import java.util.Collection;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import authentication.ports.persistence.AuthInfoRepository;
import authentication.adapters.persistence.entities.*;

@Service
class AuthInfoRepositoryImpl implements AuthInfoRepository {

	@Autowired
	AuthInfoCrudRepository repo;

	public void insertRandom() {
		var authInfo = new AuthInfo(
			UUID.randomUUID().toString(),
			"sos",
			true
		);
		repo.save(authInfo);
	}
  
    public Collection<authentication.domain.model.AuthInfo> authInfos(){
		return repo.findAll().stream().map(AuthInfo::toDomainModel).toList();
	}

}
