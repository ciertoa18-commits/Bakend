package pe.edu.upeu.vetandes.service;
import java.util.List;
import pe.edu.upeu.vetandes.dto.*;
public interface MascotaService {
    List<MascotaResponseDTO> listar(Long propietarioId);
    MascotaResponseDTO buscar(Long id);
    MascotaResponseDTO crear(MascotaRequestDTO request);
    MascotaResponseDTO actualizar(Long id, MascotaRequestDTO request);
    void eliminar(Long id);
}

