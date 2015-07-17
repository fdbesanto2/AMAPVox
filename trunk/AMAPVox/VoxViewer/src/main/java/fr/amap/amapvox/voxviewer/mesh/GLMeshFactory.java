/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.mesh;

import com.jogamp.common.nio.Buffers;
import fr.amap.amapvox.commons.math.vector.Vec3F;
import fr.amap.amapvox.commons.math.vector.Vec3i;
import fr.amap.amapvox.commons.util.ColorGradient;
import fr.amap.amapvox.jraster.asc.DTMPoint;
import fr.amap.amapvox.jraster.asc.Face;
import fr.amap.amapvox.jraster.asc.RegularDtm;
import fr.amap.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.amapvox.voxviewer.object.mesh.Grid;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class GLMeshFactory {
    
    public static TexturedGLMesh createTexturedCube(float size){
        
        TexturedGLMesh texturedMesh = (TexturedGLMesh) createCube(size);
        
        float textCoordData[] = new float[]
        {0, 1,
         1, 1,
         0, 0,
         1, 0};
        
        texturedMesh.textureCoordinatesBuffer = Buffers.newDirectFloatBuffer(textCoordData);
        
        return texturedMesh;
    }
    public static GLMesh createCube(float size){
        
        float vertexData[] = new float[]
        {size/2.0f, size/2.0f, -size/2.0f,
        size/2.0f, -size/2.0f, -size/2.0f,
        -size/2.0f, -size/2.0f, -size/2.0f,
        -size/2.0f, size/2.0f, -size/2.0f,
        size/2.0f, size/2.0f, size/2.0f,
        size/2.0f, -size/2.0f, size/2.0f,
        -size/2.0f, -size/2.0f, size/2.0f,
        -size/2.0f, size/2.0f, size/2.0f};
        
        float[] normalData = new float[]
        {0.577349f, 0.577349f, -0.577349f,
        0.577349f, -0.577349f, -0.577349f,
        -0.577349f, -0.577349f, -0.577349f,
        -0.577349f, 0.577349f, -0.577349f,
        0.577349f, 0.577349f, 0.577349f,
        0.577349f, -0.577349f, 0.577349f,
        -0.577349f, -0.577349f, 0.577349f,
        -0.577349f, 0.577349f, 0.577349f};
        
        int indexData[] = new int[]
        {0, 1, 2,
        4, 7, 6,
        0, 4, 5,
        1, 5, 6,
        2, 6, 7,
        4, 0, 3,
        3, 0, 2,
        5, 4, 6,
        1, 0, 5,
        2, 1, 6,
        3, 2, 7,
        7, 4, 3};
        
        
        
        GLMesh cube = new SimpleGLMesh();
        //cube.aoBuffer =  Buffers.newDirectFloatBuffer(aoData);
        cube.vertexBuffer = Buffers.newDirectFloatBuffer(vertexData);
        cube.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        cube.vertexCount = indexData.length;
        cube.normalBuffer  = Buffers.newDirectFloatBuffer(normalData);
        
        return cube;
    }
    
    public static GLMesh createPlane(Vec3F startPoint, int width , int height){
        
        
        
        float vertexData[] = new float[]
        {startPoint.x, startPoint.y, startPoint.z,
         startPoint.x+width, startPoint.y, startPoint.z,
         startPoint.x, startPoint.y+height, startPoint.z,
         startPoint.x+width, startPoint.y+height, startPoint.z};
        
        float textCoordData[] = new float[]
        {0, 1,
         1, 1,
         0, 0,
         1, 0};
        
        int indexData[] = new int[]
        {0, 1, 3,
        2, 0, 3};
        
        TexturedGLMesh plane = new TexturedGLMesh();
        /*
        MeshBuffer meshBuffer= new MeshBuffer();
        meshBuffer.setBuffer(MeshBuffer.VERTEX_BUFFER, Buffers.newDirectFloatBuffer(vertexData));
        meshBuffer.setBuffer(MeshBuffer.TEXTURE_COORDINATES_BUFFER, Buffers.newDirectFloatBuffer(textCoordData));
        meshBuffer.setBuffer(MeshBuffer.INDEX_BUFFER, Buffers.newDirectShortBuffer(indexData));
        plane.meshBuffer = meshBuffer;
        */
        plane.vertexBuffer = Buffers.newDirectFloatBuffer(vertexData);
        plane.textureCoordinatesBuffer = Buffers.newDirectFloatBuffer(textCoordData);
        plane.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        plane.vertexCount = indexData.length;
        
        return plane;
    }
    
    public static Grid createGrid(float resolution, int size, float ground){
        
                
        int pointsNumber = (int) (size / resolution);
        float pointsArray[] = new float[12*pointsNumber];
        int indexArray[] = new int[12*pointsNumber];
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
            
            
            indexArray[indexArrayIndex] = (pointsArrayIndex-1);
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
            
            
            indexArray[indexArrayIndex] = (pointsArrayIndex-1);
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
            
            
            indexArray[indexArrayIndex] =  (pointsArrayIndex-1);
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
            
            
            indexArray[indexArrayIndex] = (pointsArrayIndex-1);
            indexArrayIndex++;
        }
        
        Grid grid = new Grid();
        grid.vertexBuffer = Buffers.newDirectFloatBuffer(pointsArray);
        grid.indexBuffer = Buffers.newDirectIntBuffer(indexArray);
        grid.vertexCount = indexArrayIndex;
        
        return grid;
    }
    
    public static GLMesh createLandmark(float min, float max){
        
        GLMesh mesh = new SimpleGLMesh();
        
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
        
        int indexData[] = new int[]
        {
            0, 1,
            2, 3,
            4, 5
        };
        
        mesh.vertexBuffer = Buffers.newDirectFloatBuffer(vertexData);
        mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);
        mesh.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        
        mesh.vertexCount = indexData.length;
        
        return mesh;
    }
    
    public static GLMesh createPlaneFromTexture(Vec3F startPoint, Texture texture){
                
        return GLMeshFactory.createPlane(startPoint, texture.getWidth(), texture.getHeight());
    }
    
    public static GLMesh createPlaneFromTexture(Vec3F startPoint, Texture texture, int width, int height){
                
        return GLMeshFactory.createPlane(startPoint, width, height);
    }
    
    public static GLMesh createMeshFromX3D(InputStreamReader x3dFile) throws JDOMException, IOException{
        
        SAXBuilder sxb = new SAXBuilder();
        sxb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        
        try {
            Document document = sxb.build(x3dFile);
            Element root = document.getRootElement();
            List<Element> shapes = root.getChild("Scene")
                                        .getChild("Transform")
                                        .getChild("Group")
                                        .getChildren("Shape");
            
            List<Integer> faces = new ArrayList<>();
            List<Vec3F> vertices = new ArrayList<>();
            List<Vec3F> normales = new ArrayList<>();
            Map<Integer, Vec3F> colors = new HashMap<>();
            
            int facesOffset = 0;
            int verticesOffset = 0;
            
            for(Element shape : shapes){
                
                Element indexedFaceSetElement = shape.getChild("IndexedFaceSet");
                
                if(indexedFaceSetElement == null){
                    indexedFaceSetElement = shape.getChild("IndexedTriangleSet");
                }
                Element appearanceElement = shape.getChild("Appearance");
                String diffuseColorString = appearanceElement.getChild("Material").getAttributeValue("diffuseColor");
                String[] split = diffuseColorString.split(" ");
                Vec3F currentColor = new Vec3F(Float.valueOf(split[0]), Float.valueOf(split[1]), Float.valueOf(split[2]));
                
                String coordinatesIndices = indexedFaceSetElement.getAttributeValue("coordIndex");
                
                if(coordinatesIndices == null){
                    coordinatesIndices = indexedFaceSetElement.getAttributeValue("index");
                }
                String[] coordinatesIndicesArray = coordinatesIndices.split(" ");
                
                int tmp = 0;
                
                for(String s : coordinatesIndicesArray){
                    int indice = Integer.valueOf(s);
                    
                    if(indice != -1){
                        faces.add(indice + facesOffset);
                        colors.put(indice + facesOffset, currentColor);
                        tmp++;
                    }
                }
                
                facesOffset += tmp; 
                
                tmp = 0;
                
                Element coordinateElement = indexedFaceSetElement.getChild("Coordinate");
                if(coordinateElement != null){
                    String point = coordinateElement.getAttributeValue("point");
                    
                    if(point != null){
                        String[] pointArray = point.split(" ");
                    
                    int count = -1;
                    float x = 0, y = 0, z;
                    
                        for (String p : pointArray) {

                            float value = Float.valueOf(p);
                            count++;

                            switch (count) {
                                case 0:
                                    x = value;
                                    break;
                                case 1:
                                    y = value;
                                    break;
                                case 2:
                                    z = value;
                                    vertices.add(new Vec3F(x, y, z));
                                    count = -1;
                                    tmp++;
                                    break;
                            }

                        }
                    }
                    
                }
                
                verticesOffset += tmp;
                
                Element normalElement = indexedFaceSetElement.getChild("Normal");
                if(normalElement != null){
                    String normalesVector = normalElement.getAttributeValue("vector");
                    
                    if(normalesVector != null){
                        String[] normalesArray = normalesVector.split(" ");

                        int count = -1;
                        float x = 0, y = 0, z;

                        for (String p : normalesArray) {

                            float value = Float.valueOf(p);
                            count++;

                            switch (count) {
                                case 0:
                                    x = value;
                                    break;
                                case 1:
                                    y = value;
                                    break;
                                case 2:
                                    z = value;
                                    normales.add(new Vec3F(x, y, z));
                                    count = -1;
                                    break;
                            }

                        }
                    }
                }
                
            }
            
            GLMesh mesh = GLMeshFactory.createMeshWithNormales(vertices, normales, faces);
            
            float colorData[] = new float[vertices.size()*3];
            for(int i=0, j=0;i<vertices.size();i++,j+=3){

                colorData[j] = colors.get(i).x;
                colorData[j+1] = colors.get(i).y;
                colorData[j+2] = colors.get(i).z;

            }

            mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);
            
            return mesh;
            
            
        } catch (JDOMException | IOException ex) {
            throw ex;
        }
    }
    
    public static GLMesh createBoundingBox(float minX, float minY, float minZ, float maxX, float maxY, float maxZ){
        
        GLMesh mesh = new SimpleGLMesh();
        
        float vertexData[] = new float[]
        {maxX, maxY, minZ,
        maxX, minY, minZ,
        minX, minY, minZ,
        minX, maxY, minZ,
        maxX, maxY, maxZ,
        maxX, minY, maxZ,
        minX, minY, maxZ,
        minX, maxY, maxZ};
        
        int indexData[] = new int[]
        {0, 1,
        5, 4,
        4, 0,
        1, 5,
        5, 6,
        4, 7,
        1, 2,
        2, 6,
        7, 6,
        2, 3,
        3, 7,
        0, 3};
        
        mesh.vertexBuffer = Buffers.newDirectFloatBuffer(vertexData);
        mesh.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        mesh.vertexCount = indexData.length;
        
        return mesh;
    }
    
    public static GLMesh createMeshFromObj(InputStreamReader objFile, InputStreamReader objMaterial) throws FileNotFoundException, IOException{
        
        GLMesh mesh;
        
        ArrayList<Vec3F> vertices = new ArrayList<>();
        ArrayList<Vec3F> normales = new ArrayList<>();
        Map<Integer, Vec3F> colors = new HashMap<>();
        ArrayList<Integer> faces = new ArrayList<>();
        Map<String, Vec3F> materials = new HashMap<>();
        
        
        int lineNumber = 100000;
        
        Vec3F[] normalesArray = new Vec3F[lineNumber];
        
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
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
        
        int count = 0;
        
        try{
                        
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
                    
                    String[] faceSplit = line.replaceAll("//", " ").split(" ");
                    
                    Vec3i face = new Vec3i(Integer.valueOf(faceSplit[1]), Integer.valueOf(faceSplit[3]), Integer.valueOf(faceSplit[5]));
                    
                    normalesArray[face.x-1] = normales.get(Integer.valueOf(faceSplit[2])-1);
                    normalesArray[face.y-1] = normales.get(Integer.valueOf(faceSplit[4])-1);
                    normalesArray[face.z-1] = normales.get(Integer.valueOf(faceSplit[6])-1);
                    count ++;
                    
                    colors.put(face.x-1, currentColor);
                    colors.put(face.y-1, currentColor);
                    colors.put(face.z-1, currentColor);
                    
                    faces.add(face.x-1);
                    faces.add(face.y-1);
                    faces.add(face.z-1);
                    
                }else if(line.startsWith("usemtl ")){
                    currentColor = materials.get(line.split(" ")[1]);
                }
            }
            
            reader.close();
            
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
        
        normales = new ArrayList<>();
        
        for (int i=0;i<vertices.size();i++){
            normales.add(normalesArray[i]);
        }
        
        mesh = GLMeshFactory.createMeshWithNormales(vertices, normales, faces);
        
        float colorData[] = new float[vertices.size()*3];
        for(int i=0, j=0;i<vertices.size();i++,j+=3){
            
            colorData[j] = colors.get(i).x;
            colorData[j+1] = colors.get(i).y;
            colorData[j+2] = colors.get(i).z;
            
        }
        
        mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);
        
        return mesh;
    }
    
    public static GLMesh createMesh(ArrayList<Vec3F> points, ArrayList<Integer> faces){
        
        GLMesh mesh = new SimpleGLMesh();
        
        
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
        int indexData[] = new int[faces.size()];
        
        for(int i=0 ; i<faces.size() ; i ++){
            
            indexData[i] = faces.get(i);
        }
        
        mesh.vertexBuffer = Buffers.newDirectFloatBuffer(vertexData);
        mesh.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        
        mesh.vertexCount = indexData.length;
        
        return mesh;
    }
    
    public static GLMesh createMeshAndComputeNormalesFromDTM(RegularDtm dtm){
        
        List<DTMPoint> points = dtm.getPoints();
        List<Face> faces = dtm.getFaces();
        
        GLMesh mesh = new SimpleGLMesh();
        
        float[] vertexData = new float[points.size()*3];
        for(int i=0,j=0 ; i<points.size(); i++, j+=3){
            
            vertexData[j] = points.get(i).x;
            vertexData[j+1] = points.get(i).y;
            vertexData[j+2] = points.get(i).z;
        }
        
        float[] normalData = new float[points.size()*3];
        for(int i=0,j=0 ; i<points.size(); i++, j+=3){
            
            Vec3F meanNormale = new Vec3F(0, 0, 0);
            
            for(Integer faceIndex : points.get(i).faces){
                
                Face face = faces.get(faceIndex);
                
                DTMPoint point1 = points.get(face.getPoint1());
                DTMPoint point2 = points.get(face.getPoint2());
                DTMPoint point3 = points.get(face.getPoint3());
                
                Vec3F vec1 = Vec3F.substract(new Vec3F(point2.x, point2.y, point2.z), new Vec3F(point1.x, point1.y, point1.z));
                Vec3F vec2 = Vec3F.substract(new Vec3F(point3.x, point3.y, point3.z), new Vec3F(point1.x, point1.y, point1.z));
                
                meanNormale = Vec3F.add(meanNormale, Vec3F.normalize(Vec3F.cross(vec2, vec1)));
                
            }
            
            meanNormale = Vec3F.normalize(meanNormale);
            
            normalData[j] = meanNormale.x;
            normalData[j+1] = meanNormale.y;
            normalData[j+2] = meanNormale.z;
        }
        
        int indexData[] = new int[faces.size()*3];
        for(int i=0, j=0 ; i<faces.size(); i++, j+=3){
            
            indexData[j] = faces.get(i).getPoint1();
            indexData[j+1] = faces.get(i).getPoint2();
            indexData[j+2] = faces.get(i).getPoint3();
        }
        
        mesh.vertexBuffer = Buffers.newDirectFloatBuffer(vertexData);
        mesh.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        mesh.normalBuffer = Buffers.newDirectFloatBuffer(normalData);
        mesh.vertexCount = indexData.length;
        
        ColorGradient gradient = new ColorGradient(dtm.getzMin(), dtm.getzMax());
        gradient.setGradientColor(ColorGradient.GRADIENT_RAINBOW3);
        
        float colorData[] = new float[points.size()*3];
        for(int i=0, j=0;i<points.size();i++,j+=3){
            
            Color color = gradient.getColor(points.get(i).z);
            colorData[j] = color.getRed()/255.0f;
            colorData[j+1] = color.getGreen()/255.0f;
            colorData[j+2] =  color.getBlue()/255.0f;
            
        }
        
        mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);
        
        return mesh;
    }
    
    public static GLMesh createMeshWithNormales(List<Vec3F> points, List<Vec3F> normales, List<Integer> faces){
        
        GLMesh mesh = new SimpleGLMesh();
        
        
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
        
        int indexData[] = new int[faces.size()];
        
        for(int i=0 ; i<faces.size() ; i ++){
            
            indexData[i] = faces.get(i);
        }
        
        mesh.vertexBuffer = Buffers.newDirectFloatBuffer(vertexData);
        mesh.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        mesh.normalBuffer = Buffers.newDirectFloatBuffer(normalData);
        mesh.vertexCount = indexData.length;
        
        return mesh;
    }
}
