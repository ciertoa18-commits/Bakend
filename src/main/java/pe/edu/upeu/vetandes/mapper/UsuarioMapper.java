package pe.edu.upeu.vetandes.mapper;
import java.util.List;
import org.springframework.stereotype.Component;
import pe.edu.upeu.vetandes.dto.UsuarioResponseDTO;
import pe.edu.upeu.vetandes.entity.Usuario;
import pe.edu.upeu.vetandes.security.PermisosPorRol;
@Component
public class UsuarioMapper {
    public List<String> roles(Usuario u) { return u.getRoles().stream().map(r -> r.getNombre()).sorted().toList(); }
    public UsuarioResponseDTO toResponse(Usuario u) {
        var roles = roles(u);
        return new UsuarioResponseDTO(u.getId(), u.getUsername(), u.getNombreCompleto(), roles, PermisosPorRol.permisos(roles));
    }
}

