/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.loading.shader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import javax.media.opengl.GL3;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
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
    
    private final InputStreamReader fragmentShaderFilename;
    private final InputStreamReader vertexShaderFileName;
    public Map<String,Integer> attributeMap;
    public Map<String,Integer> uniformMap;
    
    private final GL3 gl;
    
    public Shader(GL3 m_gl, InputStreamReader fragmentShaderFilename, InputStreamReader vertexShaderFileName, String name){
        
        this.name = name;
        vertexShaderId=0;
        fragmentShaderId=0;
        isOrtho = false;
        this.gl=m_gl;
        this.fragmentShaderFilename = fragmentShaderFilename;
        this.vertexShaderFileName = vertexShaderFileName;
        
        if(this.load()){
            
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
    
    
    
    private boolean load(){
        
        /*****vertex shader*****/
        
        vertexShaderId = gl.glCreateShader(GL3.GL_VERTEX_SHADER);
        
        if(vertexShaderId == 0)
        {   
            return false;
        }
        
        BufferedReader reader;
        String line;
        String[] vertexShaderCode = new String[1];
        vertexShaderCode[0]="";
        try {
            
            reader = new BufferedReader(vertexShaderFileName);
            
        
            while((line = reader.readLine()) != null){
                
                vertexShaderCode[0] += line+"\n";
            }
            
        } catch (IOException ex) {
            logger.error("vertex shader "+vertexShaderFileName+" not found", ex);
            
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
        

        String[] fragmentShaderCode = new String[1];
        fragmentShaderCode[0]="";
        
        try {
            
            reader = new BufferedReader(fragmentShaderFilename);
            
        
            while((line = reader.readLine()) != null){
                
                fragmentShaderCode[0] += line+"\n";
            }
            
        } catch (IOException ex) {
            logger.error("fragment shader "+fragmentShaderFilename+" not found", ex);
            
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
