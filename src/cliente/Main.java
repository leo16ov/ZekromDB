
package cliente;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("-------- SISTEMA SGBD --------");
        System.out.println("Conectando a: " + LectorIP.getIP() + ":" + LectorIP.getPuerto());
        System.out.println("Escriba 'QUIT' en cualquier momento para salir\n");
        
        Usuario usuario = null;
        boolean sesionActiva = true;
        
        while (sesionActiva) {
            System.out.print("Usuario: ");
            String username = scanner.nextLine().trim();
            
            if (username.equalsIgnoreCase("QUIT")) {
                System.out.println("👋 Saliendo del sistema...");
                break;
            }
            
            System.out.print("Contrasena: ");
            String password = scanner.nextLine().trim();
            
            if (password.equalsIgnoreCase("QUIT")) {
                System.out.println("👋 Saliendo del sistema...");
                break;
            }
            
            usuario = new Usuario(username, password);
            System.out.println("\n🔗Intentando autenticacion...");
            
            if (usuario.conectarYAutenticar()) {
                modoConsultas(usuario, scanner);
                sesionActiva = false; 
            } else {
                System.out.println("\nAutenticacion fallida.");
                System.out.println();
            }
        }
        
        scanner.close();
        System.out.println("Sistema terminado.");
    }
    
    private static void modoConsultas(Usuario usuario, Scanner scanner) {
        System.out.println("\n" + "==============================");
        System.out.println("SESION INICIADA - MODO CONSULTAS");
        System.out.println("==============================");
        
        boolean modoConsultas = true;
        
        while (modoConsultas && usuario.estaAutenticado()) {
            System.out.print("SGBD> ");
            String consulta = scanner.nextLine().trim();
            
            if (consulta.equalsIgnoreCase("QUIT")) {
                System.out.println("Saliendo del sistema...");
                usuario.cerrarConexion();
                modoConsultas = false;
            }
            else if (!consulta.isEmpty()) {
                String respuesta = usuario.enviarMensaje(consulta);
                System.out.println(respuesta);
            }
        }
    }
}