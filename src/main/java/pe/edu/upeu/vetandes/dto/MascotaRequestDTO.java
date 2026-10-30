package pe.edu.upeu.vetandes.dto;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import pe.edu.upeu.vetandes.entity.Especie;
public record MascotaRequestDTO(
    @NotBlank @Size(min=2,max=40) String nombre,
    @NotNull Especie especie,
    @Size(max=40) String raza,
    @NotNull @PastOrPresent(message="La fecha de nacimiento no puede ser futura") LocalDate fechaNacimiento,
    @NotBlank @Pattern(regexp="[MH]",message="El sexo debe ser M o H") String sexo,
    @NotNull @Positive Long propietarioId) {
    public MascotaRequestDTO { if (nombre != null) nombre = nombre.trim(); }
}
