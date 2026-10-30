package pe.edu.upeu.vetandes.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.entity.*;
import pe.edu.upeu.vetandes.exception.*;
import pe.edu.upeu.vetandes.mapper.MascotaMapper;
import pe.edu.upeu.vetandes.repository.*;
import pe.edu.upeu.vetandes.service.MascotaService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MascotaServiceImpl implements MascotaService {
    private static final Logger log = LoggerFactory.getLogger(MascotaServiceImpl.class);
    private final MascotaRepository repository;
    private final PropietarioRepository propietarios;
    private final MascotaMapper mapper;

    public List<MascotaResponseDTO> listar(Long propietarioId) {
        if (propietarioId != null) propietario(propietarioId);
        List<Mascota> mascotas = propietarioId == null ? repository.findAll(Sort.by("nombre")) :
                repository.findByPropietarioIdOrderByNombreAsc(propietarioId);
        return mascotas.stream().map(mapper::toResponse).toList();
    }
    public MascotaResponseDTO buscar(Long id) { return mapper.toResponse(obtener(id)); }

    @Transactional
    public MascotaResponseDTO crear(MascotaRequestDTO r) {
        propietario(r.propietarioId());
        if (repository.existsByNombreAndPropietarioId(r.nombre().trim(), r.propietarioId())) throw new ReglaNegocioException("El propietario ya tiene una mascota con el nombre " + r.nombre().trim());
        Mascota mascota = new Mascota();
        mapper.actualizar(mascota, r, propietario(r.propietarioId()));
        repository.saveAndFlush(mascota);
        log.info("Mascota creada | id={} | nombre={}", mascota.getId(), mascota.getNombre());
        return mapper.toResponse(mascota);
    }

    @Transactional
    public MascotaResponseDTO actualizar(Long id, MascotaRequestDTO r) {
        Mascota mascota = bloquear(id);
        propietario(r.propietarioId());
        if (repository.existsByNombreAndPropietarioIdAndIdNot(r.nombre().trim(), r.propietarioId(), id)) throw new ReglaNegocioException("El propietario ya tiene una mascota con el nombre " + r.nombre().trim());
        mapper.actualizar(mascota, r, propietario(r.propietarioId()));
        repository.flush();
        log.info("Mascota actualizada | id={}", id);
        return mapper.toResponse(mascota);
    }

    @Transactional
    public void eliminar(Long id) {
        Mascota mascota = bloquear(id);
        if (repository.contarAtenciones(id) > 0) throw new ReglaNegocioException("La mascota tiene atenciones y no se puede eliminar");
        repository.delete(mascota);
        repository.flush();
        log.info("Mascota eliminada | id={}", id);
    }

    private Propietario propietario(Long id) {
        return propietarios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Propietario no encontrado: " + id));
    }
    private Mascota obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Mascota no encontrada: " + id));
    }
    private Mascota bloquear(Long id) {
        return repository.buscarParaActualizar(id).orElseThrow(() -> new RecursoNoEncontradoException("Mascota no encontrada: " + id));
    }
}

