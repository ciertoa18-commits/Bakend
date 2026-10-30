package pe.edu.upeu.vetandes.repository;
import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.vetandes.entity.Mascota;
public interface MascotaRepository extends JpaRepository<Mascota,Long> {
    boolean existsByNombreAndPropietarioId(String nombre,Long propietarioId);
    boolean existsByNombreAndPropietarioIdAndIdNot(String nombre,Long propietarioId,Long id);
    List<Mascota> findByPropietarioIdOrderByNombreAsc(Long propietarioId);
    @Query("select count(a) from Atencion a where a.mascota.id=:id") long contarAtenciones(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from Mascota m where m.id=:id") Optional<Mascota> buscarParaActualizar(@Param("id") Long id);
}
