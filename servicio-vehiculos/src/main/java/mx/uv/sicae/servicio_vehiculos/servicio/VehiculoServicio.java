package mx.uv.sicae.servicio_vehiculos.servicio;

import java.util.List;
import mx.uv.sicae.servicio_vehiculos.modelo.Vehiculo;
import mx.uv.sicae.servicio_vehiculos.repositorio.VehiculoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehiculoServicio {

    //Sin UtilidadJwt: el servicio no valida tokens, eso lo hace el controlador
    @Autowired
    private VehiculoRepositorio vehiculoRepositorio;

    //Listar vehiculos de un usuario
    public List<Vehiculo> buscarVehiculosPorUsuario(Integer idUsuario) {
        return vehiculoRepositorio.buscarVehiculosPorUsuario(idUsuario);
    }

    //Registrar un vehiculo
    public void registrarVehiculo(Vehiculo vehiculo) {
        
        //Validar campos obligatorios
        if (vehiculo.getIdUsuario() == null || vehiculo.getPlaca() == null || vehiculo.getIdModelo() == null) {
            throw new RuntimeException("Faltan datos para el registro del vehículo.");
        }

        //Verificar que la placa no este duplicada en el sistema
        Vehiculo existenteConPlaca = vehiculoRepositorio.buscarPorPlaca(vehiculo.getPlaca());
        if (existenteConPlaca != null) {
            throw new RuntimeException("Error: Ya existe un vehículo registrado con esta placa " + vehiculo.getPlaca());
        }

        //Validar que un usuario solo puede tener 4 usuarios activos en el sistema
        int activos = vehiculoRepositorio.contarVehiculosActivos(vehiculo.getIdUsuario());
        if (activos >= 4) {
            throw new RuntimeException("Límite alcanzado; el usuario ya cuenta con 4 vehículos activos.");
        }

        //Generar clave vehiculo automaticamente
        String claveGenerada = "AUT-" + (int)(Math.random() * 900 + 100);
        vehiculo.setClaveVehiculo(claveGenerada);

        //Persistir en la BD
        vehiculoRepositorio.registrarVehiculo(vehiculo);
    }

    //Editar un vehiculo
    public void editarVehiculo(Vehiculo vehiculo, Integer idUsuarioAutenticado) {
        //Forzar que el idUsuario sea el del token, NO el que venga en el body
        vehiculo.setIdUsuario(idUsuarioAutenticado);

        //Verificar que la nueva placa no esté en uso por otro vehículo distinto
        Vehiculo existenteConPlaca = vehiculoRepositorio.buscarPorPlaca(vehiculo.getPlaca());
        if (existenteConPlaca != null && !existenteConPlaca.getIdVehiculo().equals(vehiculo.getIdVehiculo())) {
            throw new RuntimeException("Error: La placa " + vehiculo.getPlaca() + " ya está asignada a otro vehículo.");
        }

        //Persistir en la BD
        vehiculoRepositorio.editarVehiculo(vehiculo);
    }

    //Cambiar estatus del vehiculo
    public void cambiarEstatus(Integer idVehiculo, Integer idUsuarioAutenticado) {
        
        //Contar cuántos vehículos activos tiene el usuario
        int activos = vehiculoRepositorio.contarVehiculosActivos(idUsuarioAutenticado);
        
        //Buscar todos los vehículos del usuario y localizar el que se quiere cambiar
        List<Vehiculo> misAutos = vehiculoRepositorio.buscarVehiculosPorUsuario(idUsuarioAutenticado);
        Vehiculo esteAuto = misAutos.stream()
                .filter(v -> v.getIdVehiculo().equals(idVehiculo))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El vehículo no pertenece al usuario o no existe."));

        //Si el vehículo está INACTIVO y el usuario ya tiene 4 activos manda error
        if (esteAuto.getEstatus() == 0 && activos >= 4) {
            throw new RuntimeException("No se puede activar este vehículo porque superaría el límite de 4 activos simultáneos.");
        }

        //Persistir en la BD
        vehiculoRepositorio.cambiarEstatus(idVehiculo, idUsuarioAutenticado);
    }
}