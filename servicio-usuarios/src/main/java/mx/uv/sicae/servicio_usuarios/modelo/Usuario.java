/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.uv.sicae.servicio_usuarios.modelo;

import java.time.LocalDateTime;

public class Usuario {
    
    private Integer idUsuario;
    private Integer idRol;
    private Integer idTipoUsuario;
    private Integer idProgramaEducativo;
    
    private String nombre;
    private String apellidoPaterno;
    private String usuario; 
    private String contrasena;
    private String correo;
    private String telefono;
    private String claveUsuario; 
    
    private Boolean estatus; 
    private LocalDateTime tiempoCreacion;
    private LocalDateTime tiempoActualizacion;

    public Usuario() {
    }


    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public Integer getIdTipoUsuario() {
        return idTipoUsuario;
    }

    public void setIdTipoUsuario(Integer idTipoUsuario) {
        this.idTipoUsuario = idTipoUsuario;
    }

    public Integer getIdProgramaEducativo() {
        return idProgramaEducativo;
    }

    public void setIdProgramaEducativo(Integer idProgramaEducativo) {
        this.idProgramaEducativo = idProgramaEducativo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getClaveUsuario() {
        return claveUsuario;
    }

    public void setClaveUsuario(String claveUsuario) {
        this.claveUsuario = claveUsuario;
    }

    public Boolean getEstatus() {
        return estatus;
    }

    public void setEstatus(Boolean estatus) {
        this.estatus = estatus;
    }

    public LocalDateTime getTiempoCreacion() {
        return tiempoCreacion;
    }

    public void setTiempoCreacion(LocalDateTime tiempoCreacion) {
        this.tiempoCreacion = tiempoCreacion;
    }

    public LocalDateTime getTiempoActualizacion() {
        return tiempoActualizacion;
    }

    public void setTiempoActualizacion(LocalDateTime tiempoActualizacion) {
        this.tiempoActualizacion = tiempoActualizacion;
    }
    
}
