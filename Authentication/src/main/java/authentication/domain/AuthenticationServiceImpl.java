package authentication.domain;

import java.time.*;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.auth0.jwt.*;
import com.auth0.jwt.algorithms.*;
import com.auth0.jwt.exceptions.*;
import authentication.domain.model.*;
import authentication.ports.AuthenticationService;
import authentication.ports.persistence.AuthInfoRepository;

@Service
class AuthenticationServiceImpl implements AuthenticationService {

	@Autowired
	AuthInfoRepository authInfoRepository;

	private final Duration expirationDuration = Duration.ofSeconds(20);

	private final JWTVerifier verifier = JWT.require(algorithm()).build();

	private Algorithm algorithm() {
		return Algorithm.HMAC256("secret");
	}

	public String register(Username username, String password) {
		throw new UnsupportedOperationException();
	}

	public Optional<String> authenticate(Username username, String password) {
		// TODO: impl
		var success = true;
		if (success) {
			return Optional.of(newJwt(username));
		} else {
			return Optional.empty();
		}
	}

	public Optional<String> refresh(String jwt) {
		return validate(jwt).map(this::newJwt);
	}

	public Optional<Username> validate(String jwt) {
		try {
			var decodedJWT = verifier.verify(jwt);
			// TODO: check for force authentication
			return Optional.of(new Username(decodedJWT.getClaim("username").asString()));
		} catch (JWTVerificationException exception) {
			return Optional.empty();
		}
	}

	public void forceAuthentication(Username username) {
		throw new UnsupportedOperationException();
	}

	private String newJwt(Username username) {
		var now = Instant.now();
		return JWT.create().withClaim("username", username.value()).withIssuedAt(now)
				.withExpiresAt(now.plus(expirationDuration)).sign(algorithm());
	}

}
