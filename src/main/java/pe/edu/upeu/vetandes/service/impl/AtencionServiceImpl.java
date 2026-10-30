package pe.edu.upeu.vetandes.service.impl;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.entity.*;
import pe.edu.upeu.vetandes.exception.*;
import pe.edu.upeu.vetandes.mapper.AtencionMapper;
import pe.edu.upeu.vetandes.repository.*;
import pe.edu.upeu.vetandes.service.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AtencionServiceImpl implements AtencionService {
    private static final Logger log = LoggerFactory.getLogger(AtencionServiceImpl.class);
    private final AtencionRepository atenciones;
    private final MascotaRepository mascotas;
    private final PrestacionRepository prestaciones;
    private final AtencionMapper mapper;
    private final Clock clock;

    @Transactional
    public AtencionResponseDTO registrar(AtencionRequestDTO request, String username) {
        // Este bloqueo coordina registros, cambios y eliminaciones de la mascota.
        Mascota mascota = mascotas.buscarParaActualizar(request.mascotaId()).orElseThrow(
                () -> new RecursoNoEncontradoException("Mascota no encontrada: " + request.mascotaId()));
        validarDetalles(request.detalles());
        if (request.peso() == null || request.peso().signum() <= 0) throw new DatosInvalidosException("El peso debe ser mayor que cero");
        // Bloquear prestaciones por ID ascendente evita cambios de precio/estado y reduce interbloqueos.
        Map<Long, Prestacion> catalogo = new HashMap<>();
        request.detalles().stream().map(DetalleAtencionRequestDTO::prestacionId).sorted().forEach(id -> {
            Prestacion prestacion = prestaciones.buscarParaActualizar(id).orElseThrow(
                    () -> new RecursoNoEncontradoException("Prestacion no encontrada: " + id));
            if (!Boolean.TRUE.equals(prestacion.getActivo()))
                throw new ReglaNegocioException("La prestacion " + prestacion.getCodigo() + " esta inactiva");
            if (prestacion.getEspecieAplicable() != EspecieAplicable.TODAS &&
                    !prestacion.getEspecieAplicable().name().equals(mascota.getEspecie().name()))
                throw new ReglaNegocioException("La prestacion " + prestacion.getCodigo() + " no aplica a la especie " + mascota.getEspecie());
            catalogo.put(id, prestacion);
        });

        Atencion atencion = new Atencion();
        Long correlativo = atenciones.siguienteNumero();
        if (correlativo > 999999) throw new ReglaNegocioException("Se agoto el correlativo de atenciones");
        atencion.setNumero("AT-%06d".formatted(correlativo));
        atencion.setFecha(LocalDateTime.now(clock));
        atencion.setMascota(mascota);
        atencion.setPeso(request.peso());
        atencion.setDiagnostico(request.diagnostico());
        atencion.setEstado(EstadoAtencion.REGISTRADA);
        atencion.setUsuarioRegistro(username);
        BigDecimal total = BigDecimal.ZERO;
        for (DetalleAtencionRequestDTO linea : request.detalles()) {
            Prestacion prestacion = catalogo.get(linea.prestacionId());
            DetalleAtencion detalle = new DetalleAtencion();
            detalle.setAtencion(atencion);
            detalle.setPrestacion(prestacion);
            detalle.setCantidad(linea.cantidad());
            detalle.setPrecioUnitario(prestacion.getPrecio());
            detalle.setSubtotal(prestacion.getPrecio().multiply(BigDecimal.valueOf(linea.cantidad())));
            atencion.getDetalles().add(detalle);
            total = total.add(detalle.getSubtotal());
        }
        if (total.compareTo(new BigDecimal("99999999.99")) > 0)
            throw new ReglaNegocioException("El total supera el importe maximo permitido");
        total = total.setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal descuento = request.detalles().size() >= 3
                ? total.multiply(new BigDecimal("0.10")).setScale(2, java.math.RoundingMode.HALF_UP)
                : new BigDecimal("0.00");
        atencion.setSubtotal(total);
        atencion.setDescuento(descuento);
        atencion.setTotal(total.subtract(descuento));
        // Cascade guarda cabecera y detalles. Cualquier excepcion revierte toda la transaccion.
        atenciones.saveAndFlush(atencion);
        log.info("Atencion registrada | numero={} | usuario={} | total={}", atencion.getNumero(), username, atencion.getTotal());
        return mapper.toResponse(atencion);
    }

    public List<AtencionResponseDTO> listar(String mascota, Especie especie, EstadoAtencion estado, LocalDate desde, LocalDate hasta) {
        RangoFechas.validar(desde, hasta);
        return atenciones.filtrar(mascota == null || mascota.isBlank() ? null : mascota.trim(), especie, estado,
                desde == null ? null : desde.atStartOfDay(), hasta == null ? null : hasta.plusDays(1).atStartOfDay())
                .stream().map(mapper::toResponse).toList();
    }

    public AtencionResponseDTO buscar(Long id) { return mapper.toResponse(obtener(id)); }

    @Transactional
    public AtencionResponseDTO anular(Long id, AnularAtencionRequestDTO request) {
        // La consulta escalar evita cargar un estado anterior mientras se espera el bloqueo.
        Long mascotaId = atenciones.buscarMascotaId(id).orElseThrow(
                () -> new RecursoNoEncontradoException("Atencion no encontrada: " + id));
        mascotas.buscarParaActualizar(mascotaId).orElseThrow(
                () -> new RecursoNoEncontradoException("Mascota no encontrada: " + mascotaId));
        Atencion atencion = atenciones.buscarParaActualizar(id).orElseThrow(
                () -> new RecursoNoEncontradoException("Atencion no encontrada: " + id));
        if (atencion.getEstado() != EstadoAtencion.REGISTRADA)
            throw new ReglaNegocioException("Solo se puede anular una atencion REGISTRADA");
        LocalDateTime ahora = LocalDateTime.now(clock);
        if (atencion.getFecha().isAfter(ahora) || atencion.getFecha().plusHours(48).isBefore(ahora))
            throw new ReglaNegocioException("Solo se puede anular una atencion dentro de las 48 horas de su registro");
        String motivo = request.motivo().trim();
        if (motivo.length() < 10 || motivo.length() > 200)
            throw new DatosInvalidosException("El motivo debe tener entre 10 y 200 caracteres sin espacios exteriores");
        atencion.setEstado(EstadoAtencion.ANULADA);
        atencion.setMotivoAnulacion(motivo);
        atenciones.flush();
        log.info("Atencion anulada | numero={}", atencion.getNumero());
        return mapper.toResponse(atencion);
    }

    private Atencion obtener(Long id) {
        return atenciones.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Atencion no encontrada: " + id));
    }

    private void validarDetalles(List<DetalleAtencionRequestDTO> detalles) {
        if (detalles == null || detalles.isEmpty() || detalles.size() > 8)
            throw new DatosInvalidosException("La atencion debe tener entre 1 y 8 detalles");
        Set<Long> ids = new HashSet<>();
        for (DetalleAtencionRequestDTO d : detalles) {
            if (d == null || d.prestacionId() == null || d.cantidad() == null || d.cantidad() < 1 || d.cantidad() > 3)
                throw new DatosInvalidosException("Cada detalle requiere prestacion y cantidad entre 1 y 3");
            if (!ids.add(d.prestacionId()))
                throw new DatosInvalidosException("No se puede repetir el prestacion " + d.prestacionId());
        }
    }
}

