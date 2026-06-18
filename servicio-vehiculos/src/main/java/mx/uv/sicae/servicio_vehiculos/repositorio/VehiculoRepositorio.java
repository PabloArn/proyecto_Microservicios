package mx.uv.sicae.servicio_vehiculos.repositorio;

import java.util.List;
import mx.uv.sicae.servicio_vehiculos.modelo.Vehiculo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface VehiculoRepositorio {
    
    // Buscar vehículo por placa (usado por servicio_estacionamiento al validar)
    Vehiculo buscarPorPlaca(@Param("placa") String placa);
    
    // Contar cuántos vehículos activos tiene un usuario
    int contarVehiculosActivos(@Param("idUsuario") Integer idUsuario);
    
    // Listar todos los vehículos de un usuario
    List<Vehiculo> buscarVehiculosPorUsuario(@Param("idUsuario") Integer idUsuario);
    
    // Registrar un nuevo vehículo
    void registrarVehiculo(Vehiculo vehiculo);
    
    // Editar datos de un vehículo existente
    void editarVehiculo(Vehiculo vehiculo);
    
    // Activar/desactivar un vehículo
    void cambiarEstatus(@Param("idVehiculo") Integer idVehiculo, @Param("idUsuario") Integer idUsuario);
}