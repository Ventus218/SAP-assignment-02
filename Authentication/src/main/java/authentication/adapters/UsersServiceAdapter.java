package authentication.adapters;

import org.springframework.http.HttpStatus;
import org.springframework.web.client.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import authentication.ports.usersservice.*;
import authentication.domain.model.*;

@Service
class UsersServiceAdapter implements UsersService {

	@Value("${services.users.address}")
	private String usersServiceAddress;

	@PostConstruct
	private void validateInjectedValues() {
		if (usersServiceAddress.isBlank()) {
			throw new IllegalArgumentException("jwt.signing.secret can't be blank");
		}
	}

	private final RestClient restClient;

	public UsersServiceAdapter(RestClient.Builder restClientBuilder) {
		this.restClient = restClientBuilder.baseUrl(usersServiceAddress).build();
	}

	public void registerUser(Username username) throws UserAlreadyRegisteredException, UsersServiceException {
		try {
			var result = this.restClient.post().uri(usersServiceAddress + "/users").body(username).retrieve()
					.onStatus(status -> status.isSameCodeAs(HttpStatus.CONFLICT), (request, response) -> {
					}).toBodilessEntity();
			if (result.getStatusCode().isSameCodeAs(HttpStatus.CONFLICT)) {
				throw new UserAlreadyRegisteredException();
			}
		} catch (RestClientException e) {
			throw new UsersServiceException(e);
		}
	}

}