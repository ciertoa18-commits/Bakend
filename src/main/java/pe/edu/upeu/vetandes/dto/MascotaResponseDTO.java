package pe.edu.upeu.vetandes.dto;
import java.time.LocalDate;
import pe.edu.upeu.vetandes.entity.Especie;
public record MascotaResponseDTO(Long id, String nombre, Especie especie, String raza,
    LocalDate fechaNacimiento, String sexo, Long propietarioId, String propietarioNombre) {}
