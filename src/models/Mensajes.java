package models;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;

public class Mensajes {
    private int id_solicitud;
    private String id_usuario;
    private String id_reserva;
    private Timestamp fecha;
    private String tipo_solicitud;
    private char estado_solicitud;

    public Mensajes() {
    }

    public Mensajes(int id_solicitud, String id_usuario, String id_reserva, Timestamp fecha, String tipo_solicitud, char estado_solicitud) {
        this.id_solicitud = id_solicitud;
        this.id_usuario = id_usuario;
        this.id_reserva = id_reserva;
        this.fecha = fecha;
        this.tipo_solicitud = tipo_solicitud;
        this.estado_solicitud = estado_solicitud;
    }

    public int getId_solicitud() {
        return id_solicitud;
    }

    public void setId_solicitud(int id_solicitud) {
        this.id_solicitud = id_solicitud;
    }

    public String getId_usuario() {
        return id_usuario;
    }

    public void setId_usuario(String id_usuario) {
        this.id_usuario = id_usuario;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public String getId_reserva() {
        return id_reserva;
    }

    public void setId_reserva(String id_reserva) {
        this.id_reserva = id_reserva;
    }

    public String getTipo_solicitud() {
        return tipo_solicitud;
    }

    public void setTipo_solicitud(String tipo_solicitud) {
        this.tipo_solicitud = tipo_solicitud;
    }

    public char getEstado_solicitud() {
        return estado_solicitud;
    }

    public void setEstado_solicitud(char estado_solicitud) {
        this.estado_solicitud = estado_solicitud;
    }
}
