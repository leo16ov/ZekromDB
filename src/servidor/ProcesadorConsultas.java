package servidor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ProcesadorConsultas {
    private GestorAlmacenamiento gestor;
    
    private enum Comando {
        MAKE_USER {
            @Override
            public boolean validarSintaxis(List<String> tokens) {
                return tokens.size() == 5 && 
                       tokens.get(0).equalsIgnoreCase("MAKE") &&
                       tokens.get(1).equalsIgnoreCase("USER") &&
                       !tokens.get(2).isEmpty() &&
                       tokens.get(3).equalsIgnoreCase("PASSWORD") &&
                       !tokens.get(4).isEmpty() &&
                       tokens.get(4).matches("^'.*'$");
            }
            
            @Override
            public boolean validarSemantica(GestorAlmacenamiento gestor, List<String> tokens) {
                String usuario = tokens.get(2);
                return !gestor.usuarioExistente(usuario);
            }
            
            @Override
            public String ejecutar(GestorAlmacenamiento gestor, List<String> tokens) {
                String usuario = tokens.get(2);
                String password = tokens.get(4).replace("'", "");
                boolean resultado = gestor.guardarUsuario(usuario, password);
                return resultado ? "OK: Usuario '" + usuario + "' creado" : "Error: Usuario '" + usuario + "' no creado";
            }
        },
        
        MAKE_TABLE {
            @Override
            public boolean validarSintaxis(List<String> tokens) {
                if (!validarSintaxisBasica(tokens)) {
                    return false;
                }
                
                List<List<String>> columnas = obtenerListColumnas(tokens);
                return validarSintaxisColumnas(columnas);
            }
    
            private boolean validarSintaxisBasica(List<String> tokens) {
                return tokens.size() >= 7 &&
                       tokens.get(0).equalsIgnoreCase("MAKE") &&
                       tokens.get(1).equalsIgnoreCase("TABLE") &&
                       tokens.get(2).matches("[a-zA-Z][a-zA-Z0-9_]*") &&
                       tokens.get(3).equals("(") && 
                       tokens.get(tokens.size() - 1).equals(")");
            }

            private boolean validarSintaxisColumnas(List<List<String>> columnas) {
                if (columnas.isEmpty()) {
                    return false;
                }

                for (List<String> columna : columnas) {
                    if (columna.size() < 2) { return false; }
                    
                    if (!columna.get(1).matches("[a-zA-Z][a-zA-Z0-9_]*")) { return false; }
                    
                    if (!esTipoDatoValido(columna.get(0).toUpperCase())) { return false; }
                }
                return true;
            }

            
            @Override
            public boolean validarSemantica(GestorAlmacenamiento gestor, List<String> tokens) {
                String tabla = tokens.get(2);
                List<List<String>> columnas = obtenerListColumnas(tokens);
                
                for (List<String> columna : columnas) {
                    if (columna.size() < 2 || !esTipoDatoValido(columna.get(0).toUpperCase())) {
                        return false;
                    }
                }
                
                return !gestor.tablaExistente(tabla);
            }
            
            @Override
            public String ejecutar(GestorAlmacenamiento gestor, List<String> tokens) {
                String tabla = tokens.get(2);
                List<List<String>> columnas = obtenerListColumnas(tokens);
                
                boolean exito = gestor.guardarTabla(tabla, columnas);
                return exito ? "OK: Tabla '" + tabla + "' creada" 
                             : "ERROR: No se pudo crear la tabla";
            }
        },
        
        DROP_TABLE {
            @Override
            public boolean validarSintaxis(List<String> tokens) {
                return tokens.size() == 3 &&
                       tokens.get(0).equalsIgnoreCase("DROP") &&
                       tokens.get(1).equalsIgnoreCase("TABLE") &&
                       !tokens.get(2).isEmpty();
            }
            
            @Override
            public boolean validarSemantica(GestorAlmacenamiento gestor, List<String> tokens) {
                String tabla = tokens.get(2);
                return gestor.tablaExistente(tabla);
            }
            
            @Override
            public String ejecutar(GestorAlmacenamiento gestor, List<String> tokens) {
                String tabla = tokens.get(2);
                // gestor.eliminarTabla("default", tabla);
                return "OK: Tabla '" + tabla + "' eliminada";
            }
        },
        
        DROP_USER {
            @Override
            public boolean validarSintaxis(List<String> tokens) {
                return tokens.size() == 3 &&
                       tokens.get(0).equalsIgnoreCase("DROP") &&
                       tokens.get(1).equalsIgnoreCase("USER") &&
                       !tokens.get(2).isEmpty();
            }
            
            @Override
            public boolean validarSemantica(GestorAlmacenamiento gestor, List<String> tokens) {
                String usuario = tokens.get(2);
                return gestor.usuarioExistente(usuario);
            }
            
            @Override
            public String ejecutar(GestorAlmacenamiento gestor, List<String> tokens) {
                String usuario = tokens.get(2);
                gestor.eliminarUsuario(usuario);
                return "OK: Usuario '" + usuario + "' eliminado";
            }
        },
        
        ALTER_USER {
            @Override
            public boolean validarSintaxis(List<String> tokens) {
                return tokens.size() == 6 &&
                       tokens.get(0).equalsIgnoreCase("ALTER") &&
                       tokens.get(1).equalsIgnoreCase("USER") &&
                       !tokens.get(2).isEmpty() &&
                       tokens.get(3).equalsIgnoreCase("NEW") &&
                       tokens.get(4).equalsIgnoreCase("PASSWORD") &&
                       !tokens.get(5).isEmpty() &&
                       tokens.get(5).matches("^'.*'$");
            }
            
            @Override
            public boolean validarSemantica(GestorAlmacenamiento gestor, List<String> tokens) {
                String usuario = tokens.get(2);
                return gestor.usuarioExistente(usuario);
            }
            
            @Override
            public String ejecutar(GestorAlmacenamiento gestor, List<String> tokens) {
                String usuario = tokens.get(2);
                String contrasena = tokens.get(5);
                contrasena = contrasena.substring(1, contrasena.length() - 1);

                gestor.modificarUsuario(usuario, contrasena);
                return "OK: Password de '" + usuario + "' actualizado";
            }
        },
        
        ALTER_ADD {
            @Override
            public boolean validarSintaxis(List<String> tokens) {
                return tokens.size() == 6 &&
                       !tokens.get(2).isEmpty() &&
                       tokens.get(3).equalsIgnoreCase(".")&&
                       !tokens.get(4).isEmpty() &&
                       !tokens.get(5).isEmpty();
            }
            
            @Override
            public boolean validarSemantica(GestorAlmacenamiento gestor, List<String> tokens) {
                String tabla = tokens.get(2);
                String tipoDato = tokens.get(5);
                
                if(gestor.tablaExistente(tabla) && esTipoDatoValido(tipoDato.toUpperCase())){
                    return true;
                } else{
                    return false;
                }
            }
            
            @Override
            public String ejecutar(GestorAlmacenamiento gestor, List<String> tokens) {
                String tabla = tokens.get(2);
                String columna = tokens.get(4);
                String tipoDato = tokens.get(5);
                if(gestor.addColumn(tabla, columna + ":" + tipoDato)){
                    return "OK: Columna " + columna + " añadida.";
                }else{
                    return "ERROR: No se pudo anadir la columna";
                }
            }
        },
        
        ALTER_CHANGE {
            @Override
            public boolean validarSintaxis(List<String> tokens) {
                return  tokens.size() >= 6 && 
                        !tokens.get(2).isEmpty() &&
                        tokens.get(3).equalsIgnoreCase(".")&&
                        !tokens.get(4).isEmpty() &&
                        !tokens.get(5).isEmpty();
            }
            
            @Override
            public boolean validarSemantica(GestorAlmacenamiento gestor, List<String> tokens) {
                String tabla = tokens.get(2);
                
                if(gestor.tablaExistente(tabla)){
                    return true;
                } else{
                    return false;
                }
            }
            
            @Override
            public String ejecutar(GestorAlmacenamiento gestor, List<String> tokens) {
                String tabla = tokens.get(2); 
                String campo = tokens.get(4)+":"+ tokens.get(5);
                gestor.alterTable(tabla, tokens.get(4), tokens.get(6)); 
                return "OK: Se altero la tabla.";
            }
        };
        
        public abstract boolean validarSintaxis(List<String> tokens);
        public abstract boolean validarSemantica(GestorAlmacenamiento gestor, List<String> tokens);
        public abstract String ejecutar(GestorAlmacenamiento gestor, List<String> tokens);
        
        protected static List<List<String>> obtenerListColumnas(List<String> tokens) {
            List<String> tokensColumnas = tokens.subList(4, tokens.size()-1);
            List<List<String>> columnas = new ArrayList<>();
            List<String> columnaActual = new ArrayList<>();

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
            
            return columnas;
        }
        
        protected static boolean esTipoDatoValido(String tipo) {
            List<String> tiposValidos = Arrays.asList("INT", "CHAR", "VARCHAR", "FLOAT", "DOUBLE", "BOOL");
            return tiposValidos.contains(tipo.toUpperCase());
        }
    }
    
    public ProcesadorConsultas(GestorAlmacenamiento gestor) {
        this.gestor = gestor;
    }
    
    public String procesarConsulta(String consulta) {
        List<String> tokens = dividirEnTokens(consulta);
        if (tokens.isEmpty()) return "ERROR: Consulta vacía";
        
        Comando comando = identificarComando(tokens);
        if (comando == null) {
            return "ERROR: Comando no reconocido";
        }
        
        if (!comando.validarSintaxis(tokens)) {
            return "ERROR: Sintaxis invalida";
        }
        
        if (!comando.validarSemantica(gestor, tokens)) {
            return "ERROR: Semántica incorrecta";
        }
        
        return comando.ejecutar(gestor, tokens);
    }
    
    private Comando identificarComando(List<String> tokens) {
        String posibleComando = tokens.get(0);
        if (tokens.size() >= 2) {
            posibleComando += "_" + tokens.get(1);
        }
        
        try {
            return Comando.valueOf(posibleComando.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    public static List<String> dividirEnTokens(String sentencia) {
        String aux = sentencia.endsWith(";") ? 
            sentencia.substring(0, sentencia.length() - 1) : sentencia;
        
        String[] partes = aux.trim().split("\\s+|(?=[(),.])|(?<=[(),.])");
        List<String> tokens = new ArrayList<>();
        
        for (String parte : partes) {
            if (!parte.trim().isEmpty()) {
                tokens.add(parte);
            }
        }
        return tokens;
    }
}