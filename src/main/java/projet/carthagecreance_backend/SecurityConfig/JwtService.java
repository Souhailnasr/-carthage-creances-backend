package projet.carthagecreance_backend.SecurityConfig;

import org.springframework.stereotype.Service;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.Serializable;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.apache.tomcat.util.codec.binary.Base64;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.keygen.BytesKeyGenerator;
import org.springframework.security.crypto.keygen.KeyGenerators;
import projet.carthagecreance_backend.Entity.Token;
import projet.carthagecreance_backend.Repository.TokenRepository;

@Service
public class JwtService implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@Value("${application.security.jwt.secret-key}")
	private String secretKey;
	
	private static final BytesKeyGenerator DEFAULT_TOKEN_GENERATOR = KeyGenerators.secureRandom(15);

    @Value("${jdj.secure.token.validity}")
    private int tokenValidityInSeconds;
    
   @Autowired
   TokenRepository tokenRepository;

	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String generateToken(UserDetails userDetails) {
		Map<String, Object> claims = new HashMap<>();
		claims.put("role", userDetails.getAuthorities());
		return generateToken(claims, userDetails);
	}

    public String generateToken(Map<String, Object> claims, UserDetails userDetails) {
        long nowMs = System.currentTimeMillis();
        long validityMs = (long) getTokenValidityInSeconds() * 1000L;
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(nowMs))
                .setExpiration(new Date(nowMs + validityMs))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parserBuilder().setSigningKey(getSignInKey()).build().parseClaimsJws(token).getBody();
	}

	private Key getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
	
    public Token createSecureToken(){
        String tokenValue = new String(Base64.encodeBase64URLSafeString(DEFAULT_TOKEN_GENERATOR.generateKey())); // this is a sample, you can adapt as per your security need
        Token secureToken = new Token();
        secureToken.setToken(tokenValue);
        secureToken.setExpireAt(LocalDateTime.now().plusSeconds(getTokenValidityInSeconds()));
        tokenRepository.save(secureToken);
        return secureToken;
    }
    
    public int getTokenValidityInSeconds() {
        return tokenValidityInSeconds;
    }
    
    public void removeToken(Token token) {
        tokenRepository.delete(token);
    }
}
