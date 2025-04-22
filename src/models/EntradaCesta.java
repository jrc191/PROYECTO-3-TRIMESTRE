package models;

import java.io.Serializable;

public class EntradaCesta implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombreEspectaculo;
    private int fila;
    private int col;
    private double precio;
    private boolean vip;

    public EntradaCesta(String nombreEspectaculo, int fila, int col, double precio, boolean vip) {
        this.nombreEspectaculo = nombreEspectaculo;
        this.fila = fila;
        this.col = col;
        this.precio = precio;
        this.vip = vip;
    }

    public String getNombreEspectaculo() { return nombreEspectaculo; }
    public int getFila() { return fila; }
    public int getCol() { return col; }
    public double getPrecio() { return precio; }
    public boolean isVip() { return vip; }
}
