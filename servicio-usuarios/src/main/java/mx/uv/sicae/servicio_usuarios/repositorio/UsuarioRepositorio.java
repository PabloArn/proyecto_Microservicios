
package mx.uv.sicae.servicio_usuarios.repositorio;

import mx.uv.sicae.servicio_usuarios.modelo.Usuario;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper 
public interface UsuarioRepositorio {
    
    Usuario buscarPorNombreUsuario(@Param("usuario") String usuario);
    
    void registrarUsuario(Usuario nuevoUsuario);
    
}
