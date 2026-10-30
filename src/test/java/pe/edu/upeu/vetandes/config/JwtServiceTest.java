package pe.edu.upeu.vetandes.config;

import io.jsonwebtoken.*;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = "YXV0b2FuZGVzLWRlc2Fycm9sbG8tMjAyNi1jbGF2ZS1sb2NhbC0zMmJ5dGVz";
    private final java.time.Clock ahora = java.time.Clock.fixed(Instant.parse("2026-10-29T15:00:00Z"), ZoneOffset.UTC);
    private final JwtService jwt = new JwtService(SECRET, ahora);

    @Test void firmaYRecuperaElUsuario() {
        String token = jwt.generarToken("recepcion", List.of("RECEPCIONISTA"), List.of("ATENCION_LEER"));
        assertThat(jwt.extraerUsername(token)).isEqualTo("recepcion");
    }
    @Test void rechazaTokenAlterado() {
        String token = jwt.generarToken("recepcion", List.of(), List.of());
        String[] partes = token.split("\\.");
        String alterado = partes[0] + "." + partes[1] + "." + (partes[2].startsWith("A") ? "B" : "A") + partes[2].substring(1);
        assertThatThrownBy(() -> jwt.extraerUsername(alterado)).isInstanceOf(JwtException.class);
    }
    @Test void venceExactamenteTrasUnaHora() {
        String token = jwt.generarToken("recepcion", List.of(), List.of());
        var futuro = new JwtService(SECRET, java.time.Clock.offset(ahora, Duration.ofSeconds(3601)));
        assertThatThrownBy(() -> futuro.extraerUsername(token)).isInstanceOf(ExpiredJwtException.class);
    }
}

