package pe.edu.upeu.vetandes.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.vetandes.dto.AtencionesPorEspecieDTO;
import pe.edu.upeu.vetandes.service.ReporteService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reportes")
public class ReporteController {
    private final ReporteService service;
    @GetMapping("/atenciones-por-especie")
    public List<AtencionesPorEspecieDTO> ingresos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return service.ingresos(desde, hasta);
    }
}

