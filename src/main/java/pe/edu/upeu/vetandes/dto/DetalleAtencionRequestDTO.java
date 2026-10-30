package pe.edu.upeu.vetandes.dto;
import jakarta.validation.constraints.*;
public record DetalleAtencionRequestDTO(@NotNull @Positive Long prestacionId, @NotNull @Min(1) @Max(3) Integer cantidad) {}

