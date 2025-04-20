package models;

import java.time.LocalDate;

public class Espectaculo {
    private String id;
    private String nombre;
    private LocalDate fecha;
    private double precioBase;
    private double precioVip;

    public Espectaculo(String id, String nombre, LocalDate fecha, double precioBase, double precioVip) {
        this.id = id;
        this.nombre = nombre;
        this.fecha = fecha;
        this.precioBase = precioBase;
        this.precioVip = precioVip;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public LocalDate getFecha() { return fecha; }
    public double getPrecioBase() { return precioBase; }
    public double getPrecioVip() { return precioVip; }

    public void setId(String id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setPrecioVip(double precioVip) {
        this.precioVip = precioVip;
    }

    public void setPrecioBase(double precioBase) {
        this.precioBase = precioBase;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }



}
