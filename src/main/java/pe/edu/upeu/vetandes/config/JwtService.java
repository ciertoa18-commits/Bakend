package pe.edu.upeu.vetandes.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.*;
import java.util.*;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final SecretKey clave;
    private final Clock clock;
    public static final long EXPIRA_EN_SEGUNDOS = 3600;
    public JwtService(@Value("${app.jwt.secret}") String secret, Clock clock) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.clock = clock;
    }
    public String generarToken(String username, List<String> roles, List<String> permisos) {
        Instant ahora = clock.instant();
        return Jwts.builder().subject(username).issuer("vetandes-api")
                .claim("roles", roles).claim("permisos", permisos)
                .issuedAt(Date.from(ahora)).expiration(Date.from(ahora.plusSeconds(EXPIRA_EN_SEGUNDOS)))
                .signWith(clave).compact();
    }
    public String extraerUsername(String token) {
        Claims claims = Jwts.parser().verifyWith(clave).requireIssuer("vetandes-api")
                .clock(() -> Date.from(clock.instant())).build().parseSignedClaims(token).getPayload();
        if (claims.getExpiration() == null || claims.getSubject() == null || claims.getSubject().isBlank())
            throw new JwtException("Token incompleto");
        return claims.getSubject();
    }
}

