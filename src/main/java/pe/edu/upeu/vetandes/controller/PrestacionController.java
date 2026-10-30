package pe.edu.upeu.vetandes.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.service.PrestacionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/prestaciones")
public class PrestacionController {
    private final PrestacionService service;
    @GetMapping
    public List<PrestacionResponseDTO> listar(@RequestParam(required = false) pe.edu.upeu.vetandes.entity.Especie especie) {
        return service.listar(especie);
    }
    @GetMapping("/{id}")
    public PrestacionResponseDTO buscar(@PathVariable Long id) { return service.buscar(id); }
    @PostMapping
    public ResponseEntity<PrestacionResponseDTO> crear(@Valid @RequestBody PrestacionRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.crear(request));
    }
    @PutMapping("/{id}")
    public PrestacionResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody PrestacionRequestDTO request) {
        return service.actualizar(id, request);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminar(id);
        return ResponseEntity.noContent().build();
    }
    @PatchMapping("/{id}/estado")
    public PrestacionResponseDTO cambiarEstado(@PathVariable Long id, @Valid @RequestBody EstadoPrestacionRequestDTO request) {
        return service.cambiarEstado(id, request.activo());
    }
}

