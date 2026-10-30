package pe.edu.upeu.vetandes.dto;
import java.util.List;
public record UsuarioResponseDTO(Long id, String username, String nombreCompleto, List<String> roles, List<String> permisos) {}

