/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

import fr.ird.voxelidar.util.Echo;
import fr.ird.voxelidar.util.AlsMixTrajectory;
import fr.ird.voxelidar.util.Als;
import fr.ird.voxelidar.util.TimeVector;
import fr.ird.voxelidar.util.Trajectory;
import fr.ird.voxelidar.lidar.format.als.Las;
import fr.ird.voxelidar.lidar.format.als.LasHeader;
import fr.ird.voxelidar.lidar.format.als.LasReader;
import fr.ird.voxelidar.lidar.format.als.PointDataRecordFormat0;
import fr.ird.voxelidar.math.matrix.Mat;
import fr.ird.voxelidar.math.matrix.Mat4D;
import fr.ird.voxelidar.math.vector.Vec2D;
import fr.ird.voxelidar.math.vector.Vec3D;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Julien
 */
public class VoxelPreprocessing {
    
    private final EventListenerList listeners;
    private String progress;
    private boolean finished;
    private Map<String, Echo> echos = new HashMap<>();
    public File outputFile;
    private DecimalFormat df = new DecimalFormat("0.00");
    
    long localTimeBefore;
    long localTimeAfter;
    double executionTime;
    
    long globalTimeBefore;
    long globalTimeAfter;

    public void setProgress(String progress, int ratio) {
        this.progress = progress;
        fireProgress(progress, ratio);
    }
    
    public void setFinished(boolean isFinished) {
        this.finished = isFinished;
        
        if(isFinished){
            fireFinished();
        }
    }
    
    public void fireProgress(String progress, int ratio){
        
        for(VoxelPreprocessingListener listener :listeners.getListeners(VoxelPreprocessingListener.class)){
            
            listener.voxelPreprocessingStepProgress(progress, ratio);
        }
    }
    
    public void fireFinished(){
        
        for(VoxelPreprocessingListener listener :listeners.getListeners(VoxelPreprocessingListener.class)){
            
            listener.voxelPreprocessingFinished();
        }
    }
    
    public void addVoxelPreprocessingListener(VoxelPreprocessingListener listener){
        listeners.add(VoxelPreprocessingListener.class, listener);
    }
    
    public VoxelPreprocessing(){
        listeners = new EventListenerList();
        finished = false;
    }
    
   
    private double dfdist(double x_s,double y_s,double z_s,double x,double y,double z){
        
        double result = Math.sqrt(Math.pow(x_s-x,2)+Math.pow((y_s-y),2)+Math.pow((z_s-z),2));

        return result;
    }
    
