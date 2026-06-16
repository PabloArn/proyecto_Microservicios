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

    @Autowired 
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private UtilidadJwt utilidadJwt; 

    public RespuestaLogin iniciarSesion(String nombreUsuario, String contrasenaPlana) {
        
        Usuario usuarioEnBD = usuarioRepositorio.buscarPorNombreUsuario(nombreUsuario);
        
        if (usuarioEnBD == null) {
            throw new RuntimeException("Usuario no encontrado o inactivo");
        }

        boolean passwordCorrecto = BCrypt.checkpw(contrasenaPlana, usuarioEnBD.getContrasena());
        
        if (!passwordCorrecto) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String tokenJwtGenerado = utilidadJwt.generarToken(usuarioEnBD); 

        usuarioEnBD.setContrasena(null);

        return new RespuestaLogin(tokenJwtGenerado, usuarioEnBD);
    }
    
    public String registrarUsuario(Usuario nuevoUsuario) {
        if (usuarioRepositorio.buscarPorNombreUsuario(nuevoUsuario.getUsuario()) != null) {
            throw new RuntimeException("El nombre de usuario ya está en uso.");
        }
        if (usuarioRepositorio.buscarPorCorreo(nuevoUsuario.getCorreo()) != null) {
            throw new RuntimeException("El correo electrónico ya está registrado.");
        }

        String inicialNombre = nuevoUsuario.getNombre().substring(0, 1).toUpperCase();
        String inicialApellido = nuevoUsuario.getApellidoPaterno().substring(0, 1).toUpperCase();
        int numeroAleatorio = (int) (Math.random() * 900) + 100;
        nuevoUsuario.setClaveUsuario(inicialNombre + inicialApellido + "R-" + numeroAleatorio);

        String hash = BCrypt.hashpw(nuevoUsuario.getContrasena(), BCrypt.gensalt());
        nuevoUsuario.setContrasena(hash);

        usuarioRepositorio.registrarUsuario(nuevoUsuario);

        return nuevoUsuario.getClaveUsuario();
    }
    
    public Usuario verPerfil(Integer idUsuario) {
        Usuario perfil = usuarioRepositorio.buscarPorId(idUsuario);
        if (perfil == null) {
            throw new RuntimeException("El usuario solicitado no existe.");
        }
        return perfil;
    }

    public void editarUsuario(Usuario usuarioActualizado) {
        // Validar que el usuario que intentan editar sí exista
        Usuario existente = usuarioRepositorio.buscarPorId(usuarioActualizado.getIdUsuario());
        if (existente == null) {
            throw new RuntimeException("El usuario no existe.");
        }
        
        // Validación de correo duplicado: si el correo nuevo es distinto al que ya tenía, verificamos
        if (!existente.getCorreo().equals(usuarioActualizado.getCorreo())) {
            if (usuarioRepositorio.buscarPorCorreo(usuarioActualizado.getCorreo()) != null) {
                throw new RuntimeException("El nuevo correo ya está registrado por otro usuario.");
            }
        }
        
        usuarioRepositorio.editarUsuario(usuarioActualizado);
    }

    public void cambiarEstatus(Integer idUsuario) {
        Usuario existente = usuarioRepositorio.buscarPorId(idUsuario);
        if (existente == null) {
            throw new RuntimeException("El usuario no existe.");
        }
        usuarioRepositorio.cambiarEstatus(idUsuario);
    }
}