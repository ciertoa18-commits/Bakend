package pe.edu.upeu.vetandes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "propietario")
public class Propietario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 8)
    private String dni;
    @Column(nullable = false, length = 120)
    private String nombres;
    @Column(length = 15)
    private String telefono;
}

