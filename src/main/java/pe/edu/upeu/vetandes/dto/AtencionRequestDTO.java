package pe.edu.upeu.vetandes.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;
public record AtencionRequestDTO(@NotNull @Positive Long mascotaId,
    @NotNull @DecimalMin(value="0",inclusive=false) @Digits(integer=3,fraction=2) BigDecimal peso,
    @Size(max=300) String diagnostico,
    @NotNull @Size(min=1,max=8) List<@NotNull @Valid DetalleAtencionRequestDTO> detalles) {}
