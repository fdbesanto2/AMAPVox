/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.als;

import fr.ird.voxelidar.extraction.Shot;
import fr.ird.voxelidar.lidar.format.als.Las;
import fr.ird.voxelidar.lidar.format.als.LasHeader;
import fr.ird.voxelidar.lidar.format.als.LasReader;
import fr.ird.voxelidar.lidar.format.als.PointDataRecordFormat0;
import fr.ird.voxelidar.math.matrix.Mat;
import fr.ird.voxelidar.math.matrix.Mat4D;
import fr.ird.voxelidar.math.vector.Vec3D;
import fr.ird.voxelidar.math.vector.Vec4D;
import fr.ird.voxelidar.voxelisation.Processing;
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
import java.util.logging.Level;
import javax.swing.event.EventListenerList;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class LasConversion extends Processing implements Runnable{
    
    private final Logger logger = Logger.getLogger(LasConversion.class);
    
    private Las las;
    private final File lasFile;
    private final File trajectoryFile;
    private final Mat4D popMatrix;
    private final BlockingQueue<Shot> queue;
    private final BlockingQueue<fr.ird.voxelidar.extraction.LasPoint> arrayBlockingQueue;
    private boolean isFinished;

    public void setIsFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }
    
    public LasConversion(BlockingQueue<fr.ird.voxelidar.extraction.LasPoint> arrayBlockingQueue, BlockingQueue<Shot> queue,File trajectoryFile, File lasFile, Mat4D popMatrix){

        this.trajectoryFile = trajectoryFile;
        this.popMatrix = popMatrix;
        this.lasFile = lasFile;
        this.arrayBlockingQueue = arrayBlockingQueue;
        this.queue = queue;
        isFinished = false;
    }

    @Override
    public void run() {
        
        setStepNumber(3);
        
        fireProgress("Reading *.las", getProgression());
        /*
        ArrayList<LasPoint> lasPointList = new ArrayList<>();
        
        
        
        while(!isFinished || !arrayBlockingQueue.isEmpty()) {

            try {
                fr.ird.voxelidar.extraction.LasPoint point = arrayBlockingQueue.take();
                lasPointList.add(new LasPoint(new Vec3D(point.x, point.y, point.z), point.returnNumber, point.numberOfReturns, point.gpsTime));
            }catch(Exception e){
                logger.error(e.getMessage());
            }
        }
        */
        
        
        las = LasReader.read(lasFile);
        ArrayList<LasPoint> lasPointList = readLas(las);
        
        Map<Double, Trajectory> trajectoryMap = new TreeMap<>();
        
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
                
                Double time = Double.valueOf(lineSplit[3]);

                //troncate unused values
                if(time >= minTime-0.01 && time <= maxTime+0.01){
                    trajectoryMap.put(time, traj);
                }
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
        
        Shot e = null;
        boolean isNewExp = false;
        
        int count = 0;
        
        for (LasPoint lasPoint : lasPointList) {
            
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
                    new Vec4D(lasPoint.location.x, 
                            lasPoint.location.y, 
                            lasPoint.location.z, 1));
            
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
                        queue.put(e);
                        //voxeliseOne(e);
                        //shoots.put(String.valueOf(oldTime), e);
                    } catch (InterruptedException ex) {
                        java.util.logging.Logger.getLogger(LasVoxelisation.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                count = 0;
                isNewExp = false;

            }

            if (time == oldTime || (!isNewExp && time != oldTime)) {

                if (!isNewExp && time != oldTime) {
                    
                    e= new Shot(mix.lasPoint.n, new Point3d(mix.xloc_s, mix.yloc_s, mix.zloc_s), 
                                                new Vector3d(mix.x_u, mix.y_u, mix.z_u), 
                                                new double[mix.lasPoint.n]);
                    //e = new Shot(all.lasPoint.n, all.xloc_s, all.yloc_s, all.zloc_s, all.x_u, all.y_u, all.z_u);
                    isNewExp = true;
                }

                switch (mix.lasPoint.r) {

                    case 1:
                        e.ranges[0] = (double)mix.range;
                        count++;
                        break;
                    case 2:
                        e.ranges[1] = (double)mix.range;
                        count++;
                        break;
                    case 3:
                        e.ranges[2] = (double)mix.range;
                        count++;
                        break;
                    case 4:
                        e.ranges[3] = (double)mix.range;
                        count++;
                        break;
                    case 5:
                        e.ranges[4] = (double)mix.range;
                        count++;
                        break;
                    case 6:
                        e.ranges[5] = (double)mix.range;
                        count++;
                        break;
                    case 7:
                        e.ranges[6] = (double)mix.range;
                        count++;
                        break;
                }

            }

            oldTime = time;
            oldN = mix.lasPoint.n;
        }
        
        
        fireFinished();
        
        //fireProgress("Writing file", getProgression());
        //voxelAnalysis.calculatePADAndWrite(0);
        
    }
    
    private ArrayList<LasPoint> readLas(Las lasFile) {
        
        ArrayList<LasPoint> lasPointList = new ArrayList<>();

        ArrayList<? extends PointDataRecordFormat0> pointDataRecords = lasFile.getPointDataRecords();
        LasHeader header = lasFile.getHeader();
        for (PointDataRecordFormat0 p : pointDataRecords) {

            Vec3D location = new Vec3D((p.getX() * header.getxScaleFactor()) + header.getxOffset(), (p.getY() * header.getyScaleFactor()) + header.getyOffset(), (p.getZ() * header.getzScaleFactor()) + header.getzOffset());
            LasPoint point = new LasPoint(location, p.getReturnNumber(), p.getNumberOfReturns(), p.getGpsTime());
            
            lasPointList.add(point);
        }

        return lasPointList;
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
