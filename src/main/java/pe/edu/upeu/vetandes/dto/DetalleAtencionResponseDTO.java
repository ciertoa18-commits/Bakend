package pe.edu.upeu.vetandes.dto;
import java.math.BigDecimal;
public record DetalleAtencionResponseDTO(Long id, Long prestacionId, String codigo, String prestacion,
                                      Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {}

