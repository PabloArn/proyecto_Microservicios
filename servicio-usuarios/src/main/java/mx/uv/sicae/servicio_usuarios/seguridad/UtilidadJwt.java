package mx.uv.sicae.servicio_usuarios.seguridad;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import mx.uv.sicae.servicio_usuarios.modelo.Usuario;
import org.springframework.stereotype.Component;
import java.security.Key;
import java.util.Date;

@Component
public class UtilidadJwt {
    private final Key llaveSecreta = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    private final long TIEMPO_EXPIRACION = 7200000; 

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date fechaExpiracion = new Date(ahora.getTime() + TIEMPO_EXPIRACION);

        return Jwts.builder()
                .setSubject(usuario.getUsuario()) 
                .claim("idUsuario", usuario.getIdUsuario())
                .claim("idRol", usuario.getIdRol())
                .setIssuedAt(ahora)
                .setExpiration(fechaExpiracion)
                .signWith(llaveSecreta)
                .compact();
    }
    public io.jsonwebtoken.Claims extraerClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(llaveSecreta).build().parseClaimsJws(token).getBody();
    }

    public boolean esAdmin(String token) {
        try {
            Integer idRol = extraerClaims(token).get("idRol", Integer.class);
            return idRol != null && idRol == 1;
        } catch (Exception e) {
            return false;
        }
    }
}