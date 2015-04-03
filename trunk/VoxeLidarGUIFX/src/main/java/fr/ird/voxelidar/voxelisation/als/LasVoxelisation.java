/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.als;

import fr.ird.voxelidar.voxelisation.raytracing.voxel.VoxelAnalysis;
import fr.ird.voxelidar.voxelisation.VoxelParameters;
import fr.ird.voxelidar.voxelisation.extraction.Shot;
import fr.ird.voxelidar.engine3d.object.scene.Dtm;
import fr.ird.voxelidar.engine3d.object.scene.DtmLoader;
import fr.ird.voxelidar.lidar.format.als.Las;
import fr.ird.voxelidar.lidar.format.als.LasHeader;
import fr.ird.voxelidar.lidar.format.als.LasReader;
import fr.ird.voxelidar.lidar.format.als.PointDataRecordFormat0;
import fr.ird.voxelidar.engine3d.math.matrix.Mat;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.util.Filter;
import fr.ird.voxelidar.util.Processing;
import fr.ird.voxelidar.util.ProcessingListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class LasVoxelisation extends Processing implements Runnable{
    
    private final Logger logger = Logger.getLogger(LasVoxelisation.class);
    
    private Las las;
    private final File lasFile;
    private final Mat4D popMatrix;
    private final File trajectoryFile;
    private final File outputFile;
    private final VoxelParameters parameters;
    private VoxelAnalysis voxelAnalysis;
    private LinkedBlockingQueue<Shot> queue;

    public LasVoxelisation(File lasFile, File outputFile, Mat4D transfMatrix, File trajectoryFile, VoxelParameters parameters, List<Filter> filters) {

        this.lasFile = lasFile;
        this.outputFile = outputFile;
        this.popMatrix = transfMatrix;
        this.trajectoryFile = trajectoryFile;
        this.parameters = parameters;
        
        queue = new LinkedBlockingQueue<>();
        
        Dtm terrain = null;
        
        if(parameters.getDtmFile() != null && parameters.useDTMCorrection() ){
            
            try {
                terrain = DtmLoader.readFromAscFile(parameters.getDtmFile());
                terrain.setTransformationMatrix(transfMatrix);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        
        
        voxelAnalysis = new VoxelAnalysis(queue, terrain, filters);
    }

    @Override
    public File process() {
        
                
        final long start_time = System.currentTimeMillis();
        BlockingQueue<fr.ird.voxelidar.voxelisation.extraction.LasPoint> arrayBlockingQueue = new LinkedBlockingQueue<>();
        
        voxelAnalysis.init(parameters, outputFile);
        final LasConversion conversion = new LasConversion(arrayBlockingQueue, queue, trajectoryFile, lasFile, popMatrix);
        
        try {
            
            conversion.addProcessingListener(new ProcessingListener() {

                @Override
                public void processingFinished() {
                    
                    voxelAnalysis.setIsFinished(true);
                }

                @Override
                public void processingStepProgress(String progress, int ratio) {
                    fireProgress(progress, ratio);
                }
            });
            /*
            LasExtraction extraction = new LasExtraction(arrayBlockingQueue, lasFile.getAbsolutePath());
            
            extraction.addLasExtractionListener(new LasExtractionListener() {

                @Override
                public void isFinished() {
                    conversion.setIsFinished(true);
                }
            });
            */

            //runnable to do the extraction
            //Thread t1 = new Thread(extraction);
            //t1.start();
            //t1.join();
            
            Thread t2 = new Thread(conversion);
            
            t2.start();
            //t2.join();
            
            Thread t = new Thread(voxelAnalysis);
            t.start();
            
            //wait until voxelisation finished
            t.join();
            
            return outputFile;
            
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage());
        }
        
        return null;
    }

    private double dfdist(double x_s, double y_s, double z_s, double x, double y, double z) {

        double result = Math.sqrt(Math.pow(x_s - x, 2) + Math.pow((y_s - y), 2) + Math.pow((z_s - z), 2));

        return result;
    }
    
    private ArrayList<LasPoint> readLas(Las lasFile) {
        
        ArrayList<LasPoint> lasPointList = new ArrayList<>();

        ArrayList<? extends PointDataRecordFormat0> pointDataRecords = lasFile.getPointDataRecords();
        LasHeader header = lasFile.getHeader();
        for (PointDataRecordFormat0 p : pointDataRecords) {
            
            Vector3d location = new Vector3d((p.getX() * header.getxScaleFactor()) + header.getxOffset(), (p.getY() * header.getyScaleFactor()) + header.getyOffset(), (p.getZ() * header.getzScaleFactor()) + header.getzOffset());
            LasPoint point = new LasPoint(location, p.getReturnNumber(), p.getNumberOfReturns(), p.getIntensity(), p.getClassification(), p.getGpsTime());
            
            lasPointList.add(point);
        }

        return lasPointList;
    }
    
    /*
    private ArrayList<Las2> readLas(Las lasFile) {
        ArrayList<Las2> lasList = new ArrayList<>();

        ArrayList<? extends PointDataRecordFormat0> pointDataRecords = lasFile.getPointDataRecords();
        LasHeader header = lasFile.getHeader();
        for (PointDataRecordFormat0 p : pointDataRecords) {

            Vec3D location = new Vec3D((p.getX() * header.getxScaleFactor()) + header.getxOffset(), (p.getY() * header.getyScaleFactor()) + header.getyOffset(), (p.getZ() * header.getzScaleFactor()) + header.getzOffset());
            Las2 las = new Las2(location, p.getIntensity(),
                    p.getReturnNumber(), p.getNumberOfReturns(), p.getClassification(), p.getScanAngleRank(), p.getPointSourceID(), p.getGpsTime());
            lasList.add(las);
        }

        return lasList;
    }
    */
    public void writeLocalAls(String outputFilePath, ArrayList<Vector3d> localCoordinates, ArrayList<LasTxtFormat> lasList) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFilePath)))) {

            for (int i = 0; i < lasList.size(); i++) {

                writer.write(localCoordinates.get(i).x + " " + localCoordinates.get(i).y + " " + localCoordinates.get(i).z + " "
                        + lasList.get(i).i + " " + lasList.get(i).r + " "
                        + lasList.get(i).n + " " + lasList.get(i).c + " "
                        + lasList.get(i).a + " " + lasList.get(i).p + "\n");
            }

            writer.close();

        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    /*
    public static Mat4D getMatrixTransformation(Vec3D point1, Vec3D point2) {

        Vec2D v = new Vec2D(point1.x - point2.x, point1.y - point2.y);
        double rho = (double) Math.atan(v.x / v.y);

        Vec3D trans = new Vec3D(-point2.x, -point2.y, -point2.z);
        trans.z = 0; //no vertical translation

        Mat4D mat4x4Rotation = new Mat4D();
        Mat4D mat4x4Translation = new Mat4D();

        mat4x4Rotation.mat = new double[]{
            (double) Math.cos(rho), (double) -Math.sin(rho), 0, 0,
            (double) Math.sin(rho), (double) Math.cos(rho), 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        };

        mat4x4Translation.mat = new double[]{
            1, 0, 0, trans.x,
            0, 1, 0, trans.y,
            0, 0, 1, trans.z,
            0, 0, 0, 1
        };

        Mat4D mat4x4 = Mat4D.multiply(mat4x4Translation, mat4x4Rotation);

        return mat4x4;
    }
    */
    private int searchNearestMax(double value, ArrayList<Double> list, int start){
        
        
        int indexMin = start;
        int indexMax = list.size() - 1;
        
        int index = ((indexMax - indexMin)/2) + indexMin;
        
        boolean found = false;
                
        while(!found){
            
            double currentValue = list.get(index);
            
            if(list.get(index) < value){
                
                indexMin = index;
                index = (indexMax + (index))/2;

            }else if(list.get(index) > value && list.get(index-1) > value){
                
                indexMax = index;
                index = (indexMin + (index))/2;
                
            }else{
                found = true;
            }
            
            if(indexMin == indexMax-1){
                
                index = indexMax-1;
                found = true;
            }else if(indexMin == indexMax){
                index = indexMin;
                found = true;
            }
        }
        
        return index;
    }    

    @Override
    public void run() {
        
        setStepNumber(5);
        
        fireProgress("Reading *.las", getProgression());
        las = LasReader.read(lasFile);
        ArrayList<LasPoint> lasPointList = readLas(las);
        
        Map<Double, Trajectory> trajectoryList = new TreeMap<>();
        ArrayList<LasShot> ALL = new ArrayList<>();
        
        Collections.sort(lasPointList, new Comparator<LasPoint>() {

            @Override
            public int compare(LasPoint o1, LasPoint o2) {
                if (o2.t < o1.t) {
                    return 1;
                } else if (o2.t > o1.t) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        
        double minTime = lasPointList.get(0).t;
        double maxTime = lasPointList.get(lasPointList.size()-1).t;
        
        fireProgress("Reading trajectory file", getProgression());
        
        try {

            BufferedReader reader = new BufferedReader(new FileReader(trajectoryFile));

            String line;

            //skip header
            reader.readLine();

            while ((line = reader.readLine()) != null) {

                String[] lineSplit = line.split(",");
                Trajectory traj = new Trajectory(Double.valueOf(lineSplit[0]), Double.valueOf(lineSplit[1]),
                        Double.valueOf(lineSplit[2]));
                
                //if(traj.T >= minTime && traj.T <= maxTime){
                    Double time = Double.valueOf(lineSplit[3]);
                    
                    if(time >= minTime-0.01 && time <= maxTime+0.01){
                        trajectoryList.put(time, traj);
                    }
                    
                //}
            }

        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
        
        fireProgress("Merging (interpolation)", getProgression());
        
        ArrayList<Vector3d> trajectoryInterpolate = new ArrayList<>();
        
        ArrayList<Double> tgps = new ArrayList<>();
        
        for (Map.Entry<Double, Trajectory> entry : trajectoryList.entrySet()) {
            tgps.add(entry.getKey());
        }
        
        Collections.sort(tgps);
        
        int index = 0;
        
        for (LasPoint lasPoint : lasPointList) {
            
            double targetTime = lasPoint.t;
            index = searchNearestMax(targetTime, tgps, index);
            double max = tgps.get(index);
            double min = tgps.get(index-1);
            double ratio = (lasPoint.t - min) / (max - min);
            
            //formule interpolation
            double xValue = trajectoryList.get(min).x + ((trajectoryList.get(max).x - trajectoryList.get(min).x) * ratio);
            double yValue = trajectoryList.get(min).y + ((trajectoryList.get(max).y - trajectoryList.get(min).y) * ratio);
            double zValue = trajectoryList.get(min).z + ((trajectoryList.get(max).z - trajectoryList.get(min).z) * ratio);
            
            trajectoryInterpolate.add(new Vector3d(xValue, yValue, zValue));
        }
        
        
        Mat loc_coord_offsetTraj = new Mat(4, lasPointList.size());
        Mat loc_coord_offsetLas = new Mat(4, lasPointList.size());
        
        int compteur = 0;
        for (LasPoint lasPoint : lasPointList) {

            LasShot mix = new LasShot(lasPoint, 0, 0, 0);
            
            loc_coord_offsetTraj.mat[0][compteur] = trajectoryInterpolate.get(compteur).x;
            loc_coord_offsetTraj.mat[1][compteur] = trajectoryInterpolate.get(compteur).y;
            loc_coord_offsetTraj.mat[2][compteur] = trajectoryInterpolate.get(compteur).z;
            loc_coord_offsetTraj.mat[3][compteur] = 1;
            
            loc_coord_offsetLas.mat[0][compteur] = lasPoint.location.x;
            loc_coord_offsetLas.mat[1][compteur] = lasPoint.location.y;
            loc_coord_offsetLas.mat[2][compteur] = lasPoint.location.z;
            loc_coord_offsetLas.mat[3][compteur] = 1;

            ALL.add(mix);

            compteur++;
        }
        
        fireProgress("Voxelisation", getProgression());
        
        loc_coord_offsetTraj = Mat.multiply(popMatrix.toMat(), loc_coord_offsetTraj);
        loc_coord_offsetLas = Mat.multiply(popMatrix.toMat(), loc_coord_offsetLas);
        
        double oldTime = -1;
        int oldN = -1;

        compteur = 0;
        
        Shot e = null;
        boolean isNewExp = false;
        
        
        voxelAnalysis.init(parameters, outputFile);

        for (int i = 0; i < loc_coord_offsetTraj.columnNumber; i++) {
            
            LasShot all = ALL.get(i);
            
            /*#################################
            #reproject source points in LOCS#
            #################################*/
            
            all.xloc_s = loc_coord_offsetTraj.mat[0][i];
            all.yloc_s = loc_coord_offsetTraj.mat[1][i];
            all.zloc_s = loc_coord_offsetTraj.mat[2][i];
            
            all.xloc = loc_coord_offsetLas.mat[0][i];
            all.yloc = loc_coord_offsetLas.mat[1][i];
            all.zloc = loc_coord_offsetLas.mat[2][i];
            
            all.range = dfdist(all.xloc_s, all.yloc_s, all.zloc_s, all.xloc, all.yloc, all.zloc);

            all.x_u = (all.xloc - all.xloc_s) / all.range;
            all.y_u = (all.yloc - all.yloc_s) / all.range;
            all.z_u = (all.zloc - all.zloc_s) / all.range;
            
            double time = all.lasPoint.t;

            if (isNewExp && time != oldTime) {

                /**
                 * *vérifie que le nombre d'échos lus correspond bien au nombre
                 * d'échos total* permet d'éviter le plantage du programme de
                 * voxelisation est-ce pertinent? possible perte d'imformations
                 * modifier le programme de voxelisation de Jean Dauzat si on ne
                 * veut pas nettoyer
                 *
                 */
                if (oldN == compteur) {
                    try {
                        queue.put(e);
                        //voxeliseOne(e);
                        //shoots.put(String.valueOf(oldTime), e);
                    } catch (InterruptedException ex) {
                        logger.error(ex);
                    }
                }
                compteur = 0;
                isNewExp = false;

            }

            if (time == oldTime || (!isNewExp && time != oldTime)) {

                if (!isNewExp && time != oldTime) {
                    
                    e= new Shot(all.lasPoint.n, new Point3d(all.xloc_s, all.yloc_s, all.zloc_s), 
                                                new Vector3d(all.x_u, all.y_u, all.z_u), 
                                                new double[all.lasPoint.n]);
                    //e = new Shot(all.lasPoint.n, all.xloc_s, all.yloc_s, all.zloc_s, all.x_u, all.y_u, all.z_u);
                    isNewExp = true;
                }

                switch (all.lasPoint.r) {

                    case 1:
                        e.ranges[0] = all.range;
                        //e.r1 = all.range;
                        compteur++;
                        break;
                    case 2:
                        e.ranges[1] = all.range;
                        //e.r2 = all.range;
                        compteur++;
                        break;
                    case 3:
                        e.ranges[2] = all.range;
                        //e.r3 = all.range;
                        compteur++;
                        break;
                    case 4:
                        e.ranges[3] = all.range;
                        //e.r4 = all.range;
                        compteur++;
                        break;
                    case 5:
                        e.ranges[4] = all.range;
                        //e.r5 = all.range;
                        compteur++;
                        break;
                    case 6:
                        e.ranges[5] = all.range;
                        //e.r6 = all.range;
                        compteur++;
                        break;
                    case 7:
                        e.ranges[6] = all.range;
                        //e.r7 = all.range;
                        compteur++;
                        break;
                }

            }

            oldTime = time;
            oldN = all.lasPoint.n;
        }
        
        //fireProgress("Writing file", getProgression());
        //voxelAnalysis.calculatePADAndWrite(0);
        
        fireFinished();
        
        //ArrayList<LasMixTrajectory> ALL = transformPoints(popMatrix, lasPointList, trajectoryFile);
        
        //fireProgress("Building echos", 50);
        //Map<String, Shot> shoots = buildEchos(ALL);
        
        //fireProgress("Voxelisation", 75);
        //voxelise(shoots);
    }
}
