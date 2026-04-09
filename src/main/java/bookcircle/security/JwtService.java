package bookcircle.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import bookcircle.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey key;
    private final long accessTokenMinutes;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.accessTokenMinutes}") long accessTokenMinutes
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenMinutes = accessTokenMinutes;
        log.info("JWT service initialized accessTokenMinutes={}", accessTokenMinutes);
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant exp = now.plus(accessTokenMinutes, ChronoUnit.MINUTES);
        log.debug("Generating access token userId={} role={} expiresAt={}",
                user.getId(), user.getRole(), exp);

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        Jws<Claims> parsed = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        log.debug("JWT parsed subject={} expiresAt={}",
                parsed.getBody().getSubject(),
                parsed.getBody().getExpiration());
        return parsed;
    }
}
