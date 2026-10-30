package pe.edu.upeu.vetandes.service;
import pe.edu.upeu.vetandes.dto.*;
public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO request);
    UsuarioResponseDTO actual(String username);
}

