package pe.edu.upeu.vetandes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "prestacion")
public class Prestacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 7)
    private String codigo;
    @Column(nullable = false, length = 80)
    private String nombre;
    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal precio;
    @Enumerated(EnumType.STRING)
    @Column(name = "especie_aplicable", nullable = false, length = 6)
    private EspecieAplicable especieAplicable;
    // NUMBER(1) en la tabla: 1 activa, 0 dada de baja (RN-01).
    @Convert(converter = BooleanNumericoConverter.class)
    @Column(nullable = false)
    private Boolean activo;
}

