package mx.uv.sicae.servicio_estacionamiento.controlador;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mx.uv.sicae.servicio_estacionamiento.modelo.Espacio;
import mx.uv.sicae.servicio_estacionamiento.modelo.Movimiento;
import mx.uv.sicae.servicio_estacionamiento.servicio.EstacionamientoServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estacionamiento")
public class EstacionamientoControlador {

    @Autowired
    private EstacionamientoServicio estacionamientoServicio;

    @GetMapping("/espacios")
    public ResponseEntity<?> consultarEspacios(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Acceso denegado: Token inválido.");
            }
            List<Espacio> espacios = estacionamientoServicio.consultarEspacios();
            return ResponseEntity.ok(espacios);
        } catch (Exception e) {
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
        }
    }

    @PostMapping("/entrada")
    public ResponseEntity<?> registrarEntrada(
            @RequestHeader("Authorization") String authHeader, 
            @RequestBody Movimiento movimiento) {
        try {
            estacionamientoServicio.registrarEntrada(movimiento, authHeader);
            
            Map<String, Object> respuestaExito = new HashMap<>();
            respuestaExito.put("mensaje", "Entrada registrada correctamente. La pluma ha sido levantada.");
            respuestaExito.put("idMovimiento", movimiento.getIdMovimiento());
            respuestaExito.put("tiempoEntrada", movimiento.getTiempoEntrada());
            respuestaExito.put("espacioAsignado", movimiento.getIdEspacio());
            respuestaExito.put("tarifaPorHora", movimiento.getTarifaHora());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(respuestaExito);
        } catch (Exception e) {
            Map<String, String> respuestaError = new HashMap<>();
            respuestaError.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuestaError);
        }
    }

    @PutMapping("/salida/{placa}")
    public ResponseEntity<?> registrarSalida(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String placa) {
        try {
            Movimiento ticketCerrado = estacionamientoServicio.registrarSalida(placa, authHeader);
            
            Map<String, Object> respuestaExito = new HashMap<>();
            respuestaExito.put("mensaje", "Salida procesada con éxito. Caja liberada.");
            respuestaExito.put("idMovimiento", ticketCerrado.getIdMovimiento());
            respuestaExito.put("tiempoEntrada", ticketCerrado.getTiempoEntrada());
            respuestaExito.put("tiempoSalida", ticketCerrado.getTiempoSalida());
            respuestaExito.put("espacioAsignado", ticketCerrado.getIdEspacio());
            respuestaExito.put("tarifaHora", ticketCerrado.getTarifaHora());
            respuestaExito.put("minutosEstacionado", ticketCerrado.getMinutosEstacionado());
            respuestaExito.put("horasCobradas", ticketCerrado.getHorasCobradas());
            respuestaExito.put("costoTotal", ticketCerrado.getCostoTotal());
            
            return ResponseEntity.ok(respuestaExito);
        } catch (Exception e) {
            Map<String, String> respuestaError = new HashMap<>();
            respuestaError.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuestaError);
        }
    }
}