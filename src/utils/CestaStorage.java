package utils;

import models.EntradaCesta;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CestaStorage {
    private static final String BASE_PATH = "../resources/data/cestas/";                //ruta del fichero serializable

    public static void guardarCesta(String email, List<EntradaCesta> entradas) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email no puede ser nulo o vacío");
        }

        Path path = Paths.get(BASE_PATH);
        try {
            Files.createDirectories(path);
            Path filePath = path.resolve(email + ".ser"); //el fichero es RUTA+email+.ser (p.ej: admin@admin.com.ser)

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(filePath.toFile()))) {
                oos.writeObject(entradas != null ? entradas : new ArrayList<>());
            }
        } catch (IOException e) {
            System.err.println("Error al guardar la cesta para el email: " + email);
            e.printStackTrace();
        }
    }


    public static List<EntradaCesta> cargarCesta(String email) {
        if (email == null || email.isEmpty()) {
            return new ArrayList<>();
        }

        Path filePath = Paths.get(BASE_PATH + email + ".ser");
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }


        //Carga del fichero serializable
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(filePath.toFile()))) {
            return (List<EntradaCesta>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar la cesta para el email: " + email);
            e.printStackTrace();
            return new ArrayList<>();
        }


    }
}