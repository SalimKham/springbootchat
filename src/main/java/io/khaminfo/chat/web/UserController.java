package io.khaminfo.chat.web;

import java.io.IOException;


import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.khaminfo.chat.domain.Crop;
import io.khaminfo.chat.domain.Person;
import io.khaminfo.chat.payload.JWTLoginSuccessResponse;
import io.khaminfo.chat.payload.LoginRequest;
import io.khaminfo.chat.security.JWTTokenProvider;
import io.khaminfo.chat.security.SecurityConstants;
import io.khaminfo.chat.services.MapValidationErrorService;
import io.khaminfo.chat.services.UserService;
import io.khaminfo.chat.validator.UserValidator;

@RestController
@RequestMapping("/api/users")
public class UserController {
	@Autowired
	private MapValidationErrorService mapErrorService;
	@Autowired
	private UserService userService;
	@Autowired
	private UserValidator validator;
	@Autowired
	private JWTTokenProvider jwtTokenProvider;
	@Autowired
	private AuthenticationManager authenticationManager;

	@PostMapping("/login")
	public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest, BindingResult result) {
		ResponseEntity<?> errorMap = mapErrorService.MapValidationService(result);
		if (errorMap != null)
			return errorMap;
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()

				));
		
		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = SecurityConstants.TOKEN_PREFIX + jwtTokenProvider.generateToken(authentication);
		userService.login();
		return ResponseEntity.ok(new JWTLoginSuccessResponse(true, jwt));
	}

	@PostMapping("/register/")
	public ResponseEntity<?> registerUser(@Valid @RequestBody String req, BindingResult result)
			throws JsonParseException, JsonMappingException, IOException {
		// validator.validate(user, result);
		System.out.println("adding user "+ req);
		Person user = null;
		ObjectMapper mapper = new ObjectMapper();
		
			user = mapper.readValue(req, Person.class);

		validator.validate(user, result);
		ResponseEntity<?> mappErr = mapErrorService.MapValidationService(result);
		if (mappErr != null)
			return mappErr;
      
		return new ResponseEntity<Person>(userService.saveUser(user), HttpStatus.CREATED);
	}


	@PostMapping("/updatephoto/")
	public ResponseEntity<?> singleFileUpload( @RequestParam("cropString") String cropString, @RequestParam("file") MultipartFile file
		) throws JsonParseException, JsonMappingException, IOException {
		
		String[] cropValue = cropString.split(":");
		Crop crop = new Crop();
		if( cropValue[0].indexOf(".") != -1)
			cropValue[0] = cropValue[0].substring(0 , cropValue[0].indexOf("."));
		if( cropValue[1].indexOf(".") != -1)
			cropValue[1] = cropValue[1].substring(0 , cropValue[1].indexOf("."));
		crop.setX(Integer.parseInt(cropValue[0]));
		crop.setY(Integer.parseInt(cropValue[1]));
		crop.setWith(Integer.parseInt(cropValue[2]));
		crop.setHeight(Integer.parseInt(cropValue[3]));
		crop.setDisplayWidth(Integer.parseInt(cropValue[4]));
		crop.setDisplayHeight(Integer.parseInt(cropValue[5]));

		return new ResponseEntity<String>(
				userService.updateUserPhoto(file, crop) , HttpStatus.OK);

	}

	@PostMapping("/logout/")
	public ResponseEntity<?> logoutUser() {
		userService.logoutUser();	
		return new ResponseEntity<>( HttpStatus.OK);
	}

	
	@GetMapping("/all")
	public ResponseEntity<?> getAllUsers() {
		return new ResponseEntity<List<Object[]>>( userService.getAllUsers(),HttpStatus.OK);
	}
	@GetMapping("/{id}")
	public ResponseEntity<?> getUserById(@PathVariable long id ) {
		return new ResponseEntity<Person>( userService.getUserById(id),HttpStatus.OK);
	}

	




}