    private ArrayList<Als> readTxt(){
        ArrayList<Als> alsList = new ArrayList<>();
        
        try {
            
        BufferedReader reader = new BufferedReader(new FileReader("C:\\Users\\Julien\\Desktop\\Test Als preprocess\\ALSbuf_xyzirncapt.txt"), ' ');

        String line;

        while ((line = reader.readLine()) != null) {

            if(line.charAt(0) != '#'){
                String[] lineSplit = line.split(" ");
                Als als = new Als(new Vec3D(Double.valueOf(lineSplit[0]), Double.valueOf(lineSplit[1]),
                Double.valueOf(lineSplit[2])), Integer.valueOf(lineSplit[3]),
                Integer.valueOf(lineSplit[4]), Integer.valueOf(lineSplit[5]),
                Integer.valueOf(lineSplit[6]), Integer.valueOf(lineSplit[7]),
                Integer.valueOf(lineSplit[8]), Double.valueOf(lineSplit[9]));

                alsList.add(als);
            }

        }

        } catch (FileNotFoundException ex) {
        Logger.getLogger(VoxelPreprocessing.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
        Logger.getLogger(VoxelPreprocessing.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return alsList;
    }
    
    private ArrayList<Als> readLas(Las lasFile){
        ArrayList<Als> alsList = new ArrayList<>();
        
        ArrayList<? extends PointDataRecordFormat0> pointDataRecords = lasFile.getPointDataRecords();
        LasHeader header = lasFile.getHeader();
        for(PointDataRecordFormat0 p : pointDataRecords){
            
            Vec3D location = new Vec3D((p.getX()*header.getxScaleFactor())+header.getxOffset(), (p.getY()*header.getyScaleFactor())+header.getyOffset(), (p.getZ()*header.getzScaleFactor())+header.getzOffset());
            Als als = new Als(location, p.getIntensity(), 
                            p.getReturnNumber(), p.getNumberOfReturns(), p.getClassification(),p.getScanAngleRank(), p.getPointSourceID(), p.getGpsTime());
            alsList.add(als);
        }
        
        return alsList;
    }
    
    public void writeLocalAls(String outputFilePath, ArrayList<Vec3D> localCoordinates, ArrayList<Als> alsList) {
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(outputFilePath)))) {

            for (int i = 0;i < alsList.size();i++) {

                writer.write(localCoordinates.get(i).x + " " + localCoordinates.get(i).y + " " + localCoordinates.get(i).z + " "
                    + alsList.get(i).i + " " + alsList.get(i).r+ " "
                    + alsList.get(i).n+ " " + alsList.get(i).c+ " "
                    + alsList.get(i).a+ " " + alsList.get(i).p+ "\n");
            }

            writer.close();
                
        } catch (IOException ex) {
            Logger.getLogger(VoxelPreprocessing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Mat4D getMatrixTransformation(Vec3D point1, Vec3D point2){
        
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
    
    public ArrayList<AlsMixTrajectory> transformPoints(Mat4D transfMatrix, ArrayList<Als> alsList, String trajectoryFile){
        
        
        
        System.out.print("initialize");
        setProgress("initialize", 30);
        localTimeBefore = System.nanoTime();
        
        Map<Double, Trajectory> trajectoryList = new HashMap<>();
        ArrayList<AlsMixTrajectory> ALL = new ArrayList<>();

        ArrayList<Double> X = new ArrayList<>();
        ArrayList<Double> Y = new ArrayList<>();
        ArrayList<Double> Z = new ArrayList<>();

        ArrayList<Double> x = new ArrayList<>();
        ArrayList<Double> y = new ArrayList<>();
        ArrayList<Double> z = new ArrayList<>();
        
        
        Collections.sort(alsList, new Comparator<Als>() {

            @Override
            public int compare(Als o1, Als o2) {
                if (o2.t < o1.t) {
                    return 1;
                } else if (o2.t > o1.t) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });
        
        localTimeAfter = System.nanoTime();
        executionTime = (localTimeAfter - localTimeBefore) * Math.pow(10, -9);
        System.out.println(" (" + df.format(executionTime) + " s)");
        
        System.out.print("reading trajectory file: "+trajectoryFile);
        setProgress("reading trajectory file", 40);
        localTimeBefore = System.nanoTime();
        
        try {

            BufferedReader reader = new BufferedReader(new FileReader(trajectoryFile));

            String line;

            //skip header
            reader.readLine();

            while ((line = reader.readLine()) != null) {

                String[] lineSplit = line.split(",");
                Trajectory traj = new Trajectory(Double.valueOf(lineSplit[0]), Double.valueOf(lineSplit[1]),
                        Double.valueOf(lineSplit[2]), Double.valueOf(lineSplit[3]));

                trajectoryList.put(Double.valueOf(lineSplit[3]), traj);

            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(VoxelPreprocessing.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(VoxelPreprocessing.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        
        localTimeAfter = System.nanoTime();
        executionTime = (localTimeAfter - localTimeBefore) * Math.pow(10, -9);
        System.out.println(" (" + df.format(executionTime) + " s)");

        /*##############################
         # MERGE ECHOES and TRAJECTORY #
         ##############################*/
        
        System.out.print("merge echoes and trajectory");
        setProgress("merge echoes and trajectory", 50);
        localTimeBefore = System.nanoTime();
        
        //Vecteur des temps GPS
        ArrayList<TimeVector> TGPS = new ArrayList<>();

        for (Entry<Double, Trajectory> entry : trajectoryList.entrySet()) {
            TGPS.add(new TimeVector(entry.getKey(), true, TGPS.size()));
        }

        //Vecteur des temps LIDAR
        ArrayList<TimeVector> TLIDAR = new ArrayList<>();

        for (Als alsList1 : alsList) {
            TLIDAR.add(new TimeVector(alsList1.t, false, TLIDAR.size()));
        }

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
        ArrayList<Vec3D> trajectoryInterpolate = new ArrayList<>();
        //remplace les NA par la valeur précédente non NA dans la liste
        for (int i = 0; i < fusion.size(); i++) {
            if (!fusion.get(i).isGpsTime) {
                for (int min = i - 1; min >= 0; min--) {
                    if (fusion.get(min).isGpsTime) {
                        
                        for (int max = i + 1; max < fusion.size(); max++) {
                            
                            if (fusion.get(max).isGpsTime) {
                                
                                double ratio = (fusion.get(i).x-fusion.get(min).x)/(fusion.get(max).x-fusion.get(min).x);
                                
                                double xValue = trajectoryList.get(fusion.get(min).x).x + ((trajectoryList.get(fusion.get(max).x).x - trajectoryList.get(fusion.get(min).x).x)*ratio);       
                                double yValue = trajectoryList.get(fusion.get(min).x).y + ((trajectoryList.get(fusion.get(max).x).y - trajectoryList.get(fusion.get(min).x).y)*ratio);
                                double zValue = trajectoryList.get(fusion.get(min).x).z + ((trajectoryList.get(fusion.get(max).x).z - trajectoryList.get(fusion.get(min).x).z)*ratio);
                                trajectoryInterpolate.add(new Vec3D(xValue, yValue, zValue));
                                
                                max = fusion.size()-1;
                            }
                        }

                        min = 0;
                    }
                }
            }
        }
        
        int compteur = 0;
        for (Als als : alsList) {
            
            AlsMixTrajectory mix = new AlsMixTrajectory(als, 0, 0, 0);
            
            
            X.add(trajectoryInterpolate.get(compteur).x);
            Y.add(trajectoryInterpolate.get(compteur).y);
            Z.add(trajectoryInterpolate.get(compteur).z);

            x.add(als.location.x);
            y.add(als.location.y);
            z.add(als.location.z);

            ALL.add(mix);
            
            
            compteur ++;
        }
        
        localTimeAfter = System.nanoTime();
        executionTime = (localTimeAfter - localTimeBefore) * Math.pow(10, -9);
        System.out.println(" (" + df.format(executionTime) + " s)");


        /*#################################
         #reproject source points in LOCS#
         #################################*/
        
        System.out.print("reproject source points in local coordinates system");
        setProgress("reproject source points in local coordinates system", 60);
        localTimeBefore = System.nanoTime();
        

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
        
        localTimeAfter = System.nanoTime();
        executionTime = (localTimeAfter - localTimeBefore) * Math.pow(10, -9);
        System.out.println(" (" + df.format(executionTime) + " s)");

        /*#################################
         #reproject echoes for extracting DTM in new coordinate system
         #################################*/
        
        System.out.print("reproject echoes for extracting DTM in new coordinate system");
        setProgress("reproject echoes for extracting DTM in new coordinate system", 70);
        localTimeBefore = System.nanoTime();

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

        for (AlsMixTrajectory all : ALL) {

            all.range = dfdist(all.xloc_s, all.yloc_s, all.zloc_s, all.xloc, all.yloc, all.zloc);

            all.x_u = (all.xloc - all.xloc_s) / all.range;
            all.y_u = (all.yloc - all.yloc_s) / all.range;
            all.z_u = (all.zloc - all.zloc_s) / all.range;
        }
        
        localTimeAfter = System.nanoTime();
        executionTime = (localTimeAfter - localTimeBefore) * Math.pow(10, -9);
        System.out.println(" (" + df.format(executionTime) + " s)");
        
        return ALL;
    }
    
    
//    private ArrayList<AlsMixTrajectory> mergeAlsTrajectory(String csvCoordinatesPath, ArrayList<Als> alsList, String trajectoryFile) {
//
//        
//        long localTimeBefore;
//        long localTimeAfter;
//        double executionTime;
//
//        
//
//        Map<Double, Trajectory> trajectoryList = new HashMap<>();
//        ArrayList<AlsMixTrajectory> ALL = new ArrayList<>();
//
//        Vec3D trans = new Vec3D();
//        Mat3 mat_rot = new Mat3();
//
//        ArrayList<Double> X = new ArrayList<>();
//        ArrayList<Double> Y = new ArrayList<>();
//        ArrayList<Double> Z = new ArrayList<>();
//
//        ArrayList<Double> x = new ArrayList<>();
//        ArrayList<Double> y = new ArrayList<>();
//        ArrayList<Double> z = new ArrayList<>();
//
//        System.out.print("get referenced points (read csv)");
//        setProgress("get referenced points (read csv)", 0);
//        localTimeBefore = System.nanoTime();
//
//        //get referenced points (read csv)
//        try {
//
//            CSVReader reader = new CSVReader(new FileReader(csvCoordinatesPath), ',');
//            Map<String, Vec3D> lines = new HashMap<>();
//            String line[];
//
//            //skip header
//            reader.readNext();
//
//            while ((line = reader.readNext()) != null) {
//
//                lines.put(line[0], new Vec3D(Double.valueOf(line[1]), Double.valueOf(line[2]), Double.valueOf(line[3])));
//            }
//
//            Vec2D v = new Vec2D(lines.get("B4").x - lines.get("T1").x, lines.get("B4").y - lines.get("T1").y);
//            double rho = (double) Math.atan(v.x / v.y);
//
//            trans = new Vec3D(-lines.get("T1").x, -lines.get("T1").y, -lines.get("T1").z);
//            trans.z = 0; //no vertical translation
//
//            mat_rot.mat = new double[]{
//                (double) Math.cos(rho), (double) -Math.sin(rho), 0,
//                (double) Math.sin(rho), (double) Math.cos(rho), 0,
//                0, 0, 1
//            };
//
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(AlsPreprocessing.class
//                    .getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(AlsPreprocessing.class
//                    .getName()).log(Level.SEVERE, null, ex);
//        }
//
//        localTimeAfter = System.nanoTime();
//        executionTime = (localTimeAfter - localTimeBefore) * Math.pow(10, -9);
//        System.out.println(" (" + executionTime + ")");
//
//        //read in ALS lidar and flight data
//        //ALS has been clipped to area of interest and exported in txt format.
//        System.out.print("read als file");
//        setProgress("read als file", 10);
//        localTimeBefore = System.nanoTime();
//        
//        
//        Collections.sort(alsList, new Comparator<Als>() {
//
//            @Override
//            public int compare(Als o1, Als o2) {
//                if (o2.t < o1.t) {
//                    return 1;
//                } else if (o2.t > o1.t) {
//                    return -1;
//                } else {
//                    return 0;
//                }
//            }
//        });
//        
//        localTimeAfter = System.nanoTime();
//        executionTime = (localTimeAfter - localTimeBefore) * Math.pow(10, -9);
//        System.out.println(" (" + executionTime + ")");
//
//        System.out.print("read trajectory file");
//        setProgress("read trajectory file", 20);
//        localTimeBefore = System.nanoTime();
//
//        try {
//
//            BufferedReader reader = new BufferedReader(new FileReader(trajectoryFile));
//
//            String line;
//
//            //skip header
//            reader.readLine();
//
//            while ((line = reader.readLine()) != null) {
//
//                String[] lineSplit = line.split(",");
//                Trajectory traj = new Trajectory(Double.valueOf(lineSplit[0]), Double.valueOf(lineSplit[1]),
//                        Double.valueOf(lineSplit[2]), Double.valueOf(lineSplit[3]));
//
//                trajectoryList.put(Double.valueOf(lineSplit[3]), traj);
//
//            }
//
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(AlsPreprocessing.class
//                    .getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(AlsPreprocessing.class
//                    .getName()).log(Level.SEVERE, null, ex);
//        }
//
//        localTimeAfter = System.nanoTime();
//        executionTime = (localTimeAfter - localTimeBefore) * Math.pow(10, -9);
//        System.out.println(" (" + executionTime + ")");
//
//        /*##############################
//         # MERGE ECHOES and TRAJECTORY #
//         ##############################*/
//        System.out.print("merge echoes and trajectory");
//        setProgress("merge echoes and trajectory", 30);
//        localTimeBefore = System.nanoTime();
//        
//        
//        
//        
//        //Vecteur des temps GPS
//        ArrayList<TimeVector> TGPS = new ArrayList<>();
//
//        for (Entry<Double, Trajectory> entry : trajectoryList.entrySet()) {
//            TGPS.add(new TimeVector(entry.getKey(), true, TGPS.size()));
//        }
//
//        //Vecteur des temps LIDAR
//        ArrayList<TimeVector> TLIDAR = new ArrayList<>();
//
//        for (Als alsList1 : alsList) {
//            TLIDAR.add(new TimeVector(alsList1.t, false, TLIDAR.size()));
//        }
//
//        ArrayList<TimeVector> fusion = new ArrayList<>();
//        fusion.addAll(TGPS);
//        fusion.addAll(TLIDAR);
//
//        //Trier selon le temps
//        Collections.sort(fusion, new Comparator<TimeVector>() {
//
//            @Override
//            public int compare(TimeVector vec1, TimeVector vec2) {
//                if (vec2.x < vec1.x) {
//                    return 1;
//                } else if (vec2.x > vec1.x) {
//                    return -1;
//                } else {
//                    return 0;
//                }
//            }
//        });
//        ArrayList<Vec3D> trajectoryInterpolate = new ArrayList<>();
//        //remplace les NA par la valeur précédente non NA dans la liste
//        for (int i = 0; i < fusion.size(); i++) {
//            if (!fusion.get(i).isGpsTime) {
//                for (int min = i - 1; min >= 0; min--) {
//                    if (fusion.get(min).isGpsTime) {
//                        
//                        for (int max = i + 1; max < fusion.size(); max++) {
//                            
//                            if (fusion.get(max).isGpsTime) {
//                                
//                                double ratio = (fusion.get(i).x-fusion.get(min).x)/(fusion.get(max).x-fusion.get(min).x);
//                                
//                                double xValue = trajectoryList.get(fusion.get(min).x).x + ((trajectoryList.get(fusion.get(max).x).x - trajectoryList.get(fusion.get(min).x).x)*ratio);       
//                                double yValue = trajectoryList.get(fusion.get(min).x).y + ((trajectoryList.get(fusion.get(max).x).y - trajectoryList.get(fusion.get(min).x).y)*ratio);
//                                double zValue = trajectoryList.get(fusion.get(min).x).z + ((trajectoryList.get(fusion.get(max).x).z - trajectoryList.get(fusion.get(min).x).z)*ratio);
//                                trajectoryInterpolate.add(new Vec3D(xValue, yValue, zValue));
//                                
//                                max = fusion.size()-1;
//                            }
//                        }
//
//                        min = 0;
//                    }
//                }
//            }
//        }
//        
//        int compteur = 0;
//        for (Als als : alsList) {
//            
//            AlsMixTrajectory mix = new AlsMixTrajectory(als, 0, 0, 0);
//            
//            
//            X.add(trajectoryInterpolate.get(compteur).x + trans.x);
//            Y.add(trajectoryInterpolate.get(compteur).y + trans.y);
//            Z.add(trajectoryInterpolate.get(compteur).z + trans.z);
//
//            x.add(als.location.x + trans.x);
//            y.add(als.location.y + trans.y);
//            z.add(als.location.z + trans.z);
//
//            ALL.add(mix);
//            
//            
//            compteur ++;
//        }
//
//        localTimeAfter = System.nanoTime();
//        executionTime = (localTimeAfter - localTimeBefore) * Math.pow(10, -9);
//        System.out.println(" (" + executionTime + ")");
//
//
//        /*#################################
//         #reproject source points in LOCS#
//         #################################*/
//        System.out.print("reproject source points in LOCS");
//        setProgress("reproject source points in LOCS", 40);
//        localTimeBefore = System.nanoTime();
//
//        //transposée
//        Mat loc_coord_offset = new Mat(X.size(), 3);
//        loc_coord_offset.setColumn(0, X.toArray(new Double[X.size()]));
//        loc_coord_offset.setColumn(1, Y.toArray(new Double[Y.size()]));
//        loc_coord_offset.setColumn(2, Z.toArray(new Double[Z.size()]));
//
//        //rotation            
//        Mat loc_coord_rot = Mat.transpose(Mat.multiply(mat_rot.toMat(), Mat.transpose(loc_coord_offset)));
//
//        for (int i = 0; i < loc_coord_rot.lineNumber; i++) {
//            
//            ALL.get(i).xloc_s = loc_coord_rot.mat[i][0];
//            ALL.get(i).yloc_s = loc_coord_rot.mat[i][1];
//            ALL.get(i).zloc_s = loc_coord_rot.mat[i][2];
//
//        }
//
//        localTimeAfter = System.nanoTime();
//        executionTime = (localTimeAfter - localTimeBefore) * Math.pow(10, -9);
//        System.out.println(" (" + executionTime + ")");
//
//        /*#################################
//         #reproject echoes for extracting DTM in new coordinate system
//         #################################*/
//        System.out.print("reproject echoes for extracting DTM in new coordinate system");
//        setProgress("reproject echoes for extracting DTM in new coordinate system", 50);
//        localTimeBefore = System.nanoTime();
//
//        loc_coord_offset = new Mat(x.size(), 3);
//        loc_coord_offset.setColumn(0, x.toArray(new Double[x.size()]));
//        loc_coord_offset.setColumn(1, y.toArray(new Double[y.size()]));
//        loc_coord_offset.setColumn(2, z.toArray(new Double[z.size()]));
//
//        //rotation            
//        loc_coord_rot = Mat.transpose(Mat.multiply(mat_rot.toMat(), Mat.transpose(loc_coord_offset)));
//
//        for (int i = 0; i < loc_coord_rot.lineNumber; i++) {
//            ALL.get(i).xloc = loc_coord_rot.mat[i][0];
//            ALL.get(i).yloc = loc_coord_rot.mat[i][1];
//            ALL.get(i).zloc = loc_coord_rot.mat[i][2];
//        }
//
//        localTimeAfter = System.nanoTime();
//        executionTime = (localTimeAfter - localTimeBefore) * Math.pow(10, -9);
//        System.out.println(" (" + executionTime + ")");
//
//        localTimeAfter = System.nanoTime();
//        executionTime = (localTimeAfter - localTimeBefore) * Math.pow(10, -9);
//        System.out.println(" (" + executionTime + ")");
//
//        System.out.print("récupération de la distance des échos");
//        setProgress("récupération de la distance des échos", 60);
//        localTimeBefore = System.nanoTime();
//
//        for (AlsMixTrajectory all : ALL) {
//
//            all.range = dfdist(all.xloc_s, all.yloc_s, all.zloc_s, all.xloc, all.yloc, all.zloc);
//
//            all.x_u = (all.xloc - all.xloc_s) / all.range;
//            all.y_u = (all.yloc - all.yloc_s) / all.range;
//            all.z_u = (all.zloc - all.zloc_s) / all.range;
//        }
//
//        localTimeAfter = System.nanoTime();
//        executionTime = (localTimeAfter - localTimeBefore) * Math.pow(10, -9);
//        System.out.println(" (" + executionTime + ")");
//
//        return ALL;
//    }
    
    public void writeEchosFile(String fileName){
        
        if(echos != null){
            
            try {
                
                outputFile = new File(fileName);
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

                writer.write("\"n\" \"xloc_s\" \"yloc_s\" \"zloc_s\" \"x_u\" \"y_u\" \"z_u\" \"r1\" \"r2\" \"r3\" \"r4\" \"r5\" \"r6\" \"r7\"\n");

                for (Entry<String, Echo> entry : echos.entrySet()) {

                    DecimalFormat df = new DecimalFormat("#0.00");
                    DecimalFormat df2 = new DecimalFormat("#0.00000");

                    String r1 = String.valueOf(entry.getValue().r1);
                    String r2 = String.valueOf(entry.getValue().r2);
                    String r3 = String.valueOf(entry.getValue().r3);
                    String r4 = String.valueOf(entry.getValue().r4);
                    String r5 = String.valueOf(entry.getValue().r5);
                    String r6 = String.valueOf(entry.getValue().r6);
                    String r7 = String.valueOf(entry.getValue().r7);

                    if (Double.isNaN(entry.getValue().r1)) {
                        r1 = r1.replace("NaN", "");
                    } else {
                        r1 = df.format(entry.getValue().r1);
                    }
                    if (Double.isNaN(entry.getValue().r2)) {
                        r2 = r2.replace("NaN", "");
                    } else {
                        r2 = df.format(entry.getValue().r2);
                    }
                    if (Double.isNaN(entry.getValue().r3)) {
                        r3 = r3.replace("NaN", "");
                    } else {
                        r3 = df.format(entry.getValue().r3);
                    }
                    if (Double.isNaN(entry.getValue().r4)) {
                        r4 = r4.replace("NaN", "");
                    } else {
                        r4 = df.format(entry.getValue().r4);
                    }
                    if (Double.isNaN(entry.getValue().r5)) {
                        r5 = r5.replace("NaN", "");
                    } else {
                        r5 = df.format(entry.getValue().r5);
                    }
                    if (Double.isNaN(entry.getValue().r6)) {
                        r6 = r6.replace("NaN", "");
                    } else {
                        r6 = df.format(entry.getValue().r6);
                    }
                    if (Double.isNaN(entry.getValue().r7)) {
                        r7 = r7.replace("NaN", "");
                    } else {
                        r7 = df.format(entry.getValue().r7);
                    }

                    String line = entry.getValue().n + " "
                            + df.format(entry.getValue().xloc_s) + " "
                            + df.format(entry.getValue().yloc_s) + " "
                            + df.format(entry.getValue().zloc_s) + " "
                            + df2.format(entry.getValue().x_u) + " "
                            + df2.format(entry.getValue().y_u) + " "
                            + df2.format(entry.getValue().z_u) + " "
                            + r1 + " "
                            + r2 + " "
                            + r3 + " "
                            + r4 + " "
                            + r5 + " "
                            + r6 + " "
                            + r7 + " " + "\n";
                    
                    line = line.replace(",", ".");
                    writer.write(line);
                }

                writer.close();



            } catch (IOException ex) {
                Logger.getLogger(VoxelPreprocessing.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public void buildEchos(ArrayList<AlsMixTrajectory> ALL){
        
        double oldTime = -1;
        int oldN = -1;

        int compteur = 0;

        echos = new HashMap<>();
        Echo e = null;
        boolean isNewExp = false;

        for (AlsMixTrajectory all : ALL) {

            double time = all.als.t;

            if (isNewExp && time != oldTime) {
                
                /***vérifie que le nombre d'échos lus correspond bien au nombre d'échos total*
                 permet d'éviter le plantage du programme de voxelisation
                 est-ce pertinent? possible perte d'imformations
                 modifier le programme de voxelisation de Jean Dauzat si on ne veut pas nettoyer
                 **/
                if (oldN == compteur) {
                    echos.put(String.valueOf(oldTime), e);
                }
                compteur = 0;
                isNewExp = false;

            }

            if (time == oldTime || (!isNewExp && time != oldTime)) {

                if (!isNewExp && time != oldTime) {

                    e = new Echo(all.als.n, all.xloc_s, all.yloc_s, all.zloc_s, all.x_u, all.y_u, all.z_u);
                    isNewExp = true;
                }

                switch (all.als.r) {

                    case 1:
                        e.r1 = all.range;
                        compteur++;
                        break;
                    case 2:
                        e.r2 = all.range;
                        compteur++;
                        break;
                    case 3:
                        e.r3 = all.range;
                        compteur++;
                        break;
                    case 4:
                        e.r4 = all.range;
                        compteur++;
                        break;
                    case 5:
                        e.r5 = all.range;
                        compteur++;
                        break;
                    case 6:
                        e.r6 = all.range;
                        compteur++;
                        break;
                    case 7:
                        e.r7 = all.range;
                        compteur++;
                        break;
                }

            }

            oldTime = time;
            oldN = all.als.n;

        }
    }
    
    public Map<String, Echo> generateEchosFile(final Mat4D transfMatrix, final String pointsFile, final String trajectoryFile) {
        

        setFinished(false);

        globalTimeBefore = System.nanoTime();

        System.out.println("preprocessing voxelisation:");
        setProgress("preprocessing voxelisation", 10);

        ArrayList<Als> alsList = null;

        localTimeBefore = System.nanoTime();

        if(pointsFile.endsWith(".las")){

            System.out.println("read als file: " + pointsFile);
            setProgress("preprocessing voxelisation", 20);


            alsList = readLas(LasReader.read(pointsFile));

            System.out.print("als file read");
            setProgress("als file read", 20);


        }else if(pointsFile.endsWith(".txt")){

            System.out.println("read txt file: " + pointsFile);
            setProgress("preprocessing voxelisation", 10);

            alsList = readTxt();

            System.out.print("txt file read");
            setProgress("txt file read", 20);

        }
        else if(pointsFile.endsWith(".rxp")){

            System.err.println("rxp extension not supported yet");

        }else{
            System.err.println("extension file is not known");

            return null;
        }

        localTimeAfter = System.nanoTime();
        executionTime = (localTimeAfter - localTimeBefore) * Math.pow(10, -9);
        System.out.println(" (" + df.format(executionTime) + " s)");


        ArrayList<AlsMixTrajectory> ALL = transformPoints(transfMatrix, alsList, trajectoryFile);

        setProgress("build echos", 80);

        buildEchos(ALL);

        setProgress("write echos file", 90);

        File f = new File(pointsFile);
        writeEchosFile(f.getName()+"_echos");

        globalTimeAfter = System.nanoTime();
        double executionTime = (globalTimeAfter - globalTimeBefore) * Math.pow(10, -9);
        System.out.println("Total time: " + df.format(executionTime)+" s");

        setProgress("done in "+ executionTime+" seconds", 100);
        setFinished(true);

        return Collections.unmodifiableMap(echos);
    }
    
}
