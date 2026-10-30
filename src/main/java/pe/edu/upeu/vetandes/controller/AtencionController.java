package pe.edu.upeu.vetandes.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.entity.EstadoAtencion;
import pe.edu.upeu.vetandes.service.AtencionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/atenciones")
public class AtencionController {
    private final AtencionService service;
    @PostMapping
    public ResponseEntity<AtencionResponseDTO> registrar(@Valid @RequestBody AtencionRequestDTO request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.registrar(request, principal.getName()));
    }
    @GetMapping
    public List<AtencionResponseDTO> listar(@RequestParam(required = false) String mascota,
            @RequestParam(required = false) pe.edu.upeu.vetandes.entity.Especie especie,
            @RequestParam(required = false) EstadoAtencion estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.listar(mascota, especie, estado, desde, hasta);
    }
    @GetMapping("/{id}")
    public AtencionResponseDTO buscar(@PathVariable Long id) { return service.buscar(id); }
    @PatchMapping("/{id}/anular")
    public AtencionResponseDTO anular(@PathVariable Long id, @Valid @RequestBody AnularAtencionRequestDTO request) {
        return service.anular(id, request);
    }
}

