package pe.edu.upeu.vetandes.dto;
import java.math.BigDecimal;
import pe.edu.upeu.vetandes.entity.EspecieAplicable;
public record PrestacionResponseDTO(Long id, String codigo, String nombre, BigDecimal precio,
    EspecieAplicable especieAplicable, Boolean activo) {}
