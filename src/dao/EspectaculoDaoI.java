package dao;

import models.Espectaculo;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public interface EspectaculoDaoI {
    List<Espectaculo> obtenerTodos();
    List<Espectaculo> obtenerPorNombre(String nombre);
    List<Espectaculo> obtenerPorFecha(LocalDate fecha);
    String obtenerNombrePorId(String id) throws SQLException;
    boolean eliminarEspectaculo(String id) throws SQLException;
    boolean existeEspectaculo(String id) throws SQLException;
    boolean insertarEspectaculo(Espectaculo espectaculo) throws SQLException;
    boolean actualizarEspectaculo(Espectaculo espectaculo) throws SQLException;
}
