package pe.edu.upeu.vetandes.security;

import java.util.*;
import static pe.edu.upeu.vetandes.security.Permiso.*;

public final class PermisosPorRol {
    private PermisosPorRol() {}
    private static final Map<String, Set<Permiso>> MATRIZ = Map.of(
            "ADMIN", Set.copyOf(EnumSet.allOf(Permiso.class)),
            "RECEPCIONISTA", Set.of(PRESTACION_LEER, MASCOTA_LEER, MASCOTA_ESCRIBIR, ATENCION_LEER, ATENCION_REGISTRAR),
            "VETERINARIO", Set.of(PRESTACION_LEER, PRESTACION_ESCRIBIR, MASCOTA_LEER, ATENCION_LEER, ATENCION_ANULAR, REPORTE_VER));

    public static List<String> permisos(Collection<String> roles) {
        return roles.stream().flatMap(rol -> MATRIZ.getOrDefault(rol, Set.of()).stream())
                .map(Enum::name).distinct().sorted().toList();
    }
}

