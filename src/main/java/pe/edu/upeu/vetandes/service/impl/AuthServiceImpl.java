package pe.edu.upeu.vetandes.service.impl;

import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.vetandes.config.JwtService;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.mapper.UsuarioMapper;
import pe.edu.upeu.vetandes.repository.UsuarioRepository;
import pe.edu.upeu.vetandes.security.PermisosPorRol;
import pe.edu.upeu.vetandes.service.AuthService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private static final String HASH_COMPARACION = "$2a$10$bfdU4ex4X7jJv4sHOUSVZOeGAnadyriXO4leIRcKggbeZbj4TdIFC";
    private final UsuarioRepository usuarios;
    private final UsuarioMapper mapper;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public LoginResponseDTO login(LoginRequestDTO request) {
        var usuario = usuarios.findByUsername(request.username().trim());
        // Se ejecuta BCrypt incluso para un usuario inexistente y se unifica el mensaje.
        boolean coincide = encoder.matches(request.password(), usuario.map(u -> u.getPassword()).orElse(HASH_COMPARACION));
        if (usuario.isEmpty() || !coincide || !Boolean.TRUE.equals(usuario.get().getActivo())) {
            log.warn("Login rechazado");
            throw new BadCredentialsException("Credenciales invalidas");
        }
        var u = usuario.get();
        var roles = mapper.roles(u);
        var permisos = PermisosPorRol.permisos(roles);
        log.info("Login correcto | usuario={}", u.getUsername());
        return new LoginResponseDTO(jwt.generarToken(u.getUsername(), roles, permisos), "Bearer",
                JwtService.EXPIRA_EN_SEGUNDOS, u.getUsername(), u.getNombreCompleto(), roles, permisos);
    }

    public UsuarioResponseDTO actual(String username) {
        var usuario = usuarios.findByUsername(username).filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));
        return mapper.toResponse(usuario);
    }
}

