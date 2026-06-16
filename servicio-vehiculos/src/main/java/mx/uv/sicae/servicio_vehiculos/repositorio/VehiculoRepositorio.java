package mx.uv.sicae.servicio_vehiculos.repositorio;

import java.util.List;
import mx.uv.sicae.servicio_vehiculos.modelo.Vehiculo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VehiculoRepositorio {
    
    Vehiculo buscarPorPlaca(@Param("placa") String placa);
    int contarVehiculosActivos(@Param("idUsuario") Integer idUsuario);
    
    List<Vehiculo> buscarVehiculosPorUsuario(@Param("idUsuario") Integer idUsuario);
    void registrarVehiculo(Vehiculo vehiculo);
    void editarVehiculo(Vehiculo vehiculo);
    void cambiarEstatus(@Param("idVehiculo") Integer idVehiculo, @Param("idUsuario") Integer idUsuario);
}