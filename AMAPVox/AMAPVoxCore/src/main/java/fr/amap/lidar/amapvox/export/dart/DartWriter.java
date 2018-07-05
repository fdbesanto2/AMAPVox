/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.export.dart;

import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.point.Point2F;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.point.Point3I;
import fr.amap.commons.math.geometry.BoundingBox2F;
import fr.amap.commons.raster.asc.Point;
import fr.amap.commons.raster.asc.AsciiGridHelper;
import fr.amap.commons.raster.asc.Face;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.util.io.LittleEndianOutputStream;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.commons.VoxelSpace;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxreader.VoxelFileReader;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.apache.log4j.Logger;
/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class DartWriter {
    
    private final static Logger LOGGER = Logger.getLogger(DartWriter.class);
    
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
    
    public void writeFromDart(Dart dart, File outputFile) throws IOException{
        
        BufferedWriter writer;
        try {
            
            writer = new BufferedWriter(new FileWriter(outputFile));
            
            writer.write(dart.getSceneDimension().x*dart.getCellDimension().x+" "+
                        dart.getSceneDimension().y*dart.getCellDimension().y+" "+
                        dart.getSceneDimension().z*dart.getCellDimension().z+"\n");
            
            writer.write(dart.getCellDimension().x+" "+dart.getCellDimension().y+" "+dart.getCellDimension().z+"\n");
            writer.write(dart.getCellsNumberByLayer()+"\n");
            
            
            for (int z = 0; z < dart.getSceneDimension().z; z++) {

                for (int x = 0; x < dart.getSceneDimension().x; x++) {
                    //for(int y=dart.getSceneDimension().y-1; y>=0; y--){
                    for (int y = 0; y < dart.getSceneDimension().y; y++) {

                        DartCell cell = dart.cells[x][y][z];

                        String stringToWrite = "";

                        if (cell.getType() != DartCell.CELL_TYPE_EMPTY) {

                            String turbids = "";

                            for (int i = 0; i < cell.getNbTurbids(); i++) {

                                if (cell.getTurbids()[i].LAI == 0) {
                                    turbids += " " + "0" + " " + cell.getTurbids()[i].leafPhaseFunction + " 0";
                                } else {
                                    turbids += " " + cell.getTurbids()[i].LAI + " " + cell.getTurbids()[i].leafPhaseFunction + " 0";
                                }

                            }

                            if (cell.getNbTurbids() == 0) {
                                turbids += " 0";
                            }

                            String figures = "";

                            for (int i = 0; i < cell.getNbFigures(); i++) {
                                figures += " " + cell.getFigureIndex()[i];
                            }

                            stringToWrite = cell.getType() + " " + cell.getNbFigures() + figures + " " + cell.getNbTurbids() + turbids + " ";

                        } else {
                            stringToWrite = "0" + " ";
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
            throw ex;
        }
    }
    
    /*public void writeFromVoxelFile(File voxelFile, File outputFile) throws IOException, Exception{
        
        VoxelFileReader reader = new VoxelFileReader(voxelFile);
        VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();
        
        Dart dart = new Dart(
                new Point3I(infos.getSplit().x, infos.getSplit().y, infos.getSplit().z),
                new Point3F(infos.getResolution(), infos.getResolution(), infos.getResolution()),
                infos.getSplit().x*infos.getSplit().y);
        
        Raster dtm = null;
        
        Set<Integer>[][][] faces = null;
        
        if(generateTrianglesFile && dtmFile != null){
            
            try {
                //logger.info("Reading DTM : "+dtmFile.getAbsolutePath());
                
                dtm = AsciiGridHelper.readFromAscFile(dtmFile);
                dtm.setTransformationMatrix(transfMatrix);
                
                dtm.setLimits(new BoundingBox2F(new Point2F((float)infos.getMinCorner().x, (float)infos.getMinCorner().y), 
                                                new Point2F((float)infos.getMaxCorner().x, (float)infos.getMaxCorner().y)), 10);
                
                //logger.info("Building DTM");
                dtm.buildMesh();
                //dtm.exportObj(new File("/home/calcul/Documents/Julien/test.obj"));
                
                faces = new HashSet[infos.getSplit().x][infos.getSplit().y][infos.getSplit().z];
                List<DTMPoint> points = dtm.getPoints();
                
                //VoxelSpaceData data = new VoxelSpaceData();
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
                dtm = null;
                throw new Exception("Cannot read dtm file "+dtmFile.getAbsolutePath(), ex);
            }
        }
        
        Iterator<Voxel> iterator = reader.iterator();
        
        while(iterator.hasNext()){
            
            Voxel voxel = iterator.next();
            
            float densite = voxel.PadBVTotal;
            
            densite *= infos.getResolution();
            
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
            
            dart.cells[indiceX][indiceY][indiceZ].setTurbids(new Turbid[]{new Turbid(densite, 0)});
            
            if(faces != null){
                
                if(faces[indiceX][indiceY][indiceZ] != null){
                    
                    int[] figureIndices = new int[faces[indiceX][indiceY][indiceZ].size()];
                
                    Iterator<Integer> facesIterator = faces[indiceX][indiceY][indiceZ].iterator();
                    int count = 0;
                    while (facesIterator.hasNext()) {
                        Integer next = facesIterator.next();
                        figureIndices[count] = next;
                        count++;
                    }

                    dart.cells[indiceX][indiceY][indiceZ].setFigureIndex(figureIndices);
                }
            }
        }
        
        if(generateTrianglesFile && dtmFile != null){
            
            //logger.info("Writing triangles file");
            
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
                        
                        writer.write(shapeType+" "+(float)(point1.x-infos.getMinCorner().x)+" "+(float)(point1.y-infos.getMinCorner().y)+" "+(float)(point1.z-infos.getMinCorner().z)+" "+
                                                    (float)(point2.x-infos.getMinCorner().x)+" "+(float)(point2.y-infos.getMinCorner().y)+" "+(float)(point2.z-infos.getMinCorner().z)+" "+
                                                    (float)(point3.x-infos.getMinCorner().x)+" "+(float)(point3.y-infos.getMinCorner().y)+" "+(float)(point3.z-infos.getMinCorner().z)+" "+
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
                    throw new IOException("Cannot write triangles file : "+trianglesFile.getAbsolutePath(), ex);
                }
                
            }
        }
        
        //logger.info("Writing dart file "+outputFile.getAbsolutePath());
        writeFromDart(dart, outputFile);
        
        
    }*/
    
    public void writeFromVoxelFile(File voxelFile, File outputFile) throws IOException, Exception{
        
        VoxelFileReader reader = new VoxelFileReader(voxelFile, true);
        Iterator<Voxel> iterator = reader.iterator(); //load voxel space into memory
        while(iterator.hasNext()){
            iterator.next();
        }
        
        writeFromVoxelSpace(reader.voxelSpace, outputFile);
    }
    
    public void writeFromVoxelSpace(VoxelSpace data, File outputFile) throws Exception{
        
        VoxelSpaceInfos infos = data.getVoxelSpaceInfos();
        
        Dart dart = new Dart(
                new Point3I(infos.getSplit().x, infos.getSplit().y, infos.getSplit().z),
                new Point3F((float)infos.getVoxelSize().x, (float)infos.getVoxelSize().y, (float)infos.getVoxelSize().z),
                infos.getSplit().x * infos.getSplit().y);
        
        List<String> attributsNames = new ArrayList<>();
        
        for(String s : infos.getColumnNames()){
            attributsNames.add(s);
        }
        
        Raster dtm = null;
        
        Set<Integer>[][][] faces = null; //faces of a voxel
        
        if(generateTrianglesFile && dtmFile != null){
            
            try {
                //logger.info("Reading DTM : "+dtmFile.getAbsolutePath());
                
                dtm = AsciiGridHelper.readFromAscFile(dtmFile);
                dtm.setTransformationMatrix(transfMatrix);
                
                dtm.setLimits(new BoundingBox2F(new Point2F((float)infos.getMinCorner().x, (float)infos.getMinCorner().y), 
                                                new Point2F((float)infos.getMaxCorner().x, (float)infos.getMaxCorner().y)), 1);
                
                //logger.info("Building DTM");
                dtm.buildMesh();
                //dtm.exportObj(new File("/home/calcul/Documents/Julien/test.obj"));
                
                faces = new HashSet[infos.getSplit().x][infos.getSplit().y][infos.getSplit().z];
                List<Point> points = dtm.getPoints();
                
                for(Point point : points){
                    
                    Point3I voxelIndice = data.getIndicesFromPoint(point.x, point.y, point.z);
                    if(voxelIndice != null){
                        
                        if(faces[voxelIndice.x][voxelIndice.y][voxelIndice.z] == null){
                            faces[voxelIndice.x][voxelIndice.y][voxelIndice.z] = new HashSet<>();
                        }
                        
                        faces[voxelIndice.x][voxelIndice.y][voxelIndice.z].addAll(point.faces);
                    }
                }
                
            } catch (Exception ex) {
                dtm = null;
                throw new Exception("Cannot read dtm file "+dtmFile.getAbsolutePath(), ex);
            }
        }
        
        for (Iterator it = data.voxels.iterator(); it.hasNext();) {
            
            Voxel voxel = (Voxel) it.next();
            
            float densite;
            try{
                densite = voxel.PadBVTotal;
            }catch(Exception e){ 
                throw new Exception("could not find attribut PadBflTotal or PadBVTotal", e);
            }
            densite *= infos.getVoxelSize().z;
            int indiceX = voxel.i;
            int indiceY = voxel.j;
            int indiceZ = voxel.k;
            dart.cells[indiceX][indiceY][indiceZ] = new DartCell();
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
                    dart.cells[indiceX][indiceY][indiceZ].setType(DartCell.CELL_TYPE_DEM_GROUND);
                    dart.cells[indiceX][indiceY][indiceZ].setNbTurbids(0);
                }
                
                densite = 0f;
            }else{
                if(nbFigures > 0){
                    dart.cells[indiceX][indiceY][indiceZ].setType(DartCell.CELL_TYPE_DEM_GROUND);
                    dart.cells[indiceX][indiceY][indiceZ].setNbTurbids(0);
                }else{
                    dart.cells[indiceX][indiceY][indiceZ].setType(DartCell.CELL_TYPE_TURBID_CROWN);
                }
                
            }
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
            
            //logger.info("Writing triangles file");
            
            if(dtm != null){
                
                List<Face> faceList = dtm.getFaces();
                List<Point> pointList = dtm.getPoints();
                
                int shapeType = 0; //triangle = 0 and parallelogram = 1
                int scattererType = 0;
                int scattererPropertyIndex = 0;
                int temperaturePropertyIndex = 0;
                int simpleOrDoubleFace = 0;
                int typeOfSurface = 2; //ground
                
                try(LittleEndianOutputStream writer = new LittleEndianOutputStream(new BufferedOutputStream(new FileOutputStream(trianglesFile)))) {
                    
                    for(Face face : faceList){
                                                
                        Point point1 = pointList.get(face.getPoint3());
                        Point point2 = pointList.get(face.getPoint2());
                        Point point3 = pointList.get(face.getPoint1());
                        
                        writer.writeBoolean(shapeType != 0);
                        
                        
                        writer.writeDouble(point1.x-infos.getMinCorner().x);
                        writer.writeDouble(point1.y-infos.getMinCorner().y);
                        writer.writeDouble(point1.z-infos.getMinCorner().z);
                        
                        writer.writeDouble(point2.x-infos.getMinCorner().x);
                        writer.writeDouble(point2.y-infos.getMinCorner().y);
                        writer.writeDouble(point2.z-infos.getMinCorner().z);
                        
                        writer.writeDouble(point3.x-infos.getMinCorner().x);
                        writer.writeDouble(point3.y-infos.getMinCorner().y);
                        writer.writeDouble(point3.z-infos.getMinCorner().z);
                        
                        writer.writeInt(scattererType);
                        writer.writeInt(scattererPropertyIndex);
                        writer.writeInt(temperaturePropertyIndex);
                        writer.writeBoolean(simpleOrDoubleFace != 0);
                        writer.writeInt(scattererType);
                        writer.writeInt(scattererPropertyIndex);
                        writer.writeInt(temperaturePropertyIndex);
                        writer.writeInt(typeOfSurface);
                    }
                    
                    LOGGER.info(faceList.size()+" faces have been written.");
                    
                } catch (IOException ex) {
                    throw new IOException("Cannot write triangles file : "+trianglesFile.getAbsolutePath(), ex);
                }
                
//                try(BufferedWriter writer = new BufferedWriter(new FileWriter(trianglesFile))) {
//                    
//                    for(Face face : faceList){
//                                                
//                        Point point1 = pointList.get(face.getPoint3());
//                        Point point2 = pointList.get(face.getPoint2());
//                        Point point3 = pointList.get(face.getPoint1());
//                        
//                        writer.write(shapeType+" "+(float)(point1.x-infos.getMinCorner().x)+" "+(float)(point1.y-infos.getMinCorner().y)+" "+(float)(point1.z-infos.getMinCorner().z)+" "+
//                                                    (float)(point2.x-infos.getMinCorner().x)+" "+(float)(point2.y-infos.getMinCorner().y)+" "+(float)(point2.z-infos.getMinCorner().z)+" "+
//                                                    (float)(point3.x-infos.getMinCorner().x)+" "+(float)(point3.y-infos.getMinCorner().y)+" "+(float)(point3.z-infos.getMinCorner().z)+" "+
//                                                    scattererType+" "+
//                                                    scattererPropertyIndex+" "+
//                                                    temperaturePropertyIndex+" "+
//                                                    simpleOrDoubleFace+" "+
//                                                    scattererType+" "+
//                                                    scattererPropertyIndex+" "+
//                                                    temperaturePropertyIndex+" "+
//                                                    typeOfSurface+"\n");
//                    }
//                    
//                } catch (IOException ex) {
//                    throw new IOException("Cannot write triangles file : "+trianglesFile.getAbsolutePath(), ex);
//                }
                
            }
        }
        
        //logger.info("Writing dart file "+outputFile.getAbsolutePath());
        writeFromDart(dart, outputFile);
        //logger.info("dart file written");
    }
}
