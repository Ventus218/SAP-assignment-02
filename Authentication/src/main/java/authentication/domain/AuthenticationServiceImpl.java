package authentication.domain;

import java.time.*;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.*;
import jakarta.annotation.PostConstruct;
import com.auth0.jwt.*;
import com.auth0.jwt.algorithms.*;
import com.auth0.jwt.exceptions.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import authentication.domain.model.*;
import authentication.domain.exceptions.*;
import authentication.ports.*;
import authentication.ports.usersservice.*;
import authentication.ports.persistence.AuthInfoRepository;
import authentication.ports.persistence.exceptions.*;

@Service
class AuthenticationServiceImpl implements AuthenticationService {

	@Autowired
	UsersService usersService;

	@Autowired
	AuthInfoRepository authInfoRepository;

	@Value("${jwt.signing.secret}")
	private String signingSecret;

	@Value("${jwt.expiration.seconds}")
	private String expirationSecondsString;

	@PostConstruct
	private void validateInjectedValues() {
		if (signingSecret.isBlank()) {
			throw new IllegalArgumentException("jwt.signing.secret can't be blank");
		}
		if (expirationSecondsString.isBlank()) {
			throw new IllegalArgumentException("jwt.expiration.seconds can't be blank");
		}
		try {
			Integer.parseInt(expirationSecondsString);
		} catch (Exception e) {
			throw new IllegalArgumentException("jwt.expiration.seconds needs to be an int");
		}
	}

	private Algorithm algorithm() {
		return Algorithm.HMAC256(signingSecret);
	}

	private JWTVerifier verifier() {
		return JWT.require(algorithm()).build();
	}

	private Duration expirationDuration() {
		return Duration.ofSeconds(Integer.parseInt(expirationSecondsString));
	}

	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public String register(Username username, String password)
			throws UserAlreadyExistsException, SomethingWentWrongException {
		if (authInfoRepository.retrieve(username).isPresent()) {
			throw new UserAlreadyExistsException();
		}
		try {
			usersService.registerUser(username);
		} catch (UserAlreadyRegisteredException e) {
			// handling eventual consistency look at docs
		} catch (UsersServiceException e) {
			throw new SomethingWentWrongException(
					"Something went wrong while registering the new user through the users service", e);
		}
		try {
			authInfoRepository.insert(new AuthInfo(username, passwordEncoder.encode(password), true));
			return newJwt(username);
		} catch (AuthInfoAlreadyExistsException e) {
			throw new UserAlreadyExistsException();
		}
	}

	public String authenticate(Username username, String password)
			throws UserNotFoundException, WrongCredentialsException {
		var authInfoOpt = authInfoRepository.retrieve(username);
		if (authInfoOpt.isEmpty()) {
			throw new UserNotFoundException();
		}
		var authInfo = authInfoOpt.get();

		if (!passwordEncoder.matches(password, authInfo.passwordHash())) {
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
			throw new IllegalStateException("Trying to refresh a token whom user authentication data is not stored");
		}
		var authInfo = authInfoOpt.get();
		if (!authInfo.canRenew()) {
			throw new PasswordAuthenticationRequiredException();
		}
		return newJwt(username);
	}

	public Username validate(String jwt) throws SessionExpiredException, InvalidTokenException {
		try {
			var decodedJWT = verifier().verify(jwt);
			var username = new Username(decodedJWT.getClaim("username").asString());
			return username;
		} catch (TokenExpiredException e) {
			throw new SessionExpiredException();
		} catch (JWTVerificationException exception) {
			throw new InvalidTokenException();
		}
	}

	public void forceAuthentication(String jwt, Username username)
			throws UserNotFoundException, SessionExpiredException, InvalidTokenException {
		try {
			validate(jwt);
			authInfoRepository.forcePasswordAuthentication(username);
		} catch (AuthInfoNotExistsException e) {
			throw new UserNotFoundException();
		}
	}

	private String newJwt(Username username) {
		var now = Instant.now();
		return JWT.create().withClaim("username", username.value()).withIssuedAt(now)
				.withExpiresAt(now.plus(expirationDuration())).sign(algorithm());
	}

	public Optional<String> healthCheckError() {
		return Optional.empty();
	}
}
