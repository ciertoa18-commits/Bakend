package pe.edu.upeu.vetandes.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import pe.edu.upeu.vetandes.entity.*;
public record AtencionResponseDTO(Long id, String numero, LocalDateTime fecha, Long mascotaId,
    String mascotaNombre, Especie especie, String propietarioNombre, BigDecimal peso, String diagnostico,
    EstadoAtencion estado, BigDecimal subtotal, BigDecimal descuento, BigDecimal total,
    String usuarioRegistro, String motivoAnulacion, List<DetalleAtencionResponseDTO> detalles) {}
