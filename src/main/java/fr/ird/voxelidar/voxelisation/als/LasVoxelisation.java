/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.als;

import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.VoxelAnalysis;
import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.VoxelParameters;
import fr.ird.voxelidar.extraction.Shot;
import fr.ird.voxelidar.lidar.format.als.Las;
import fr.ird.voxelidar.lidar.format.als.LasHeader;
import fr.ird.voxelidar.lidar.format.als.PointDataRecordFormat0;
import fr.ird.voxelidar.math.matrix.Mat;
import fr.ird.voxelidar.math.matrix.Mat4D;
import fr.ird.voxelidar.math.vector.Vec3D;
import fr.ird.voxelidar.util.TimeCounter;
import fr.ird.voxelidar.voxelisation.Las2;
import fr.ird.voxelidar.voxelisation.LasMixTrajectory;
import fr.ird.voxelidar.voxelisation.LasPoint;
import fr.ird.voxelidar.voxelisation.Processing;
import fr.ird.voxelidar.voxelisation.Shoot;
import fr.ird.voxelidar.voxelisation.TimeVector;
import fr.ird.voxelidar.voxelisation.Trajectory;
import fr.ird.voxelidar.voxelisation.VoxelisationParameters;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class LasVoxelisation extends Processing {
    
    private final Logger logger = Logger.getLogger(LasVoxelisation.class);
    
    private Las las;
    private Mat4D popMatrix;
    private File trajectoryFile;
    private File outputFile;
    private VoxelParameters parameters;

    public LasVoxelisation(Las las, File outputFile, Mat4D popMatrix, File trajectoryFile, VoxelParameters parameters) {

        this.las = las;
        this.outputFile = outputFile;
        this.popMatrix = popMatrix;
        this.trajectoryFile = trajectoryFile;
        this.parameters = parameters;
    }

    @Override
    public File process() {
        
        
        ArrayList<LasPoint> lasPointList = readLas(las);
        ArrayList<LasMixTrajectory> ALL = transformPoints(popMatrix, lasPointList, trajectoryFile);
        
        Map<String, Shot> shoots = buildEchos(ALL);
        voxelise(shoots);
        //writeShootsFile(shoots);
        
        return outputFile;
    }

    private double dfdist(double x_s, double y_s, double z_s, double x, double y, double z) {

        double result = Math.sqrt(Math.pow(x_s - x, 2) + Math.pow((y_s - y), 2) + Math.pow((z_s - z), 2));

        return result;
    }

    private ArrayList<Las2> readTxt() {
        ArrayList<Las2> lasList = new ArrayList<>();

        try {

            BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Julien\\Desktop\\Test Als preprocess\\ALSbuf_xyzirncapt.txt"), ' ');

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.charAt(0) != '#') {
                    String[] lineSplit = line.split(" ");
                    Las2 las = new Las2(new Vec3D(Double.valueOf(lineSplit[0]), Double.valueOf(lineSplit[1]),
                            Double.valueOf(lineSplit[2])), Integer.valueOf(lineSplit[3]),
                            Integer.valueOf(lineSplit[4]), Integer.valueOf(lineSplit[5]),
                            Integer.valueOf(lineSplit[6]), Integer.valueOf(lineSplit[7]),
                            Integer.valueOf(lineSplit[8]), Double.valueOf(lineSplit[9]));

                    lasList.add(las);
                }

            }

        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }

        return lasList;
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
    public void writeLocalAls(String outputFilePath, ArrayList<Vec3D> localCoordinates, ArrayList<Las2> lasList) {

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
    private ArrayList<LasMixTrajectory> transformPoints(Mat4D transfMatrix, ArrayList<LasPoint> lasPointList, File trajectoryFile) {

        Map<Double, Trajectory> trajectoryList = new TreeMap<>();
        ArrayList<LasMixTrajectory> ALL = new ArrayList<>();

        ArrayList<Double> X = new ArrayList<>();
        ArrayList<Double> Y = new ArrayList<>();
        ArrayList<Double> Z = new ArrayList<>();

        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        ArrayList<Double> z = new ArrayList<>();

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
        
        //Vecteur des temps LIDAR
        //ArrayList<TimeVector> TLIDAR = new ArrayList<>();
        /*
        for (LasPoint lasPoint : lasPointList) {
            TLIDAR.add(new TimeVector(lasPoint.t, false, TLIDAR.size()));
        }
        */
        try {

            BufferedReader reader = new BufferedReader(new FileReader(trajectoryFile));

            String line;

            //skip header
            reader.readLine();

            while ((line = reader.readLine()) != null) {

                String[] lineSplit = line.split(",");
                Trajectory traj = new Trajectory(Double.valueOf(lineSplit[0]), Double.valueOf(lineSplit[1]),
                        Double.valueOf(lineSplit[2]), Double.valueOf(lineSplit[3]));
                
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

        /*##############################
         # MERGE ECHOES and TRAJECTORY #
         ##############################*/
        //Vecteur des temps GPS
        /*
        ArrayList<TimeVector> TGPS = new ArrayList<>();

        for (Map.Entry<Double, Trajectory> entry : trajectoryList.entrySet()) {
            TGPS.add(new TimeVector(entry.getKey(), true, TGPS.size()));
        }
        */
        
        /*
        ArrayList<TimeVector> fusion = new ArrayList<>();
        fusion.addAll(TGPS);
        fusion.addAll(TLIDAR);

        //Trier selon le temps
        Collections.sort(fusion, new Comparator<TimeVector>() {

            @Override
            public int compare(TimeVector vec1, TimeVector vec2) {
                if (vec2.x < vec1.x) {
                    return 1;
                } else if (vec2.x > vec1.x) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        */
        ArrayList<Vec3D> trajectoryInterpolate = new ArrayList<>();
        
        //ancien algo
        /*
        long start_time = System.currentTimeMillis();
        
        for (int i = 0; i < fusion.size(); i++) {
            if (!fusion.get(i).isGpsTime) { //point lidar
                for (int min = i - 1; min >= 0; min--) {
                    if (fusion.get(min).isGpsTime) {

                        for (int max = i + 1; max < fusion.size(); max++) {

                            if (fusion.get(max).isGpsTime) {

                                double ratio = (fusion.get(i).x - fusion.get(min).x) / (fusion.get(max).x - fusion.get(min).x);

                                //formule interpolation
                                double xValue = trajectoryList.get(fusion.get(min).x).x + ((trajectoryList.get(fusion.get(max).x).x - trajectoryList.get(fusion.get(min).x).x) * ratio);
                                double yValue = trajectoryList.get(fusion.get(min).x).y + ((trajectoryList.get(fusion.get(max).x).y - trajectoryList.get(fusion.get(min).x).y) * ratio);
                                double zValue = trajectoryList.get(fusion.get(min).x).z + ((trajectoryList.get(fusion.get(max).x).z - trajectoryList.get(fusion.get(min).x).z) * ratio);
                                trajectoryInterpolate.add(new Vec3D(xValue, yValue, zValue));

                                max = fusion.size() - 1;
                            }
                        }

                        min = 0;
                    }
                }
            }
        }
        logger.info("time ( "+TimeCounter.getElapsedTimeInSeconds(start_time)+" )");
        */
        ArrayList<Double> tgps = new ArrayList<>();
        
        for (Map.Entry<Double, Trajectory> entry : trajectoryList.entrySet()) {
            tgps.add(entry.getKey());
        }
        
        Collections.sort(tgps);
        
        int index = 0;
        
        //vérifier que le nouvel algorithme de recherche de min et max 
        //donne le même résulat que l'ancien
        
        //long start_time = System.currentTimeMillis();
        //nouvel algo
        for(int i=0;i<lasPointList.size();i++){
            double targetTime = lasPointList.get(i).t;
            
            index = searchNearestMax(targetTime, tgps, index);
            
            double max = tgps.get(index);
            double min = tgps.get(index-1);

            double ratio = (lasPointList.get(i).t - min) / (max - min);

            //formule interpolation
            double xValue = trajectoryList.get(min).x + ((trajectoryList.get(max).x - trajectoryList.get(min).x) * ratio);
            double yValue = trajectoryList.get(min).y + ((trajectoryList.get(max).y - trajectoryList.get(min).y) * ratio);
            double zValue = trajectoryList.get(min).z + ((trajectoryList.get(max).z - trajectoryList.get(min).z) * ratio);

            trajectoryInterpolate.add(new Vec3D(xValue, yValue, zValue));
            //System.out.println("test");
            //double max = searchNearestMax(targetTime, tgps);
        }
        
        //logger.info("time ( "+TimeCounter.getElapsedTimeInSeconds(start_time)+" )");
        
        int compteur = 0;
        for (LasPoint lasPoint : lasPointList) {

            LasMixTrajectory mix = new LasMixTrajectory(lasPoint, 0, 0, 0);

            X.add(trajectoryInterpolate.get(compteur).x);
            Y.add(trajectoryInterpolate.get(compteur).y);
            Z.add(trajectoryInterpolate.get(compteur).z);

            x.add(lasPoint.location.x);
            y.add(lasPoint.location.y);
            z.add(lasPoint.location.z);

            ALL.add(mix);

            compteur++;
        }


        /*#################################
         #reproject source points in LOCS#
         #################################*/
        //transposée
        Mat loc_coord_offset = new Mat(X.size(), 4);
        loc_coord_offset.setColumn(0, X.toArray(new Double[X.size()]));
        loc_coord_offset.setColumn(1, Y.toArray(new Double[Y.size()]));
        loc_coord_offset.setColumn(2, Z.toArray(new Double[Z.size()]));
        loc_coord_offset.setColumn(3, 1);

        //rotation            
        //Mat loc_coord_rot = Mat.transpose(Mat.multiply(mat_rot.toMat(), Mat.transpose(loc_coord_offset)));
        //transformation
        Mat mat4x4 = Mat.transpose(Mat.multiply(transfMatrix.toMat(), Mat.transpose(loc_coord_offset)));

        for (int i = 0; i < mat4x4.lineNumber; i++) {

            ALL.get(i).xloc_s = mat4x4.mat[i][0];
            ALL.get(i).yloc_s = mat4x4.mat[i][1];
            ALL.get(i).zloc_s = mat4x4.mat[i][2];

        }

        /*#################################
         #reproject echoes for extracting DTM in new coordinate system
         #################################*/
        loc_coord_offset = new Mat(x.size(), 4);
        loc_coord_offset.setColumn(0, x.toArray(new Double[x.size()]));
        loc_coord_offset.setColumn(1, y.toArray(new Double[y.size()]));
        loc_coord_offset.setColumn(2, z.toArray(new Double[z.size()]));
        loc_coord_offset.setColumn(3, 1);

        //rotation            
        mat4x4 = Mat.transpose(Mat.multiply(transfMatrix.toMat(), Mat.transpose(loc_coord_offset)));

        for (int i = 0; i < mat4x4.lineNumber; i++) {
            ALL.get(i).xloc = mat4x4.mat[i][0];
            ALL.get(i).yloc = mat4x4.mat[i][1];
            ALL.get(i).zloc = mat4x4.mat[i][2];
        }

        for (LasMixTrajectory all : ALL) {

            all.range = dfdist(all.xloc_s, all.yloc_s, all.zloc_s, all.xloc, all.yloc, all.zloc);

            all.x_u = (all.xloc - all.xloc_s) / all.range;
            all.y_u = (all.yloc - all.yloc_s) / all.range;
            all.z_u = (all.zloc - all.zloc_s) / all.range;
        }
        return ALL;
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
    
    
    
    public void voxelise(Map<String, Shot> shots){
        
        VoxelAnalysis voxelAnalysis = new VoxelAnalysis(null);
        voxelAnalysis.init(parameters, outputFile);
        
        for(Entry entry:shots.entrySet()){
            
            Shot shot = (Shot) entry.getValue();
            
            voxelAnalysis.voxelise(shot);
            
        }
        
        voxelAnalysis.calculatePADAndWrite(0);
    }

    public Map<String,Shot> buildEchos(ArrayList<LasMixTrajectory> ALL) {

        double oldTime = -1;
        int oldN = -1;

        int compteur = 0;
        
        Map<String,Shot> shoots = new TreeMap<>();
        
        Shot e = null;
        boolean isNewExp = false;

        for (LasMixTrajectory all : ALL) {

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
                    shoots.put(String.valueOf(oldTime), e);
                }
                compteur = 0;
                isNewExp = false;

            }

            if (time == oldTime || (!isNewExp && time != oldTime)) {

                if (!isNewExp && time != oldTime) {
                    
                    e= new Shot(all.lasPoint.n, new Point3f((float)all.xloc_s, (float)all.yloc_s, (float)all.zloc_s), 
                                                new Vector3f((float)all.x_u, (float)all.y_u, (float)all.z_u), 
                                                new float[all.lasPoint.n]);
                    //e = new Shot(all.lasPoint.n, all.xloc_s, all.yloc_s, all.zloc_s, all.x_u, all.y_u, all.z_u);
                    isNewExp = true;
                }

                switch (all.lasPoint.r) {

                    case 1:
                        e.ranges[0] = (float)all.range;
                        //e.r1 = all.range;
                        compteur++;
                        break;
                    case 2:
                        e.ranges[1] = (float)all.range;
                        //e.r2 = all.range;
                        compteur++;
                        break;
                    case 3:
                        e.ranges[2] = (float)all.range;
                        //e.r3 = all.range;
                        compteur++;
                        break;
                    case 4:
                        e.ranges[3] = (float)all.range;
                        //e.r4 = all.range;
                        compteur++;
                        break;
                    case 5:
                        e.ranges[4] = (float)all.range;
                        //e.r5 = all.range;
                        compteur++;
                        break;
                    case 6:
                        e.ranges[5] = (float)all.range;
                        //e.r6 = all.range;
                        compteur++;
                        break;
                    case 7:
                        e.ranges[6] = (float)all.range;
                        //e.r7 = all.range;
                        compteur++;
                        break;
                }

            }

            oldTime = time;
            oldN = all.lasPoint.n;

        }
        /*
        try {
        
            BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\Julien\\Desktop\\test.txt"));
            //writer.write("\"key\" \"n\" \"xloc_s\" \"yloc_s\" \"zloc_s\" \"x_u\" \"y_u\" \"z_u\" \"r1\" \"r2\" \"r3\" \"r4\" \"r5\" \"r6\" \"r7\"\n");
            writer.write("\"n\" \"xloc_s\" \"yloc_s\" \"zloc_s\" \"x_u\" \"y_u\" \"z_u\" \"r1\" \"r2\" \"r3\" \"r4\" \"r5\" \"r6\" \"r7\"\n");
            
            for (Entry entry : shoots.entrySet()) {
                
                String time = (String) entry.getKey();
                Shot shot = (Shot) entry.getValue();
                //String line = "\""+time+"\""+" "+shot.nbEchos+" "+shot.origin.x+" "+shot.origin.y+" "+shot.origin.z+" "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z;
                String line = shot.nbEchos+" "+shot.origin.x+" "+shot.origin.y+" "+shot.origin.z+" "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z;
                for(int i=0;i<shot.ranges.length;i++){
                    line+=" " + shot.ranges[i];
                }
                
                writer.write(line+"\n");
            }
            
            writer.close();
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(LasVoxelisation.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
        return shoots;
    }

    
    
}
