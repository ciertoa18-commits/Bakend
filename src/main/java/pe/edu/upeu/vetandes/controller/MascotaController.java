package pe.edu.upeu.vetandes.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.service.MascotaService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/mascotas")
public class MascotaController {
    private final MascotaService service;
    @GetMapping
    public List<MascotaResponseDTO> listar(@RequestParam(required = false) Long propietarioId) {
        return service.listar(propietarioId);
    }
    @GetMapping("/{id}")
    public MascotaResponseDTO buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping
    public ResponseEntity<MascotaResponseDTO> crear(@Valid @RequestBody MascotaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }
    @PutMapping("/{id}")
    public MascotaResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody MascotaRequestDTO request) {
        return service.actualizar(id, request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}

