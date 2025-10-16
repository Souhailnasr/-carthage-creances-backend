package projet.carthagecreance_backend.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import projet.carthagecreance_backend.PayloadRequest.AuthenticationRequest;
import projet.carthagecreance_backend.PayloadRequest.RegisterRequest;
import projet.carthagecreance_backend.PayloadResponse.AuthenticationResponse;
import projet.carthagecreance_backend.SecurityServices.AuthenticationService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {
	private final AuthenticationService service;

	@PostMapping("/register")
	public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody RegisterRequest request,
                                                           BindingResult result){
		if (result.hasErrors()) {
			List<String> errors = result.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage)
					.collect(Collectors.toList());
			return ResponseEntity.badRequest().body(AuthenticationResponse.builder().errors(errors).build());
		}
		return ResponseEntity.ok(service.register(request));
	}

	@PostMapping("/authenticate")
	public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
		System.out.println(request);
		return ResponseEntity.ok(service.authenticate(request));
	}
}
