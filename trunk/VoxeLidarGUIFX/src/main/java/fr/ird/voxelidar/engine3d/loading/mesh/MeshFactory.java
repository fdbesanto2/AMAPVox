/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.loading.mesh;

import com.jogamp.common.nio.Buffers;
import fr.ird.voxelidar.engine3d.loading.texture.Texture;
import fr.ird.voxelidar.engine3d.object.mesh.Grid;
import fr.ird.voxelidar.engine3d.object.mesh.Mesh;
import fr.ird.voxelidar.engine3d.math.vector.Vec3F;
import fr.ird.voxelidar.engine3d.math.vector.Vec3i;
import fr.ird.voxelidar.engine3d.object.mesh.TexturedMesh;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class MeshFactory {
    
    private final static Logger logger = Logger.getLogger(MeshFactory.class);
    
    public static TexturedMesh createTexturedCube(float size){
        
        Mesh mesh = createCube(size);
        TexturedMesh texturedMesh = new TexturedMesh(mesh);
        
        float textCoordData[] = new float[]
        {0, 1,
         1, 1,
         0, 0,
         1, 0};
        
        texturedMesh.textureCoordinatesBuffer = Buffers.newDirectFloatBuffer(textCoordData);
        
        return texturedMesh;
    }
    public static Mesh createCube(float size){
        
        float vertexData[] = new float[]
        {size/2.0f, -size/2.0f, -size/2.0f,
        size/2.0f, -size/2.0f, size/2.0f,
        -size/2.0f, -size/2.0f, size/2.0f,
        -size/2.0f, -size/2.0f, -size/2.0f,
        size/2.0f, size/2.0f, -size/2.0f,
        size/2.0f, size/2.0f, size/2.0f,
        -size/2.0f, size/2.0f, size/2.0f,
        -size/2.0f, size/2.0f, -size/2.0f};
        /*
        float aoData[] = new float[]
            {1, 0, 0, 0,
            1, -0.649359f, 0.680704f,0.339080f,
            1, -0.384346f,-0.401203f,-0.831453f,
            0.750000f,-0.888967f,0.025329f,0.457271f,
            0.500000f,0.016968f,0.453997f,-0.890842f,
            0.500000f,0.605377f,-0.568562f,-0.557006f,
            0.400000f,0.999899f,-0.009149f,0.010885f,
            0.750000f, 0.602468f, -0.559680f, 0.569026f };
        */
        short indexData[] = new short[]
        {0, 1, 2,
        7, 6, 5,
        4, 5, 1,
        5, 6, 2,
        2, 6, 7,
        4, 0, 3,
        3, 0, 2,
        4, 7, 5,
        0, 4, 1,
        1, 5, 2,
        3, 2, 7,
        7, 4, 3};
        
        Mesh cube = new Mesh();
        //cube.aoBuffer =  Buffers.newDirectFloatBuffer(aoData);
        cube.vertexBuffer = Buffers.newDirectFloatBuffer(vertexData);
        cube.indexBuffer = Buffers.newDirectShortBuffer(indexData);
        cube.vertexCount = indexData.length;
        
        
        return cube;
    }
    
    public static Mesh createPlane(Vec3F startPoint, int width , int height){
        
        
        
        float vertexData[] = new float[]
        {startPoint.x, startPoint.y, startPoint.z,
         startPoint.x+width, startPoint.y, 0,
         startPoint.x, startPoint.y+height, 0,
         startPoint.x+width, startPoint.y+height, 0};
        
        float textCoordData[] = new float[]
        {0, 1,
         1, 1,
         0, 0,
         1, 0};
        
        short indexData[] = new short[]
        {0, 1, 3,
        2, 0, 3};
        
        TexturedMesh plane = new TexturedMesh();
        /*
        MeshBuffer meshBuffer= new MeshBuffer();
        meshBuffer.setBuffer(MeshBuffer.VERTEX_BUFFER, Buffers.newDirectFloatBuffer(vertexData));
        meshBuffer.setBuffer(MeshBuffer.TEXTURE_COORDINATES_BUFFER, Buffers.newDirectFloatBuffer(textCoordData));
        meshBuffer.setBuffer(MeshBuffer.INDEX_BUFFER, Buffers.newDirectShortBuffer(indexData));
        plane.meshBuffer = meshBuffer;
        */
        plane.vertexBuffer = Buffers.newDirectFloatBuffer(vertexData);
        plane.textureCoordinatesBuffer = Buffers.newDirectFloatBuffer(textCoordData);
        plane.indexBuffer = Buffers.newDirectShortBuffer(indexData);
        plane.vertexCount = indexData.length;
        
        return plane;
    }
    
    public static Grid createGrid(float resolution, int size, float ground){
        
                
        int pointsNumber = (int) (size / resolution);
        float pointsArray[] = new float[12*pointsNumber];
        short indexArray[] = new short[12*pointsNumber];
        int pointsArrayIndex = 0, indexArrayIndex = 0;
        
        /**grid for x axis**/
        for(int i=0;i<pointsNumber;i++){
            
            /**first point**/
            //x
            pointsArray[pointsArrayIndex] = (i+resolution)-(size/2);
            pointsArrayIndex++;
            //z
            pointsArray[pointsArrayIndex] = ground;
            pointsArrayIndex++;
            //y
            pointsArray[pointsArrayIndex] = (float)-size/2;
            pointsArrayIndex++;
            
            
            indexArray[indexArrayIndex] = (short)(pointsArrayIndex-1);
            indexArrayIndex++;
            
            /**second point**/
            //x
            pointsArray[pointsArrayIndex] = (i+resolution)-(size/2);
            pointsArrayIndex++;
            //z
            pointsArray[pointsArrayIndex] = ground;
            pointsArrayIndex++;
            //y
            pointsArray[pointsArrayIndex] = (float)size/2;
            pointsArrayIndex++;
            
            
            indexArray[indexArrayIndex] = (short)(pointsArrayIndex-1);
            indexArrayIndex++;
        }
        
        /**grid for y axis**/
        for(int i=0;i<pointsNumber;i++){
            
            /**first point**/
            //x
            pointsArray[pointsArrayIndex] = (float)-size/2;
            pointsArrayIndex++;
            //z
            pointsArray[pointsArrayIndex] = ground;
            pointsArrayIndex++;
            //y
            pointsArray[pointsArrayIndex] = (i+resolution)-(size/2);
            pointsArrayIndex++;
            
            
            indexArray[indexArrayIndex] = (short) (pointsArrayIndex-1);
            indexArrayIndex++;
            
            /**second point**/
            //x
            pointsArray[pointsArrayIndex] = (float)size/2;
            pointsArrayIndex++;
            //z
            pointsArray[pointsArrayIndex] = ground;
            pointsArrayIndex++;
            //y
            pointsArray[pointsArrayIndex] = (i+resolution)-(size/2);
            pointsArrayIndex++;
            
            
            indexArray[indexArrayIndex] = (short)(pointsArrayIndex-1);
            indexArrayIndex++;
        }
        
        Grid grid = new Grid();
        grid.vertexBuffer = Buffers.newDirectFloatBuffer(pointsArray);
        grid.indexBuffer = Buffers.newDirectShortBuffer(indexArray);
        grid.vertexCount = indexArrayIndex;
        
        return grid;
    }
    
    public static Mesh createLandmark(float min, float max){
        
        Mesh mesh = new Mesh();
        
        float vertexData[] = new float[]
        {
            0.0f,0.0f,min,
            0.0f,0.0f,max,
            0.0f,min,0.0f,
            0.0f,max,0.0f,
            min,0.0f,0.0f,
            max,0.0f,0.0f
        };
        
        float colorData[] = new float[]
        {
            0.0f,0.0f,1.0f,
            0.0f,0.0f,1.0f,
            0.0f,1.0f,0.0f,
            0.0f,1.0f,0.0f,
            1.0f,0.0f,0.0f,
            1.0f,0.0f,0.0f
        };
        
        short indexData[] = new short[]
        {
            0, 1,
            2, 3,
            4, 5
        };
        
        mesh.vertexBuffer = Buffers.newDirectFloatBuffer(vertexData);
        mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);
        mesh.indexBuffer = Buffers.newDirectShortBuffer(indexData);
        
        mesh.vertexCount = indexData.length;
        
        return mesh;
    }
    
    public static Mesh createPlaneFromTexture(Vec3F startPoint, Texture texture){
                
        return MeshFactory.createPlane(startPoint, texture.getWidth(), texture.getHeight());
    }
    
    public static Mesh createPlaneFromTexture(Vec3F startPoint, Texture texture, int width, int height){
                
        return MeshFactory.createPlane(startPoint, width, height);
    }
    
    public static Mesh createMeshFromObj(InputStreamReader objFile, InputStreamReader objMaterial){
        
        Mesh mesh = new Mesh();
        
        ArrayList<Vec3F> vertices = new ArrayList<>();
        ArrayList<Vec3F> normales = new ArrayList<>();
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
                    
                }else if(line.startsWith("vn ")){
                    
                    String[] normale = line.split(" ");
                    normales.add(new Vec3F(Float.valueOf(normale[1]), Float.valueOf(normale[2]), Float.valueOf(normale[3])));
                    
                }else if(line.startsWith("f ")){
                    
                    String[] faceSplit = line.split(" ");
                    Vec3i face = new Vec3i(Integer.valueOf(faceSplit[1].split("/")[0]), Integer.valueOf(faceSplit[2].split("/")[0]), Integer.valueOf(faceSplit[3].split("/")[0]));
                    
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
        
        
        mesh = MeshFactory.createMeshWithNormales(vertices, normales, faces);
        
        float colorData[] = new float[vertices.size()*3];
        for(int i=0, j=0;i<vertices.size();i++,j+=3){
            
            colorData[j] = colors.get(i).x;
            colorData[j+1] = colors.get(i).y;
            colorData[j+2] = colors.get(i).z;
            
        }
        
        mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);
        
        return mesh;
    }
    
    public static Mesh createMesh(ArrayList<Vec3F> points, ArrayList<Short> faces){
        
        Mesh mesh = new Mesh();
        
        
        float[] vertexData = new float[points.size()*3];
        for(int i=0,j=0 ; i<points.size(); i++, j+=3){
            
            vertexData[j] = points.get(i).x;
            vertexData[j+1] = points.get(i).y;
            vertexData[j+2] = points.get(i).z;
        }
        /*
        for(int i=0 ; i<points.size()-3 ; i += 3){
            
            vertexData[i] = points.get(i).x;
            vertexData[i+1] = points.get(i).y;
            vertexData[i+2] = points.get(i).z;
        }
        */
        short indexData[] = new short[faces.size()];
        
        for(int i=0 ; i<faces.size() ; i ++){
            
            indexData[i] = faces.get(i);
        }
        
        mesh.vertexBuffer = Buffers.newDirectFloatBuffer(vertexData);
        mesh.indexBuffer = Buffers.newDirectShortBuffer(indexData);
        
        mesh.vertexCount = indexData.length;
        
        return mesh;
    }
    
    public static Mesh createMeshWithNormales(ArrayList<Vec3F> points, ArrayList<Vec3F> normales, ArrayList<Short> faces){
        
        Mesh mesh = new Mesh();
        
        
        float[] vertexData = new float[points.size()*3];
        for(int i=0,j=0 ; i<points.size(); i++, j+=3){
            
            vertexData[j] = points.get(i).x;
            vertexData[j+1] = points.get(i).y;
            vertexData[j+2] = points.get(i).z;
        }
        
        float[] normalData = new float[normales.size()*3];
        for(int i=0,j=0 ; i<normales.size(); i++, j+=3){
            
            normalData[j] = normales.get(i).x;
            normalData[j+1] = normales.get(i).y;
            normalData[j+2] = normales.get(i).z;
        }
        
        short indexData[] = new short[faces.size()];
        
        for(int i=0 ; i<faces.size() ; i ++){
            
            indexData[i] = faces.get(i);
        }
        
        mesh.vertexBuffer = Buffers.newDirectFloatBuffer(vertexData);
        mesh.indexBuffer = Buffers.newDirectShortBuffer(indexData);
        mesh.normalBuffer = Buffers.newDirectFloatBuffer(normalData);
        mesh.vertexCount = indexData.length;
        
        return mesh;
    }
}
