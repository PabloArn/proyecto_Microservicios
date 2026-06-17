package mx.uv.sicae.servicio_estacionamiento.servicio;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import mx.uv.sicae.servicio_estacionamiento.modelo.Espacio;
import mx.uv.sicae.servicio_estacionamiento.modelo.Movimiento;
import mx.uv.sicae.servicio_estacionamiento.repositorio.EstacionamientoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EstacionamientoServicio {

    @Autowired
    private EstacionamientoRepositorio estacionamientoRepositorio;

    @Autowired
    private RestTemplate restTemplate;

    private final String URL_USUARIOS = "http://localhost:8081/api/usuarios";
    private final String URL_VEHICULOS = "http://localhost:8082/api/vehiculos";

    public List<Espacio> consultarEspacios() {
        return estacionamientoRepositorio.obtenerEspacios();
    }

    public void registrarEntrada(Movimiento movimiento, String tokenCompleto) {
        if (movimiento.getClaveUsuario() == null || movimiento.getPlaca() == null || movimiento.getIdEspacio() == null) {
            throw new RuntimeException("Error: Clave de usuario, placa y espacio son campos obligatorios.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", tokenCompleto);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        Integer idUsuario;
        try {
            ResponseEntity<Map> respuestaUser = restTemplate.exchange(URL_USUARIOS + "/perfil", HttpMethod.GET, entity, Map.class);
            Map<String, Object> datosUsuario = respuestaUser.getBody();
            
            if (datosUsuario == null || !(Boolean) datosUsuario.get("estatus")) {
                throw new RuntimeException("El usuario no se encuentra activo en el sistema.");
            }
            idUsuario = (Integer) datosUsuario.get("idUsuario");
        } catch (Exception e) {
            throw new RuntimeException("Acceso denegado: Usuario inválido o inactivo en el sistema.");
        }

        Integer idVehiculo = null;
        try {
            ResponseEntity<List> respuestaAutos = restTemplate.exchange(URL_VEHICULOS, HttpMethod.GET, entity, List.class);
            List<Map<String, Object>> misVehiculos = respuestaAutos.getBody();
            
            boolean vehiculoEncontradoYActivo = false;
            if (misVehiculos != null) {
                for (Map<String, Object> auto : misVehiculos) {
                    if (movimiento.getPlaca().equalsIgnoreCase((String) auto.get("placa"))) {
                        if ((Boolean) auto.get("estatus")) {
                            vehiculoEncontradoYActivo = true;
                            idVehiculo = (Integer) auto.get("idVehiculo");
                        }
                        break;
                    }
                }
            }
            if (!vehiculoEncontradoYActivo) {
                throw new RuntimeException("El vehículo con placas " + movimiento.getPlaca() + " no pertenece a su cuenta o está inactivo.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Validación vehicular fallida: " + e.getMessage());
        }

        Movimiento autoAdentro = estacionamientoRepositorio.obtenerMovimientoActivoPorVehiculo(idVehiculo);
        if (autoAdentro != null) {
            throw new RuntimeException("El vehículo ya se encuentra registrado dentro de las instalaciones.");
        }

        int autosEstacionados = 0;
        List<Espacio> todosLosEspacios = estacionamientoRepositorio.obtenerEspacios();
        
        movimiento.setIdVehiculo(idVehiculo);
        movimiento.setTiempoEntrada(LocalDateTime.now());
        movimiento.setTiempoCreacion(LocalDateTime.now());
        if (movimiento.getTarifaHora() == null) {
            movimiento.setTarifaHora(20.00); 
        }

        estacionamientoRepositorio.registrarEntrada(movimiento);
        
        estacionamientoRepositorio.actualizarOcupacionEspacio(movimiento.getIdEspacio(), true);
    }

    public Movimiento registrarSalida(String placa, String tokenCompleto) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", tokenCompleto);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<List> respuestaAutos = restTemplate.exchange(URL_VEHICULOS, HttpMethod.GET, entity, List.class);
        List<Map<String, Object>> misVehiculos = respuestaAutos.getBody();
        Integer idVehiculo = null;
        
        if (misVehiculos != null) {
            for (Map<String, Object> auto : misVehiculos) {
                if (placa.equalsIgnoreCase((String) auto.get("placa"))) {
                    idVehiculo = (Integer) auto.get("idVehiculo");
                    break;
                }
            }
        }

        if (idVehiculo == null) {
            throw new RuntimeException("No se encontró ningún vehículo asociado a su token con la placa: " + placa);
        }

        Movimiento movimientoActivo = estacionamientoRepositorio.obtenerMovimientoActivoPorVehiculo(idVehiculo);
        if (movimientoActivo == null) {
            throw new RuntimeException("Error: No se registra ninguna entrada activa para el vehículo especificado.");
        }

        LocalDateTime entrada = movimientoActivo.getTiempoEntrada();
        LocalDateTime salida = LocalDateTime.now(); 
        
        Duration duracion = Duration.between(entrada, salida);
        long minutosTotales = duracion.toMinutes();
        if (minutosTotales <= 0) minutosTotales = 1; 

        int horasACobrar = (int) Math.ceil(minutosTotales / 60.0);
        double costoFinal = horasACobrar * movimientoActivo.getTarifaHora();

        movimientoActivo.setTiempoSalida(salida);
        movimientoActivo.setMinutosEstacionado((int) minutosTotales);
        movimientoActivo.setHorasCobradas(horasACobrar);
        movimientoActivo.setCostoTotal(costoFinal);
        movimientoActivo.setTiempoActualizacion(LocalDateTime.now());

        estacionamientoRepositorio.registrarSalida(movimientoActivo);

        estacionamientoRepositorio.actualizarOcupacionEspacio(movimientoActivo.getIdEspacio(), false);

        return movimientoActivo;
    }
}