/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.frame;

import java.util.ArrayList;
import javax.swing.DefaultListModel;

/**
 *
 * @author Julien
 */
public class FilterDefaultListModel extends DefaultListModel<String>{
    
    private ArrayList<String> fullList;
    
    public FilterDefaultListModel(){
        super();
        
        fullList = new ArrayList<>();
    }
    
    public void doFilter(String containedString){
        
        if(fullList.size() == 0){
            
            for(int i=0;i<size();i++){
                fullList.add(get(i));
            }
        }
        
        clear();
        
        for(String item : fullList){
            
            if(containedString == null){
                addElement(item);
                
            }else if(containedString.isEmpty()){
                addElement(item);
                
            }else if(item.contains(containedString)){
                
                addElement(item);
            }
        }
    }
    
    public void doInverseFilter(String containedString){
        
        if(fullList.size() == 0){
            
            for(int i=0;i<size();i++){
                fullList.add(get(i));
            }
        }
        
        clear();
        
        for(String item : fullList){
            
            if(containedString == null){
                addElement(item);
                
            }else if(containedString.isEmpty()){
                addElement(item);
                
            }else if(!item.contains(containedString)){
                
                addElement(item);
            }
        }
    }
}
