package pe.edu.upeu.vetandes.mapper;
import org.springframework.stereotype.Component;
import pe.edu.upeu.vetandes.dto.PropietarioResponseDTO;
import pe.edu.upeu.vetandes.entity.Propietario;
@Component
public class PropietarioMapper {
    public PropietarioResponseDTO toResponse(Propietario c) {
        return new PropietarioResponseDTO(c.getId(), c.getDni(), c.getNombres(), c.getTelefono());
    }
}

