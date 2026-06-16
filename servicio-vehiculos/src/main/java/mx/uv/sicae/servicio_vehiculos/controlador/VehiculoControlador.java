package mx.uv.sicae.servicio_vehiculos.controlador;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mx.uv.sicae.servicio_vehiculos.modelo.Vehiculo;
import mx.uv.sicae.servicio_vehiculos.seguridad.UtilidadJwt;
import mx.uv.sicae.servicio_vehiculos.servicio.VehiculoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoControlador {

    @Autowired
    private VehiculoServicio vehiculoServicio;

    @Autowired
    private UtilidadJwt utilidadJwt;

    private Integer obtenerIdUsuarioDesdeToken(String tokenHeader) {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Acceso denegado: Token ausente o formato incorrecto.");
        }
        String tokenPuro = tokenHeader.substring(7);
        return utilidadJwt.extraerClaims(tokenPuro).get("idUsuario", Integer.class);
    }

    @GetMapping
    public ResponseEntity<?> obtenerMisVehiculos(@RequestHeader("Authorization") String authHeader) {
        try {
            Integer idUsuario = obtenerIdUsuarioDesdeToken(authHeader);
            List<Vehiculo> misVehiculos = vehiculoServicio.buscarVehiculosPorUsuario(idUsuario);
            return ResponseEntity.ok(misVehiculos);
        } catch (Exception e) {
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(respuesta);
        }
    }

    @PostMapping
    public ResponseEntity<?> registrarVehiculo(@RequestHeader("Authorization") String authHeader, @RequestBody Vehiculo vehiculo) {
        try {
            Integer idUsuario = obtenerIdUsuarioDesdeToken(authHeader);
            vehiculo.setIdUsuario(idUsuario); 
            
            vehiculoServicio.registrarVehiculo(vehiculo);
            
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Vehículo registrado correctamente bajo su cuenta.");
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        } catch (Exception e) {
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }

    @PutMapping("/{idVehiculo}")
    public ResponseEntity<?> editarVehiculo(
            @RequestHeader("Authorization") String authHeader, 
            @PathVariable Integer idVehiculo, 
            @RequestBody Vehiculo vehiculo) {
        try {
            Integer idUsuario = obtenerIdUsuarioDesdeToken(authHeader);
            vehiculo.setIdVehiculo(idVehiculo);
            
            vehiculoServicio.editarVehiculo(vehiculo, idUsuario);
            
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Información del vehículo actualizada exitosamente.");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }

    @PutMapping("/estatus/{idVehiculo}")
    public ResponseEntity<?> cambiarEstatus(
            @RequestHeader("Authorization") String authHeader, 
            @PathVariable Integer idVehiculo) {
        try {
            Integer idUsuario = obtenerIdUsuarioDesdeToken(authHeader);
            
            vehiculoServicio.cambiarEstatus(idVehiculo, idUsuario);
            
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "El estatus del vehículo ha cambiado correctamente.");
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }
}