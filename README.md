# VetAndes API — Backend del examen, version B

Implementa las preguntas 1, 2 y 3 del PDF: Spring Boot 4.0.7, Java 21, Oracle, JPA, Flyway, JWT y Swagger. El paquete principal es `pe.edu.upeu.vetandes`.

## Ejecutar

Necesitas JDK 21 y un esquema Oracle **vacio**. El perfil de desarrollo apunta a `FERRE` en `localhost:1522/freepdb1`; si tu listener usa otro puerto, compruebalo con `lsnrctl status`.

Si el esquema ya se uso para otro proyecto (AutoAndes, PharmaBackend), Flyway encontrara su tabla `flyway_schema_history` con una version 1 ya aplicada, **no** ejecutara `V1__esquema_y_semilla.sql` y el arranque fallara al validar el esquema. En ese caso recrea el usuario con `sql/crear_usuario.sql`.

Configura `DB_PASSWORD` y `JWT_SECRET` en tu entorno local antes de iniciar la aplicacion. No guardes sus valores en el repositorio. `JWT_SECRET` debe ser Base64 de al menos 32 bytes aleatorios.

Desde esta carpeta, en PowerShell:

```powershell
$env:DB_URL = 'jdbc:oracle:thin:@//localhost:1522/freepdb1'
$env:DB_USERNAME = 'FERRE'
.\mvnw.cmd spring-boot:run
```

El perfil de desarrollo usa esta conexion local; requiere que `DB_PASSWORD` y `JWT_SECRET` ya esten configuradas en el entorno. El perfil de produccion tambien exige las variables de conexion. En Linux/macOS: `sh mvnw spring-boot:run`.

- API: http://localhost:8080/api/v1
- Swagger: http://localhost:8080/swagger-ui.html
- OpenAPI generado desde los controladores y DTO: http://localhost:8080/v3/api-docs

Flyway ejecuta `V1__esquema_y_semilla.sql` automaticamente. Hibernate valida el esquema y no modifica tablas. No ejecutes la migracion manualmente. Cambia el puerto con `SERVER_PORT` si 8080 esta ocupado.

Para compilar: `.\mvnw.cmd clean package`. Para ejecutar el JAR: `java -jar target/vetandes-backend-1.0.0.jar`, con las mismas variables de conexion.

## Usuarios y permisos

La migracion crea las cuentas de prueba y almacena sus contrasenas con BCrypt. No publiques ni compartas credenciales; configura las cuentas localmente para probar el login.

| Usuario | Rol | Estado |
| --- | --- | --- |
| admin | ADMIN | Activo |
| recepcion | RECEPCIONISTA | Activo |
| veterinario | VETERINARIO | Activo |
| inactivo | RECEPCIONISTA | Inactivo; login 401 |

`POST /auth/login` recibe `{"username":"recepcion","password":"{{password}}"}` y devuelve `token`, `tipo: "Bearer"`, `expiraEnSegundos: 3600`, `username`, `nombreCompleto`, `roles` y `permisos`. Usa `Authorization: Bearer <token>`. En Swagger, copia el token en **Authorize**.

| Permiso | ADMIN | RECEPCIONISTA | VETERINARIO |
| --- | :---: | :---: | :---: |
| PRESTACION_LEER | Si | Si | Si |
| PRESTACION_ESCRIBIR | Si | No | Si |
| MASCOTA_LEER | Si | Si | Si |
| MASCOTA_ESCRIBIR | Si | Si | No |
| ATENCION_LEER | Si | Si | Si |
| ATENCION_REGISTRAR | Si | Si | No |
| ATENCION_ANULAR | Si | No | Si |
| REPORTE_VER | Si | No | Si |

`PermisosPorRol` centraliza la matriz y une los permisos de todos los roles. `SecurityConfig` verifica `hasAuthority`. El filtro verifica firma, emisor y expiracion y consulta el usuario activo en cada peticion. API STATELESS. Solo login, Swagger y OPTIONS son publicos.

## Contrato obligatorio

Prefijo de todas las rutas: `/api/v1`. Los GET de listas responden arrays; sin resultados, `[]`.

