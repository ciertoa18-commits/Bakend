package pe.edu.upeu.vetandes.service;

import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.entity.*;
import pe.edu.upeu.vetandes.exception.*;
import pe.edu.upeu.vetandes.mapper.AtencionMapper;
import pe.edu.upeu.vetandes.repository.*;
import pe.edu.upeu.vetandes.service.impl.AtencionServiceImpl;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AtencionServiceImplTest {
    @Mock AtencionRepository atenciones;
    @Mock MascotaRepository mascotas;
    @Mock PrestacionRepository prestaciones;
    AtencionServiceImpl service;
    final Clock clock = Clock.fixed(Instant.parse("2026-10-30T15:00:00Z"), ZoneId.of("America/Lima"));
    Mascota mascota;

    @BeforeEach void preparar() {
        service = new AtencionServiceImpl(atenciones, mascotas, prestaciones, new AtencionMapper(), clock);
        var p = new Propietario(); p.setId(1L); p.setNombres("Ana Torres");
        mascota = new Mascota(); mascota.setId(1L); mascota.setNombre("Firulais");
        mascota.setEspecie(Especie.PERRO); mascota.setPropietario(p);
        when(mascotas.buscarParaActualizar(1L)).thenReturn(Optional.of(mascota));
    }
    Prestacion prestacion(long id, String precio, EspecieAplicable especie, boolean activo) {
        var p = new Prestacion(); p.setId(id); p.setCodigo("PRS-%03d".formatted(id));
        p.setNombre("Prestacion " + id); p.setPrecio(new BigDecimal(precio));
        p.setEspecieAplicable(especie); p.setActivo(activo);
        when(prestaciones.buscarParaActualizar(id)).thenReturn(Optional.of(p)); return p;
    }
    AtencionRequestDTO request(DetalleAtencionRequestDTO... detalles) {
        return new AtencionRequestDTO(1L, new BigDecimal("12.50"), "Control", List.of(detalles));
    }
    DetalleAtencionRequestDTO linea(long id, int cantidad) { return new DetalleAtencionRequestDTO(id,cantidad); }
    void anularHace(Duration antiguedad) {
        var a = new Atencion(); a.setId(1L); a.setMascota(mascota); a.setEstado(EstadoAtencion.REGISTRADA);
        a.setFecha(LocalDateTime.now(clock).minus(antiguedad));
        when(atenciones.buscarMascotaId(1L)).thenReturn(Optional.of(1L));
        when(atenciones.buscarParaActualizar(1L)).thenReturn(Optional.of(a));
    }
    @Test void tresPrestacionesAplicanDescuentoConRedondeo() {
        prestacion(1,"50.05",EspecieAplicable.TODAS,true);
        prestacion(2,"30.00",EspecieAplicable.TODAS,true);
        prestacion(3,"80.00",EspecieAplicable.PERRO,true);
        when(atenciones.siguienteNumero()).thenReturn(5L);
        var r = service.registrar(request(linea(1,1),linea(2,1),linea(3,1)),"recepcion");
        assertThat(r.subtotal()).isEqualByComparingTo("160.05");
        assertThat(r.descuento()).isEqualByComparingTo("16.01");
        assertThat(r.total()).isEqualByComparingTo("144.04");
        assertThat(r.numero()).isEqualTo("AT-000005");
        assertThat(r.usuarioRegistro()).isEqualTo("recepcion");
        var captor = ArgumentCaptor.forClass(Atencion.class);
        verify(atenciones).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getDetalles()).allSatisfy(d -> assertThat(d.getAtencion()).isSameAs(captor.getValue()));
    }
    @Test void cantidadTresDeUnaSolaPrestacionNoAplicaDescuento() {
        prestacion(1,"50.50",EspecieAplicable.TODAS,true);
        when(atenciones.siguienteNumero()).thenReturn(5L);
        var r = service.registrar(request(linea(1,3)),"recepcion");
        assertThat(r.total()).isEqualByComparingTo("151.50");
        assertThat(r.descuento()).isEqualByComparingTo("0");
    }
    @Test void tercerDetalleDeOtraEspecieNoGuardaNada() {
        prestacion(1,"50",EspecieAplicable.TODAS,true);
        prestacion(2,"30",EspecieAplicable.TODAS,true);
        prestacion(4,"70",EspecieAplicable.GATO,true);
        assertThatThrownBy(() -> service.registrar(request(linea(1,1),linea(2,1),linea(4,1)),"recepcion"))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("PRS-004");
        verify(atenciones,never()).saveAndFlush(any());
        verify(atenciones,never()).siguienteNumero();
    }
    @Test void prestacionInactivaNoSeRegistra() {
        prestacion(9,"90",EspecieAplicable.TODAS,false);
        assertThatThrownBy(() -> service.registrar(request(linea(9,1)),"recepcion")).isInstanceOf(ReglaNegocioException.class);
        verify(atenciones,never()).saveAndFlush(any());
    }
    @Test void rechazaPrestacionRepetida() {
        assertThatThrownBy(() -> service.registrar(request(linea(1,1),linea(1,2)),"recepcion")).isInstanceOf(DatosInvalidosException.class);
        verify(atenciones,never()).saveAndFlush(any());
    }
    @Test void rechazaCantidadFueraDeRango() {
        assertThatThrownBy(() -> service.registrar(request(linea(1,4)),"recepcion")).isInstanceOf(DatosInvalidosException.class);
    }
    @Test void rechazaMasDeOchoDetalles() {
        var detalles = java.util.stream.LongStream.rangeClosed(1,9).mapToObj(id -> linea(id,1)).toList();
        assertThatThrownBy(() -> service.registrar(new AtencionRequestDTO(1L,BigDecimal.ONE,null,detalles),"recepcion"))
                .isInstanceOf(DatosInvalidosException.class);
    }
    @Test void rechazaPesoCero() {
        assertThatThrownBy(() -> service.registrar(new AtencionRequestDTO(1L,BigDecimal.ZERO,null,List.of(linea(1,1))),"recepcion"))
                .isInstanceOf(DatosInvalidosException.class);
    }
    @Test void anulaExactamenteALas48Horas() {
        anularHace(Duration.ofHours(48));
        assertThat(service.anular(1L,new AnularAtencionRequestDTO("Propietario cancelo la visita")).estado()).isEqualTo(EstadoAtencion.ANULADA);
    }
    @Test void anulaAunqueSeaDeAyer() {
        anularHace(Duration.ofHours(30));
        assertThat(service.anular(1L,new AnularAtencionRequestDTO("Propietario cancelo la visita")).estado()).isEqualTo(EstadoAtencion.ANULADA);
    }
    @Test void rechaza48HorasYUnSegundo() {
        anularHace(Duration.ofHours(48).plusSeconds(1));
        assertThatThrownBy(() -> service.anular(1L,new AnularAtencionRequestDTO("Propietario cancelo la visita")))
                .isInstanceOf(ReglaNegocioException.class).hasMessageContaining("48 horas");
        verify(atenciones,never()).flush();
    }
    @Test void rechazaMotivoConSoloEspacios() {
        anularHace(Duration.ofHours(1));
        assertThatThrownBy(() -> service.anular(1L,new AnularAtencionRequestDTO("              "))).isInstanceOf(DatosInvalidosException.class);
    }
}
