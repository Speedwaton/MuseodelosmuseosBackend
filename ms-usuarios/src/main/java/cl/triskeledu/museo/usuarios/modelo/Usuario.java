package cl.triskeledu.museo.usuarios.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Tabla "usuarios". La contraseña se guarda cifrada (claveHash), nunca la real.
@Entity
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    @Column(unique = true)
    private String correo;
    @Column(name = "clave_hash")
    private String claveHash;
    @Enumerated(EnumType.STRING)
    private Rol rol;
    private String avatar;
    private boolean activo = true;
    private boolean notificaciones = true;
    @Column(name = "texto_grande")
    private boolean textoGrande = false;
    private String idioma = "es";

    public Usuario() {
    }

    public Usuario(String nombre, String correo, String claveHash, Rol rol, String avatar) {
        this.nombre = nombre;
        this.correo = correo;
        this.claveHash = claveHash;
        this.rol = rol;
        this.avatar = avatar;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getClaveHash() { return claveHash; }
    public void setClaveHash(String claveHash) { this.claveHash = claveHash; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public boolean isNotificaciones() { return notificaciones; }
    public void setNotificaciones(boolean notificaciones) { this.notificaciones = notificaciones; }
    public boolean isTextoGrande() { return textoGrande; }
    public void setTextoGrande(boolean textoGrande) { this.textoGrande = textoGrande; }
    public String getIdioma() { return idioma; }
    public void setIdioma(String idioma) { this.idioma = idioma; }
}
