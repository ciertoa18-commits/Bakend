package pe.edu.upeu.vetandes.dto;
import jakarta.validation.constraints.*;
public record AnularAtencionRequestDTO(@NotBlank @Size(min = 10, max = 200) String motivo) {}

