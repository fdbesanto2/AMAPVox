/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

/**
 *
 * @author Julien
 */
public class Filter {
    
    public final static int NOT_EQUAL = 0;
    public final static int EQUAL = 1;
    public final static int LESS_THAN = 2;
    public final static int LESS_THAN_OR_EQUAL = 3;
    public final static int GREATER_THAN = 4;
    public final static int GREATER_THAN_OR_EQUAL = 5;
    
    private final String variable;
    private final double value;
    private final int condition;
    
    public Filter(String variable, double value, int condition) {
        this.variable = variable;
        this.value = value;
        this.condition = condition;
    }

    public String getVariable() {
        return variable;
    }

    public double getValue() {
        return value;
    }

    public int getCondition() {
        return condition;
    }
    
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
    
    public static Filter getFilterFromString(String filterString){
        
        String[] split = filterString.split(" ");
        
        if(split.length != 3){
            return null;
        }
        
        Filter filter = new Filter(split[0], Double.valueOf(split[2]), getConditionFromString(split[1]));
        
        return filter;
    }
    
    
}
