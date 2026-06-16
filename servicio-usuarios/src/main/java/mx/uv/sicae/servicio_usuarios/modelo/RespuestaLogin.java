package mx.uv.sicae.servicio_usuarios.modelo;

public class RespuestaLogin {
    private String token;
    private Usuario usuario; 

    public RespuestaLogin(String token, Usuario usuario) {
        this.token = token;
        this.usuario = usuario;
    }
    
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    
}