package mx.uv.sicae.servicio_usuarios.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import mx.uv.sicae.servicio_usuarios.modelo.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

// @Component: Spring lo registra como bean y se puede inyectar con @Autowired
@Component
public class UtilidadJwt {

    //Lo lee desde application properties
    @Value("${jwt.secret}")
    private String llaveTexto;

    // El token expira después de 2 horas de generarlo
    private final long TIEMPO_EXPIRACION = 7200000;

    // Crea la llave secreta HMAC-SHA a partir del texto
    private Key getLlaveSecreta() {
        return Keys.hmacShaKeyFor(llaveTexto.getBytes());
    }

    // Se llama AL REGISTRAR UN USUARIO o AL INICIAR SESIÓN
    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date fechaExpiracion = new Date(ahora.getTime() + TIEMPO_EXPIRACION);

        return Jwts.builder()
                .setSubject(usuario.getUsuario())
                .claim("idUsuario", usuario.getIdUsuario())
                .claim("idRol", usuario.getIdRol())
                .setIssuedAt(ahora)
                .setExpiration(fechaExpiracion)
                .signWith(getLlaveSecreta())
                .compact();
    }

    // Parsea el token y devuelve los datos dentro (subject, claims, fechas)
    public Claims extraerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getLlaveSecreta())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extrae idRol del token y verifica que sea == 1
    public boolean esAdmin(String token) {
        try {
            Integer idRol = extraerClaims(token).get("idRol", Integer.class);
            return idRol != null && idRol == 1;
        } catch (Exception e) {
            return false;
        }
    }
}