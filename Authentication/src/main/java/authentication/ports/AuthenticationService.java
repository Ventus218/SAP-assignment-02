package authentication.ports;

import java.util.Collection;
import authentication.domain.model.*;

public interface AuthenticationService {
    public void insert();

	public Collection<AuthInfo> all();
}
