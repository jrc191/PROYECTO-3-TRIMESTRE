package dao;

import models.Butaca;

import java.util.List;

public interface ButacaDaoI {
    List<Butaca> obtenerButacasOcupadas(String idEspectaculo);
    List<Butaca> obtenerButacasVIP();

    List<Butaca> obtenerTodasButacas(String idEspectaculoSeleccionado);
}
