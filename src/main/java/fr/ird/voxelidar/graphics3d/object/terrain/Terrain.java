/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.object.terrain;

import fr.ird.voxelidar.Principal;
import fr.ird.voxelidar.graphics3d.mesh.Face;
import fr.ird.voxelidar.math.vector.Vec2F;
import fr.ird.voxelidar.math.vector.Vec3F;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.collections.map.MultiKeyMap;

/**
 *
 * @author Julien
 */
public class Terrain {
    
    private final ArrayList<Vec3F> points;
    private final ArrayList<Face> faces;
    private String path;

    public String getPath() {
        return path;
    }
    
    public ArrayList<Vec3F> getPoints() {
        return points;
    }

    public ArrayList<Face> getFaces() {
        return faces;
    }
    
    public ArrayList<Short> getIndices(){
        
        ArrayList<Short> indices = new ArrayList<>();
        
        for (Face face : faces) {
            indices.add((short) face.getPoint1());
            indices.add((short) face.getPoint2());
            indices.add((short) face.getPoint3());
        }
        
        return indices;
    }
    
        
    public Terrain(String path, ArrayList<Vec3F> points, ArrayList<Face> faces){
        
        this.points = points;
        this.faces = faces;
        this.path = path;
    }
    
    public MultiKeyMap getXYStructure(){
        
        MultiKeyMap map = new MultiKeyMap();
        
        for (Vec3F point : points) {
            map.put(point.x, point.z, point.y);
        }
        
        return map;
    }
    
    public void exportObj(){
        
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter("C:\\Users\\Julien\\Desktop\\DTM1m_local_xyz_rectangle.obj"));
            
            writer.write("o terrain\n");
            
            for (Vec3F point : points) {
                writer.write("v " + point.x + " " + point.y + " " + point.z + " " + "\n");
            }
            
            for (Face face : faces) {
                writer.write("f " + (face.getPoint1() + 1) + " " + (face.getPoint2() + 1) + " " + (face.getPoint3() + 1) + "\n");
            }
            
            
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Principal.class.getName()).log(Level.SEVERE, null, ex);
        }

        
    }
    
    public Face getFaceContainingPoint(float x, float y){
        
        for (Face triangle : faces) {
            
            Vec3F pointA = points.get(triangle.getPoint1());
            Vec3F pointB = points.get(triangle.getPoint2());
            Vec3F pointC = points.get(triangle.getPoint3());
            
            Vec2F vecAB = Vec2F.createVec2FromPoints(new Vec2F(pointA.x, pointA.z), new Vec2F(pointB.x, pointB.z));
            Vec2F vecBC = Vec2F.createVec2FromPoints(new Vec2F(pointB.x, pointB.z), new Vec2F(pointC.x, pointC.z));
            Vec2F vecCA = Vec2F.createVec2FromPoints(new Vec2F(pointC.x, pointC.z), new Vec2F(pointA.x, pointA.z));
            
            
            Vec2F vecAM = Vec2F.createVec2FromPoints(new Vec2F(pointA.x, pointA.z), new Vec2F(x, y));
            Vec2F vecBM = Vec2F.createVec2FromPoints(new Vec2F(pointB.x, pointB.z), new Vec2F(x, y));
            Vec2F vecCM = Vec2F.createVec2FromPoints(new Vec2F(pointC.x, pointC.z), new Vec2F(x, y));
            
            float detABAM = Vec2F.determinant(vecAB, vecAM);
            float detBCBM = Vec2F.determinant(vecBC, vecBM);
            float detCACM = Vec2F.determinant(vecCA, vecCM);
            
            if(detABAM<=0 && detBCBM<=0 && detCACM<=0){
                return triangle;
            }else if(detABAM>0 && detBCBM>0 && detCACM>0){
                return triangle;
            }
        }
        
        return null;
    }
    
    public float getZFromXY(float x, float y){
        
        //détermination des 3 points formant la face qui contient x,y
        Face triangle = getFaceContainingPoint(x, y);
        
        Vec3F pointA = points.get(triangle.getPoint1());
        Vec3F pointB = points.get(triangle.getPoint2());
        Vec3F pointC = points.get(triangle.getPoint3());
        
        //calcul de l'équation du plan
        Vec3F vecAB = Vec3F.createVec3FromPoints(pointA, pointB);
        Vec3F vecAC = Vec3F.createVec3FromPoints(pointA, pointC);
        
        Vec3F vecNorm = Vec3F.cross(vecAB, vecAC);
        float a = vecNorm.x;
        float b = vecNorm.y;
        float c = vecNorm.z;
        float d = (-pointA.x * vecNorm.x) + (-pointA.y * vecNorm.y) + (-pointA.z * vecNorm.z);
        
        //on remplace dans l'équation pour trouver z
        //float z = ((a*x) + (b*y) + d)/(-c);
        float z = ((-a*x) - (c*y) - d)/(b);
        return z;
    }
}
