package pe.edu.upeu.vetandes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.*;
import org.junit.jupiter.api.Test;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.entity.*;
import static org.assertj.core.api.Assertions.*;

class ValidacionesTest {
    @Test void validaFechaNombreYSexo() {
        try(var factory=Validation.buildDefaultValidatorFactory()) {
            var r=new MascotaRequestDTO(" x ",Especie.GATO,null,LocalDate.now().plusDays(2),"X",1L);
            var campos=factory.getValidator().validate(r).stream().map(v->v.getPropertyPath().toString()).toList();
            assertThat(campos).contains("nombre","fechaNacimiento","sexo");
        }
    }
    @Test void validaPesoYCantidadAnidada() {
        try(var factory=Validation.buildDefaultValidatorFactory()) {
            var r=new AtencionRequestDTO(1L,BigDecimal.ZERO,null,List.of(new DetalleAtencionRequestDTO(1L,4)));
            var campos=factory.getValidator().validate(r).stream().map(v->v.getPropertyPath().toString()).toList();
            assertThat(campos).contains("peso","detalles[0].cantidad");
        }
    }
    @Test void validaCodigoYPrecio() {
        try(var factory=Validation.buildDefaultValidatorFactory()) {
            var r=new PrestacionRequestDTO("MAL","Consulta",new BigDecimal("0"),null,true);
            var campos=factory.getValidator().validate(r).stream().map(v->v.getPropertyPath().toString()).toList();
            assertThat(campos).contains("codigo","precio","especieAplicable");
        }
    }
    @Test void semillaAlmacenaHashesBcrypt() throws Exception {
        try(var recurso=getClass().getResourceAsStream("/db/migration/V1__esquema_y_semilla.sql")) {
            assertThat(recurso).isNotNull();
            String sql=new String(recurso.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
            var matcher=java.util.regex.Pattern.compile("\\$2a\\$10\\$[./A-Za-z0-9]{53}").matcher(sql);
            int cantidad=0;
            while(matcher.find()){assertThat(matcher.group()).hasSize(60);cantidad++;}
            assertThat(cantidad).isEqualTo(4);
        }
    }
}
