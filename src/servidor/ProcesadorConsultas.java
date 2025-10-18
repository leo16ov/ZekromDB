
package servidor;

import java.util.ArrayList;
import java.util.List;


public class ProcesadorConsultas {
    
    private GestorAlmacenamiento gestor;
    
    public ProcesadorConsultas(GestorAlmacenamiento gestor){
        this.gestor = gestor;
    }
    
    public String procesarConsulta(String consulta){
        List<String> tokens = dividirEnTokens(consulta); 
        
        if (validarSintaxis(tokens)){
            if(validarSemantica(tokens)){
                return ejecutarConsulta(tokens);
            } else {
                return "Error en la semantica de la consulta.";
            }
        } else {
            return "Error en la sintaxis de la consulta";
        }
    }
    
    public static List<String> dividirEnTokens(String sentencia) {
        String aux = sentencia;
        
        if(sentencia.endsWith(";")){
            aux = sentencia.substring(0, sentencia.length() - 1); //El -1 es porque el último carácter de la sentencia es ";"
        }
        
        String[] partes = aux.trim().split("\\s+|(?=[(),.])|(?<=[(),.])");
    
        List<String> tokens = new ArrayList<>();
        for (String parte : partes) {
            if (!parte.trim().isEmpty()) {
                tokens.add(parte);
            }
        }

        return tokens;
    }
    
