package pe.edu.upeu.vetandes.entity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
@Getter @Setter @Entity
@Table(name="mascota", uniqueConstraints=@UniqueConstraint(name="uq_mascota", columnNames={"nombre","propietario_id"}))
public class Mascota {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false,length=40) private String nombre;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=6) private Especie especie;
    @Column(length=40) private String raza;
    @Column(name="fecha_nacimiento",nullable=false) private LocalDate fechaNacimiento;
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
    @Column(nullable=false,length=1,columnDefinition="CHAR(1)") private String sexo;
    @ManyToOne(fetch=FetchType.LAZY,optional=false)
    @JoinColumn(name="propietario_id",nullable=false) private Propietario propietario;
}
