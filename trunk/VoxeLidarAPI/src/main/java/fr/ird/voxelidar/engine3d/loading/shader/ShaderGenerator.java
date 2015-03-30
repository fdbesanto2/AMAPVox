/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.loading.shader;

import com.jogamp.opengl.GL3;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author calcul
 */
public class ShaderGenerator {
    
    public class ShaderData{
        public String type;
        public String name;

        public ShaderData(String type, String name) {
            this.type = type;
            this.name = name;
        }
        
    }
    
    public enum Flag{
        
        INSTANCED,
        TEXTURED,
        COLORED;
    }
    
    public static final int INSTANCED = 1;
    public static final int TEXTURED  = 2;
    public static final int COLORED = 3;
    
    public String[] vertexShaderCode;
    public String[] fragmentShaderCode;
    
    public Set<ShaderData> vertexShaderInputs;
    public Set<ShaderData> vertexShaderOutputs;
    public Set<ShaderData> vertexShaderUniforms;
    
    public Set<ShaderData> fragmentShaderInputs;
    public Set<ShaderData> fragmentShaderOutputs;
    public Set<ShaderData> fragmentShaderUniforms;
    
    private EnumSet<Flag>  flags;
    
    public ShaderGenerator(){
        
        vertexShaderInputs = new HashSet<>();
        vertexShaderOutputs = new HashSet<>();
        vertexShaderUniforms = new HashSet<>();
        
        fragmentShaderInputs = new HashSet<>();
        fragmentShaderOutputs = new HashSet<>();
        fragmentShaderUniforms = new HashSet<>();
    }
    
    public Shader generateShader(final GL3 gl, EnumSet<Flag> flags, String name){
        
        vertexShaderInputs = new HashSet<>();
        vertexShaderOutputs = new HashSet<>();
        vertexShaderUniforms = new HashSet<>();
        
        fragmentShaderInputs = new HashSet<>();
        fragmentShaderOutputs = new HashSet<>();
        fragmentShaderUniforms = new HashSet<>();
        
        this.flags = flags;
        generateVertexShader();
        generateFragmentShader();
        
        Shader shader = new Shader(gl, vertexShaderCode, fragmentShaderCode, name);
        
        shader.setUniformLocations(getUniformArray());
        shader.setAttributeLocations(getAttributesArray());
        
        return shader;
    }
    
    public String[] getUniformArray(){
        
        Set<String> uniformSet = new TreeSet<>();
        
        Iterator<ShaderData> it = vertexShaderUniforms.iterator();
        while(it.hasNext()){
            uniformSet.add(it.next().name);
        }
        
        it = fragmentShaderUniforms.iterator();
        while(it.hasNext()){
            uniformSet.add(it.next().name);
        }
        
        return (String[]) uniformSet.toArray(new String[uniformSet.size()]);
    }
    
    public String[] getAttributesArray(){
        
        Set<String> attributeSet = new TreeSet<>();
        
        Iterator<ShaderData> it = vertexShaderInputs.iterator();
        while(it.hasNext()){
            attributeSet.add(it.next().name);
        }
        
        return (String[]) attributeSet.toArray(new String[attributeSet.size()]);
    }
    
    private void generateVertexShader(){
        
        
        StringBuilder vertexShaderBuilder = new StringBuilder();
        
        //common
        vertexShaderBuilder
                    .append("#version 140\n")
                    .append("uniform mat4 viewMatrix, projMatrix;\n")
                    .append("in vec4 position;\n");
        
        vertexShaderUniforms.add(new ShaderData("mat4","viewMatrix"));
        vertexShaderUniforms.add(new ShaderData("mat4","projMatrix"));
        vertexShaderInputs.add(new ShaderData("vec4","position"));
        
        if (flags.contains(Flag.TEXTURED)){
            
            vertexShaderBuilder
                    .append("in vec2 textureCoordinates;\n")
                    .append("out vec2 texCoordinates;\n");
            
            vertexShaderInputs.add(new ShaderData("vec2","textureCoordinates"));
            vertexShaderOutputs.add(new ShaderData("vec2","texCoordinates"));
            
        }else{
            
            vertexShaderBuilder.append("in vec4 color;\n");
            vertexShaderBuilder.append("out vec4 Color;\n");
            
            vertexShaderInputs.add(new ShaderData("vec4","color"));
            vertexShaderOutputs.add(new ShaderData("vec4","Color"));
        }
        
       if (flags.contains(Flag.INSTANCED)){
            
            vertexShaderBuilder
                    .append("in vec3 instance_position;\n")
                    .append("in vec4 instance_color;\n");
            
            vertexShaderInputs.add(new ShaderData("vec3","instance_position"));
            vertexShaderInputs.add(new ShaderData("vec4","instance_color"));
        }
        
        vertexShaderBuilder
                    .append("void main()\n")
                    .append("{\n");
        
        if(flags.contains(Flag.TEXTURED)){
            vertexShaderBuilder.append("texCoordinates = textureCoordinates;\n");
        }else if(flags.contains(Flag.INSTANCED)){
            vertexShaderBuilder.append("\tColor = instance_color;\n");
        }else{
            vertexShaderBuilder.append("\tColor = color;\n");
        }
        
        vertexShaderBuilder.append("\tgl_Position = projMatrix * viewMatrix  * (position");
        vertexShaderBuilder.append(flags.contains(Flag.INSTANCED) ? "+vec4(instance_position,0.0)" : "");
        
        vertexShaderBuilder.append(");\n");
        vertexShaderBuilder.append("}\n");
        
        vertexShaderCode = vertexShaderBuilder.toString().split("\n", 1);
    }
    
    private void generateFragmentShader(){
        
        StringBuilder fragmentShaderBuilder = new StringBuilder();
        
        //common
        fragmentShaderBuilder.append("#version 140\n");
        
        if(flags.contains(Flag.TEXTURED)){
            fragmentShaderBuilder.append("in vec2 texCoordinates;\n");
            fragmentShaderInputs.add(new ShaderData("vec4","texCoordinates"));
            
            fragmentShaderBuilder.append("uniform sampler2D texture;\n");
            fragmentShaderUniforms.add(new ShaderData("sampler2D","texture"));
            
        }else{
            fragmentShaderBuilder.append("in vec4 Color;\n");
            fragmentShaderInputs.add(new ShaderData("vec4","Color"));
        }
        
        fragmentShaderBuilder.append("out vec4 outColor;\n");
        fragmentShaderOutputs.add(new ShaderData("vec4","outColor"));

        
        fragmentShaderUniforms.add(new ShaderData("mat4","viewMatrix"));
        fragmentShaderUniforms.add(new ShaderData("mat4","projMatrix"));
        fragmentShaderInputs.add(new ShaderData("vec4","position"));
        
        fragmentShaderBuilder
                    .append("void main()\n")
                    .append("{\n");
        
        if(flags.contains(Flag.TEXTURED)){
            fragmentShaderBuilder.append("outColor = texture2D(texture, texCoordinates);\n");
        }else{
            fragmentShaderBuilder
                    .append("if(Color.a == 0){\n")
                    .append("\tdiscard;\n")
                    .append("}\n");
            
            fragmentShaderBuilder.append("\toutColor = Color;\n");
        }
        
        fragmentShaderBuilder.append("}");
        
        fragmentShaderCode = fragmentShaderBuilder.toString().split("\n", 1);
    }
}
