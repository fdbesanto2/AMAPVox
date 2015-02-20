/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.mesh;

import java.util.Set;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 *
 * @author Julien
 */
public class Attribut {
    
    private String name;
    private String expressionString;
    private Expression expression;
    
    public Attribut(String name, String expression, Set<String> variablesNames){
        
        this.expressionString = expression;
        this.name = name;
        this.expression = new ExpressionBuilder(expression).variables(variablesNames).build();
        //this.expression = new ExpressionBuilder(expressionString).variables(variablesNames).build();
    }

    public Expression getExpression() {
        return expression;
    }

    public String getName() {
        return name;
    }

    public String getExpressionString() {
        return expressionString;
    }
    
    
}
