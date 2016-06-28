/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util.script;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import java.io.File;
import java.io.IOException;
import org.codehaus.groovy.control.CompilationFailedException;

/**
 *
 * @author Julien Heurtebize
 */
public class GroovyScriptExecutor {

    private final GroovyShell shell = new GroovyShell();
    private final Script script;
    
    public GroovyScriptExecutor(File scriptFile, Binding binding) throws CompilationFailedException, IOException {
        
        script = shell.parse(scriptFile);
        //shell.getClassLoader().
        if(binding == null){
            binding = new Binding();
        }
        
        binding.setVariable("groovyScriptExecutor", this);
        script.setBinding(binding);
    }
    
    public Object execute(){
        Object result = script.run();
        return result;
    }
}
