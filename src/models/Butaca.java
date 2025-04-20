package models;

public class Butaca {
    private String id;
    private int fila;
    private int columna;
    private char tipo;

    public Butaca(String id, int fila, int columna, char tipo) {
        this.id = id;
        this.fila = fila;
        this.columna = columna;
        this.tipo = tipo;
    }

    public String getId() { return id; }
    public int getFila() { return fila; }
    public int getColumna() { return columna; }
    public char getTipo() { return tipo; }
}
