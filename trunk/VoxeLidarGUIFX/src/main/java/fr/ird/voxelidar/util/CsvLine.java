/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

/**
 * Class used to add line with separator
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class CsvLine {
    
    private final StringBuilder line;
    private final String separator;
    
    public CsvLine(String separator){
        
        this.separator = separator;
        this.line = new StringBuilder();
    }
    
    public void add(String element){
        line.append(element).append(separator);
    }
    
    public String getLine(){
        return line.toString().trim()+"\n";
    }
    
    
}
