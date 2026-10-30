package pe.edu.upeu.vetandes.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDTO(LocalDateTime timestamp, int status, String error,
                               String mensaje, String ruta, Map<String, String> campos) {}