| Metodo | Ruta | Permiso | Exito |
| --- | --- | --- | --- |
| POST | /auth/login | Publico | 200 |
| GET | /auth/me | Autenticado | 200 |
| GET | /propietarios | MASCOTA_LEER | 200 |
| GET | /prestaciones?especie | PRESTACION_LEER | 200 |
| GET | /prestaciones/{id} | PRESTACION_LEER | 200 |
| POST | /prestaciones | PRESTACION_ESCRIBIR | 201 |
| PUT | /prestaciones/{id} | PRESTACION_ESCRIBIR | 200 |
| DELETE | /prestaciones/{id} | PRESTACION_ESCRIBIR | 204 |
| PATCH | /prestaciones/{id}/estado | PRESTACION_ESCRIBIR | 200 |
| GET | /mascotas?propietarioId | MASCOTA_LEER | 200 |
| GET | /mascotas/{id} | MASCOTA_LEER | 200 |
| POST | /mascotas | MASCOTA_ESCRIBIR | 201 |
| PUT | /mascotas/{id} | MASCOTA_ESCRIBIR | 200 |
| DELETE | /mascotas/{id} | MASCOTA_ESCRIBIR | 204 |
| POST | /atenciones | ATENCION_REGISTRAR | 201 |
| GET | /atenciones?mascota&especie&estado&desde&hasta | ATENCION_LEER | 200 |
| GET | /atenciones/{id} | ATENCION_LEER | 200 |
| PATCH | /atenciones/{id}/anular | ATENCION_ANULAR | 200 |
| GET | /reportes/atenciones-por-especie?desde&hasta | REPORTE_VER | 200 |

Prestacion Request:

```json
{"codigo":"PRS-010","nombre":"Consulta de seguimiento","precio":45.50,"especieAplicable":"TODAS","activo":true}
```

Codigo `PRS-000`, nombre 3–80 caracteres, precio positivo con hasta dos decimales, especie aplicable `PERRO`, `GATO` o `TODAS`. Response agrega `id`. El filtro `especie=PERRO` incluye `PERRO` y `TODAS`; `OTRO` incluye solo `TODAS`. Incluye tambien las prestaciones inactivas para administrar el catalogo. Un codigo duplicado responde 409. DELETE de una prestacion utilizada responde 409; baja logica con PATCH y `{"activo":false}`.

Mascota Request:

```json
{"nombre":"Bobby","especie":"PERRO","raza":"Mestizo","fechaNacimiento":"2024-01-01","sexo":"M","propietarioId":1}
```

Nombre 2–40 caracteres despues de recortar espacios, especie `PERRO`, `GATO` u `OTRO`, raza opcional hasta 40 caracteres, fecha obligatoria no futura y sexo `M` o `H`. Response agrega `id`, `propietarioNombre` y conserva `propietarioId`, sin entidades anidadas. Nombre duplicado para el mismo propietario: 409; propietario inexistente: 404; eliminar mascota con atenciones: 409. La unicidad sigue la restriccion `(nombre, propietario_id)` del anexo.

Atencion Request:

```json
{
  "mascotaId":1,
  "peso":12.50,
  "diagnostico":"Control y vacunacion",
  "detalles":[
    {"prestacionId":1,"cantidad":1},
    {"prestacionId":2,"cantidad":1},
    {"prestacionId":3,"cantidad":1}
  ]
}
```

Los IDs del ejemplo corresponden a una instalacion nueva; consulta los listados para obtener los IDs reales. Peso positivo hasta 999.99 y dos decimales (precision de la tabla), diagnostico opcional hasta 300 caracteres; 1–8 detalles distintos, cantidades 1–3. Prestacion inexistente: 404; inactiva o incompatible con la especie: 409. Duplicados, peso o cantidad invalidos: 400.

El servicio obtiene los precios desde Oracle y calcula importes con BigDecimal. Con tres o mas prestaciones distintas descuenta 10 %, redondeado a dos decimales con HALF_UP. `total = subtotal - descuento`. El ejemplo semilla produce subtotal 160, descuento 16 y total 144. El usuario sale del token; se ignoran totales y descuentos adicionales enviados por el cliente.

La respuesta contiene `id, numero, fecha, mascotaId, mascotaNombre, especie, propietarioNombre, peso, diagnostico, estado, subtotal, descuento, total, usuarioRegistro, motivoAnulacion, detalles`. Cada detalle contiene `id, prestacionId, codigo, prestacion, cantidad, precioUnitario, subtotal`.

`@Transactional` engloba cabecera y detalles con cascade; una excepcion revierte las escrituras. Los bloqueos coordinan cambios de mascota y prestaciones durante el registro. La secuencia Oracle produce `AT-000001`; la semilla usa 1–4 y las altas empiezan desde 5. Puede haber saltos por rollback; los numeros no se reutilizan.

Anulacion: PATCH con `{"motivo":"Propietario cancelo la visita"}`. Solo REGISTRADA y como maximo 48 horas despues del registro, inclusive. Motivo de 10–200 caracteres tras recortar espacios. Se conserva el historial y sus detalles.

Filtros de atenciones opcionales y combinables: mascota por nombre parcial sin distinguir mayusculas, especie, estado y fechas `yyyy-MM-dd`. Ambas fechas son inclusivas; orden descendente por fecha e ID. Rango invertido: 400.

Reporte: devuelve `especie, cantidadAtenciones, importeTotal`. Una consulta JPQL hace COUNT, SUM y GROUP BY sobre las cabeceras REGISTRADAS y suma el total despues del descuento. Excluye ANULADAS y ordena por importe descendente.

