package mx.uv.sicae.servicio_estacionamiento.modelo;

import java.time.LocalDateTime;

public class Movimiento {
    private Integer idMovimiento;
    private Integer idVehiculo;
    private LocalDateTime tiempoEntrada;
    private LocalDateTime tiempoSalida;
    private Integer minutosEstacionado;
    private Integer horasCobradas;
    private Double costoTotal;
    private Double tarifaHora;
    private LocalDateTime tiempoCreacion;
    private LocalDateTime tiempoActualizacion;
    private Integer idEspacio;

    private String claveUsuario;
    private String placa;

    public Movimiento() {}

    public Integer getIdMovimiento() {
        return idMovimiento;
    }

    public void setIdMovimiento(Integer idMovimiento) {
        this.idMovimiento = idMovimiento;
    }

    public Integer getIdVehiculo() {
        return idVehiculo;
    }

    public void setIdVehiculo(Integer idVehiculo) {
        this.idVehiculo = idVehiculo;
    }

    public LocalDateTime getTiempoEntrada() {
        return tiempoEntrada;
    }

    public void setTiempoEntrada(LocalDateTime tiempoEntrada) {
        this.tiempoEntrada = tiempoEntrada;
    }

    public LocalDateTime getTiempoSalida() {
        return tiempoSalida;
    }

    public void setTiempoSalida(LocalDateTime tiempoSalida) {
        this.tiempoSalida = tiempoSalida;
    }

    public Integer getMinutosEstacionado() {
        return minutosEstacionado;
    }

    public void setMinutosEstacionado(Integer minutosEstacionado) {
        this.minutosEstacionado = minutosEstacionado;
    }

    public Integer getHorasCobradas() {
        return horasCobradas;
    }

    public void setHorasCobradas(Integer horasCobradas) {
        this.horasCobradas = horasCobradas;
    }

    public Double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(Double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public Double getTarifaHora() {
        return tarifaHora;
    }

    public void setTarifaHora(Double tarifaHora) {
        this.tarifaHora = tarifaHora;
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

    public Integer getIdEspacio() {
        return idEspacio;
    }

    public void setIdEspacio(Integer idEspacio) {
        this.idEspacio = idEspacio;
    }

    public String getClaveUsuario() {
        return claveUsuario;
    }

    public void setClaveUsuario(String claveUsuario) {
        this.claveUsuario = claveUsuario;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    
}