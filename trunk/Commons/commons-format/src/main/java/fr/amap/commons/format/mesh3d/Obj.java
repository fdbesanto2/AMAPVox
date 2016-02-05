/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.format.mesh3d;

import fr.amap.commons.math.point.Point2F;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.point.Point3I;
import java.util.List;

/**
 *
 * @author calcul
 */
public class Obj {
    
    private Point3F[] points;
    private Point3I[] faces;
    private Point3F[] normals;
    private Point2F[] texCoords;
    
    private boolean hasTexCoordIndices;
    private boolean hasNormalsIndices;
    
    private int[] materialOffsets;

    public Point3F[] getPoints() {
        return points;
    }

    public void setPoints(Point3F[] points) {
        this.points = points;
    }

    public Point3I[] getFaces() {
        return faces;
    }
    
    public int[] get1DFaces() {
        
        int[] facesArray = new int[faces.length * 3];
        
        for(int i=0, j=0 ; i<faces.length; i++, j+=3){
            
            facesArray[j] = faces[i].x;
            facesArray[j+1] = faces[i].y;
            facesArray[j+2] = faces[i].z;
        }
        return facesArray;
    }

    public void setFaces(Point3I[] faces) {
        this.faces = faces;
    }

    public Point3F[] getNormals() {
        return normals;
    }

    public void setNormals(Point3F[] normals) {
        this.normals = normals;
    }

    public Point2F[] getTexCoords() {
        return texCoords;
    }

    public void setTexCoords(Point2F[] texCoords) {
        this.texCoords = texCoords;
    }

    public boolean isHasTexCoordIndices() {
        return hasTexCoordIndices;
    }

    public void setHasTexCoordIndices(boolean hasTexCoordIndices) {
        this.hasTexCoordIndices = hasTexCoordIndices;
    }

    public boolean isHasNormalsIndices() {
        return hasNormalsIndices;
    }

    public void setHasNormalsIndices(boolean hasNormalsIndices) {
        this.hasNormalsIndices = hasNormalsIndices;
    }

    public int[] getMaterialOffsets() {
        return materialOffsets;
    }

    public void setMaterialOffsets(int[] materialOffsets) {
        this.materialOffsets = materialOffsets;
    }
}
