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

    // 2. REGISTRAR MOVIMIENTO (ENTRADA)
    public void registrarEntrada(Movimiento movimiento, String tokenCompleto) {
        // Regla 1: Validar datos obligatorios básicos
        if (movimiento.getClaveUsuario() == null || movimiento.getPlaca() == null || movimiento.getIdEspacio() == null) {
            throw new RuntimeException("Error: Clave de usuario, placa y espacio son campos obligatorios.");
        }

        // Configurar cabecera de seguridad para las llamadas HTTP internas
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", tokenCompleto);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // --- 1. LLAMADA AL MICROSERVICIO DE VEHÍCULOS (8082) PRIMERO ---
        Integer idVehiculo = null;
        Integer idUsuarioExtraido = null; // Variable para atrapar el ID

        try {
            ResponseEntity<List> respuestaAutos = restTemplate.exchange(URL_VEHICULOS, HttpMethod.GET, entity, List.class);
            List<Map<String, Object>> misVehiculos = respuestaAutos.getBody();
            
            boolean vehiculoEncontradoYActivo = false;
            if (misVehiculos != null) {
                for (Map<String, Object> auto : misVehiculos) {
                    if (movimiento.getPlaca().equalsIgnoreCase((String) auto.get("placa"))) {
                        String estatusStr = String.valueOf(auto.get("estatus"));
                        if (estatusStr.equals("true") || estatusStr.equals("1")) {
                            vehiculoEncontradoYActivo = true;
                            idVehiculo = (Integer) auto.get("idVehiculo");
                            idUsuarioExtraido = (Integer) auto.get("idUsuario");
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

        // --- 2. LLAMADA AL MICROSERVICIO DE USUARIOS (8081) CON EL ID CORRECTO ---
        try {
            // Ahora sí, armamos la URL perfecta: /api/usuarios/perfil/1
            ResponseEntity<Map> respuestaUser = restTemplate.exchange(URL_USUARIOS + "/perfil/" + idUsuarioExtraido, HttpMethod.GET, entity, Map.class);
            Map<String, Object> datosUsuario = respuestaUser.getBody();
            
            if (datosUsuario == null || !(Boolean) datosUsuario.get("estatus")) {
                throw new RuntimeException("El usuario no se encuentra activo en el sistema.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Acceso denegado: Usuario inválido o inactivo en el sistema.");
        }

        // Regla de Negocio Local 1: Verificar si el vehículo ya se encuentra dentro del estacionamiento
        Movimiento autoAdentro = estacionamientoRepositorio.obtenerMovimientoActivoPorVehiculo(idVehiculo);
        if (autoAdentro != null) {
            throw new RuntimeException("El vehículo ya se encuentra registrado dentro de las instalaciones.");
        }
        
        // Inyectar datos calculados internamente para persistir en la base de datos
        movimiento.setIdVehiculo(idVehiculo);
        movimiento.setTiempoEntrada(LocalDateTime.now());
        movimiento.setTiempoCreacion(LocalDateTime.now());
        if (movimiento.getTarifaHora() == null) {
            movimiento.setTarifaHora(20.00); 
        }

        // Persistir la entrada en MySQL
        estacionamientoRepositorio.registrarEntrada(movimiento);
        
        // Cambiar estatus del cajón
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