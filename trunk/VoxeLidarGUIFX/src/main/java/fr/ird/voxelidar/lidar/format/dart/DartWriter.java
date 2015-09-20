/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.dart;

import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.math.point.Point2F;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceData;
import fr.ird.voxelidar.engine3d.math.point.Point3F;
import fr.ird.voxelidar.engine3d.math.point.Point3I;
import fr.ird.voxelidar.engine3d.object.scene.VoxelObject;
import fr.ird.voxelidar.lidar.format.dtm.DTMPoint;
import fr.ird.voxelidar.lidar.format.dtm.DtmLoader;
import fr.ird.voxelidar.lidar.format.dtm.Face;
import fr.ird.voxelidar.lidar.format.dtm.RegularDtm;
import fr.ird.voxelidar.util.BoundingBox2F;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class DartWriter {
    
    private final static Logger logger = Logger.getLogger(DartWriter.class);
    
    private boolean generateTrianglesFile;
    private Mat4D transfMatrix;
    private File dtmFile;
    private File trianglesFile;

    public boolean isGenerateTrianglesFile() {
        return generateTrianglesFile;
    }

    public void setGenerateTrianglesFile(boolean generateTrianglesFile) {
        this.generateTrianglesFile = generateTrianglesFile;
    }

    public File getDtmFile() {
        return dtmFile;
    }

    public void setDtmFile(File dtmFile) {
        this.dtmFile = dtmFile;
    }

    public File getTrianglesFile() {
        return trianglesFile;
    }

    public void setTrianglesFile(File trianglesFile) {
        this.trianglesFile = trianglesFile;
    }    

    public Mat4D getTransfMatrix() {
        return transfMatrix;
    }

    public void setTransfMatrix(Mat4D transfMatrix) {
        this.transfMatrix = transfMatrix;
    }    
    
    public DartWriter(){
        transfMatrix = Mat4D.identity();
    }
    
    public void writeFromDart(Dart dart, File outputFile){
        
        BufferedWriter writer;
        try {
            
            writer = new BufferedWriter(new FileWriter(outputFile));
            
            writer.write(dart.getSceneDimension().x*dart.getCellDimension().x+" "+
                        dart.getSceneDimension().y*dart.getCellDimension().y+" "+
                        dart.getSceneDimension().z*dart.getCellDimension().z+"\n");
            
            writer.write(dart.getCellDimension().x+" "+dart.getCellDimension().y+" "+dart.getCellDimension().z+"\n");
            writer.write(dart.getCellsNumberByLayer()+"\n");
            
            
            for(int z=0; z<dart.getSceneDimension().z; z++){
                
                for(int x=0; x<dart.getSceneDimension().x; x++){
                //for(int y=dart.getSceneDimension().y-1; y>=0; y--){
                for(int y=0; y<dart.getSceneDimension().y; y++){
                    
                    
                        
                        DartCell cell = dart.cells[x][y][z];
                        
                        String stringToWrite = "";
                        
                        if(cell.getType() != DartCell.CELL_TYPE_EMPTY){
                            
                            String turbids ="";

                            for(int i=0;i<cell.getNbTurbids();i++){
                                
                                if(cell.getTurbids()[i].LAI == 0){
                                    turbids+=" "+"0"+" "+cell.getTurbids()[i].leafPhaseFunction+" 0";
                                }else{
                                    turbids+=" "+cell.getTurbids()[i].LAI+" "+cell.getTurbids()[i].leafPhaseFunction+" 0";
                                }
                                
                            }
                            
                            if(cell.getNbTurbids() == 0){
                                turbids += " 0";
                            }

                            String figures ="";

                            for(int i=0;i<cell.getNbFigures();i++){
                                figures+=" "+cell.getFigureIndex()[i];
                            }
                            
                            stringToWrite = cell.getType()+" "+cell.getNbFigures()+figures+" "+cell.getNbTurbids()+turbids+" ";

                        }else{
                            stringToWrite = "0"+" ";
                        }
                        
                        /*
                        if(x == dart.getSceneDimension().x-1){
                            stringToWrite = stringToWrite.trim();
                        }*/
                        
                        writer.write(stringToWrite);
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
    
    public void writeFromVoxelSpace(VoxelSpaceData data, File outputFile){
        
        Dart dart = new Dart(
                new Point3I(data.header.split.x,data.header.split.y,data.header.split.z),
                new Point3F(data.header.res, data.header.res, data.header.res),
                data.header.split.x*data.header.split.y);
        
        List<String> attributsNames = data.header.attributsNames;
        
        RegularDtm dtm = null;
        
        Set<Integer>[][][] faces = null;
        
        if(generateTrianglesFile && dtmFile != null){
            
            try {
                logger.info("Reading DTM : "+dtmFile.getAbsolutePath());
                
                dtm = DtmLoader.readFromAscFile(dtmFile);
                dtm.setTransformationMatrix(transfMatrix);
                
                dtm.setLimits(new BoundingBox2F(new Point2F((float)data.header.bottomCorner.x, (float)data.header.bottomCorner.y), 
                                                new Point2F((float)data.header.topCorner.x, (float)data.header.topCorner.y)), 10);
                
                logger.info("Building DTM");
                dtm.buildMesh();
                //dtm.exportObj(new File("/home/calcul/Documents/Julien/test.obj"));
                
                faces = new HashSet[data.header.split.x][data.header.split.y][data.header.split.z];
                List<DTMPoint> points = dtm.getPoints();
                
                for(DTMPoint point : points){
                    
                    Point3I voxelIndice = data.getIndicesFromPoint(point.x, point.y-1, point.z);
                    if(voxelIndice != null){
                        
                        if(faces[voxelIndice.x][voxelIndice.y][voxelIndice.z] == null){
                            faces[voxelIndice.x][voxelIndice.y][voxelIndice.z] = new HashSet<>();
                        }
                        
                        faces[voxelIndice.x][voxelIndice.y][voxelIndice.z].addAll(point.faces);
                    }
                }
                
            } catch (Exception ex) {
                logger.error("Cannot read dtm file "+dtmFile.getAbsolutePath(), ex);
                dtm = null;
            }
        }
        
        for (VoxelObject voxel : data.voxels) {
            
            float[] attributs = voxel.attributs;
            
            float densite;
            try{
                densite = attributs[attributsNames.indexOf("PadBVTotal")];
            }catch(Exception e){ 
                logger.error("could not find attribut PadBflTotal or PadBVTotal");
                return;
            }
            
            densite *= data.header.res;
            
            int indiceX = voxel.$i;
            int indiceY = voxel.$j;
            int indiceZ = voxel.$k;
            
            dart.cells[indiceX][indiceY][indiceZ] = new DartCell();
            
            //on récupère les triangles contenus dans le voxel
            
            int nbFigures = 0;
            if(faces != null){
                
                if(faces[indiceX][indiceY][indiceZ] == null){
                    nbFigures = 0;
                }else{
                    nbFigures = faces[indiceX][indiceY][indiceZ].size();
                }
            }
            
            dart.cells[indiceX][indiceY][indiceZ].setNbFigures(nbFigures);
            dart.cells[indiceX][indiceY][indiceZ].setNbTurbids(1);
            
            
            if(Float.isNaN(densite) || densite == 0){
                
                if(nbFigures == 0){
                    dart.cells[indiceX][indiceY][indiceZ].setType(DartCell.CELL_TYPE_EMPTY);
                }else{
                    dart.cells[indiceX][indiceY][indiceZ].setType(DartCell.CELL_TYPE_OPAQUE_GROUND);
                    dart.cells[indiceX][indiceY][indiceZ].setNbTurbids(0);
                }
                
                densite = 0f;
            }else{
                if(nbFigures > 0){
                    dart.cells[indiceX][indiceY][indiceZ].setType(DartCell.CELL_TYPE_OPAQUE_GROUND);
                    dart.cells[indiceX][indiceY][indiceZ].setNbTurbids(0);
                }else{
                    dart.cells[indiceX][indiceY][indiceZ].setType(DartCell.CELL_TYPE_TURBID_CROWN);
                }
                
            }
            /*
            if(densite == 0){
                //dart.cells[indiceX][indiceY][indiceZ].setNbTurbids(0);
                densite = 0.001f;
            }*/
            
            //densite = ((int)(densite*1000))/1000.0f;
            
            //test
            //dart.cells[indiceX][indiceY][indiceZ].setType(DartCell.CELL_TYPE_TURBID_CROWN);
            //densite = 0.5f;
            
            dart.cells[indiceX][indiceY][indiceZ].setTurbids(new Turbid[]{new Turbid(densite, 0)});
            
            if(faces != null){
                
                if(faces[indiceX][indiceY][indiceZ] != null){
                    
                    int[] figureIndices = new int[faces[indiceX][indiceY][indiceZ].size()];
                
                    Iterator<Integer> iterator = faces[indiceX][indiceY][indiceZ].iterator();
                    int count = 0;
                    while (iterator.hasNext()) {
                        Integer next = iterator.next();
                        figureIndices[count] = next;
                        count++;
                    }

                    dart.cells[indiceX][indiceY][indiceZ].setFigureIndex(figureIndices);
                }
            }
        }
        
        if(generateTrianglesFile && dtmFile != null){
            
            logger.info("Writing triangles file");
            
            if(dtm != null){
                
                List<Face> faceList = dtm.getFaces();
                List<DTMPoint> pointList = dtm.getPoints();
                
                int shapeType = 0; //triangle = 0 and parallelogram = 1
                int scattererType = 0;
                int scattererPropertyIndex = 0;
                int temperaturePropertyIndex = 0;
                int simpleOrDoubleFace = 0;
                int typeOfSurface = 2; //ground
                
                try(BufferedWriter writer = new BufferedWriter(new FileWriter(trianglesFile))) {
                    
                    for(Face face : faceList){
                                                
                        DTMPoint point1 = pointList.get(face.getPoint3());
                        DTMPoint point2 = pointList.get(face.getPoint2());
                        DTMPoint point3 = pointList.get(face.getPoint1());
                        
                        writer.write(shapeType+" "+(float)(point1.x-data.header.bottomCorner.x)+" "+(float)(point1.y-data.header.bottomCorner.y)+" "+(float)(point1.z-data.header.bottomCorner.z)+" "+
                                                    (float)(point2.x-data.header.bottomCorner.x)+" "+(float)(point2.y-data.header.bottomCorner.y)+" "+(float)(point2.z-data.header.bottomCorner.z)+" "+
                                                    (float)(point3.x-data.header.bottomCorner.x)+" "+(float)(point3.y-data.header.bottomCorner.y)+" "+(float)(point3.z-data.header.bottomCorner.z)+" "+
                                                    scattererType+" "+
                                                    scattererPropertyIndex+" "+
                                                    temperaturePropertyIndex+" "+
                                                    simpleOrDoubleFace+" "+
                                                    scattererType+" "+
                                                    scattererPropertyIndex+" "+
                                                    temperaturePropertyIndex+" "+
                                                    typeOfSurface+"\n");
                    }
                    
                } catch (IOException ex) {
                    logger.error("Cannot write triangles file : "+trianglesFile.getAbsolutePath(), ex);
                }
                
            }
        }
        
        logger.info("Writing dart file "+outputFile.getAbsolutePath());
        writeFromDart(dart, outputFile);
        logger.info("dart file written");
    }
}
