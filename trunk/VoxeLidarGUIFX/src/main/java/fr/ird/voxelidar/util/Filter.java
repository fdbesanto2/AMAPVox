/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

import java.io.Serializable;

/**
 * This class represent a filter and can is compound of a variable,
 * a condition and a value
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Filter implements Serializable{
    
    public final static int NOT_EQUAL = 0;

    public final static int EQUAL = 1;
    
    public final static int LESS_THAN = 2;
    
    public final static int LESS_THAN_OR_EQUAL = 3;
    
    public final static int GREATER_THAN = 4;

    public final static int GREATER_THAN_OR_EQUAL = 5;
    
    private final String variable;
    private final float value;
    private final int condition;
    
    /**
     *
     * @param variable The variable name
     * @param value The value to test
     * @param condition The condition to compare the variable to value
     */
    public Filter(String variable, float value, int condition) {
        this.variable = variable;
        this.value = value;
        this.condition = condition;
    }

    /**
     *
     * @return
     */
    public String getVariable() {
        return variable;
    }

    /**
     *
     * @return
     */
    public double getValue() {
        return value;
    }

    /**
     *
     * @return
     */
    public int getCondition() {
        return condition;
    }
    
    /**
     *
     * @return the condition as a string
     */
    public String getConditionString(){
        
        switch(condition){
            
            case Filter.EQUAL:
                return "==";
            case Filter.NOT_EQUAL:
                return "!=";
            case Filter.LESS_THAN:
                return "<";
            case Filter.LESS_THAN_OR_EQUAL:
                return "<=";
            case Filter.GREATER_THAN:
                return ">";
            case Filter.GREATER_THAN_OR_EQUAL:
                return ">=";
            default:
                return "";
        }
    }
    
    public boolean doFilter(float value){
        
        switch(condition){
            case Filter.EQUAL:
                if(Float.isNaN(this.value)){
                    if(Float.isNaN(value))return true;
                }else{
                    if(value == this.value)return true;
                }
                break;
            case Filter.GREATER_THAN:
                if(value > this.value)return true;
                break;
            case Filter.GREATER_THAN_OR_EQUAL:
                if(value >= this.value)return true;
                break;
            case Filter.LESS_THAN:
                if(value < this.value)return true;
                break;
            case Filter.LESS_THAN_OR_EQUAL:
                if(value <= this.value)return true;
                break;
            case Filter.NOT_EQUAL:
                if(Float.isNaN(this.value)){
                    if(!Float.isNaN(value))return true;
                }else{
                    if(value != this.value)return true;
                }
                
                break;
        }
        
        return false;
    }
    
    /**
     *
     * @param condition the condition as a string 
     * Possible values ("!=" , "==" , "&lt;", "&gt;" "&gt;=", "&lt;=")
     * @return the integer value of the condition
     */
    public static int getConditionFromString(String condition){
        
        switch(condition){
            
            case "==":
                return Filter.EQUAL;
            case "!=":
                return Filter.NOT_EQUAL;
            case "<":
                return Filter.LESS_THAN;
            case "<=":
                return Filter.LESS_THAN_OR_EQUAL;
            case ">":
                return Filter.GREATER_THAN;
            case ">=":
                return Filter.GREATER_THAN_OR_EQUAL;
            default:
                return -1;
        }
    }
    
    /**
     *
     * @param filterString string to be convert to Filter object <br>
     * It is represented for example like this: myVariable > 3
     * @return
     */
    public static Filter getFilterFromString(String filterString){
        
        String[] split = filterString.split(" ");
        
        if(split.length != 3){
            return null;
        }
        
        Filter filter = new Filter(split[0], Float.valueOf(split[2]), getConditionFromString(split[1]));
        
        return filter;
    }
    
    @Override
    public String toString(){
        return variable+"\t"+getConditionString()+"\t"+value;
    }
    
}
