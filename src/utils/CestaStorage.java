package utils;

import models.EntradaCesta;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CestaStorage {
    private static final String BASE_PATH = "resources/data/cestas/";

    public static void guardarCesta(String email, List<EntradaCesta> entradas) {
        try {
            Files.createDirectories(Paths.get(BASE_PATH));
            ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(BASE_PATH + email + ".ser")
            );
            oos.writeObject(entradas);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<EntradaCesta> cargarCesta(String email) {
        File file = new File(BASE_PATH + email + ".ser");
        if (!file.exists()) return new ArrayList<>();

        try {
            ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(file)
            );
            List<EntradaCesta> entradas = (List<EntradaCesta>) ois.readObject();
            ois.close();
            return entradas;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}
