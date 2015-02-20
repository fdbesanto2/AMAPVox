/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.scene;

import fr.ird.voxelidar.engine3d.object.mesh.Face;
import fr.ird.voxelidar.lidar.format.als.Las;
import fr.ird.voxelidar.lidar.format.als.PointDataRecordFormat0;
import fr.ird.voxelidar.engine3d.math.vector.Vec3F;
import java.util.ArrayList;

/**
 *
 * @author Julien
 */
public class LasToDtm {
    
    public static Dtm getDtmFromLas(Las las){
        
        ArrayList<? extends PointDataRecordFormat0> pointDataRecords = las.getPointDataRecords();
        ArrayList<Vec3F> points = new ArrayList<>();
        
        int index = 0;
        for(PointDataRecordFormat0 point:pointDataRecords){
            if(point.getClassification() == 2){
                Vec3F pt = new Vec3F((float)las.getTransformedX(index), (float)las.getTransformedY(index), (float)las.getTransformedZ(index));
                points.add(pt);
            }
            
            index++;
        }
        
        ArrayList<Face> faces = DtmLoader.delaunaytriangulate(points);        
        
        Dtm terrain = new Dtm("", points, faces);
        
        return terrain;
    }
}
