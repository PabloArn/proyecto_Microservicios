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

    // Inyecta el repositorio — acceso a la Base de datos
    @Autowired
    private EstacionamientoRepositorio estacionamientoRepositorio;

    // RestTemplate: cliente HTTP de Spring para hacer llamadas a los otros microservicios.
    @Autowired
    private RestTemplate restTemplate;

    // URLs de los otros microservicios
    private final String URL_USUARIOS = "http://app-usuarios:8081/api/usuarios";
    private final String URL_VEHICULOS = "http://app-vehiculos:8082/api/vehiculos";

    //Obtiene y devuelve todos los espacios del almacenamiento
    public List<Espacio> consultarEspacios() {
        return estacionamientoRepositorio.obtenerEspacios();
    }

    //REGISTRAR MOVIMIENTO (ENTRADA)
    public void registrarEntrada(Movimiento movimiento, String tokenCompleto) {
        // Antes de hacer cualquier llamada externa, verifica que llegaron los datos mínimos.
        if (movimiento.getClaveUsuario() == null || movimiento.getPlaca() == null || movimiento.getIdEspacio() == null) {
            throw new RuntimeException("Error: Clave de usuario, placa y espacio son campos obligatorios.");
        }

        // El token que recibió este endpoint se reenvía a los otros servicios
        // para que también puedan validar que la petición está autenticada.
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", tokenCompleto);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // ── LLAMADA 1: Microservicio de Vehículos
        // GET /api/vehiculos : trae todos los vehículos del usuario autenticado
        // Luego busca en la lista si la placa recibida coincide con alguno.
        Integer idVehiculo = null;
        Integer idUsuarioExtraido = null; 

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
            // Si la placa no existe o el vehículo está inactivo, se rechaza la entrada
            if (!vehiculoEncontradoYActivo) {
                throw new RuntimeException("El vehículo con placas " + movimiento.getPlaca() + " no pertenece a su cuenta o está inactivo.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al validar el carro: " + e.getMessage());
        }

        // ── LLAMADA 2: Microservicio de Usuarios
        // GET /api/usuarios/perfil/{id} → verifica que el usuario dueño del vehículo esté activo.
        // Usa el idUsuario extraído del JSON del vehículo.
        try {
           
            
            ResponseEntity<Map> respuestaUser = restTemplate.exchange(URL_USUARIOS + "/perfil/" + idUsuarioExtraido, HttpMethod.GET, entity, Map.class);
            Map<String, Object> datosUsuario = respuestaUser.getBody();
            
            if (datosUsuario == null || !(Boolean) datosUsuario.get("estatus")) {
                throw new RuntimeException("El usuario no se encuentra activo en el sistema.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Acceso denegado: Usuario inválido o inactivo en el sistema.");
        }

        //Verificar si el vehículo ya se encuentra dentro del estacionamiento
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
        
        // Cambiar estatus del espacio
        estacionamientoRepositorio.actualizarOcupacionEspacio(movimiento.getIdEspacio(), true);
    }
    
    //REGISTRAR MOVIMIENTO (SALIDA)
    public Movimiento registrarSalida(String placa, String tokenCompleto) {
        
        // El token que recibió este endpoint se reenvía a los otros servicios
        // para que también puedan validar que la petición está autenticada.
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

        // ── BUSCAR TICKET ABIERTO ──
        // Obtiene el movimiento activo (tiempoSalida) para este vehículo.
        // Si no existe, el vehículo no tiene entrada registrada.
        Movimiento movimientoActivo = estacionamientoRepositorio.obtenerMovimientoActivoPorVehiculo(idVehiculo);
        if (movimientoActivo == null) {
            throw new RuntimeException("Error: No se registra ninguna entrada activa para el vehículo especificado.");
        }

        // Calcular el cobro
        LocalDateTime entrada = movimientoActivo.getTiempoEntrada();
        LocalDateTime salida = LocalDateTime.now(); 
        
        // Duration.between() calcula la diferencia exacta entre dos LocalDateTime
        Duration duracion = Duration.between(entrada, salida);
        long minutosTotales = duracion.toMinutes();
        if (minutosTotales <= 0) minutosTotales = 1; 

        // Math.ceil(): redondea hacia arriba al entero más cercano.
        int horasACobrar = (int) Math.ceil(minutosTotales / 60.0);
        
        // Costo = horas cobradas × tarifa que se guardó al momento de la entrada
        double costoFinal = horasACobrar * movimientoActivo.getTarifaHora();

        // Completar el movimiento con los datos calculados
        movimientoActivo.setTiempoSalida(salida);
        movimientoActivo.setMinutosEstacionado((int) minutosTotales);
        movimientoActivo.setHorasCobradas(horasACobrar);
        movimientoActivo.setCostoTotal(costoFinal);
        movimientoActivo.setTiempoActualizacion(LocalDateTime.now());

        // Para que haya persistencia
        estacionamientoRepositorio.registrarSalida(movimientoActivo);

        estacionamientoRepositorio.actualizarOcupacionEspacio(movimientoActivo.getIdEspacio(), false);

        // Regresa el movimiento completo al controlador (con costoTotal calculado)
        return movimientoActivo;
    }
}