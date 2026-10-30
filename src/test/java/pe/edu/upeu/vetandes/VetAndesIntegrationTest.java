package pe.edu.upeu.vetandes;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.security.crypto.bcrypt.BCrypt;
import tools.jackson.databind.*;
import pe.edu.upeu.vetandes.mapper.AtencionMapper;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.ArgumentMatchers.any;

/** Pruebas HTTP y persistencia REAL. Solo se habilitan con RUN_ORACLE_TESTS=true. */
@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnabledIfEnvironmentVariable(named="RUN_ORACLE_TESTS",matches="true")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VetAndesIntegrationTest {
    @Value("${local.server.port}") int port;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    @MockitoSpyBean AtencionMapper atencionMapper;
    final HttpClient http=HttpClient.newHttpClient();
    final List<Long> creadas=new ArrayList<>();
    final List<Long> mascotas=new ArrayList<>();
    final List<Long> prestaciones=new ArrayList<>();
    final String testPassword=UUID.randomUUID().toString();
    String admin, recepcion, veterinario;

    @BeforeAll void configurarCredencialEfimera() {
        jdbc.update("update usuarios set password=?",BCrypt.hashpw(testPassword,BCrypt.gensalt()));
    }

    @BeforeEach void login() throws Exception {
        admin=login("admin"); recepcion=login("recepcion"); veterinario=login("veterinario");
    }
    @AfterEach void limpiar() {
        for(Long id:creadas){jdbc.update("delete from detalle_atencion where atencion_id=?",id);jdbc.update("delete from atencion where id=?",id);}
        for(Long id:mascotas)jdbc.update("delete from mascota where id=?",id);
        for(Long id:prestaciones)jdbc.update("delete from prestacion where id=?",id);
    }
    @Test void seguridadYErroresComunes() throws Exception {
        for(String ruta:List.of("/propietarios","/prestaciones","/mascotas","/atenciones","/auth/me","/reportes/atenciones-por-especie"))
            call("GET",ruta,null,null,401);
        call("GET","/auth/me","token.alterado.invalido",null,401);
        var desconocido=call("POST","/auth/login",null,Map.of("username","nadie","password",testPassword),401);
        var inactivo=call("POST","/auth/login",null,Map.of("username","inactivo","password",testPassword),401);
        var incorrecto=call("POST","/auth/login",null,Map.of("username","admin","password","incorrecta"),401);
        assertThat(desconocido.path("mensaje").asText()).isEqualTo(inactivo.path("mensaje").asText()).isEqualTo(incorrecto.path("mensaje").asText());
        call("POST","/auth/login",null,Map.of(),400);
        call("GET","/reportes/atenciones-por-especie",recepcion,null,403);
        call("PATCH","/atenciones/1/anular",recepcion,Map.of("motivo","Propietario cancelo visita"),403);
        call("POST","/atenciones",veterinario,Map.of(),403);
        call("POST","/mascotas",veterinario,Map.of(),403);
        call("POST","/prestaciones",recepcion,Map.of(),403);
        assertThat(call("GET","/auth/me",recepcion,null,200).has("password")).isFalse();
        for(String hash:jdbc.queryForList("select password from usuarios",String.class))assertThat(hash).startsWith("$2a$").hasSize(60);
    }
    @Test void crudPrestacionesYMascotas() throws Exception {
        var p=new LinkedHashMap<String,Object>(Map.of("codigo","PRS-990","nombre","Consulta prueba","precio",45.50,"especieAplicable","TODAS","activo",true));
        long pid=call("POST","/prestaciones",veterinario,p,201).path("id").asLong();prestaciones.add(pid);
        call("POST","/prestaciones",admin,p,409);
        p.put("nombre","Consulta actualizada");call("PUT","/prestaciones/"+pid,veterinario,p,200);
        call("PATCH","/prestaciones/"+pid+"/estado",veterinario,Map.of("activo",false),200);
        call("GET","/prestaciones/"+pid,recepcion,null,200);
        call("DELETE","/prestaciones/"+pid,veterinario,null,204);
        call("GET","/prestaciones/"+pid,admin,null,404);
        p.put("precio",0);assertThat(call("POST","/prestaciones",admin,p,400).path("campos").has("precio")).isTrue();
        call("DELETE","/prestaciones/"+id("select id from prestacion where codigo='PRS-001'"),admin,null,409);
        long propietario=id("select id from propietario where dni='70000001'");
        var m=new LinkedHashMap<String,Object>(Map.of("nombre","Mascota prueba API","especie","PERRO","raza","Mestizo","fechaNacimiento","2024-01-01","sexo","M","propietarioId",propietario));
        var creada=call("POST","/mascotas",recepcion,m,201);long mid=creada.path("id").asLong();mascotas.add(mid);
        assertThat(creada.path("propietarioNombre").asText()).isEqualTo("Ana Torres");assertThat(creada.has("propietario")).isFalse();
        call("POST","/mascotas",recepcion,m,409);
        m.put("raza","Beagle");call("PUT","/mascotas/"+mid,recepcion,m,200);
        call("GET","/mascotas?propietarioId="+propietario,recepcion,null,200);
        call("DELETE","/mascotas/"+mid,recepcion,null,204);call("GET","/mascotas/"+mid,recepcion,null,404);
        m.put("propietarioId",999999999);call("POST","/mascotas",recepcion,m,404);
        m.put("fechaNacimiento",LocalDate.now().plusDays(2).toString());
        assertThat(call("POST","/mascotas",recepcion,m,400).path("campos").has("fechaNacimiento")).isTrue();
        call("DELETE","/mascotas/"+id("select id from mascota where nombre='Firulais'"),recepcion,null,409);
    }
    @Test void tercerDetalleIncompatibleNoPersisteCabeceraNiDetalle() throws Exception {
        long cabeceras=id("select count(*) from atencion"), detalles=id("select count(*) from detalle_atencion");
        call("POST","/atenciones",recepcion,request("PRS-001","PRS-002","PRS-004"),409);
        assertThat(id("select count(*) from atencion")).isEqualTo(cabeceras);
        assertThat(id("select count(*) from detalle_atencion")).isEqualTo(detalles);
        call("POST","/atenciones",recepcion,request("PRS-001","PRS-001"),400);
        call("POST","/atenciones",recepcion,request("PRS-009"),409);
    }
    @Test void falloDespuesDeLosInsertRevierteLaTransaccion() throws Exception {
        long cabeceras=id("select count(*) from atencion"), detalles=id("select count(*) from detalle_atencion");
        doThrow(new IllegalStateException("Fallo controlado despues del flush")).when(atencionMapper).toResponse(any());
        call("POST","/atenciones",recepcion,request("PRS-001"),500);
        assertThat(id("select count(*) from atencion")).isEqualTo(cabeceras);
        assertThat(id("select count(*) from detalle_atencion")).isEqualTo(detalles);
    }
    @Test void calculoReporteFiltrosYAnulacion() throws Exception {
        BigDecimal antes=jdbc.queryForObject("select nvl(sum(total),0) from atencion where estado='REGISTRADA'",BigDecimal.class);
        var payload=new HashMap<String,Object>(request("PRS-001","PRS-002","PRS-003"));payload.put("total",1);payload.put("descuento",1000);
        var a=call("POST","/atenciones",recepcion,payload,201);long aid=a.path("id").asLong();creadas.add(aid);
        assertThat(a.path("subtotal").decimalValue()).isEqualByComparingTo("160");
        assertThat(a.path("descuento").decimalValue()).isEqualByComparingTo("16");
        assertThat(a.path("total").decimalValue()).isEqualByComparingTo("144");
        assertThat(a.path("numero").asText()).matches("AT-[0-9]{6}");
        assertThat(id("select count(*) from detalle_atencion where atencion_id="+aid)).isEqualTo(3);
        var hoy=LocalDate.now(ZoneId.of("America/Lima")).toString();
        var lista=call("GET","/atenciones?mascota=fIRu&especie=PERRO&estado=REGISTRADA&desde="+hoy+"&hasta="+hoy,recepcion,null,200);
        assertThat(lista.toString()).contains(a.path("numero").asText());
        assertThat(call("GET","/atenciones?mascota=NO-EXISTE",recepcion,null,200).isEmpty()).isTrue();
        assertThat(sumaReporte()).isEqualByComparingTo(antes.add(new BigDecimal("144")));
        call("GET","/atenciones/"+aid,veterinario,null,200);
        call("PATCH","/atenciones/"+aid+"/anular",veterinario,Map.of("motivo","corto"),400);
        call("PATCH","/atenciones/"+aid+"/anular",veterinario,Map.of("motivo","Propietario cancelo la visita"),200);
        assertThat(sumaReporte()).isEqualByComparingTo(antes);
        call("PATCH","/atenciones/"+aid+"/anular",veterinario,Map.of("motivo","Propietario cancelo la visita"),409);
        call("PATCH","/atenciones/"+id("select id from atencion where numero='AT-000001'")+"/anular",veterinario,Map.of("motivo","Propietario cancelo la visita"),409);
        call("GET","/atenciones?desde=2026-11-01&hasta=2026-10-01",recepcion,null,400);
        var aplicables=call("GET","/prestaciones?especie=PERRO",recepcion,null,200);
        for(JsonNode p:aplicables)assertThat(p.path("especieAplicable").asText()).isIn("PERRO","TODAS");
    }
    @Test void corsYSwagger() throws Exception {
        var req=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/v1/atenciones"))
            .header("Origin","http://localhost:4200").header("Access-Control-Request-Method","POST")
            .header("Access-Control-Request-Headers","authorization,content-type").method("OPTIONS",HttpRequest.BodyPublishers.noBody()).build();
        var res=http.send(req,HttpResponse.BodyHandlers.ofString());assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.headers().firstValue("Access-Control-Allow-Origin")).contains("http://localhost:4200");
        var swagger=http.send(HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/v3/api-docs")).GET().build(),HttpResponse.BodyHandlers.ofString());
        assertThat(swagger.statusCode()).isEqualTo(200);assertThat(json.readTree(swagger.body()).path("paths").has("/api/v1/atenciones")).isTrue();
    }
    Map<String,Object> request(String... codigos) {
        var lineas=Arrays.stream(codigos).map(c -> Map.of("prestacionId",id("select id from prestacion where codigo='"+c+"'"),"cantidad",1)).toList();
        return Map.of("mascotaId",id("select id from mascota where nombre='Firulais'"),"peso",12.50,"diagnostico","Control de prueba","detalles",lineas);
    }
    BigDecimal sumaReporte() throws Exception {
        BigDecimal suma=BigDecimal.ZERO;for(JsonNode fila:call("GET","/reportes/atenciones-por-especie",veterinario,null,200))suma=suma.add(fila.path("importeTotal").decimalValue());return suma;
    }
    long id(String sql){return jdbc.queryForObject(sql,Long.class);}
    String login(String u) throws Exception{return call("POST","/auth/login",null,Map.of("username",u,"password",testPassword),200).path("token").asText();}
    JsonNode call(String metodo,String ruta,String token,Object body,int status) throws Exception {
        var b=HttpRequest.newBuilder(URI.create("http://localhost:"+port+"/api/v1"+ruta)).header("Content-Type","application/json");
        if(token!=null)b.header("Authorization","Bearer "+token);
        var res=http.send(b.method(metodo,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body))).build(),HttpResponse.BodyHandlers.ofString());
        assertThat(res.statusCode()).as(metodo+" "+ruta+" -> "+res.body()).isEqualTo(status);
        var parsed=res.body().isBlank()?json.nullNode():json.readTree(res.body());
        if(status>=400){assertThat(parsed.path("status").asInt()).isEqualTo(status);assertThat(parsed.has("timestamp")&&parsed.has("error")&&parsed.has("mensaje")&&parsed.has("ruta")).isTrue();}
        return parsed;
    }
}
