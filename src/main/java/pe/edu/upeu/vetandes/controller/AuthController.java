package pe.edu.upeu.vetandes.controller;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.service.AuthService;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService service;
    @Operation(security = {})
    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) { return service.login(request); }
    @GetMapping("/me")
    public UsuarioResponseDTO actual(Principal principal) { return service.actual(principal.getName()); }
}

