package pe.edu.upeu.vetandes.mapper;
import org.springframework.stereotype.Component;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.entity.*;
@Component public class MascotaMapper {
    public MascotaResponseDTO toResponse(Mascota m) {
        return new MascotaResponseDTO(m.getId(),m.getNombre(),m.getEspecie(),m.getRaza(),m.getFechaNacimiento(),m.getSexo(),m.getPropietario().getId(),m.getPropietario().getNombres());
    }
    public void actualizar(Mascota m, MascotaRequestDTO r, Propietario p) {
        m.setNombre(r.nombre().trim());m.setEspecie(r.especie());m.setRaza(r.raza()==null?null:r.raza().trim());
        m.setFechaNacimiento(r.fechaNacimiento());m.setSexo(r.sexo());m.setPropietario(p);
    }
}
