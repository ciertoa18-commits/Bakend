package pe.edu.upeu.vetandes.repository;

import java.util.Optional;
import java.util.List;
import pe.edu.upeu.vetandes.entity.EspecieAplicable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.vetandes.entity.Prestacion;

public interface PrestacionRepository extends JpaRepository<Prestacion, Long> {
    List<Prestacion> findByEspecieAplicableInOrderByCodigoAsc(List<EspecieAplicable> especies);
    boolean existsByCodigo(String codigo);
    boolean existsByCodigoAndIdNot(String codigo, Long id);
    @Query(value = "select count(*) from detalle_atencion where prestacion_id = :id", nativeQuery = true)
    long contarUsos(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Prestacion s where s.id = :id")
    Optional<Prestacion> buscarParaActualizar(@Param("id") Long id);
}

