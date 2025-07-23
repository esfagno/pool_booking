package com.poolapp.pool.security;

import com.poolapp.pool.model.User;
import com.poolapp.pool.util.exception.ErrorMessages;
import com.poolapp.pool.util.exception.ForbiddenOperationException;
import com.poolapp.pool.util.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final UserDetailsService userDetailsService;
    @Value("${token.signing.key}")
    private String jwtSigningKey;
    private Key signingKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSigningKey);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(UserDetails userDetails) {
        long accessTokenMs = 15 * 60 * 1000;
        return generateToken(userDetails, accessTokenMs);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        long refreshTokenMs = 7 * 24 * 60 * 60 * 1000;
        return generateToken(userDetails, refreshTokenMs);
    }

    private String generateToken(UserDetails userDetails, long expirationMs) {
        Map<String, Object> claims = new HashMap<>();
        if (userDetails instanceof User user) {
            claims.put("id", user.getId());
            claims.put("email", user.getEmail());
            claims.put("role", user.getRole().getName().name());
        }
        return Jwts.builder().setClaims(claims).setSubject(userDetails.getUsername()).setIssuedAt(new Date()).setExpiration(new Date(System.currentTimeMillis() + expirationMs)).signWith(signingKey, SignatureAlgorithm.HS256).compact();
    }

    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        try {
            String username = extractUserName(refreshToken);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!isTokenValid(refreshToken, userDetails)) {
                throw new InvalidTokenException(ErrorMessages.REFRESH_TOKEN);
            }

            String newAccessToken = generateAccessToken(userDetails);

            return JwtAuthenticationResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (ExpiredJwtException e) {
            throw new ForbiddenOperationException(ErrorMessages.REFRESH_TOKEN);
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractClaim(token, Claims::getSubject);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(parseToken(token));
    }

    private Claims parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(signingKey).build().parseClaimsJws(token).getBody();
    }
}
