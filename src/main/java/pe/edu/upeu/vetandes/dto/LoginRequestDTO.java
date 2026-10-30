package pe.edu.upeu.vetandes.dto;
import jakarta.validation.constraints.*;
public record LoginRequestDTO(@NotBlank @Size(max = 30) String username, @NotBlank @Size(max = 72) String password) {}

