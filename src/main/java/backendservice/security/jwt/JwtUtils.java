package backendservice.security.jwt;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import backendservice.security.services.UserDetailsImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

@Component
public class JwtUtils {
	private final Logger logger = Logger.getLogger(this.getClass().getName());

	@Value("${jwtSecret}")
	private String jwtSecret;

	@Value("${jwtExpirationMs}")
	private int jwtExpirationMs;

	public String generateJwtToken(Authentication authentication) {

		UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
				System.out.println(jwtExpirationMs);
		return Jwts.builder()
				.setSubject((userPrincipal.getUsername()))
				.setIssuedAt(new Date())
				.setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
				.signWith(SignatureAlgorithm.HS512, jwtSecret)
				.compact();
	}

	public String getUserNameFromJwtToken(String token) {
		return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
	}

	public boolean validateJwtToken(String authToken) {
		try {
			Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(authToken);
			return true;
		} catch (SignatureException e) {
			logger.log(Level.SEVERE, "Invalid JWT signature: {}");
		} catch (MalformedJwtException e) {
			logger.log(Level.SEVERE, "Invalid JWT token: {}");
		} catch (ExpiredJwtException e) {
			logger.log(Level.SEVERE, "JWT token is expired: {}");
		} catch (UnsupportedJwtException e) {
			logger.log(Level.SEVERE, "JWT token is unsupported: {}");
		} catch (IllegalArgumentException e) {
			logger.log(Level.SEVERE, "JWT claims string is empty: {}");
		}

		return false;
	}
}
