package pe.edu.upeu.vetandes.controller;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.vetandes.dto.PropietarioResponseDTO;
import pe.edu.upeu.vetandes.service.PropietarioService;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/propietarios")
public class PropietarioController {
    private final PropietarioService service;
    @GetMapping
    public List<PropietarioResponseDTO> listar() { return service.listar(); }
}

