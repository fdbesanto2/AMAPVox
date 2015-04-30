/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.dart;

import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceData;
import fr.ird.voxelidar.engine3d.math.point.Point3F;
import fr.ird.voxelidar.engine3d.math.point.Point3I;
import fr.ird.voxelidar.engine3d.object.scene.VoxelObject;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.Voxel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class DartWriter {
    
    private final static Logger logger = Logger.getLogger(DartWriter.class);
    
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
            logger.error(ex);
        }
    }
    
    public static void writeFromVoxelSpace(VoxelSpaceData data, File outputFile){
        
        Dart dart = new Dart(
                new Point3I(data.split.x,data.split.y,data.split.z),
                new Point3F(data.res, data.res, data.res),
                data.split.x*data.split.y);
        
        ArrayList<String> attributsNames = data.attributsNames;
        
        for (Voxel v : data.voxels) {
            
            VoxelObject voxel = (VoxelObject) v;
            
            float[] attributs = voxel.attributs;
            
            float densite;
            try{
                densite = attributs[attributsNames.indexOf("PadBVTotal")];
            }catch(Exception e){ 
                logger.error("could not find attribut PadBflTotal or PadBVTotal");
                return;
            }
            
            densite *= data.res;
            
            int indiceX = voxel.$i;
            int indiceY = voxel.$j;
            int indiceZ = voxel.$k;
            
            dart.cells[indiceX][indiceY][indiceZ] = new DartCell();
            
            dart.cells[indiceX][indiceY][indiceZ].setNbFigures(0);
            dart.cells[indiceX][indiceY][indiceZ].setNbTurbids(1);
            
            if(Float.isNaN(densite) || densite == 0){
                dart.cells[indiceX][indiceY][indiceZ].setType(DartCell.CELL_TYPE_EMPTY);
                densite = 0f;
            }else{
                dart.cells[indiceX][indiceY][indiceZ].setType(DartCell.CELL_TYPE_TURBID_CROWN);
            }
            
            dart.cells[indiceX][indiceY][indiceZ].setTurbids(new Turbid[]{new Turbid(densite, 0)});
            
        }
        
        logger.info("Writing dart file "+outputFile.getAbsolutePath());
        DartWriter.writeFromDart(dart, outputFile);
        logger.info("dart file written");
    }
}
