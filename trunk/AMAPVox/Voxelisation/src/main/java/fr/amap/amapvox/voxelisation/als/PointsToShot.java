/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxelisation.als;

import fr.amap.amapvox.als.LasHeader;
import fr.amap.amapvox.als.LasPoint;
import fr.amap.amapvox.als.las.Las;
import fr.amap.amapvox.als.las.LasReader;
import fr.amap.amapvox.als.las.PointDataRecordFormat;
import fr.amap.amapvox.als.las.QLineExtrabytes;
import fr.amap.amapvox.als.laz.LazExtraction;
import fr.amap.commons.util.io.file.FileManager;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.amapvox.commons.util.Processing;
import fr.amap.amapvox.io.tls.rxp.Shot;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * This class merge trajectory file with point file (las, laz)
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class PointsToShot extends Processing implements Iterable<Shot>{
    
    
    private final float RATIO_REFLECTANCE_VEGETATION_SOL = 0.4f;
    
    private Las las;
    private final File alsFile;
    private final Mat4D vopMatrix;
    List<Integer> classifiedPointsToDiscard;
    private List<Trajectory> trajectoryList;
    private boolean updateALS = true;
    private List<LasPoint> lasPointList;
    
    public PointsToShot(List<Trajectory> trajectoryList, File alsFile, Mat4D vopMatrix, List<Integer> classifiedPointsToDiscard){

        this.trajectoryList = trajectoryList;
        this.vopMatrix = vopMatrix;
        this.alsFile = alsFile;
        this.classifiedPointsToDiscard = classifiedPointsToDiscard;
    }

    public boolean isUpdateALS() {
        return updateALS;
    }

    public void setUpdateALS(boolean updateALS) {
        this.updateALS = updateALS;
    }

    public void init() {
        
        setStepNumber(3);
        
        fireProgress("Reading *.las", 0);
        
        int iterations = 0;
        long maxIterations;
        int step;
        
        if(updateALS){
            
            /***reading las***/
            
            lasPointList = new ArrayList<>();
            
            LasHeader header;

            switch(FileManager.getExtension(alsFile)){
                case ".las":

                    LasReader lasReader = new LasReader();
                    try {
                        lasReader.open(alsFile);
                    } catch (IOException ex) {
                        Logger.getLogger(PointsToShot.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (Exception ex) {
                        Logger.getLogger(PointsToShot.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    header = lasReader.getHeader();

                    maxIterations = header.getNumberOfPointrecords();
                    step = (int) (maxIterations/10);

                    for (PointDataRecordFormat p : lasReader) {

                        if(iterations % step == 0){
                            fireProgress("Reading *.las", (int) ((iterations*100)/(float)maxIterations));
                        }

                        if(p.isHasQLineExtrabytes()){
                            QLineExtrabytes qLineExtrabytes = p.getQLineExtrabytes();
                            //logger.info("QLineExtrabytes" + qLineExtrabytes.getAmplitude()+" "+qLineExtrabytes.getPulseWidth());
                        }
                        Vector3d location = new Vector3d((p.getX() * header.getxScaleFactor()) + header.getxOffset(), (p.getY() * header.getyScaleFactor()) + header.getyOffset(), (p.getZ() * header.getzScaleFactor()) + header.getzOffset());
                        
                        
                        if(!classifiedPointsToDiscard.contains(new Integer(p.getClassification()))){
                            
                            LasPoint point = new LasPoint(location.x, location.y, location.z, p.getReturnNumber(), p.getNumberOfReturns(), p.getIntensity(), p.getClassification(), p.getGpsTime());
                            lasPointList.add(point);
                        }


                        iterations++;
                    }
                    break;

                case ".laz":

                    LazExtraction laz = new LazExtraction();
                    try {
                        laz.openLazFile(alsFile);
                    } catch (Exception ex) {
                        Logger.getLogger(PointsToShot.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    header = laz.getHeader();

                    for (LasPoint p : laz) {

                        p.x = (p.x * header.getxScaleFactor()) + header.getxOffset();
                        p.y = (p.y * header.getyScaleFactor()) + header.getyOffset();
                        p.z = (p.z * header.getzScaleFactor()) + header.getzOffset();

                        if(!classifiedPointsToDiscard.contains(new Integer(p.classification))){
                            lasPointList.add(p);
                        }

                    }
                    laz.close();

                    break;
            }

            /***sort las by time***/
            //lasPointList.sort(null);
            Collections.sort(lasPointList);

            double minTime = lasPointList.get(0).t;
            double maxTime = lasPointList.get(lasPointList.size()-1).t;

            if(minTime == maxTime){
                //logger.error("ALS file doesn't contains time relative information, minimum and maximum time = "+minTime);
                return;
            }
        }
                
        fireProgress("Voxelisation", getProgression());       
        
    }
    
    
    private double dfdist(double x_s, double y_s, double z_s, double x, double y, double z) {

        double result = Math.sqrt(Math.pow(x_s - x, 2) + Math.pow((y_s - y), 2) + Math.pow((z_s - z), 2));

        return result;
    }
    
    private int searchNearestMaxV2(double value, List<Trajectory> list, int start){
        
        int low = start;
        int high = list.size()-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            double midVal = list.get(mid).t;

            if (midVal < value){
                low = mid + 1;
            }else if (midVal > value){
                high = mid - 1;
            }else{
                return mid; // key found
            } 
        }
        
        if(low < list.size() && low > 0){
            return low;
        }        
        
        return -(low + 1);  // key not found
    }
    
    private int searchNearestMax(double value, ArrayList<Double> list, int start){
        
        
        int indexMin = start;
        int indexMax = list.size() - 1;
        
        int index = ((indexMax - indexMin)/2) + indexMin;
        
        boolean found = false;
                
        while(!found){
            
            if(index > list.size() -1){
                throw new IndexOutOfBoundsException("Index is out");
            }
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

    @Override
    public Iterator<Shot> iterator() {
        
        Iterator it = new Iterator() {

            boolean isNewExp = false;
            boolean wasReturned = false;
            double oldTime = -1;
            int oldN = -1;
            Shot shot = null;
            int count = 0;
            int currentLasPointIndex = 0;
            int index = 0;
            LasShot mix;
            int currentNbEchos = 0;
            int currentEchoFound = 0;
            
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Shot next() {
                
                
                //parcours les points las jusqu'à retrouver un tir avec tous ses échos
                for (int i=currentLasPointIndex;i<lasPointList.size(); i++) {

                    if(!wasReturned){
                        
                        LasPoint lasPoint = lasPointList.get(i);
                        

                        double targetTime = lasPoint.t;

                        index = searchNearestMaxV2(targetTime, trajectoryList, index);
                        if(index < 0){
                            //logger.error("Trajectory file is invalid, out of bounds exception.");
                            return null;
                        }

                        int indexMax = index;
                        int indexMin = index-1;

                        double max = trajectoryList.get(index).t;
                        double min = trajectoryList.get(index-1).t;
                        double ratio = (lasPoint.t - min) / (max - min);

                        //formule interpolation
                        double xValue = trajectoryList.get(indexMin).x + ((trajectoryList.get(indexMax).x - trajectoryList.get(indexMin).x) * ratio);
                        double yValue = trajectoryList.get(indexMin).y + ((trajectoryList.get(indexMax).y - trajectoryList.get(indexMin).y) * ratio);
                        double zValue = trajectoryList.get(indexMin).z + ((trajectoryList.get(indexMax).z - trajectoryList.get(indexMin).z) * ratio);

                        mix = new LasShot(lasPoint, 0, 0, 0);

                        Vec4D trajTransform = Mat4D.multiply(vopMatrix, 
                                new Vec4D(xValue, 
                                        yValue, 
                                        zValue, 1));

                        Vec4D lasTransform = Mat4D.multiply(vopMatrix, 
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
                    }
                    

                    double time = mix.lasPoint.t;

                    //le temps associé au point à changé donc le tir peut être retourné
                    if (isNewExp && time != oldTime && !wasReturned) {

                        /**
                         * *vérifie que le nombre d'échos lus correspond bien au nombre
                         * d'échos total* permet d'éviter le plantage du programme de
                         * voxelisation est-ce pertinent? possible perte d'imformations
                         * modifier le programme de voxelisation de Jean Dauzat si on ne
                         * veut pas nettoyer
                         *
                         */
                        //if (oldN == count) {
                            
                            //currentLasPointIndex++;
                            count = 0;
                            isNewExp = false;
                            wasReturned = true;
                            
                            return shot;
                        //}
                        
                        //count = 0;
                        //isNewExp = false;

                    }

                    //le point appartient toujours au même tir
                    if (time == oldTime || (!isNewExp && time != oldTime)) {

                        //le point est associé à un nouveau tir donc on crée ce tir
                        if ((!isNewExp && time != oldTime) || currentEchoFound == currentNbEchos) {

                            shot= new Shot(mix.lasPoint.n, new Point3d(mix.xloc_s, mix.yloc_s, mix.zloc_s), 
                                                        new Vector3d(mix.x_u, mix.y_u, mix.z_u), 
                                                        new double[mix.lasPoint.n], new int[mix.lasPoint.n], new float[mix.lasPoint.n]);
                            
                            currentNbEchos = mix.lasPoint.n;
                            currentEchoFound = 0;
                            
                            shot.calculateAngle();

                            isNewExp = true;
                        }
                        
                        currentEchoFound++;

                        if(mix.lasPoint.r - 1 < 0){

                        }else if(mix.lasPoint.r <= mix.lasPoint.n){

                            int currentEchoIndex = mix.lasPoint.r-1; //rang de l'écho
                            
                            if(currentEchoIndex == shot.ranges.length){
                                System.out.println("test");
                            }

                            shot.ranges[currentEchoIndex] = mix.range;
                            shot.classifications[currentEchoIndex] = mix.lasPoint.classification;

                            if(shot.classifications[currentEchoIndex] == LasPoint.CLASSIFICATION_GROUND){
                                shot.intensities[currentEchoIndex] = (int) (mix.lasPoint.i * RATIO_REFLECTANCE_VEGETATION_SOL);
                            }else{
                                shot.intensities[currentEchoIndex] = mix.lasPoint.i;
                            }
                            count++;
                        }
                    }

                    oldTime = time;
                    oldN = mix.lasPoint.n;
                    
                    currentLasPointIndex++;
                    wasReturned = false;
                }

                fireFinished();
                
                return null;
            }
        };
        
        return it;
    }
}
