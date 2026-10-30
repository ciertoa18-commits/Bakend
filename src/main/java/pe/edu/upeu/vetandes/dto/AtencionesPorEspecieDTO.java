package pe.edu.upeu.vetandes.dto;
import java.math.BigDecimal;
import pe.edu.upeu.vetandes.entity.Especie;
public record AtencionesPorEspecieDTO(Especie especie, Long cantidadAtenciones, BigDecimal importeTotal) {}
