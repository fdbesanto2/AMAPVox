/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.mesh;

import com.jogamp.common.nio.Buffers;
import fr.ird.voxelidar.graphics2d.texture.Texture;
import fr.ird.voxelidar.math.vector.Vec3F;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 *
 * @author Julien
 */
public class MeshFactory {
    
    
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
        cube.vertexBuffer = Buffers.newDirectFloatBuffer(vertexData);
        cube.indexBuffer = Buffers.newDirectShortBuffer(indexData);
        cube.vertexCount = indexData.length;
        
        return cube;
    }
    
    public static Mesh createPlane(int width , int height){
        
        float vertexData[] = new float[]
        {0, 0, 0,
         width, 0, 0,
         0, height, 0,
         width, height, 0};
        
        float textCoordData[] = new float[]
        {0, 1,
         1, 1,
         0, 0,
         1, 0};
        
        short indexData[] = new short[]
        {0, 1, 3,
        2, 0, 3};
        
        Mesh plane = new Mesh();
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
    
    public static Mesh createMeshFromImage(BufferedImage image){
                
        return MeshFactory.createPlane(image.getWidth(), image.getHeight());
    }
    
    public static Mesh createMeshFromTexture(Texture texture){
                
        return MeshFactory.createPlane(texture.getWidth(), texture.getHeight());
    }
    
    public static Mesh createMesh(ArrayList<Vec3F> points, ArrayList<Short> faces){
        
        Mesh mesh = new Mesh();
        
        
        float[] vertexData = new float[points.size()*3];
        
        for(int i=0 ; i<points.size()-3 ; i += 3){
            
            vertexData[i] = points.get(i).x;
            vertexData[i+1] = points.get(i).y;
            vertexData[i+2] = points.get(i).z;
        }
        
        short indexData[] = new short[faces.size()];
        
        for(int i=0 ; i<faces.size() ; i ++){
            
            indexData[i] = faces.get(i);
        }
        
        mesh.vertexBuffer = Buffers.newDirectFloatBuffer(vertexData);
        mesh.indexBuffer = Buffers.newDirectShortBuffer(indexData);
        
        mesh.vertexCount = indexData.length;
        
        return mesh;
    }
}
