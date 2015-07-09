/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxelisation;

import fr.amap.amapvox.commons.configuration.Configuration;
import fr.amap.amapvox.commons.util.CommandLineParser;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.JDOMException;

/**
 *
 * @author calcul
 */
public class Voxelizer {
    
    private static void usage(){
        
        System.out.println("Utilisation : java -jar Voxelizer.jar [PARAMETRES]\n");
        System.out.println("Parametre\tDescription\n");
        System.out.println("--help\tAffiche ce message");
        System.out.println("--input=<Fichier de configuration>\tFichier de configuration de type XML");
        System.out.println("--cores=<Nombre de coeurs>\tNombre de coeurs processeur à utiliser\n"
                            + "\t\t (0 pour le maximum)");
        
    }
    
    public static void main(String[] args) {
        
        CommandLineParser parser = new CommandLineParser(args);
        Map<String, String> named = parser.getNamed();
        List<String> raw = parser.getRaw();
        
        if(raw.size() <= 1){
            
            if(raw.isEmpty()){
                usage();
                System.exit(0);
            }else if(raw.size() == 1 && raw.get(0).equals("help")){
                usage();
                System.exit(0);
            }else{
                System.out.println("Unknow command");
                System.exit(-1);
            }
        }
        
        if(named.containsKey("input")){
            
            File cfgFile = new File(named.get("input"));
            try {
                Configuration.readType(cfgFile);
            } catch (JDOMException ex) {
                Logger.getLogger(Voxelizer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Voxelizer.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
    }
    
}
