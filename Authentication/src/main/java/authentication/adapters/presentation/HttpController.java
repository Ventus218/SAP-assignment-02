package authentication.adapters.presentation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import authentication.ports.*;
import authentication.domain.model.*;
import authentication.domain.exceptions.*;
import authentication.adapters.presentation.exceptions.*;

@RestController()
public class HttpController {

	private final String BASE_PATH = "/authentication";
	private final String METRICS_COUNTER_NAME = "authentication_service_requests";

	@Autowired
	AuthenticationService authenticationService;

	@Autowired
	MetricsService metricsService;

	@PostMapping(BASE_PATH + "/register")
	public String register(@RequestBody RegisterDTO dto)
			throws UserAlreadyExistsException, SomethingWentWrongException {
		incrementMetricsCounterByOne();
		return authenticationService.register(dto.username(), dto.password());
	}

	@PostMapping(BASE_PATH + "/{username}/authenticate")
	public String authenticate(@PathVariable("username") String username, @RequestBody AuthenticateDTO dto)
			throws UserNotFoundException, WrongCredentialsException {
		incrementMetricsCounterByOne();
		return authenticationService.authenticate(new Username(username), dto.password());
	}

	@PostMapping(BASE_PATH + "/refresh")
	public String refresh(@RequestHeader("Authorization") String bearerToken) throws BadAuthorizationHeaderException,
			PasswordAuthenticationRequiredException, SessionExpiredException, InvalidTokenException {
		incrementMetricsCounterByOne();
		var token = extractJwtToken(bearerToken);
		return authenticationService.refresh(token);
	}

	@PostMapping(BASE_PATH + "/{username}/forceAuthentication")
	public void forceAuthentication(@RequestHeader("Authorization") String bearerToken,
			@PathVariable("username") String username) throws UserNotFoundException, BadAuthorizationHeaderException,
			SessionExpiredException, InvalidTokenException {
		incrementMetricsCounterByOne();
		authenticationService.forceAuthentication(new Username(username));
	}

	@GetMapping(BASE_PATH + "/validate")
	public boolean validate(@RequestHeader("Authorization") String bearerToken)
			throws BadAuthorizationHeaderException, SessionExpiredException, InvalidTokenException {
		incrementMetricsCounterByOne();
		var token = extractJwtToken(bearerToken);
		authenticationService.validate(token);
		return true;
	}

	@GetMapping("healthCheck")
	public void healthCheck() throws HealthCheckError {
		var errorOpt = authenticationService.healthCheckError();
		if (errorOpt.isPresent()) {
			var error = errorOpt.get();
			throw new HealthCheckError();
		} else {
			return;
		}
	}

	private String extractJwtToken(String bearerToken) throws BadAuthorizationHeaderException {
		if (!bearerToken.startsWith("Bearer ")) {
			throw new BadAuthorizationHeaderException();
		}
		return bearerToken.substring(7);
	}

	private void incrementMetricsCounterByOne() {
		metricsService.incrementCounter(METRICS_COUNTER_NAME, 1);
	}

	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Something went wrong.")
	@ExceptionHandler(SomethingWentWrongException.class)
	public void somethingWentWrongExceptionExceptionHandler() {
	}

	@ResponseStatus(value = HttpStatus.CONFLICT, reason = "A user with that username is already registered in the authentication service")
	@ExceptionHandler(UserAlreadyExistsException.class)
	public void userAlreadyExistsExceptionExceptionHandler() {
	}

	@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Expecting an authorization header in the form of Authentication: Bearer <JWT>.")
	@ExceptionHandler(BadAuthorizationHeaderException.class)
	public void badAuthorizationHeaderExceptionHandler() {
	}

	@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "User not found")
	@ExceptionHandler(UserNotFoundException.class)
	public void userNotFoundExceptionHandler() {
	}

	@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Password authentication is required")
	@ExceptionHandler(PasswordAuthenticationRequiredException.class)
	public void passwordAuthenticationRequiredExceptionHandler() {
	}

	@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Your session has expired. Please authenticate with password.")
	@ExceptionHandler(SessionExpiredException.class)
	public void sessionExpiredExceptionHandler() {
	}

	@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "The provided token is invalid or has insufficient permissions.")
	@ExceptionHandler(InvalidTokenException.class)
	public void invalidTokenExceptionHandler() {
	}

	@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Invalid credentials. Please check your username and password and try again.")
	@ExceptionHandler(WrongCredentialsException.class)
	public void wrongCredentialsExceptionHandler() {
	}

	@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE, reason = "Healthcheck failed")
	@ExceptionHandler(HealthCheckError.class)
	public void healthCheckErrorExceptionHandler() {
	}
}