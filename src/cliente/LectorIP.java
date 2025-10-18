
package cliente;

import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

public class LectorIP {
    private static String rutaArchivo = "IPConfig.txt";

    
    public static String getIP() {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            return br.readLine();
        } catch (IOException e) {
            System.err.println("Error leyendo la IP: " + e.getMessage());
            return null;
        }
    }

    public static int getPuerto() {
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            br.readLine();
            String puerto = br.readLine(); 
            return Integer.parseInt(puerto);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error leyendo el puerto: " + e.getMessage());
            return -1; 
        }
    }
}
