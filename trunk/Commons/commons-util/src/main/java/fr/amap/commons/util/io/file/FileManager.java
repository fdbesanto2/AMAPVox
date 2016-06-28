/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util.io.file;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
    
    public static void skipLines(BufferedReader reader, int number) throws IOException{
        try {
            for(int i=0;i<number;i++){
                reader.readLine();
            }
        } catch (IOException ex) {
            throw ex;
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
    
    public static int getLineNumber(InputStreamReader stream) throws IOException{
        
        int count = 0;
        
        try{

            BufferedReader reader = new BufferedReader(stream);
                    
            while ((reader.readLine()) != null) {
                count++;
            }
            
            //reader.close();
            
            return count;
            

        } catch (IOException ex) {
            throw new IOException("Cannot read file", ex);
        }
    }
    
    public static int getLineNumber(File file) throws IOException{
        
        return getLineNumber(file.getAbsolutePath());
    }
    
    public static int getLineNumber(String path) throws IOException{
        
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
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
    }
    
    public static String readHeader(String path){
        
        try {
            
            BufferedReader reader = new BufferedReader(new FileReader(new File(path)));
            String line = reader.readLine();
            reader.close();
            
            return line;
            
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }
    
    public static String readSpecificLine(String path, int lineNumber) throws IOException{
        
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
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
    }
    
    public ArrayList<String> readAllLines(File file) throws FileNotFoundException, IOException{
        
        ArrayList<String> lines = new ArrayList<>();
        
        try {

            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                
                String line;
                
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }
            
            setFileRead(true);
            
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
        
        return lines;
    }
    
    public static String getExtension(File file){
        
        String extension = file.getName().substring(file.getName().lastIndexOf("."), file.getName().length());
        
        return extension;
    }
    
    
    
}
