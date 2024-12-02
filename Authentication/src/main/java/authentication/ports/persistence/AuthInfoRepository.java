package authentication.ports.persistence;

import java.util.Collection;
import authentication.domain.model.*;

public interface AuthInfoRepository {

    void insertRandom();
  
    Collection<AuthInfo> authInfos();
  
}
