package pe.edu.upeu.vetandes.service;
import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.vetandes.dto.AtencionesPorEspecieDTO;
public interface ReporteService { List<AtencionesPorEspecieDTO> ingresos(LocalDate desde, LocalDate hasta); }

