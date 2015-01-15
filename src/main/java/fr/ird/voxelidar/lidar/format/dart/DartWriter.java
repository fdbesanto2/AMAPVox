/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.dart;

import fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpace;
import fr.ird.voxelidar.lidar.format.voxelspace.Voxel;
import fr.ird.voxelidar.lidar.format.voxelspace.VoxelSpaceFormat;
import fr.ird.voxelidar.math.point.Point3F;
import fr.ird.voxelidar.math.point.Point3I;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Julien
 */
public class DartWriter {
    
    public static void writeFromDart(Dart dart, File outputFile){
        
        BufferedWriter writer;
        try {
            
            writer = new BufferedWriter(new FileWriter(outputFile));
            
            writer.write(dart.getSceneDimension().x+" "+dart.getSceneDimension().y+" "+dart.getSceneDimension().z+"\n");
            writer.write(dart.getCellDimension().x+" "+dart.getCellDimension().y+" "+dart.getCellDimension().z+"\n");
            writer.write(dart.getCellsNumberByLayer()+"\n");
            
            
            for(int z=0; z<dart.getSceneDimension().z; z++){
                
                for(int x=0; x<dart.getSceneDimension().x; x++){
                    
                    for(int y=0; y<dart.getSceneDimension().y; y++){
                        
                        DartCell cell = dart.cells[x][y][z];
                        
                        if(cell.getType() == DartCell.CELL_TYPE_TURBID_CROWN){
                            
                            String turbids ="";

                            for(int i=0;i<cell.getNbTurbids();i++){
                                
                                if(cell.getTurbids()[i].LAI == 0){
                                    turbids+=" "+"0"+" "+cell.getTurbids()[i].leafPhaseFunction+" 0";
                                }else{
                                    turbids+=" "+cell.getTurbids()[i].LAI+" "+cell.getTurbids()[i].leafPhaseFunction+" 0";
                                }
                                
                            }

                            String figures ="";

                            for(int i=0;i<cell.getNbFigures();i++){
                                figures+=" "+cell.getFigureIndex()[i];
                            }

                            writer.write(cell.getType()+" "+cell.getNbFigures()+figures+" "+cell.getNbTurbids()+turbids+" ");

                        }else{
                            writer.write("0"+" ");
                        }
                        
                    }
                    
                    writer.write("\n");
                }
                
                writer.write("\n");
            }
            
            writer.close();
            
        } catch (IOException ex) {
            Logger.getLogger(DartWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void writeFromVoxelsFile(File inputFile, File outputFile){
        
        DartWriter.writeFromDart(null, outputFile);
    }
    
    public static void writeFromVoxelSpace(VoxelSpaceFormat voxelSpaceFormat, File outputFile){
        
        Dart dart = new Dart(
                new Point3I(voxelSpaceFormat.xNumberVox,voxelSpaceFormat.yNumberVox,voxelSpaceFormat.zNumberVox),
                new Point3F(voxelSpaceFormat.resolution, voxelSpaceFormat.resolution, voxelSpaceFormat.resolution),
                voxelSpaceFormat.xNumberVox*voxelSpaceFormat.yNumberVox);
        
        for (Voxel voxel : voxelSpaceFormat.voxels) {
            
            Map<String, Float> attributs = voxel.getAttributs();
            Float densite = attributs.get("densite");
            
            int indiceX = voxel.indiceX;
            int indiceY = voxel.indiceY;
            int indiceZ = voxel.indiceZ;
            
            dart.cells[indiceX][indiceZ][indiceY] = new DartCell();
            
            dart.cells[indiceX][indiceZ][indiceY].setNbFigures(0);
            dart.cells[indiceX][indiceZ][indiceY].setNbTurbids(1);
            
            if(densite == -1 || densite == 0){
                dart.cells[indiceX][indiceZ][indiceY].setType(DartCell.CELL_TYPE_EMPTY);
                densite = 0f;
            }else{
                dart.cells[indiceX][indiceZ][indiceY].setType(DartCell.CELL_TYPE_TURBID_CROWN);
            }
            
            dart.cells[indiceX][indiceZ][indiceY].setTurbids(new Turbid[]{new Turbid(densite, 0)});
            
        }
        
        DartWriter.writeFromDart(dart, outputFile);
    }
}
