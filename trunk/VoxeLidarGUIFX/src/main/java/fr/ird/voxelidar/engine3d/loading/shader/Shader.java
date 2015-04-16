/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.loading.shader;

import com.jogamp.opengl.GL3;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Shader {
    
    final static Logger logger = Logger.getLogger(Shader.class);
    
    private int vertexShaderId;
    private int fragmentShaderId;
    private int programId;
    public boolean isOrtho;
    public String name;

    public int getProgramId() {
        return programId;
    }
    
    public Map<String,Integer> attributeMap;
    public Map<String,Integer> uniformMap;
    
    private final GL3 gl;
    
    public Shader(GL3 m_gl, InputStreamReader fragmentShaderStream, InputStreamReader vertexShaderStream, String name){
        
        this.name = name;
        vertexShaderId=0;
        fragmentShaderId=0;
        isOrtho = false;
        this.gl=m_gl;
        
        String[] vertexShaderCode = readFromInputStreamReader(vertexShaderStream);
        String[] fragmenthaderCode = readFromInputStreamReader(fragmentShaderStream);
        
        linkProgram(vertexShaderCode, fragmenthaderCode);
        
    }
    
    public Shader(GL3 m_gl, String[] vertexShaderCode, String[] fragmentShaderCode, String name){
        
        this.name = name;
        vertexShaderId=0;
        fragmentShaderId=0;
        isOrtho = false;
        this.gl=m_gl;
        
        linkProgram(vertexShaderCode, fragmentShaderCode);
    }
    
    private void linkProgram(String[] vertexShaderCode, String[] fragmentShaderCode){
        
        if(this.compile(vertexShaderCode, fragmentShaderCode)){
            
            programId = gl.glCreateProgram();
            gl.glAttachShader(programId, vertexShaderId);
            gl.glAttachShader(programId, fragmentShaderId);
            gl.glLinkProgram(programId);
            
            int[] params = new int[]{0};
            gl.glGetProgramiv(programId, GL3.GL_LINK_STATUS, params, 0);
            
            if(params[0] == GL3.GL_FALSE){
                logger.error("Fail link program");
            }
            
        }else{
            logger.error("Fail reading shaders files");
        }
    }
    
    private String[] readFromInputStreamReader(InputStreamReader stream){
        
        BufferedReader reader;
        String line;
        String[] shaderCode = new String[1];
        shaderCode[0]="";
        try {
            
            reader = new BufferedReader(stream);
            
        
            while((line = reader.readLine()) != null){
                
                shaderCode[0] += line+"\n";
            }
            
        } catch (IOException ex) {
            logger.error("vertex shader reading error not found", ex);
        }
        
        return shaderCode;
    }
    
    private boolean compile(String[] vertexShaderCode, String[] fragmentShaderCode){
        
        /*****vertex shader*****/
        
        vertexShaderId = gl.glCreateShader(GL3.GL_VERTEX_SHADER);
        
        if(vertexShaderId == 0)
        {   
            return false;
        }
        
        
        gl.glShaderSource(vertexShaderId, 1, vertexShaderCode, null);
        
        
        gl.glCompileShader(vertexShaderId);
        
        
        //check for error
        int[] params = new int[]{0};
        gl.glGetShaderiv(vertexShaderId, GL3.GL_COMPILE_STATUS, params, 0);
        if(params[0] == GL3.GL_FALSE){
            
            byte[] infoLog = new byte[1024];
            gl.glGetShaderInfoLog(vertexShaderId, 1024, null, 0, infoLog, 0);
            String error=new String(infoLog);
            
            logger.error("Failed compile vertex shader: "+error);
            
            return false;
        }
        
        /*****fragment shader*****/
        
        fragmentShaderId = gl.glCreateShader(GL3.GL_FRAGMENT_SHADER);
        
        if(fragmentShaderId == 0)
        {   
            return false;
        }
        
        gl.glShaderSource(fragmentShaderId, 1, fragmentShaderCode, null);
        gl.glCompileShader(fragmentShaderId);
        
        //check for error
        params = new int[]{0};
        gl.glGetShaderiv(fragmentShaderId, GL3.GL_COMPILE_STATUS, params, 0);
        
        if(params[0] == GL3.GL_FALSE){
            
            byte[] infoLog = new byte[1024];
            gl.glGetShaderInfoLog(fragmentShaderId, 1024, null, 0, infoLog, 0);
            String error=new String(infoLog);
            
            logger.error("Failed compile fragment shader: "+error);
            
            return false;
        }
        
        return true;
    }
    
    public void bind(){
        
        gl.glUseProgram(programId);
    }
    
    public void setAttributeLocations(String[] attributeList){
                
        attributeMap = new HashMap<>();
        
        for (String attribute : attributeList) {
            attributeMap.put(attribute, gl.glGetAttribLocation(programId, attribute));
        }
    }
    
    public void setUniformLocations(String[] uniformList){
        
        uniformMap = new HashMap<>();
        
        for (String uniform : uniformList) {
            uniformMap.put(uniform,gl.glGetUniformLocation(programId, uniform));
        }
    }
}
