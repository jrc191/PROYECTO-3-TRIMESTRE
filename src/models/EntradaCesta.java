package models;

import java.io.Serial;
import java.io.Serializable;

//Para que cada entrada sea serializable y poderla guardar en un fichero .ser. Necesita un serial.
public class EntradaCesta implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String nombreEspectaculo;
    private String idEspectaculo;  // Nuevo campo
    private int fila;
    private int col;
    private double precio;
    private boolean vip;

    // Constructor modificado
    public EntradaCesta(String nombreEspectaculo, String idEspectaculo, int fila, int col, double precio, boolean esVip) {
        this.nombreEspectaculo = nombreEspectaculo;
        this.idEspectaculo = idEspectaculo;
        this.fila = fila;
        this.col = col;
        this.precio = precio;
        this.vip = esVip;
    }


    public String getIdEspectaculo() { return idEspectaculo; }
    public String getNombreEspectaculo() { return nombreEspectaculo; }
    public int getFila() { return fila; }
    public int getCol() { return col; }
    public double getPrecio() { return precio; }
    public boolean isVip() { return vip; }
}
