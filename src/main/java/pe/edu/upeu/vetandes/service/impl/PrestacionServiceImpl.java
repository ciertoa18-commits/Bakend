package pe.edu.upeu.vetandes.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.entity.Prestacion;
import pe.edu.upeu.vetandes.exception.*;
import pe.edu.upeu.vetandes.mapper.PrestacionMapper;
import pe.edu.upeu.vetandes.repository.PrestacionRepository;
import pe.edu.upeu.vetandes.service.PrestacionService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrestacionServiceImpl implements PrestacionService {
    private static final Logger log = LoggerFactory.getLogger(PrestacionServiceImpl.class);
    private final PrestacionRepository repository;
    private final PrestacionMapper mapper;

    public List<PrestacionResponseDTO> listar(pe.edu.upeu.vetandes.entity.Especie especie) {
        if (especie == null) return repository.findAll(Sort.by("codigo")).stream().map(mapper::toResponse).toList();
        var aplicables = new java.util.ArrayList<pe.edu.upeu.vetandes.entity.EspecieAplicable>();
        aplicables.add(pe.edu.upeu.vetandes.entity.EspecieAplicable.TODAS);
        if (especie != pe.edu.upeu.vetandes.entity.Especie.OTRO) aplicables.add(pe.edu.upeu.vetandes.entity.EspecieAplicable.valueOf(especie.name()));
        return repository.findByEspecieAplicableInOrderByCodigoAsc(aplicables).stream().map(mapper::toResponse).toList();
    }
    public PrestacionResponseDTO buscar(Long id) { return mapper.toResponse(obtener(id)); }

    @Transactional
    public PrestacionResponseDTO crear(PrestacionRequestDTO r) {
        if (repository.existsByCodigo(r.codigo())) throw new ReglaNegocioException("Ya existe el codigo " + r.codigo());
        Prestacion prestacion = new Prestacion();
        mapper.actualizar(prestacion, r);
        repository.saveAndFlush(prestacion);
        log.info("Prestacion creada | id={} | codigo={}", prestacion.getId(), prestacion.getCodigo());
        return mapper.toResponse(prestacion);
    }

    @Transactional
    public PrestacionResponseDTO actualizar(Long id, PrestacionRequestDTO r) {
        Prestacion prestacion = bloquear(id);
        if (repository.existsByCodigoAndIdNot(r.codigo(), id)) throw new ReglaNegocioException("Ya existe el codigo " + r.codigo());
        mapper.actualizar(prestacion, r);
        repository.flush();
        log.info("Prestacion actualizada | id={}", id);
        return mapper.toResponse(prestacion);
    }

    @Transactional
    public void eliminar(Long id) {
        Prestacion prestacion = bloquear(id);
        if (repository.contarUsos(id) > 0) throw new ReglaNegocioException(
                "La prestacion tiene atenciones; use PATCH /api/v1/prestaciones/" + id + "/estado para darlo de baja");
        repository.delete(prestacion);
        repository.flush();
        log.info("Prestacion eliminada | id={}", id);
    }

    @Transactional
    public PrestacionResponseDTO cambiarEstado(Long id, Boolean activo) {
        Prestacion prestacion = bloquear(id);
        prestacion.setActivo(activo);
        repository.flush();
        log.info("Estado de prestacion cambiado | id={} | activo={}", id, activo);
        return mapper.toResponse(prestacion);
    }

    private Prestacion obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Prestacion no encontrada: " + id));
    }
    private Prestacion bloquear(Long id) {
        return repository.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Prestacion no encontrada: " + id));
    }
}

