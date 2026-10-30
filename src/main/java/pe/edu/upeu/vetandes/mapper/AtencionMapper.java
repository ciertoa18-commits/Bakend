package pe.edu.upeu.vetandes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.entity.Atencion;

@Component
public class AtencionMapper {
    public AtencionResponseDTO toResponse(Atencion o) {
        var detalles = o.getDetalles().stream().map(d -> new DetalleAtencionResponseDTO(
                d.getId(), d.getPrestacion().getId(), d.getPrestacion().getCodigo(), d.getPrestacion().getNombre(),
                d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal())).toList();
        return new AtencionResponseDTO(o.getId(), o.getNumero(), o.getFecha(), o.getMascota().getId(),
                o.getMascota().getNombre(), o.getMascota().getEspecie(), o.getMascota().getPropietario().getNombres(), o.getPeso(), o.getDiagnostico(),
                o.getEstado(), o.getSubtotal(), o.getDescuento(), o.getTotal(), o.getUsuarioRegistro(), o.getMotivoAnulacion(), detalles);
    }
}

