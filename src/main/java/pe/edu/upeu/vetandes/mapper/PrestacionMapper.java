package pe.edu.upeu.vetandes.mapper;
import org.springframework.stereotype.Component;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.entity.Prestacion;
@Component
public class PrestacionMapper {
    public PrestacionResponseDTO toResponse(Prestacion s) {
        return new PrestacionResponseDTO(s.getId(), s.getCodigo(), s.getNombre(), s.getPrecio(), s.getEspecieAplicable(), s.getActivo());
    }
    public void actualizar(Prestacion s, PrestacionRequestDTO r) {
        s.setCodigo(r.codigo()); s.setNombre(r.nombre().trim()); s.setPrecio(r.precio());
        s.setEspecieAplicable(r.especieAplicable()); s.setActivo(r.activo());
    }
}

