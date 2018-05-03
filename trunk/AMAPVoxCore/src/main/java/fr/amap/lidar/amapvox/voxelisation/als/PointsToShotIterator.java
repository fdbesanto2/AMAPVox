/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.als;

import fr.amap.amapvox.als.LasPoint;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.lidar.amapvox.shot.Shot;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author calcul
 */
public class PointsToShotIterator implements IteratorWithException<fr.amap.lidar.amapvox.voxelisation.als.AlsShot>{

    private boolean isNewShot = true;
    private boolean wasReturned = false;
    private double oldTime = -1;
    private int oldN = -1;
    private AlsShot shot = null;
    private int count = 0;
    private int currentLasPointIndex = 0;
    private int index = 0;
    private LasShot mix;
    private int currentNbEchos = 0;
    private int currentEchoFound = 0;
    
    private final static float RATIO_REFLECTANCE_VEGETATION_SOL = 0.4f;
    
    private final List<Trajectory> trajectoryList;
    private final List<LasPoint> lasPointList;
    private final Mat4D vopMatrix;

    public PointsToShotIterator(List<Trajectory> trajectoryList, List<LasPoint> lasPointList, Mat4D vopMatrix) {
        this.trajectoryList = trajectoryList;
        this.lasPointList = lasPointList;
        this.vopMatrix = vopMatrix;
    }
            
    @Override
    public boolean hasNext() {
        return true;
    }
    
//    @Override
//    public AlsShot next() throws Exception {
//        //parcours les points las jusqu'à retrouver un tir avec tous ses échos
//        for (int i = currentLasPointIndex; i < lasPointList.size(); i++) {
//
//            LasPoint currentLasPoint = lasPointList.get(i);
////            LasPoint nextLasPoint;
////            if (i + 1 < lasPointList.size()) {
////                nextLasPoint = lasPointList.get(i + 1);
////            } else {
////                nextLasPoint = null;
////            }
//            
//            if(currentLasPoint.t == 309385.650774){
//                System.out.println("test");
//            }
//            
//            if(currentLasPoint.t != oldTime && oldTime != -1){ //on passe à un autre tir
//                
//                oldTime = currentLasPoint.t;
//                isNewShot = true;
//                return shot;
//            }else{
//                oldTime = currentLasPoint.t;
//            }
//            
//            
//            index = searchNearestMaxV2(currentLasPoint.t, trajectoryList, index);
//            if (index < 0) {
//                //logger.error("Trajectory file is invalid, out of bounds exception.");
//                return null;
//            }
//
//            int indexMax = index;
//            int indexMin = index - 1;
//
//            double max = trajectoryList.get(index).t;
//            double min = trajectoryList.get(index - 1).t;
//
//            double xValue;
//            double yValue;
//            double zValue;
//
//            if (max != min) {
//
//                double ratio = (currentLasPoint.t - min) / (max - min);
//
//                //interpolation
//                xValue = trajectoryList.get(indexMin).x + ((trajectoryList.get(indexMax).x - trajectoryList.get(indexMin).x) * ratio);
//                yValue = trajectoryList.get(indexMin).y + ((trajectoryList.get(indexMax).y - trajectoryList.get(indexMin).y) * ratio);
//                zValue = trajectoryList.get(indexMin).z + ((trajectoryList.get(indexMax).z - trajectoryList.get(indexMin).z) * ratio);
//
//            } else {
//                xValue = (trajectoryList.get(indexMin).x + trajectoryList.get(indexMax).x) / 2.0;
//                yValue = (trajectoryList.get(indexMin).y + trajectoryList.get(indexMax).y) / 2.0;
//                zValue = (trajectoryList.get(indexMin).z + trajectoryList.get(indexMax).z) / 2.0;
//            }
//
//            mix = new LasShot(currentLasPoint, 0, 0, 0);
//
//            Vec4D trajTransform = Mat4D.multiply(vopMatrix,
//                    new Vec4D(xValue,
//                            yValue,
//                            zValue, 1));
//
//            Vec4D lasTransform = Mat4D.multiply(vopMatrix,
//                    new Vec4D(currentLasPoint.x,
//                            currentLasPoint.y,
//                            currentLasPoint.z, 1));
//
//            mix.xloc_s = trajTransform.x;
//            mix.yloc_s = trajTransform.y;
//            mix.zloc_s = trajTransform.z;
//
//            mix.xloc = lasTransform.x;
//            mix.yloc = lasTransform.y;
//            mix.zloc = lasTransform.z;
//
//            mix.range = dfdist(mix.xloc_s, mix.yloc_s, mix.zloc_s, mix.xloc, mix.yloc, mix.zloc);
//
//            mix.x_u = (mix.xloc - mix.xloc_s) / mix.range;
//            mix.y_u = (mix.yloc - mix.yloc_s) / mix.range;
//            mix.z_u = (mix.zloc - mix.zloc_s) / mix.range;
//
//            double time = mix.lasPoint.t;
//
////            if (time == oldTime && currentNbEchos != mix.lasPoint.n && mix.lasPoint.n > 1) {
////                throw new Exception("Shot cannot be retrieved because the als file contains different shots with the same gps time.");
////            }
//
//            //le point est associé à un nouveau tir donc on crée ce tir
//            if ((isNewShot) || currentEchoFound == currentNbEchos) {
//
//                shot = new AlsShot(new Point3d(mix.xloc_s, mix.yloc_s, mix.zloc_s),
//                        new Vector3d(mix.x_u, mix.y_u, mix.z_u),
//                        new double[mix.lasPoint.n], new int[mix.lasPoint.n], new float[mix.lasPoint.n]);
//                shot.time = time;
//
//                currentNbEchos = mix.lasPoint.n;
//                currentEchoFound = 0;
//
//                isNewShot = false;
//            }
//
//            currentEchoFound++;
//
//            if (mix.lasPoint.r - 1 < 0) {
//
//            } else if (mix.lasPoint.r <= mix.lasPoint.n) {
//
//                int currentEchoIndex = mix.lasPoint.r - 1; //rang de l'écho
//
//                shot.ranges[currentEchoIndex] = mix.range;
//                shot.classifications[currentEchoIndex] = mix.lasPoint.classification;
//
//                if (shot.classifications[currentEchoIndex] == LasPoint.CLASSIFICATION_GROUND) {
//                    shot.intensities[currentEchoIndex] = (int) (mix.lasPoint.i * RATIO_REFLECTANCE_VEGETATION_SOL);
//                } else {
//                    shot.intensities[currentEchoIndex] = mix.lasPoint.i;
//                }
//                count++;
//            }
//                
//            oldN = mix.lasPoint.n;
//
//            currentLasPointIndex++;
//            wasReturned = false;
//        }
//
//        return null;
//    }

