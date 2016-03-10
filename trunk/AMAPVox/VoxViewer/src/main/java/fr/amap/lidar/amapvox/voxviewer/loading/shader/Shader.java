/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.loading.shader;

import com.jogamp.opengl.GL3;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Shader {
    
    private final static Logger LOGGER = Logger.getLogger(Shader.class);
    
    //protected String vertexShaderStreamPath;
    //protected String fragmentShaderStreamPath;
    
    protected String[] vertexShaderCode;
    protected String[] fragmentShaderCode;
    
    protected String[] attributes;
    
    private int vertexShaderId;
    private int fragmentShaderId;
    private int programId;
    
    private final Stack<Uniform> dirtyUniforms = new Stack();
    

    public int getProgramId() {
        return programId;
    }
    
    public Map<String,Integer> attributeMap;
    public Map<String,Integer> uniformMap;
    
    protected GL3 gl;
    
    public Shader(){
        
        attributeMap = new HashMap<>();
        uniformMap = new HashMap<>();
        vertexShaderId=0;
        fragmentShaderId=0;
        programId = -1;
    }
    
    public void init(GL3 m_gl){
        
        this.gl=m_gl;
        
        linkProgram(vertexShaderCode, fragmentShaderCode);
    }

    public String[] getVertexShaderCode() {
        return vertexShaderCode;
    }

    public final void setVertexShaderCode(String[] vertexShaderCode) {
        this.vertexShaderCode = vertexShaderCode;
    }

    public String[] getFragmentShaderCode() {
        return fragmentShaderCode;
    }

    public final void setFragmentShaderCode(String[] fragmentShaderCode) {
        this.fragmentShaderCode = fragmentShaderCode;
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
                LOGGER.error("Fail link program");
            }
            
            extractActiveUniforms();
            extractActiveAttributes();
            
        }else{
            LOGGER.error("Fail compile shaders files");
        }
    }
    
    private void extractActiveUniforms(){
        
        IntBuffer buf = IntBuffer.allocate(1);
        gl.glGetProgramiv(programId, GL3.GL_ACTIVE_UNIFORMS, buf);

        IntBuffer size = IntBuffer.allocate(40);
        IntBuffer length = IntBuffer.allocate(40);
        ByteBuffer nm = ByteBuffer.allocate(256);
        IntBuffer type = IntBuffer.allocate(1);

        for(int i = 0;i< buf.get(0);i++){
            gl.glGetActiveUniform(programId, i, 40, length, size, type, nm);
            String uniformName = new String(ArrayUtils.subarray(nm.array(), 0, length.get(0)));

            uniformMap.put(uniformName, gl.glGetUniformLocation(programId, uniformName));
        }
    }
    
    private void extractActiveAttributes(){
        
        IntBuffer buf = IntBuffer.allocate(1);
        gl.glGetProgramiv(programId, GL3.GL_ACTIVE_ATTRIBUTES, buf);

        IntBuffer size = IntBuffer.allocate(40);
        IntBuffer length = IntBuffer.allocate(40);
        ByteBuffer nm = ByteBuffer.allocate(256);
        IntBuffer type = IntBuffer.allocate(1);

        for(int i = 0;i< buf.get(0);i++){
            gl.glGetActiveAttrib(programId, i, 40, length, size, type, nm);
            String attributeName = new String(ArrayUtils.subarray(nm.array(), 0, length.get(0)));

            attributeMap.put(attributeName, gl.glGetAttribLocation(programId, attributeName));
        }
    }
    
    public static String[] loadCodeFromFile(File file){
        
        BufferedReader reader;
        String line;
        String[] shaderCode = new String[1];
        shaderCode[0]="";
        try {
            
            reader = new BufferedReader(new FileReader(file));
            
        
            while((line = reader.readLine()) != null){
                
                shaderCode[0] += line+"\n";
            }
            
        } catch (IOException ex) {
            LOGGER.error("Error reading shader stream", ex);
        }
        
        return shaderCode;
    }
    
    public static String[] loadCodeFromInputStream(InputStreamReader stream){
        
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
            LOGGER.error("Error reading shader stream", ex);
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
            
            LOGGER.error("Failed compile vertex shader: "+error);
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
            
            LOGGER.error("Failed compile fragment shader: "+error);
        }
        
        return true;
    }
    
    public void bind(){
        
        gl.glUseProgram(programId);
    }
    
    protected static InputStreamReader getStream(String path){
        
        return new InputStreamReader(Shader.class.getClassLoader().getResourceAsStream(path));
    }
    
    public void notifyDirty(Uniform uniform){
        
        dirtyUniforms.addElement(uniform);
    }
    
    public void updateProgram(GL3 gl){
        
        if(!dirtyUniforms.empty()){
            
            gl.glUseProgram(programId);
        
            while(!dirtyUniforms.empty() ){

                Uniform uniform = dirtyUniforms.pop();
                
                uniform.update(gl, uniformMap.get(uniform.getName()));
                /*if(index != null){
                    uniform.update(gl, index);
                }else{
                    System.out.println("test");
                }*/
                
            }

            gl.glUseProgram(0);
        }
    }
}
