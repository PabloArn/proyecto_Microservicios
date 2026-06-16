package mx.uv.sicae.servicio_usuarios.repositorio;

import mx.uv.sicae.servicio_usuarios.modelo.Usuario;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper 
public interface UsuarioRepositorio {
    
    Usuario buscarPorNombreUsuario(@Param("usuario") String usuario);
    
    Usuario buscarPorCorreo(@Param("correo") String correo);
    
    void registrarUsuario(Usuario nuevoUsuario);
    
    Usuario buscarPorId(@Param("idUsuario") Integer idUsuario);
    
    void editarUsuario(Usuario usuarioActualizado);
    
    void cambiarEstatus(@Param("idUsuario") Integer idUsuario);
}