    private boolean validarSintaxis(List<String> tokens){
        if (tokens.get(0).equalsIgnoreCase("MAKE")) { //Pregunta si la primera palabra de la sentencia es MAKE
            if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("USER")){
                if (tokens.size() == 5) { //Pregunta si la sentencia tiene 5 tokens (MAKE USER <usuario> PASSWORD <contraseña>)
            
                return tokens.get(0).equalsIgnoreCase("MAKE") && //Pregunta si el primer token (palabra) es MAKE
                       tokens.get(1).equalsIgnoreCase("USER") && //Pregunta si el segundo token es USER
                       !tokens.get(2).isEmpty() &&               //Se asegura que el token no esté vacío
                       tokens.get(3).equalsIgnoreCase("PASSWORD") && //Pregunta que el cuarto token sea PASSWORD
                       !tokens.get(4).isEmpty() &&  //Se asegura que el quinto campo no esté vacío
                       tokens.get(4).matches("^'.*'$");  //Se asegura la contraseña este entre ' '
                                                                
                            //Puede ser que si se intenta escribir un usuario
                            //y contraseña con espacios el sistema no lo valide correctamente
                } else {                                             
                    return false;
                }
            }
            else if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("TABLE")) {
                if(tokens.size() >= 7){
                    
                    return tokens.get(0).equalsIgnoreCase("MAKE") &&
                           tokens.get(1).equalsIgnoreCase("TABLE") &&
                           tokens.get(2).matches("[a-zA-Z][a-zA-Z0-9_]*") &&
                           tokens.get(3).equals("(") && 
                           tokens.get(tokens.size() - 1).equals(")");
                    
                }else{
                    return false;
                }
            }else {
                System.out.println("ERROR - Sintaxis invalida despues de MAKE");
            }
        }
        else if(tokens.get(0).equalsIgnoreCase("DROP")){
            if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("USER")){
                if(tokens.size() == 3){ //Pregunta si la sentencia tiene 3 tokens (DROP USER <usuario>)
                    return tokens.get(0).equalsIgnoreCase("DROP") && //Pregunta si el primer token es DROP
                           tokens.get(1).equalsIgnoreCase("USER") && //Pregunta si el segundo token es USER
                           !tokens.get(2).isEmpty(); //Se asegura que el token no este vacio 
                } else {
                    return false;
                }

            } else if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("TABLE")){
                return false;
            }
        }else if(tokens.get(0).equalsIgnoreCase("ALTER")){
            if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("USER")){
                if(tokens.size() == 6){ //Pregunta si la sentencia tiene 6 tokens (ALTER USER <usuario> NEW PASSWORD <contraseña>)
            
                    return tokens.get(0).equalsIgnoreCase("ALTER") && //Pregunta si el primer token (palabra) es MAKE
                           tokens.get(1).equalsIgnoreCase("USER") && //Pregunta si el segundo token es USER
                           !tokens.get(2).isEmpty() &&               //Se asegura que el token no esté vacío
                           tokens.get(3).equalsIgnoreCase("NEW")&& //Se asegura que el cuarto token sea NEW
                           tokens.get(4).equalsIgnoreCase("PASSWORD") && //Pregunta que el quinto token sea PASSWORD
                           !tokens.get(5).isEmpty() &&  //Se asegura que el sexto campo no esté vacío
                           tokens.get(5).matches("^'.*'$");  //Se asegura la contraseña este entre ' '}              
            
                }else{
                    return false;
                }
            }else if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("ADD")){
                if(tokens.size()== 6){
                    return !tokens.get(2).isEmpty() &&   //Se asegura que el token no esté vacío
                           tokens.get(3).equalsIgnoreCase(".")&& //Se asegura que el 4° token sea un punto
                           !tokens.get(4).isEmpty() && !tokens.get(5).isEmpty();
                }else{
                    return false;
                }
            }else if(tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("CHANGE")){
                if(tokens.size()== 7){
                    return !tokens.get(2).isEmpty() &&   //Se asegura que el token no esté vacío
                           tokens.get(3).equalsIgnoreCase(".")&& //Se asegura que el 4° token sea un punto
                           !tokens.get(4).isEmpty() && 
                           tokens.get(5).equalsIgnoreCase("TO")&&
                           !tokens.get(6).isEmpty(); 
                }if(tokens.size()== 8){
                    return !tokens.get(2).isEmpty() &&   //Se asegura que el token no esté vacío
                           tokens.get(3).equalsIgnoreCase(".")&& //Se asegura que el 4° token sea un punto
                           !tokens.get(4).isEmpty() && 
                           tokens.get(5).equalsIgnoreCase("TO")&&
                           !tokens.get(6).isEmpty() &&
                           !tokens.get(7).isEmpty();
                }else{
                    return false;
                }
            }
        }else{
            System.out.println("ERROR - Sintaxis inválida");
            System.out.println("");
        }
        return false;
    }
    private boolean validarSemantica(List<String> tokens){
        if (tokens.get(0).equalsIgnoreCase("MAKE")) { //Pregunta si la primera palabra de la sentencia es MAKE
            if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("USER")){
                if(!gestor.usuarioExistente(tokens.get(2))){
                    return true;
                }else{
                    return false;
                }
            }else if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("TABLE")) {
                if(!gestor.tablaExistente(tokens.get(2))){
                    return true;
                }else{
                    return false;
                }
            }else {
                System.out.println("ERROR - Sintaxis invalida despues de MAKE");
            }
        }
        else if(tokens.get(0).equalsIgnoreCase("DROP")){
            if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("USER")){
                
            }else if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("TABLE")){
                return false;
            }else {
                System.out.println("ERROR - Sintaxis invalida despues de DROP");
            }
        }else if(tokens.get(0).equalsIgnoreCase("ALTER")){
            if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("USER")){
                
            }else if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("ADD")){
                return gestor.tablaExistente(tokens.get(2)) && 
                       !gestor.campoExistente(tokens.get(4), "tablas/"+tokens.get(2)+".txt");
                
            }else if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("CHANGE")){
                return gestor.tablaExistente(tokens.get(2)) && 
                       gestor.campoExistente(tokens.get(4), "tablas/"+tokens.get(2)+".txt");
                
            }else {
                System.out.println("ERROR - Sintaxis invalida despues de ALTER");
            }
        }else{
            System.out.println("ERROR - Sintaxis inválida");
            System.out.println("");
        }
        return true;
    }
    
    private String ejecutarConsulta(List<String> tokens){
        if (tokens.get(0).equalsIgnoreCase("MAKE")) { //Pregunta si la primera palabra de la sentencia es MAKE
            if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("USER")){
                
            }else if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("TABLE")) {
                
            }else {
                System.out.println("ERROR - Sintaxis invalida despues de MAKE");
            }
        }else if(tokens.get(0).equalsIgnoreCase("DROP")){
            if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("USER")){
                

            } else if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("TABLE")){
                return "mal";
            } else {
                System.out.println("ERROR - Sintaxis invalida despues de DROP");
            }
        }else if(tokens.get(0).equalsIgnoreCase("ALTER")){
            if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("USER")){
            
            }else if (tokens.size() > 1 && tokens.get(1).equalsIgnoreCase("TABLE")){
                
            } else {
                System.out.println("ERROR - Sintaxis invalida despues de ALTER");
            }
        }else{
            System.out.println("ERROR - Sintaxis inválida");
            System.out.println("");
        }
        return "bien";
    }
}

