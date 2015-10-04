/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.io.stream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class FluxViewer implements Runnable{
    
    private final InputStream inputStream;
    
    private BufferedReader getBufferedReader(InputStream is) {
        return new BufferedReader(new InputStreamReader(is));
    }
    
    public FluxViewer(InputStream inputStream) {
        this.inputStream = inputStream;
    }
    
    @Override
    public void run() {
        
        BufferedReader br = getBufferedReader(inputStream);
        String ligne = "";
        try {
            
            while ((ligne = br.readLine()) != null) {
                
                System.out.println(ligne);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
    
}
