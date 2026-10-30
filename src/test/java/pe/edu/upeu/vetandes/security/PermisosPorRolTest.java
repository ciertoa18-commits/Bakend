package pe.edu.upeu.vetandes.security;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PermisosPorRolTest {
    @Test void adminTieneTodosLosPermisos() {
        assertThat(PermisosPorRol.permisos(List.of("ADMIN"))).hasSize(Permiso.values().length);
    }
    @Test void recepcionNoPuedeAnularNiVerReportes() {
        assertThat(PermisosPorRol.permisos(List.of("RECEPCIONISTA"))).contains("ATENCION_REGISTRAR", "MASCOTA_ESCRIBIR")
                .doesNotContain("ATENCION_ANULAR", "REPORTE_VER", "PRESTACION_ESCRIBIR");
    }
    @Test void veterinarioNoRegistraAtencionesNiModificaMascotas() {
        assertThat(PermisosPorRol.permisos(List.of("VETERINARIO"))).contains("ATENCION_ANULAR", "REPORTE_VER")
                .doesNotContain("ATENCION_REGISTRAR", "MASCOTA_ESCRIBIR");
    }
    @Test void variosRolesRecibenLaUnionSinDuplicados() {
        assertThat(PermisosPorRol.permisos(List.of("RECEPCIONISTA", "VETERINARIO")))
                .containsExactlyElementsOf(PermisosPorRol.permisos(List.of("ADMIN")));
        assertThat(PermisosPorRol.permisos(List.of("DESCONOCIDO"))).isEmpty();
    }
}

