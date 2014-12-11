/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.io.file;

import fr.ird.voxelidar.frame.JFrameSettingUp;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;



public class FileManager {
    
    private String path;
    private File file;
    private boolean fileRead;
    private final EventListenerList listeners;

    public void setFileRead(boolean fileRead) {
        this.fileRead = fileRead;
        
        if(fileRead){
            
            fireFileRead();
        }
    }
    
    private void fireFileRead() {
        
        for(FileListener listener :listeners.getListeners(FileListener.class)){
            
            listener.fileRead();
        }
    }
    
    
    
    public void addFileListener(FileListener listener) {
        listeners.add(FileListener.class, listener);
    }
    
    protected void fileLoadedChanged() {
        for(FileListener listener : listeners.getListeners(FileListener.class)) {
            listener.fileRead();
        }
    }
    
    public FileManager(){
        
        listeners = new EventListenerList();
        fileRead = false;
    }
    
    
    public static int getLineNumber(String path){
        
        int count = 0;
        InputStream is;
        try {
            is = new BufferedInputStream(new FileInputStream(path));
            
            byte[] c = new byte[1024];

            int readChars = 0;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
            }
            
            return count;
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        return -1;
    }
    
    public static String readHeader(String path){
        
        try {
            
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
            
            return reader.readLine();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public static String readSpecificLine(String path, int lineNumber){
        
        try {
            
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
            int count = 0;
            String line = null;
            
            
            do{
                
                line = reader.readLine();
                count++;
            }while(count != lineNumber);
            
            return line;
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public ArrayList<String> readAllLines(String path){
        
        ArrayList<String> lines = new ArrayList<>();
        
        File file = new File(path);
        
        try {

            /******read file*****/

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();

            while (line != null) {

                line = reader.readLine();
                lines.add(line);
            }

            reader.close();
            
            setFileRead(true);
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JFrameSettingUp.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JFrameSettingUp.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return lines;
    }

    
    
    
}