Errores comunes: 400 validacion, 401 autenticacion, 403 permiso, 404 recurso inexistente, 409 regla/conflicto y 500 inesperado. Formato:

```json
{"timestamp":"2026-10-30T10:00:00","status":400,"error":"Bad Request","mensaje":"Datos invalidos","ruta":"/api/v1/mascotas","campos":{"fechaNacimiento":"La fecha de nacimiento no puede ser futura"}}
```

`campos` solo aparece en Bean Validation. INFO registra altas, cambios, bajas y anulaciones; WARN registra reglas rechazadas y errores de entrada. CORS autoriza `http://localhost:4200`, GET/POST/PUT/PATCH/DELETE/OPTIONS y Authorization.

## Semilla y pruebas

La migracion conserva las cinco tablas del anexo y agrega las tres de seguridad indicadas por la sesion 10. Incluye 5 propietarios, 9 prestaciones, 8 mascotas, 3 atenciones REGISTRADAS y 1 ANULADA. PRS-003 es exclusiva de PERRO; PRS-004 de GATO; PRS-009 esta inactiva. AT-000001 tiene tres prestaciones y descuento; fue registrada hace tres dias para demostrar el rechazo por plazo.

```powershell
.\mvnw.cmd test
# Pruebas HTTP y de persistencia contra el esquema Oracle de pruebas:
$env:RUN_ORACLE_TESTS = 'true'
.\mvnw.cmd verify
Remove-Item Env:RUN_ORACLE_TESTS
```

Las pruebas unitarias no necesitan Oracle. Las de integracion quedan omitidas sin `RUN_ORACLE_TESTS=true`; prueban CRUD, permisos 403, formato de errores, descuento, filtros, reporte, CORS, Swagger, detalle incompatible y rollback forzado despues de los INSERT. Ejecutalas sobre la semilla sin modificar precios. Limpian los registros propios; las secuencias pueden avanzar. No ejecutes otras operaciones concurrentes durante estas pruebas.

Importa `postman/VetAndes.postman_collection.json` y ejecuta las carpetas en orden. La coleccion captura tokens e IDs, prueba los codigos 200/201, 400, 401, 403, 404 y 409 y deja una atencion ANULADA como historial. Los ejemplos guardados son ilustrativos del contrato; no constituyen capturas de una ejecucion real. `sql/verificacion.sql` permite mostrar cabecera, detalles, totales, reporte y conteos antes/despues del rechazo.

Estado de validacion: 29 pruebas aprobadas, sin fallos ni omisiones (23 unitarias y 6 de integracion HTTP contra un esquema Oracle local). Flyway aplicado y esquema JPA validado. Se verificaron CRUD, login, permisos 401/403, descuento, filtros, reporte, CORS, Swagger y rollback real despues de los INSERT. Empaquetado JAR correcto. Coleccion JSON y sintaxis de scripts verificadas; la coleccion no se ejecuto en Postman. No se incluyen capturas de pantalla.

## Recorrido para la defensa

1. `AtencionController` valida el Request y obtiene el nombre del usuario autenticado.
2. `AtencionServiceImpl.registrar` aplica RN-03/RN-04, calcula importes y guarda dentro de la transaccion.
3. `AtencionRepository.saveAndFlush` persiste cabecera y detalles por cascade.
4. `AtencionMapper` genera DTO con relaciones planas antes de cerrar la transaccion.
5. `GlobalExceptionHandler` devuelve el formato comun de errores.
6. `AtencionRepository.reporte` contiene el GROUP BY; cuenta cabeceras y suma el total con descuento.
7. `JwtAuthenticationFilter` valida JWT; `SecurityConfig` verifica permisos antes del controlador.

`VetAndesIntegrationTest.falloDespuesDeLosInsertRevierteLaTransaccion` demuestra rollback tras escribir. El rechazo de PRS-004 para Firulais demuestra que tampoco se guarda una solicitud con un tercer detalle incompatible.

## Versionado y alcance

Esta entrega contiene exclusivamente el backend. La publicacion de repositorios remotos, el frontend y sus capturas no estan incluidos. El criterio de seis commits del examen corresponde a ambos repositorios juntos; el backend tiene commits por sus tres preguntas y validacion.

En un repositorio remoto vacio, configura `git remote add origin <URL>` y publica `main`, `develop`, la rama de examen y la etiqueta `v1.0-examen-u2`. La rama usa el apellido Alarcon del README del proyecto fuente; puedes renombrarla si corresponde otro apellido.

Para produccion: `SPRING_PROFILES_ACTIVE=prod`, DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET (Base64 de al menos 32 bytes aleatorios) y CORS_ORIGINS. No hay contrasenas ni claves JWT predeterminadas; configura valores seguros en el entorno.
