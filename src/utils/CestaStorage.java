package utils;

import models.EntradaCesta;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class CestaStorage {
    private static final String BASE_PATH = System.getProperty("user.home") + File.separator + "CINESJRC" + File.separator + "data" + File.separator + "cestas" + File.separator;

    public static void guardarCesta(String email, List<EntradaCesta> entradas) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email no puede ser nulo o vacío");
        }

        try {
            Path path = Paths.get(BASE_PATH);
            Files.createDirectories(path); // crea carpetas si no existen
            Path filePath = path.resolve(email + ".ser");

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
