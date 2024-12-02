package authentication.adapters.presentation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import authentication.ports.AuthenticationService;

@RestController
public class HttpController {

	@Autowired
	AuthenticationService authenticationService;

	@GetMapping("/insert")
	public void insert() {
		authenticationService.insert();
	}

	@GetMapping("/all")
	public void all() {
		authenticationService.all();
	}
}