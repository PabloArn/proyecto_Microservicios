package mx.uv.sicae.servicio_usuarios.controlador;

import mx.uv.sicae.servicio_usuarios.modelo.RespuestaLogin;
import mx.uv.sicae.servicio_usuarios.modelo.Usuario;
import mx.uv.sicae.servicio_usuarios.servicio.UsuarioServicio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import mx.uv.sicae.servicio_usuarios.seguridad.UtilidadJwt;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;
    
    @PostMapping("/login")
    public ResponseEntity<?> iniciarSesion(@RequestBody Map<String, String> credenciales) {
        try {
            String usuario = credenciales.get("usuario");
            String contrasena = credenciales.get("contrasena");
            RespuestaLogin respuesta = usuarioServicio.iniciarSesion(usuario, contrasena);
            return ResponseEntity.ok(respuesta);
            
        } catch (RuntimeException excepcion) {
            return ResponseEntity.status(401).body(Map.of("error", excepcion.getMessage()));
        }
    }

    @Autowired
    private UtilidadJwt utilidadJwt;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarUsuario(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Usuario nuevoUsuario) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado. Falta Token."));
            }
            
            String token = authHeader.substring(7);
            
            if (!utilidadJwt.esAdmin(token)) {
                return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado. Solo los administradores pueden registrar."));
            }

            String claveGenerada = usuarioServicio.registrarUsuario(nuevoUsuario);
            
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Usuario creado correctamente",
                    "claveUsuario", claveGenerada
            ));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido o expirado."));
        }
    }
    
    @GetMapping("/perfil/{idUsuario}")
    public ResponseEntity<?> verPerfil(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer idUsuario) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado."));
            }
            
            Usuario perfil = usuarioServicio.verPerfil(idUsuario);
            return ResponseEntity.ok(perfil);
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/editar")
    public ResponseEntity<?> editarUsuario(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Usuario usuarioActualizado) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado."));
            }
            utilidadJwt.extraerClaims(authHeader.substring(7));
            
            usuarioServicio.editarUsuario(usuarioActualizado);
            
            return ResponseEntity.ok(Map.of("mensaje", "Usuario actualizado correctamente"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido."));
        }
    }

    @PutMapping("/estatus/{idUsuario}")
    public ResponseEntity<?> cambiarEstatus(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable Integer idUsuario) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(Map.of("error", "No autorizado."));
            }
            
            String token = authHeader.substring(7);
            
            if (!utilidadJwt.esAdmin(token)) {
                return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado. Solo administradores."));
            }
            
            usuarioServicio.cambiarEstatus(idUsuario);
            return ResponseEntity.ok(Map.of("mensaje", "Estatus del usuario actualizado correctamente"));
            
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido."));
        }
    }
}