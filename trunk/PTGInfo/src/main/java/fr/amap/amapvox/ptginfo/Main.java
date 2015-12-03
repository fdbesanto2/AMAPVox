/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.ptginfo;

import fr.amap.amapvox.jleica.ptg.PTGReader;
import fr.amap.amapvox.jleica.ptg.PTGScan;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author calcul
 */
public class Main {
    
    public static void main(String[] args) {
        
        if(args.length == 2){
            
            String argName = args[0];
            String argValue = args[1];
            
            if(argName.equals("--input")){
                File f = new File(argValue);
                PTGReader reader = new PTGReader();
                try {
                    reader.openPTGFile(f);
                    
                    if(reader.isAsciiFile()){
                        List<File> scanList = reader.getScanList();
                        
                        for(File file : scanList){
                            PTGScan scan = new PTGScan();
                            
                            try {
                                scan.openScanFile(file);
                                System.out.println("File : "+file.getName()+"\n");
                                System.out.println(scan.getHeader().toString());
                                System.out.println("\n");
                            } catch (Exception ex) {
                                System.err.println(ex);
                            }
                        }
                    }else{
                        
                        PTGScan scan = new PTGScan();
                        try {
                            scan.openScanFile(f);
                            System.out.println("File : "+f.getName()+"\n");
                            System.out.println(scan.getHeader().toString());
                        } catch (Exception ex) {
                            System.err.println(ex);
                        }
                    }
                    
                } catch (IOException ex) {
                    System.err.println(ex);
                }
            }
        }
        
    }
}
