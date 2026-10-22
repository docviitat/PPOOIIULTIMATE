package com.PPOOII.Laboratorio.Config;

import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static com.PPOOII.Laboratorio.Config.Model.Constans.*;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {
    @org.springframework.beans.factory.annotation.Autowired
    private com.PPOOII.Laboratorio.Repository.UsuarioRepository usuarios;

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI().substring(request.getContextPath().length());
		return "OPTIONS".equals(request.getMethod()) || "/authenticate".equals(path)
				|| path.startsWith("/LaboratorioV2/publico/")
				|| "/LaboratorioV3/upload-excel".equals(path)
				|| "/api-docs".equals(path) || path.startsWith("/api-docs/")
				|| "/swagger-ui.html".equals(path) || path.startsWith("/swagger-ui/")
				|| "/rutas.html".equals(path) || "/error".equals(path);
	}


	private Claims setSigningKey(HttpServletRequest request) {
		String jwtToken = request.
				getHeader(HEADER_AUTHORIZACION_KEY).
				replace(TOKEN_BEARER_PREFIX, "");

		return Jwts.parserBuilder()
				.setSigningKey(getSigningKey(SUPER_SECRET_KEY))
				.build()
				.parseClaimsJws(jwtToken)
				.getBody();
	}

	private void setAuthentication(Claims claims) {

		List<String> authorities = (List<String>) claims.get("authorities");

		UsernamePasswordAuthenticationToken auth =
				new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
				authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));

		SecurityContextHolder.getContext().setAuthentication(auth);

	}

	private boolean isJWTValid(HttpServletRequest request, HttpServletResponse res) {
		String authenticationHeader = request.getHeader(HEADER_AUTHORIZACION_KEY);
		if (authenticationHeader == null || !authenticationHeader.startsWith(TOKEN_BEARER_PREFIX))
			return false;
		return true;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		try {
			if (isJWTValid(request, response)) {
				Claims claims = setSigningKey(request);
				if (claims.get("authorities") != null) {
					setAuthentication(claims);
				} else {
					SecurityContextHolder.clearContext();
				}
			} else {
				SecurityContextHolder.clearContext();
			}
            String path=request.getRequestURI().substring(request.getContextPath().length());
            if ((path.startsWith("/LaboratorioV1/vehiculo") || path.startsWith("/LaboratorioV1/documento"))
                && !"OPTIONS".equals(request.getMethod())) {
                var auth=SecurityContextHolder.getContext().getAuthentication();
                if(auth==null || usuarios.findByUsernameANDAPIKey(auth.getName(),request.getHeader("APIkey"))==null) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN,"Token y APIkey del mismo usuario son obligatorios.");
                    return;
                }
            }
			filterChain.doFilter(request, response);
		} catch (JwtException | IllegalArgumentException e) {
			SecurityContextHolder.clearContext();
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token inválido o vencido.");
			return;
		}
	}

}
