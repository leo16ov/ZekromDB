
package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

class ManejadorCliente implements Runnable {
    private Socket socketCliente;
    private GestorAlmacenamiento gestorAlmacenamiento;
    private boolean autenticado;
    private String usuarioActual;
    private ProcesadorConsultas procesador;
    
    public ManejadorCliente(Socket socket, GestorAlmacenamiento gestor) {
        this.socketCliente = socket;
        this.gestorAlmacenamiento = gestor;
        this.autenticado = false;
        this.procesador = new ProcesadorConsultas(gestor);
    }
    
    @Override
    public void run() {
        String IPCliente = socketCliente.getInetAddress().getHostAddress();
        
        try (BufferedReader entrada = new BufferedReader(
            new InputStreamReader(socketCliente.getInputStream()));
            PrintWriter salida = new PrintWriter(socketCliente.getOutputStream(), true)
        ) {
            String mensajeCliente;
            
            while ((mensajeCliente = entrada.readLine()) != null) {
                System.out.println("[" + IPCliente + "] " + mensajeCliente);
                
                String respuesta;
                
                if (!autenticado) {
                    respuesta = procesarLogin(mensajeCliente);
                    salida.println(respuesta);
                    
                    if (respuesta.startsWith("OK")) {
                        autenticado = true;
                        System.out.println("Cliente " + IPCliente + " autenticado como: " + usuarioActual);
                    } else {
                        break;
                    }
                    
                } else { // Consulta
                    if (mensajeCliente.equalsIgnoreCase("QUIT")) {
                        respuesta = "OK: Conexion finalizada";
                        salida.println(respuesta);
                        System.out.println("Cliente " + IPCliente + " (" + usuarioActual + ") desconectado.");
                        break;
                    } else {
                        respuesta = procesador.procesarConsulta(mensajeCliente);
                        salida.println(respuesta);
                    }
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error con cliente " + IPCliente + ": " + e.getMessage());
        } finally {
            try {
                socketCliente.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
    
    private String procesarLogin(String mensaje) {
        if (mensaje.startsWith("LOGIN ")) {
            String[] partes = mensaje.split(" ", 3);
            
            if (partes.length == 3) {
                String username = partes[1];
                String password = partes[2];
                
                if (gestorAlmacenamiento.buscarUsuario(username, password)) {
                    usuarioActual = username;
                    return "OK: Autenticacion exitosa. Bienvenido " + username;
                } else {
                    return "ERROR: Credenciales incorrectas";
                }
            }
        }
        return "ERROR: Formato: LOGIN usuario contraseña";
    }
}