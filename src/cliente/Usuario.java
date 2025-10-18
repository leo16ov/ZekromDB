
package cliente;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.net.ConnectException;

public class Usuario {
    private String username;
    private String password;
    private Socket socket;
    private BufferedReader br;
    private PrintWriter pw;
    private boolean autenticado;

    
    public Usuario(String username, String password) {
        this.username = username;
        this.password = password;
        this.autenticado = false;
    }
    
    public boolean conectar() {
        try {
            String servidor = LectorIP.getIP();
            int puerto = LectorIP.getPuerto();
            
            this.socket = new Socket(servidor, puerto);
            this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.pw = new PrintWriter(socket.getOutputStream(), true);
            
            System.out.println("Conectado a: " + servidor + ":" + puerto);
            return true;
            
        } catch (IOException e) {
            System.out.println("Error de conexión: " + e.getMessage());
            return false;
        }
    }

    public boolean autenticar() {
        if (socket == null || socket.isClosed()) {
            System.out.println("No hay conexion activa");
            return false;
        }
        
        try {
            pw.println("LOGIN " + username + " " + password);
            
            String respuesta = br.readLine();
            
            if (respuesta != null && respuesta.startsWith("OK")) {
                this.autenticado = true;
                System.out.println(respuesta);
                return true;
            } else {
                System.out.println(respuesta);
                return false;
            }
            
        } catch (IOException e) {
            System.out.println("Error durante autenticacion: " + e.getMessage());
            return false;
        }
    }
    
    public boolean conectarYAutenticar() {
        if (conectar()) {
            return autenticar();
        }
        return false;
    }
    
    public String enviarMensaje(String mensaje) {
        if (!autenticado) {
            return "ERROR: No autenticado. Ejecute autenticar() primero.";
        }
        
        if (socket == null || socket.isClosed()) {
            return "ERROR: No hay conexión activa.";
        }
        
        try {
            pw.println(mensaje);
            return br.readLine();
            
        } catch (IOException e) {
            return "ERROR: " + e.getMessage();
        }
    }
    
    public void cerrarConexion() {
        try {
            if (autenticado && pw != null) enviarMensaje("QUIT");
            if (br != null) br.close();
            if (pw != null) pw.close();
            if (socket != null) socket.close();
            autenticado = false;
        } catch (IOException e) {
            System.out.println("Error al cerrar conexion: " + e.getMessage());
        }
    }
    
    // Getters
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public boolean estaAutenticado() { return autenticado; }
}