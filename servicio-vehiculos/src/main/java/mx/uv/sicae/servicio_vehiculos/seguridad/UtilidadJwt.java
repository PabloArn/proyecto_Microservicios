package mx.uv.sicae.servicio_vehiculos.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.security.Key;

@Component
public class UtilidadJwt {

    @Value("${jwt.secret}")
    private String llaveTexto;

    private Key getLlaveSecreta() {
        return Keys.hmacShaKeyFor(llaveTexto.getBytes());
    }

    public Claims extraerClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getLlaveSecreta())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}