
package servidor;

import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GestorAlmacenamiento {
    private String directorio;
    private String directorioTablas;
    private Map<String, String> usuarios; 
    
    public GestorAlmacenamiento() {
        this.directorio = "SGBD/";
        this.directorioTablas = directorio +"tablas/";
        this.usuarios = new HashMap<>();
        inicializarDirectorio();
        
        cargarUsuarios();
    }
    
    private String inicializarDirectorio() {
        File f_directorio = new File(directorio);
        File f_directorioTablas = new File(directorioTablas);
        
        String mensaje = "";
        if (!f_directorio.exists()) {
            if (f_directorio.mkdirs()) {
                mensaje +="Directorio de datos creado.";
            }
        }
        if(!f_directorioTablas.exists()){
            if (f_directorioTablas.mkdirs()) {
                mensaje +="\nDirectorio de tablas creado.";
            }
        }
        return mensaje;
    }
    
    public boolean buscarUsuario(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        return usuarios.containsKey(username) && usuarios.get(username).equals(password);
    }
    
    public boolean buscarUsuario(String username) {
        if (username == null) {
            return false;
        }
        return usuarios.containsKey(username);
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
    
    public static void guardarTabla(List<String> tokens) {
        String nombreTabla = tokens.get(2);
        
        File carpetaTablas = new File("tablas");
        if (!carpetaTablas.exists()) {
            carpetaTablas.mkdir();
        }
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("datostabla.txt", true))) {
            bw.write(nombreTabla);
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        crearArchivoTabla(tokens);
    }
    
    private static void crearArchivoTabla(List<String> tokens) {
        String nombreTabla = tokens.get(2);
        String nombreArchivo = "SGBD/tablas/" + nombreTabla + ".txt";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(nombreArchivo))) {
            bw.write("ESTRUCTURA:");
            bw.newLine();
            
            
            List<List<String>> columnas = obtenerListColumnas(tokens);

            // Formato: nombre:tipo
            for (List<String> columna : columnas) {
                String nombreColumna = columna.get(1);
                String tipoColumna = columna.get(0).toUpperCase();
                bw.write(nombreColumna + ":" + tipoColumna);
                bw.newLine();
            }
            
            bw.newLine();
            bw.write("DATOS:");
            bw.newLine();

        } catch (IOException e) {
            System.out.println("Error al crear archivo de tabla: " + e.getMessage());
        }
    }

    private void crearArchivoUsuarios() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(directorio+ "usuarios.txt"))) {
            writer.println("root,12341234");
            System.out.println("Archivo de usuarios creado: usuarios.txt");
            
        } catch (IOException e) {
            System.err.println("Error creando archivo de usuarios: " + e.getMessage());
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
    public boolean usuarioExistente(String usuario){
        try (BufferedReader br = new BufferedReader(new FileReader(directorio+"usuarios.txt"))) {
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
    
    //CODIGO DE LA ANTERIOR VERSION:
    
    public boolean tablaExistente(String nombreTabla) {
        try (BufferedReader br = new BufferedReader(new FileReader("datostabla.txt"))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().equalsIgnoreCase(nombreTabla)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    public void guardarUsuario(String usuario, String contraseña){
        try (
            FileWriter fw = new FileWriter(directorio+"usuarios.txt", true);
            BufferedWriter bw = new BufferedWriter(fw)
        ) {
            bw.write(usuario + "," + contraseña);         
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void addColumn(String nombreTabla, String nuevNombre){
        agregarTextoEnEstructura(directorioTablas+ nombreTabla+".txt", nuevNombre);
    }
    private void agregarTextoEnEstructura(String rutaArchivo, String textoAAgregar) {
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
                // Si encuentro DATOS y todavía estoy en ESTRUCTURA,
                // primero agrego el nuevo campo, y luego sigo normalmente
                if (linea.trim().equalsIgnoreCase("DATOS:") && enEstructura) {
                    nuevasLineas.add(textoAAgregar);
                    enEstructura = false; // ya terminé con estructura
                }
                nuevasLineas.add(linea);
            }
            // Si no había sección DATOS, igual agrego al final de ESTRUCTURA
            if (enEstructura) {
                nuevasLineas.add(textoAAgregar);
            }
            Files.write(Paths.get(rutaArchivo), nuevasLineas);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void modificarArchivo(String rutaArchivo, String textoBuscado, String nuevoTexto) {
        File archivo = new File(rutaArchivo);
        List<String> lineas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                // Si la línea contiene el texto a modificar, lo reemplaza
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
    public void modificarUsuario(String usuario, String nuevaContrasena) {
        List<String> lineas = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(directorio+"usuarios.txt"))) {
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
        
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(directorio+"usuarios.txt"))) {
            for (String l : lineas) {
                bw.write(l);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void eliminarUsuario(String usuario) {
        File archivo = new File(directorio+"usuarios.txt");
        File archivoTemp = new File(directorio+"usuarios_temp.txt");
        try (BufferedReader br = new BufferedReader(new FileReader(archivo));
            BufferedWriter bw = new BufferedWriter(new FileWriter(archivoTemp))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] partes = linea.split(",");
                // Pregunta: si no es el usuario a eliminar, se copia
                if (!partes[0].equals(usuario)) {
                    bw.write(linea);
                    bw.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        // Reemplazo el archivo original con el temporal
        if (archivo.delete()) {
            archivoTemp.renameTo(archivo);
        }
    } 
    public boolean campoExistente(String nombreCampo, String rutaArchivo) {
        boolean dentroEstructura = false;
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                // Detecta inicio y fin de la sección ESTRUCTURA
                if (linea.equalsIgnoreCase("ESTRUCTURA:")) {
                    dentroEstructura = true;
                    continue;
                }
                if (linea.equalsIgnoreCase("DATOS:")) {
                    break; // Ya no estamos en ESTRUCTURA
                }
                // Si estamos dentro de ESTRUCTURA, analiza los campos
                if (dentroEstructura && !linea.isEmpty()) {
                    String[] partes = linea.split(":");
                    if (partes.length > 0) {
                        String nombre = partes[0].trim();
                        if (nombre.equalsIgnoreCase(nombreCampo)) {
                            return true; // Campo encontrado
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        }

        return false; // No se encontró el campo
    }
    private static boolean esTipoDatoValido(String tipo){
        List<String> tiposValidos = Arrays.asList("INT", "CHAR", "VARCHAR", "FLOAT", "DOUBLE", "BOOL");
        return tiposValidos.contains(tipo.toUpperCase());
    }
    private static List<List<String>> obtenerListColumnas(List<String> tokens){
        List<String> tokensColumnas = tokens.subList(4, tokens.size()-1);
        List<List<String>> columnas = new ArrayList<>();
        List<String> columnaActual = new ArrayList<>();

        if(!tokensColumnas.isEmpty()){
            for (String token : tokensColumnas) {
                if (token.equals(",")) {
                    if (!columnaActual.isEmpty()) {
                        columnas.add(new ArrayList<>(columnaActual));
                        columnaActual.clear();
                    }
                } else {
                    columnaActual.add(token);
                }
            }
            if (!columnaActual.isEmpty()) {
                columnas.add(new ArrayList<>(columnaActual));
            }
            
        }
        return columnas;
    }
}