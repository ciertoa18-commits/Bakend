package pe.edu.upeu.vetandes.config;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.upeu.vetandes.security.RestAuthenticationEntryPoint;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UserDetailsService usuarios;
    private final RestAuthenticationEntryPoint entryPoint;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String path = req.getServletPath();
        return req.getMethod().equals("OPTIONS") || path.equals("/api/v1/auth/login") ||
                path.equals("/swagger-ui.html") || path.startsWith("/swagger-ui/") || path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header == null) { chain.doFilter(req, res); return; }
        try {
            if (!header.startsWith("Bearer ")) throw new BadCredentialsException("Token invalido");
            String username = jwt.extraerUsername(header.substring(7).trim());
            var usuario = usuarios.loadUserByUsername(username);
            if (!usuario.isEnabled()) throw new BadCredentialsException("Usuario inactivo");
            var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException | IllegalArgumentException | AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            entryPoint.commence(req, res, new BadCredentialsException("Token invalido", ex));
            return;
        }
        chain.doFilter(req, res);
    }
}

