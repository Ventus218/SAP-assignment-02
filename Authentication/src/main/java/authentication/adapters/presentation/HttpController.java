package authentication.adapters.presentation;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import authentication.ports.AuthenticationService;
import authentication.domain.model.*;

@RestController
public class HttpController {

	@Autowired
	AuthenticationService authenticationService;

	@PostMapping("/authenticate")
	public String authenticate(@RequestBody AuthenticateDTO dto) {
		// TODO: return actual error
		return authenticationService.authenticate(dto.username(), dto.password()).orElse("Error");
	}

	@PostMapping("/refresh")
	public String refresh(@RequestHeader("Authorization") String bearerToken) {
		if (!bearerToken.startsWith("Bearer ")) {
			// TODO: return actual error
			return "Error";
		}
		var token = bearerToken.substring(7);
		// TODO: return actual error
		return authenticationService.refresh(token).orElse("Error");
	}

	@GetMapping("/validate")
	public boolean validate(@RequestHeader("Authorization") String bearerToken) {
		if (!bearerToken.startsWith("Bearer ")) {
			return false;
		}
		var token = bearerToken.substring(7);
		return authenticationService.validate(token).map(username -> true).orElse(false);
	}
}