    @Override
    public AlsShot next() throws Exception {
        //parcours les points las jusqu'à retrouver un tir avec tous ses échos
        for (int i=currentLasPointIndex;i<lasPointList.size(); i++) {

            if(!wasReturned){

                LasPoint lasPoint = lasPointList.get(i);


                double targetTime = lasPoint.t;
                
                if(targetTime == 309385.650774){
                    System.out.println("test");
                }

                index = searchNearestMaxV2(targetTime, trajectoryList, index);
                if(index < 0){
                    //logger.error("Trajectory file is invalid, out of bounds exception.");
                    return null;
                }

                int indexMax = index;
                int indexMin = index-1;

                double max = trajectoryList.get(index).t;
                double min = trajectoryList.get(index-1).t;
                
                double xValue;
                double yValue;
                double zValue;

                if(max != min){
                    
                    double ratio = (lasPoint.t - min) / (max - min);

                    //interpolation
                    xValue = trajectoryList.get(indexMin).x + ((trajectoryList.get(indexMax).x - trajectoryList.get(indexMin).x) * ratio);
                    yValue = trajectoryList.get(indexMin).y + ((trajectoryList.get(indexMax).y - trajectoryList.get(indexMin).y) * ratio);
                    zValue = trajectoryList.get(indexMin).z + ((trajectoryList.get(indexMax).z - trajectoryList.get(indexMin).z) * ratio);

                }else{
                    xValue = (trajectoryList.get(indexMin).x + trajectoryList.get(indexMax).x)/2.0;
                    yValue = (trajectoryList.get(indexMin).y + trajectoryList.get(indexMax).y)/2.0;
                    zValue = (trajectoryList.get(indexMin).z + trajectoryList.get(indexMax).z)/2.0;
                }
                

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
            if (isNewShot  && time != oldTime && !wasReturned) {

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
                    isNewShot = false;
                    wasReturned = true;

                    if(shot.getEchoesNumber() != 0){ //handle the case (file bug) when an echo has a nbEchos equals to 0
                        
                        return shot;
                    }
                //}

                //count = 0;
                //isNewExp = false;

            }
            
            if(time == oldTime && currentNbEchos != mix.lasPoint.n && mix.lasPoint.n > 1){
                throw new Exception("Shot cannot be retrieved because the als file contains different shots with the same gps time.");
            }

            //le point appartient toujours au même tir
            if (time == oldTime || (!isNewShot && time != oldTime)) {

                //le point est associé à un nouveau tir donc on crée ce tir
                if ((!isNewShot && time != oldTime) || currentEchoFound == currentNbEchos) {

                    shot = new AlsShot(new Point3d(mix.xloc_s, mix.yloc_s, mix.zloc_s), 
                                                new Vector3d(mix.x_u, mix.y_u, mix.z_u), 
                                                new double[mix.lasPoint.n], new int[mix.lasPoint.n], new float[mix.lasPoint.n]);
                    shot.time = time;
                    
                    currentNbEchos = mix.lasPoint.n;
                    currentEchoFound = 0;

                    isNewShot = true;
                }

                currentEchoFound++;

                if(mix.lasPoint.r - 1 < 0){

                }else if(mix.lasPoint.r <= mix.lasPoint.n){

                    int currentEchoIndex = mix.lasPoint.r-1; //rang de l'écho

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

        return null;
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

    public int getNbPoints(){
        return lasPointList.size();
    }
    
    public int getNbPointsProcessed(){
        return currentLasPointIndex;
    }
}
