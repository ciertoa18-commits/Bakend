package pe.edu.upeu.vetandes.service;
import java.util.List;
import pe.edu.upeu.vetandes.dto.*;
public interface PrestacionService {
    List<PrestacionResponseDTO> listar(pe.edu.upeu.vetandes.entity.Especie especie);
    PrestacionResponseDTO buscar(Long id);
    PrestacionResponseDTO crear(PrestacionRequestDTO request);
    PrestacionResponseDTO actualizar(Long id, PrestacionRequestDTO request);
    void eliminar(Long id);
    PrestacionResponseDTO cambiarEstado(Long id, Boolean activo);
}

