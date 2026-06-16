package mx.uv.sicae.servicio_vehiculos.servicio;

import java.util.List;
import mx.uv.sicae.servicio_vehiculos.modelo.Vehiculo;
import mx.uv.sicae.servicio_vehiculos.repositorio.VehiculoRepositorio;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VehiculoServicio {

    @Autowired
    private VehiculoRepositorio vehiculoRepositorio;

    public List<Vehiculo> buscarVehiculosPorUsuario(Integer idUsuario) {
        return vehiculoRepositorio.buscarVehiculosPorUsuario(idUsuario);
    }

    public void registrarVehiculo(Vehiculo vehiculo) {
        if (vehiculo.getIdUsuario() == null || vehiculo.getPlaca() == null || vehiculo.getIdModelo() == null) {
            throw new RuntimeException("Faltan datos para el registro del vehículo.");
        }

        Vehiculo existenteConPlaca = vehiculoRepositorio.buscarPorPlaca(vehiculo.getPlaca());
        if (existenteConPlaca != null) {
            throw new RuntimeException("Error: Ya existe un vehículo registrado con esta placa " + vehiculo.getPlaca());
        }

        int activos = vehiculoRepositorio.contarVehiculosActivos(vehiculo.getIdUsuario());
        if (activos >= 4) {
            throw new RuntimeException("Límite alcanzado; el usuario ya cuenta con 4 vehículos activos.");
        }

        String claveGenerada = "AUT-" + (int)(Math.random() * 900 + 100);
        vehiculo.setClaveVehiculo(claveGenerada);

        vehiculoRepositorio.registrarVehiculo(vehiculo);
    }

    public void editarVehiculo(Vehiculo vehiculo, Integer idUsuarioAutenticado) {
        vehiculo.setIdUsuario(idUsuarioAutenticado);

        Vehiculo existenteConPlaca = vehiculoRepositorio.buscarPorPlaca(vehiculo.getPlaca());
        if (existenteConPlaca != null && !existenteConPlaca.getIdVehiculo().equals(vehiculo.getIdVehiculo())) {
            throw new RuntimeException("Error: La placa " + vehiculo.getPlaca() + " ya está asignada a otro vehículo.");
        }

        vehiculoRepositorio.editarVehiculo(vehiculo);
    }

    public void cambiarEstatus(Integer idVehiculo, Integer idUsuarioAutenticado) {
        int activos = vehiculoRepositorio.contarVehiculosActivos(idUsuarioAutenticado);
        
        List<Vehiculo> misAutos = vehiculoRepositorio.buscarVehiculosPorUsuario(idUsuarioAutenticado);
        Vehiculo esteAuto = misAutos.stream()
                .filter(v -> v.getIdVehiculo().equals(idVehiculo))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El vehículo no pertenece al usuario o no existe."));

        if (esteAuto.getEstatus() == 0 && activos >= 4) {
            throw new RuntimeException("No se puede activar este vehículo porque superaría el límite de 4 activos simultáneos.");
        }

        vehiculoRepositorio.cambiarEstatus(idVehiculo, idUsuarioAutenticado);
    }
}