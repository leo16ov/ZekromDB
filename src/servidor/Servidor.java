
package servidor;

import java.io.*;
import java.net.*;

public class Servidor {
    private ServerSocket serverSocket;
    private boolean ejecutando;
    private GestorAlmacenamiento gestorAlmacenamiento;
    
    public Servidor() {
        this.ejecutando = false;
        this.gestorAlmacenamiento = new GestorAlmacenamiento();
    }
    
    public void iniciar() {
        try {
            serverSocket = new ServerSocket(gestorAlmacenamiento.getPuerto());
            ejecutando = true;
            
            System.out.println("Servidor SGBD iniciado.");
            System.out.println("Esperando conexiones...\n");
            
            while (ejecutando) {
                Socket socketCliente = serverSocket.accept();
                String IPCliente = socketCliente.getInetAddress().getHostAddress();
                System.out.println("Cliente conectado: " + IPCliente);
                
                Thread hiloCliente = new Thread(new ManejadorCliente(socketCliente, gestorAlmacenamiento));
                hiloCliente.start();
            }
            
        } catch (IOException e) {
            System.err.println("Error en el servidor: " + e.getMessage());
        }
    }
    
    public void detener() {
        ejecutando = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error al detener servidor: " + e.getMessage());
        }
    }
}