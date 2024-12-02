package authentication.adapters.persistence;

import org.springframework.stereotype.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import authentication.adapters.persistence.entities.*;

@Service
interface AuthInfoCrudRepository extends JpaRepository<AuthInfo, String> {

}
