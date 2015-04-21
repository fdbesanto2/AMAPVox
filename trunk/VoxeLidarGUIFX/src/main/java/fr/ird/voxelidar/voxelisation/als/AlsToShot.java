/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.als;

import fr.ird.voxelidar.voxelisation.extraction.Shot;
import fr.ird.voxelidar.io.file.FileManager;
import fr.ird.voxelidar.lidar.format.als.Las;
import fr.ird.voxelidar.lidar.format.als.LasHeader;
import fr.ird.voxelidar.lidar.format.als.LasReader;
import fr.ird.voxelidar.lidar.format.als.PointDataRecordFormat0;
import fr.ird.voxelidar.lidar.format.als.QLineExtrabytes;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.math.vector.Vec4D;
import fr.ird.voxelidar.util.Processing;
import fr.ird.voxelidar.voxelisation.extraction.als.LazExtraction;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class AlsToShot extends Processing implements Runnable{
    
    private final Logger logger = Logger.getLogger(AlsToShot.class);
    
    private final float RATIO_REFLECTANCE_VEGETATION_SOL = 0.4f;
    
    private Las las;
    private final File alsFile;
    private final File trajectoryFile;
    private final Mat4D popMatrix;
    private final BlockingQueue<Shot> queue;
    private boolean isFinished;

    public void setIsFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }
    
    public AlsToShot(BlockingQueue<Shot> queue,File trajectoryFile, File alsFile, Mat4D popMatrix){

        this.trajectoryFile = trajectoryFile;
        this.popMatrix = popMatrix;
        this.alsFile = alsFile;
        this.queue = queue;
        isFinished = false;
    }

    @Override
    public void run() {
        
        
        setStepNumber(3);
        
        fireProgress("Reading *.las", 0);
        
        int iterations = 0;
        long maxIterations;
        int step;
        
        /***reading las***/
        ArrayList<LasPoint> lasPointList = new ArrayList<>();
        LasHeader header;
        
        switch(FileManager.getExtension(alsFile)){
            case ".las":
                
                LasReader lasReader = new LasReader();

                lasReader.open(alsFile);
                header = lasReader.getHeader();

                maxIterations = header.getNumberOfPointrecords();
                step = (int) (maxIterations/10);

                for (PointDataRecordFormat0 p : lasReader) {

                    if(iterations % step == 0){
                        fireProgress("Reading *.las", (int) ((iterations*100)/(float)maxIterations));
                    }

                    if(p.isHasQLineExtrabytes()){
                        QLineExtrabytes qLineExtrabytes = p.getQLineExtrabytes();
                        logger.info("QLineExtrabytes" + qLineExtrabytes.getAmplitude()+" "+qLineExtrabytes.getPulseWidth());
                    }
                    Vector3d location = new Vector3d((p.getX() * header.getxScaleFactor()) + header.getxOffset(), (p.getY() * header.getyScaleFactor()) + header.getyOffset(), (p.getZ() * header.getzScaleFactor()) + header.getzOffset());


                    LasPoint point = new LasPoint(location.x, location.y, location.z, p.getReturnNumber(), p.getNumberOfReturns(), p.getIntensity(), p.getClassification(), p.getGpsTime());
                    lasPointList.add(point);

                    iterations++;
                }
                break;
                
            case ".laz":
                
                LazExtraction laz = new LazExtraction();
                laz.openLazFile(alsFile);
                
                header = laz.getHeader();
                
                for (LasPoint p : laz) {
                    
                    p.x = (p.x * header.getxScaleFactor()) + header.getxOffset();
                    p.y = (p.y * header.getyScaleFactor()) + header.getyOffset();
                    p.z = (p.z * header.getzScaleFactor()) + header.getzOffset();
                    
                    lasPointList.add(p);
                }
                laz.close();
                        
                break;
        }
        
        /***sort las by time***/
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
        
        
        /***reading trajectory file***/
        
        fireProgress("Reading trajectory file", 0);
        
        Map<Double, Trajectory> trajectoryMap = new TreeMap<>();
        
        try {
            maxIterations = FileManager.getLineNumber(trajectoryFile.getAbsolutePath());
            step = (int) (maxIterations/10);
            iterations = 0;
            
            BufferedReader reader = new BufferedReader(new FileReader(trajectoryFile));

            String line;

            //skip header
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                
                if(iterations % step == 0){
                    fireProgress("Reading trajectory file", (int) ((iterations*100)/(float)maxIterations));
                }
                
                line = line.replaceAll(",", " ");
                String[] lineSplit = line.split(" ");
                Trajectory traj = new Trajectory(Double.valueOf(lineSplit[0]), Double.valueOf(lineSplit[1]),
                        Double.valueOf(lineSplit[2]));
                
                Double time = Double.valueOf(lineSplit[3]);

                //troncate unused values
                if(time >= minTime-0.01 && time <= maxTime+0.01){
                    trajectoryMap.put(time, traj);
                }
                
                iterations++;
            }

        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
        
        ArrayList<Double> tgps = new ArrayList<>();
        
        for (Map.Entry<Double, Trajectory> entry : trajectoryMap.entrySet()) {
            tgps.add(entry.getKey());
        }
        
        double oldTime = -1;
        int oldN = -1;
        int index = 0;
        int shotId = 0;
        
        Shot e = null;
        boolean isNewExp = false;
        
        int count = 0;
        iterations = 0;
        maxIterations = lasPointList.size();
        step = (int) (maxIterations/10);
        
        fireProgress("Voxelisation", getProgression());
        
        for (LasPoint lasPoint : lasPointList) {
            
            
            if(iterations % step == 0){
                fireProgress("Voxelisation", (int) ((iterations*100)/(float)maxIterations));
            }
            
            double targetTime = lasPoint.t;
            index = searchNearestMax(targetTime, tgps, index);
            double max = tgps.get(index);
            double min = tgps.get(index-1);
            double ratio = (lasPoint.t - min) / (max - min);
            
            //formule interpolation
            double xValue = trajectoryMap.get(min).x + ((trajectoryMap.get(max).x - trajectoryMap.get(min).x) * ratio);
            double yValue = trajectoryMap.get(min).y + ((trajectoryMap.get(max).y - trajectoryMap.get(min).y) * ratio);
            double zValue = trajectoryMap.get(min).z + ((trajectoryMap.get(max).z - trajectoryMap.get(min).z) * ratio);
            
            //trajectoryInterpolate.add(new Vec3D(xValue, yValue, zValue));
            
            LasShot mix = new LasShot(lasPoint, 0, 0, 0);
            
            Vec4D trajTransform = Mat4D.multiply(popMatrix, 
                    new Vec4D(xValue, 
                            yValue, 
                            zValue, 1));
            
            Vec4D lasTransform = Mat4D.multiply(popMatrix, 
                    new Vec4D(lasPoint.x, 
                            lasPoint.y, 
                            lasPoint.z, 1));
            
            mix.xloc_s = trajTransform.x;
            mix.yloc_s = trajTransform.y;
            mix.zloc_s = trajTransform.z;
            
            mix.xloc = lasTransform.x;
            mix.yloc = lasTransform.y;
            mix.zloc = lasTransform.z;
            
            mix.range = dfdist(mix.xloc_s, mix.yloc_s, mix.zloc_s, mix.xloc, mix.yloc, mix.zloc);

            mix.x_u = (mix.xloc - mix.xloc_s) / mix.range;
            mix.y_u = (mix.yloc - mix.yloc_s) / mix.range;
            mix.z_u = (mix.zloc - mix.zloc_s) / mix.range;
            
            double time = mix.lasPoint.t;

            if (isNewExp && time != oldTime) {

                /**
                 * *vérifie que le nombre d'échos lus correspond bien au nombre
                 * d'échos total* permet d'éviter le plantage du programme de
                 * voxelisation est-ce pertinent? possible perte d'imformations
                 * modifier le programme de voxelisation de Jean Dauzat si on ne
                 * veut pas nettoyer
                 *
                 */
                if (oldN == count) {
                    try {
                        /*
                        String rangesString = "";
                        for(int i=0;i<e.ranges.length;i++){
                            rangesString += e.ranges[i]+" ";
                        }
                        rangesString = rangesString.trim();
                        */
                        //writer.write(shotId+" "+e.nbEchos+" "+e.origin.x+" "+e.origin.y+" "+e.origin.z+" "+e.direction.x+" "+e.direction.y+" "+e.direction.z+" "+rangesString+"\n");
                        queue.put(e);
                        
                        shotId ++;
                        
                    } catch (InterruptedException ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
                count = 0;
                isNewExp = false;

            }

            if (time == oldTime || (!isNewExp && time != oldTime)) {

                if (!isNewExp && time != oldTime) {
                    
                    e= new Shot(mix.lasPoint.n, new Point3d(mix.xloc_s, mix.yloc_s, mix.zloc_s), 
                                                new Vector3d(mix.x_u, mix.y_u, mix.z_u), 
                                                new double[mix.lasPoint.n], new short[mix.lasPoint.n], new int[mix.lasPoint.n]);
                    e.calculateAngle();
                    
                    isNewExp = true;
                }
                
                if(mix.lasPoint.r - 1 < 0){
                    
                }else if(mix.lasPoint.r <= mix.lasPoint.n){
                    
                    int currentEchoIndex = mix.lasPoint.r-1;
                    
                    e.ranges[currentEchoIndex] = mix.range;
                    e.classifications[currentEchoIndex] = mix.lasPoint.classification;
                    
                    if(e.classifications[currentEchoIndex] == LasPoint.CLASSIFICATION_GROUND){
                        e.intensities[currentEchoIndex] = (int) (mix.lasPoint.i * RATIO_REFLECTANCE_VEGETATION_SOL);
                    }else{
                        e.intensities[currentEchoIndex] = mix.lasPoint.i;
                    }
                    count++;
                }
            }

            oldTime = time;
            oldN = mix.lasPoint.n;
            
            iterations++;
        }
        
        fireFinished();
        
    }
    
    
    private double dfdist(double x_s, double y_s, double z_s, double x, double y, double z) {

        double result = Math.sqrt(Math.pow(x_s - x, 2) + Math.pow((y_s - y), 2) + Math.pow((z_s - z), 2));

        return result;
    }
    
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
    public File process() {
        return null;
    }
}
