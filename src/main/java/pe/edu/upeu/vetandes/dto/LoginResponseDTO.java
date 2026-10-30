package pe.edu.upeu.vetandes.dto;
import java.util.List;
public record LoginResponseDTO(String token, String tipo, long expiraEnSegundos, String username,
                               String nombreCompleto, List<String> roles, List<String> permisos) {}

