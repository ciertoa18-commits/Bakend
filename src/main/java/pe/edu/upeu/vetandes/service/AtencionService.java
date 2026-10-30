package pe.edu.upeu.vetandes.service;
import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.entity.EstadoAtencion;
public interface AtencionService {
    AtencionResponseDTO registrar(AtencionRequestDTO request, String username);
    List<AtencionResponseDTO> listar(String mascota, pe.edu.upeu.vetandes.entity.Especie especie, EstadoAtencion estado, LocalDate desde, LocalDate hasta);
    AtencionResponseDTO buscar(Long id);
    AtencionResponseDTO anular(Long id, AnularAtencionRequestDTO request);
}

