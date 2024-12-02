package authentication.adapters.presentation;

import authentication.domain.model.*;

public record AuthenticateDTO(Username username, String password) {}
