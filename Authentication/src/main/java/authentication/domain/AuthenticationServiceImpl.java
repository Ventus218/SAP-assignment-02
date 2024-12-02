package authentication.domain;

import java.time.*;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.auth0.jwt.*;
import com.auth0.jwt.algorithms.*;
import com.auth0.jwt.exceptions.*;
import authentication.domain.model.*;
import authentication.domain.exceptions.*;
import authentication.ports.AuthenticationService;
import authentication.ports.persistence.AuthInfoRepository;
import authentication.ports.persistence.exceptions.*;

@Service
class AuthenticationServiceImpl implements AuthenticationService {

	@Autowired
	AuthInfoRepository authInfoRepository;

	private final Duration expirationDuration = Duration.ofSeconds(20);

	private final JWTVerifier verifier = JWT.require(algorithm()).build();

	private Algorithm algorithm() {
		return Algorithm.HMAC256("secret");
	}

	public String register(Username username, String password) throws UserAlreadyExistsException {
		// TODO: hash password
		throw new UnsupportedOperationException();
	}

	public String authenticate(Username username, String password)
			throws UserNotFoundException, WrongCredentialsException {
		// TODO: hash password
		var authInfoOpt = authInfoRepository.retrieve(username);
		if (authInfoOpt.isEmpty()) {
			throw new UserNotFoundException();
		}
		var authInfo = authInfoOpt.get();

		if (!authInfo.passwordHash().equals(password)) {
			throw new WrongCredentialsException();
		}
		try {
			authInfoRepository.allowTokenRenewal(username);
		} catch (AuthInfoNotExistsException e) {
			throw new UserNotFoundException(); // Should not happen
		}
		return newJwt(username);
	}

	public String refresh(String jwt)
			throws PasswordAuthenticationRequiredException, SessionExpiredException, InvalidTokenException {
		var username = validate(jwt);
		var authInfoOpt = authInfoRepository.retrieve(username);
		if (authInfoOpt.isEmpty()) {
			// TODO: handle consistency error
		}
		var authInfo = authInfoOpt.get();
		if (!authInfo.canRenew()) {
			throw new PasswordAuthenticationRequiredException();
		}
		return newJwt(username);
	}

	public Username validate(String jwt) throws SessionExpiredException, InvalidTokenException {
		try {
			var decodedJWT = verifier.verify(jwt);
			var username = new Username(decodedJWT.getClaim("username").asString());
			return username;
		} catch (TokenExpiredException e) {
			throw new SessionExpiredException();
		} catch (JWTVerificationException exception) {
			throw new InvalidTokenException();
		}
	}

	public void forceAuthentication(Username username) throws UserNotFoundException {
		try {
			authInfoRepository.forcePasswordAuthentication(username);
		} catch (AuthInfoNotExistsException e) {
			throw new UserNotFoundException();
		}
	}

	private String newJwt(Username username) {
		var now = Instant.now();
		return JWT.create().withClaim("username", username.value()).withIssuedAt(now)
				.withExpiresAt(now.plus(expirationDuration)).sign(algorithm());
	}

}
