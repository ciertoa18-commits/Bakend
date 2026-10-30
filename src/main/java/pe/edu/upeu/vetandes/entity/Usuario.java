package pe.edu.upeu.vetandes.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Entity @Table(name = "usuarios")
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 30)
    private String username;
    @Column(nullable = false, length = 100)
    private String password;
    @Column(name = "nombre_completo", nullable = false, length = 120)
    private String nombreCompleto;
    // NUMBER(1) en la tabla: 0 deshabilita el inicio de sesion.
    @Convert(converter = BooleanNumericoConverter.class)
    @Column(nullable = false)
    private Boolean activo;
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id"))
    private Set<Rol> roles = new HashSet<>();
}

