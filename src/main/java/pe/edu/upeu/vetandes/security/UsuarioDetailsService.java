package pe.edu.upeu.vetandes.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.vetandes.repository.UsuarioRepository;

@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarios;
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        var usuario = usuarios.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales invalidas"));
        var roles = usuario.getRoles().stream().map(r -> r.getNombre()).toList();
        return User.withUsername(usuario.getUsername()).password(usuario.getPassword())
                .authorities(PermisosPorRol.permisos(roles).toArray(String[]::new))
                .disabled(!Boolean.TRUE.equals(usuario.getActivo())).build();
    }
}

