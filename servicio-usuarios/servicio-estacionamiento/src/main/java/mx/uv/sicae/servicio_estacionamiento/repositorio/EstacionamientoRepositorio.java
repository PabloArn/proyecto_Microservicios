package mx.uv.sicae.servicio_estacionamiento.repositorio;

import java.util.List;
import mx.uv.sicae.servicio_estacionamiento.modelo.Espacio;
import mx.uv.sicae.servicio_estacionamiento.modelo.Movimiento;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EstacionamientoRepositorio {

    List<Espacio> obtenerEspacios();

    void registrarEntrada(Movimiento movimiento);

    void registrarSalida(Movimiento movimiento);

    void actualizarOcupacionEspacio(@Param("idEspacio") Integer idEspacio, @Param("ocupado") Boolean ocupado);

    Movimiento obtenerMovimientoActivoPorVehiculo(@Param("idVehiculo") Integer idVehiculo);
}