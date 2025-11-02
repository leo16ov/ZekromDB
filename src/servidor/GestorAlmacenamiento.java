
package servidor;

import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GestorAlmacenamiento {
    private static String directorio;
    private Map<String, String> usuarios; 
    private final ReentrantReadWriteLock lockUsuario = new ReentrantReadWriteLock();
    
    public GestorAlmacenamiento() {
        this.directorio = "SGBD/";
        this.usuarios = new HashMap<>();
        inicializarDirectorio();
        cargarUsuarios();
    }
    
    private void inicializarDirectorio() {
        File f_directorio = new File(directorio);
        if (!f_directorio.exists()) {
            if (f_directorio.mkdirs()) {
                System.out.println("Directorio de datos creado.");
            }
        }
    }

    private void cargarUsuarios() {
        File archivoUsuarios = new File(directorio + "usuarios.txt");
        
        if (!archivoUsuarios.exists()) {
            crearArchivoUsuarios();
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(archivoUsuarios))) {
            String linea;
            
            while ((linea = reader.readLine()) != null) {
                linea = linea.trim();
                if (!linea.isEmpty()) {
                    String[] partes = linea.split(",", 2);
                    if (partes.length == 2) {
                        String user = partes[0].trim();
                        String pass = partes[1].trim();
                        usuarios.put(user, pass);   
                    }
                }
            }
            
            System.out.println("Usuarios cargados.");
        } catch (IOException e) {
            System.err.println("Error cargando usuarios: " + e.getMessage());
        }
    }
    
    private void crearArchivoUsuarios() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(directorio + "usuarios.txt"))) {
            writer.println("root,12341234"); // Crea el usuario root
            System.out.println("Archivo de usuarios creado: usuarios.txt");
            
        } catch (IOException e) {
            System.err.println("Error creando archivo de usuarios: " + e.getMessage());
        }
    }
    
    public boolean buscarUsuario(String username, String password) {
        lockUsuario.readLock().lock();
        try{
            if (username == null || password == null) {
                return false;
            }
            return usuarios.containsKey(username) && usuarios.get(username).equals(password);
        } finally {
            lockUsuario.readLock().unlock();
        }
        
    }
    
    public boolean buscarUsuario(String username) {
        lockUsuario.readLock().lock();
        try{
            if (username == null) {
                return false;
            }
            return usuarios.containsKey(username);
        } finally {
            lockUsuario.readLock().unlock();
        }
    }
    
    public boolean usuarioExistente(String usuario){
        try (BufferedReader br = new BufferedReader(new FileReader(directorio + "usuarios.txt"))) {
            String linea;
            while((linea = br.readLine()) != null){
                String[] partes = linea.split(","); //Divide la linea leída en partes
                if(partes[0].equals(usuario)){ //Pregunta si el usuario ingresado por el usuario ya existe en usuarios.txt
                    return true; // usuario ya existe
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false; // usuario no existe
    }
    
    public boolean tablaExistente(String tabla) {
        try (BufferedReader br = new BufferedReader(new FileReader(directorio + "datostabla.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().equalsIgnoreCase(tabla)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean guardarUsuario(String usuario, String contraseña){
        try (
            FileWriter fw = new FileWriter(directorio + "usuarios.txt", true);
            BufferedWriter bw = new BufferedWriter(fw)
        ) {
            bw.write(usuario + "," + contraseña + '\n');
            return true;
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean guardarTabla(String tabla, List<List<String>> columnas) {

        File carpetaTablas = new File(directorio + "tablas");
        if (!carpetaTablas.exists()) {
            carpetaTablas.mkdir();
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(directorio + "datostabla.txt", true))) {
            bw.write(tabla);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        crearArchivoTabla(tabla, columnas);
        
        return true;
    }

    private void crearArchivoTabla(String tabla, List<List<String>> columnas) {
        String nombreArchivo = "tablas/" + tabla + ".txt";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(directorio + nombreArchivo))) {
            bw.write("ESTRUCTURA:");
            bw.newLine();

            // Formato: nombre:tipo
            for (List<String> columna : columnas) {
                String nombreColumna = columna.get(1);
                String tipoColumna = columna.get(0).toUpperCase();
                bw.write(nombreColumna + ":" + tipoColumna);
                bw.newLine();
            }
            
            bw.write("DATOS:");
            bw.newLine();

        } catch (IOException e) {
            System.out.println("Error al crear archivo de tabla: " + e.getMessage());
        }
    }
    
    public void eliminarUsuario(String usuario) {
        File archivo = new File(directorio + "usuarios.txt");
        File archivoTemp = new File(directorio + "usuarios_temp.txt");

        try (BufferedReader br = new BufferedReader(new FileReader(archivo));
             BufferedWriter bw = new BufferedWriter(new FileWriter(archivoTemp))) {

            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                
                if (!partes[0].equals(usuario)) {
                    bw.write(linea);
                    bw.newLine();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        
        if (archivo.delete()) {
            archivoTemp.renameTo(archivo);
        }
    }
    
    public void modificarUsuario(String usuario, String nuevaContrasena) {
        List<String> lineas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(directorio + "usuarios.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                if(partes[0].equals(usuario)){
                    lineas.add(usuario + "," + nuevaContrasena); // Reemplazar la contraseña
                } else {
                    lineas.add(linea);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(directorio + "usuarios.txt"))) {
            for (String l : lineas) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public boolean addColumn(String tabla, String textoAAgregar) {
        String rutaArchivo = directorio + "tablas/" + tabla + ".txt";
        try {
            List<String> lineas = Files.readAllLines(Paths.get(rutaArchivo));
            List<String> nuevasLineas = new ArrayList<>();
            boolean enEstructura = false;

            for (String linea : lineas) {
                if (linea.trim().equalsIgnoreCase("ESTRUCTURA:")) {
                    enEstructura = true;
                    nuevasLineas.add(linea);
                    continue;
                }
                
                if (linea.trim().equalsIgnoreCase("DATOS:") && enEstructura) {
                    nuevasLineas.add(textoAAgregar);
                    enEstructura = false; 
                }
                nuevasLineas.add(linea);
            }
            
            if (enEstructura) {
                nuevasLineas.add(textoAAgregar);
            }
            
            Files.write(Paths.get(rutaArchivo), nuevasLineas);
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public void alterTable(String tabla, String textoBuscado, String nuevoTexto) {
        File archivo = new File(directorio + "tablas/" + tabla + ".txt");
        List<String> lineas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.contains(textoBuscado)) {
                    linea = linea.replace(textoBuscado, nuevoTexto);
                }
                lineas.add(linea);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivo))) {
            for (String l : lineas) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public int getPuerto(){
        try (BufferedReader br = new BufferedReader(new FileReader("IPConfig.txt"))) {
            br.readLine();
            String puerto = br.readLine();
            return Integer.parseInt(puerto);
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error leyendo el puerto: " + e.getMessage());
            return -1;
        }
    }
}