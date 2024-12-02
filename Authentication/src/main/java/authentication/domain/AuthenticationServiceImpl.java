package authentication.domain;


import java.util.Collection;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import authentication.domain.model.*;
import authentication.ports.AuthenticationService;
import authentication.ports.persistence.AuthInfoRepository;

@Service
class AuthenticationServiceImpl implements AuthenticationService {

    @Autowired
	AuthInfoRepository authInfoRepository;

	public void insert() {
        authInfoRepository.insertRandom();
    }

	public Collection<AuthInfo> all() {
        return authInfoRepository.authInfos();
    }
}
