package authentication.adapters.persistence.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class AuthInfo {
	@Id
	private String username;
	private String passwordHash;
	private Boolean canRenew;

	protected AuthInfo() {
	}

	public AuthInfo(String username, String passwordHash, Boolean canRenew) {
		this.username = username;
		this.passwordHash = passwordHash;
		this.canRenew = canRenew;
	}

	public String username() {
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String passwordHash() {
		return this.passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public boolean canRenew() {
		return this.canRenew;
	}

	public void setCanRenew(boolean canRenew) {
		this.canRenew = canRenew;
	}

	public authentication.domain.model.AuthInfo toDomainModel() {
		var username = new authentication.domain.model.Username(username());
		return new authentication.domain.model.AuthInfo(username, passwordHash(), canRenew());
	}

	public static AuthInfo fromDomainModel(authentication.domain.model.AuthInfo authInfo) {
		var entity = new AuthInfo();
		entity.setUsername(authInfo.username().value());
		entity.setPasswordHash(authInfo.passwordHash());
		entity.setCanRenew(authInfo.canRenew());
		return entity;
	}
}
