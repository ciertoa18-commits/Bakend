package pe.edu.upeu.vetandes.repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.vetandes.entity.*;

public interface AtencionRepository extends JpaRepository<Atencion, Long> {
@Query("""
            select o from Atencion o join o.mascota v
            where (:mascota is null or upper(v.nombre) like concat('%', concat(upper(:mascota), '%')))
              and (:especie is null or v.especie = :especie)
              and (:estado is null or o.estado = :estado)
              and (:desde is null or o.fecha >= :desde)
              and (:hasta is null or o.fecha < :hasta)
            order by o.fecha desc, o.id desc
            """)
    List<Atencion> filtrar(@Param("mascota") String mascota, @Param("especie") Especie especie, @Param("estado") EstadoAtencion estado,
            @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hastaExclusivo);

    @Query("""
            select new pe.edu.upeu.vetandes.dto.AtencionesPorEspecieDTO(m.especie, count(a), sum(a.total))
            from Atencion a join a.mascota m
            where a.estado = pe.edu.upeu.vetandes.entity.EstadoAtencion.REGISTRADA
              and (:desde is null or a.fecha >= :desde) and (:hasta is null or a.fecha < :hasta)
            group by m.especie order by sum(a.total) desc, m.especie asc
            """)
    List<pe.edu.upeu.vetandes.dto.AtencionesPorEspecieDTO> reporte(
            @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Atencion o where o.id = :id")
    Optional<Atencion> buscarParaActualizar(@Param("id") Long id);

    @Query("select o.mascota.id from Atencion o where o.id = :id")
    Optional<Long> buscarMascotaId(@Param("id") Long id);

    @Query(value = "select atencion_numero_seq.nextval from dual", nativeQuery = true)
    Long siguienteNumero();
}

