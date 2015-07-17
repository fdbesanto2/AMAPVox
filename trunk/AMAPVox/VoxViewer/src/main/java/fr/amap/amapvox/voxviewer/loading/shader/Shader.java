/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.loading.shader;

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
public abstract class Shader {
    
    private final static Logger logger = Logger.getLogger(Shader.class);
    
    public final static String[] MINIMAL_SHADER_UNIFORMS = new String[]{"viewMatrix","projMatrix"};
    public final static String[] LIGHT_SHADER_UNIFORMS = new String[]{"normalMatrix", "Material", "Light"};
    public final static String[] TEXTURE_SHADER_UNIFORMS = new String[]{"texture"};
    
    public final static String[] MINIMAL_SHADER_ATTRIBUTES = new String[]{"position"};
    public final static String[] LIGHT_SHADER_ATTRIBUTES = new String[]{"normal"};
    public final static String[] INSTANCE_SHADER_ATTRIBUTES = new String[]{"instance_position", "instance_color"};
    public final static String[] TEXTURE_SHADER_ATTRIBUTES = new String[]{"textureCoordinates"};
    
    
    private int vertexShaderId;
    private int fragmentShaderId;
    private int programId;
    public boolean isOrtho;
    public String name;

    public int getProgramId() {
        return programId;
    }
    
    public final Map<String,Integer> attributeMap;
    public final Map<String,Integer> uniformMap;
    
    private final GL3 gl;
    
    
    public Shader(GL3 m_gl, String name){
        
        this.name = name;
        vertexShaderId=0;
        fragmentShaderId=0;
        isOrtho = false;
        this.gl=m_gl;
        
        attributeMap = new HashMap<>();
        uniformMap = new HashMap<>();
    }
    
    
    protected void load(String vertexShaderStreamPath, String fragmentShaderStreamPath){
        
        String[] vertexShaderCode = readFromInputStreamReader(getStream(vertexShaderStreamPath));
        String[] fragmenthaderCode = readFromInputStreamReader(getStream(fragmentShaderStreamPath));
        
        linkProgram(vertexShaderCode, fragmenthaderCode);
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
                logger.error("Fail link program: "+this.name);
            }
            
        }else{
            logger.error("Fail compile shaders files");
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
            logger.error("Error reading shader stream", ex);
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
        }
        
        return true;
    }
    
    public void bind(){
        
        gl.glUseProgram(programId);
    }
    
    public void setAttributeLocations(String... attributeList){
                
        for (String attribute : attributeList) {
            attributeMap.put(attribute, gl.glGetAttribLocation(programId, attribute));
        }
    }
    
    public void setUniformLocations(String... uniformList){
        
        for (String uniform : uniformList) {
            uniformMap.put(uniform,gl.glGetUniformLocation(programId, uniform));
        }
    }
    
    public static String[] composeShaderUniforms(String[]... uniformsArrays){
        
        int count = 0;
        
        for (String[] uniformsArray : uniformsArrays) {
            count += uniformsArray.length;
        }
        
        String[] globalUniformArray = new String[count];
        
        int count2 = 0;
        for (int i=0;i< uniformsArrays.length;i++) {
            for (int j = 0; j<uniformsArrays[i].length; j++) {
                globalUniformArray[count2] = uniformsArrays[i][j];
                count2++;
            }
        }
        
        return globalUniformArray;
    }
    
    public static String[] composeShaderAttributes(String[]... attributesArrays){
        
        int count = 0;
        
        for (String[] attributesArray : attributesArrays) {
            count += attributesArray.length;
        }
        
        String[] globalAttributeArray = new String[count];
        
        int count2 = 0;
        for (int i=0;i< attributesArrays.length;i++) {
            for (int j = 0; j<attributesArrays[i].length; j++) {
                globalAttributeArray[count2] = attributesArrays[i][j];
                count2++;
            }
        }
        
        return globalAttributeArray;
    }
    
    private InputStreamReader getStream(String path){
        
        return new InputStreamReader(Shader.class.getClassLoader().getResourceAsStream(path));
    }
}
