package com.PPOOII.Laboratorio.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.PPOOII.Laboratorio.Config.JWTAuthtenticationConfig;
import com.PPOOII.Laboratorio.Config.Model.JwtRequest;
import com.PPOOII.Laboratorio.Config.Model.JwtResponse;
import com.PPOOII.Laboratorio.Entities.Usuario;
import com.PPOOII.Laboratorio.Services.UsuarioServiceImpl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@CrossOrigin
@Tag(name = "autenticacion", description = "Autenticación: generación del token JWT")
public class JwtAuthenticationController {
	@Autowired
	JWTAuthtenticationConfig jwtAuthtenticationConfig;
	@Autowired
	private UserDetailsService jwtInMemoryUserDetailsService;
	@Autowired
	@Qualifier("UsuarioService")
	private UsuarioServiceImpl usuarioServiceImp;

	@Operation(summary = "Genera un token JWT para un usuario.",
			description = "Requiere el login del usuario en el body y su APIKey en el header 'APIkey'. "
					+ "Este servicio no requiere token. El token retornado se usa como Bearer en los demás servicios.")
	@ApiResponses(value = {
		@ApiResponse(responseCode = "200", description = "Token generado",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
		@ApiResponse(responseCode = "400", description = "Cuerpo o header APIkey ausentes", content = @Content),
		@ApiResponse(responseCode = "401", description = "Usuario, contraseña o APIKey incorrectos", content = @Content) })
	@RequestMapping(
			value = "/authenticate", 
			method = RequestMethod.POST,
			consumes = MediaType.APPLICATION_JSON_VALUE,
			produces = MediaType.APPLICATION_JSON_VALUE
	)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest,
			@Parameter(description = "APIKey del usuario", required = true) @RequestHeader("APIkey") String APIKey )
			throws Exception {
		Usuario user = usuarioServiceImp.findByUsernameANDAPIKey(authenticationRequest.getUsername(), APIKey);
		if (user == null || authenticationRequest.getPassword() == null
				|| !authenticationRequest.getPassword().equals(user.getPassword())) {
			throw new com.PPOOII.Laboratorio.Exception.CargueException(org.springframework.http.HttpStatus.UNAUTHORIZED,
					"Usuario, contraseña o APIKey incorrectos.");
		}
		final UserDetails userDetails = jwtInMemoryUserDetailsService
				.loadUserByUsername(user.getId().getLogin());
		final String token = jwtAuthtenticationConfig.getJWTToken(userDetails.getUsername());
		return ResponseEntity.ok(new JwtResponse(token));
	}
}
