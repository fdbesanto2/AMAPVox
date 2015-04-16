/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.lighting;

import java.io.InputStreamReader;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class SceneObjectFactory {
    /*
    public static SceneObject createSceneObjectFromObj(InputStreamReader objFile, InputStreamReader objMaterial, Shader shader){
        
        SceneObject sceneObject = new SceneObject();
        
        Mesh mesh = new Mesh();
        
        ArrayList<Vec3F> vertices = new ArrayList<>();
        Map<Integer, Vec3F> colors = new HashMap<>();
        ArrayList<Short> faces = new ArrayList<>();
        Map<String, Vec3F> materials = new HashMap<>();
        
        try {
            BufferedReader reader = new BufferedReader(objMaterial);
            
            String line;
            String currentMaterial="";
            
            while((line = reader.readLine()) != null){
                
                if(line.startsWith("newmtl ")){
                    
                    String[] material = line.split(" ");
                    currentMaterial = material[1];
                    
                }else if(line.startsWith("Kd ")){
                    String[] diffuse = line.split(" ");
                    materials.put(currentMaterial, new Vec3F(Float.valueOf(diffuse[1]), Float.valueOf(diffuse[2]), Float.valueOf(diffuse[3])));
                }
            }
            
            reader.close();
        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
        
        try {
            BufferedReader reader = new BufferedReader(objFile);
                        
            String line;
            
            Vec3F currentColor = new Vec3F();
            
            while((line = reader.readLine()) != null){
                
                if(line.startsWith("v ")){
                    
                    String[] vertex = line.split(" ");
                    vertices.add(new Vec3F(Float.valueOf(vertex[1]), Float.valueOf(vertex[2]), Float.valueOf(vertex[3])));
                    
                }else if(line.startsWith("f ")){
                    
                    String[] faceSplit = line.split(" ");
                    Vec3i face = new Vec3i(Integer.valueOf(faceSplit[1]), Integer.valueOf(faceSplit[2]), Integer.valueOf(faceSplit[3]));
                    
                    colors.put(face.x-1, currentColor);
                    colors.put(face.y-1, currentColor);
                    colors.put(face.z-1, currentColor);
                    
                    faces.add((short)(face.x-1));
                    faces.add((short)(face.y-1));
                    faces.add((short)(face.z-1));
                    
                }else if(line.startsWith("usemtl ")){
                    currentColor = materials.get(line.split(" ")[1]);
                }
            }
            
            reader.close();
            
        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
        
        
        mesh = MeshFactory.createMesh(vertices, faces);
        
        float colorData[] = new float[vertices.size()*3];
        for(int i=0, j=0;i<vertices.size();i++,j+=3){
            
            colorData[j] = colors.get(i).x;
            colorData[j+1] = colors.get(i).y;
            colorData[j+2] = colors.get(i).z;
            
        }
        
        mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);
        
        return mesh;
        
        return sceneObject;
    }*/
}
