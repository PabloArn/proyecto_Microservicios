package mx.uv.sicae.servicio_usuarios.servicio;

import mx.uv.sicae.servicio_usuarios.modelo.RespuestaLogin;
import mx.uv.sicae.servicio_usuarios.modelo.Usuario;
import mx.uv.sicae.servicio_usuarios.repositorio.UsuarioRepositorio;
import mx.uv.sicae.servicio_usuarios.seguridad.UtilidadJwt;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service 
public class UsuarioServicio {

    // Inyecta el repositorio
    @Autowired 
    private UsuarioRepositorio usuarioRepositorio;

    // Inyecta UtilidadJwt
    @Autowired
    private UtilidadJwt utilidadJwt; 

    // Se llama al hacer POST /login con {usuario, password}
    public RespuestaLogin iniciarSesion(String nombreUsuario, String contrasenaPlana) {
        
        // Buscar usuario en BD por su username/email
        Usuario usuarioEnBD = usuarioRepositorio.buscarPorNombreUsuario(nombreUsuario);
        
        // Si no existe, lanza error
        if (usuarioEnBD == null) {
            throw new RuntimeException("Usuario no encontrado o inactivo");
        }

        // Valida el password con BCrypt, compara con el hash, no el texto plano
        boolean passwordCorrecto = BCrypt.checkpw(contrasenaPlana, usuarioEnBD.getContrasena());
        
        if (!passwordCorrecto) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // Genera token JWT con los datos del usuario
        String tokenJwtGenerado = utilidadJwt.generarToken(usuarioEnBD); 

        // Eliminar la contraseña del objeto antes de regresarlo al cliente
        usuarioEnBD.setContrasena(null);

        // Regresa token + usuario
        return new RespuestaLogin(tokenJwtGenerado, usuarioEnBD);
    }
   
    // Se llama al hacer POST /usuarios (registro)
    public String registrarUsuario(Usuario nuevoUsuario) {
        
        // Verifica que el username no esté duplicado
        if (usuarioRepositorio.buscarPorNombreUsuario(nuevoUsuario.getUsuario()) != null) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }
        
        // Verificar que el correo no esté duplicado
        if (usuarioRepositorio.buscarPorCorreo(nuevoUsuario.getCorreo()) != null) {
            throw new RuntimeException("El correo electrónico ya está registrado.");
        }

        // Generamos claveUsuario de manera automatica
        String inicialNombre = nuevoUsuario.getNombre().substring(0, 1).toUpperCase();
        String inicialApellido = nuevoUsuario.getApellidoPaterno().substring(0, 1).toUpperCase();
        int numeroAleatorio = (int) (Math.random() * 900) + 100;
        nuevoUsuario.setClaveUsuario(inicialNombre + inicialApellido + "R-" + numeroAleatorio);

        // Cifra la password con BCrypt
        String hash = BCrypt.hashpw(nuevoUsuario.getContrasena(), BCrypt.gensalt());
        nuevoUsuario.setContrasena(hash);

        // Registra el nuevo usuario en la BD con ayuda del repositorio
        usuarioRepositorio.registrarUsuario(nuevoUsuario);

        // Regresa la clave generada al cliente
        return nuevoUsuario.getClaveUsuario();
    }
 
    // Se llama al hacer GET /usuarios/perfil/{id}
    public Usuario verPerfil(Integer idUsuario) {
        Usuario perfil = usuarioRepositorio.buscarPorId(idUsuario);
        if (perfil == null) {
            throw new RuntimeException("El usuario solicitado no existe.");
        }
        return perfil;
    }

    // Se llama al hacer GET /usuarios/perfil/{id}
    public void editarUsuario(Usuario usuarioActualizado) {
        Usuario existente = usuarioRepositorio.buscarPorId(usuarioActualizado.getIdUsuario());
        
        //Checamos si el usuario existe en la BD
        if (existente == null) {
            throw new RuntimeException("El usuario no existe.");
        }
        
        // Si el correo cambió, verificar que no esté duplicado 
        if (!existente.getCorreo().equals(usuarioActualizado.getCorreo())) {
            if (usuarioRepositorio.buscarPorCorreo(usuarioActualizado.getCorreo()) != null) {
                throw new RuntimeException("El nuevo correo ya está registrado por otro usuario.");
            }
        }
        
        // Actualizar datos en BD
        usuarioRepositorio.editarUsuario(usuarioActualizado);
    }

    // Se llama al hacer PUT /usuarios/estatus/{id} (solo admin)
    public void cambiarEstatus(Integer idUsuario) {
        Usuario existente = usuarioRepositorio.buscarPorId(idUsuario);
        if (existente == null) {
            throw new RuntimeException("El usuario no existe.");
        }
        
        //Bloquea y desbloquea usuarios
        usuarioRepositorio.cambiarEstatus(idUsuario);
    }
}