package pe.edu.upeu.vetandes.service.impl;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.vetandes.dto.AtencionesPorEspecieDTO;
import pe.edu.upeu.vetandes.repository.AtencionRepository;
import pe.edu.upeu.vetandes.service.*;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {
    private final AtencionRepository detalles;
    public List<AtencionesPorEspecieDTO> ingresos(LocalDate desde, LocalDate hasta) {
        RangoFechas.validar(desde, hasta);
        return detalles.reporte(desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay());
    }
}

