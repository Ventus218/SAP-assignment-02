package authentication.domain.model;

public record AuthInfo(Username username, String passwordHash, boolean canRenew) {}
