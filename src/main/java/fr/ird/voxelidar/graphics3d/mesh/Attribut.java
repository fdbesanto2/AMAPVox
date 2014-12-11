/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.mesh;

import java.util.Set;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

/**
 *
 * @author Julien
 */
public class Attribut {
    
    private final Expression expression;
    private String name;
    private String expressionString;
    private Set<String> variablesNames;
    
    public Attribut(String name, String expression, Set<String> variablesNames){
        
        this.expressionString = expression;
        this.name = name;
        this.variablesNames = variablesNames;
        this.expression = new ExpressionBuilder(expressionString).variables(variablesNames).build();
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
