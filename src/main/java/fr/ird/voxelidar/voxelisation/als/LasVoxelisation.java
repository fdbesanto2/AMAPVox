/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.als;

import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.Shot;
import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.VoxelAnalysis;
import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.VoxelParameters;
import fr.ird.voxelidar.lidar.format.als.Las;
import fr.ird.voxelidar.lidar.format.als.LasHeader;
import fr.ird.voxelidar.lidar.format.als.PointDataRecordFormat0;
import fr.ird.voxelidar.math.matrix.Mat;
import fr.ird.voxelidar.math.matrix.Mat4D;
import fr.ird.voxelidar.math.vector.Vec3D;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

/**
 *
 * @author Julien
 */
public class LasVoxelisation extends Processing {

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
        
        Map<String, Shoot> shoots = buildEchos(ALL);
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
            Logger.getLogger(LasVoxelisation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LasVoxelisation.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(LasVoxelisation.class.getName()).log(Level.SEVERE, null, ex);
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

        Map<Double, Trajectory> trajectoryList = new HashMap<>();
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
            Logger.getLogger(LasVoxelisation.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LasVoxelisation.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        /*##############################
         # MERGE ECHOES and TRAJECTORY #
         ##############################*/
        //Vecteur des temps GPS
        ArrayList<TimeVector> TGPS = new ArrayList<>();

        for (Map.Entry<Double, Trajectory> entry : trajectoryList.entrySet()) {
            TGPS.add(new TimeVector(entry.getKey(), true, TGPS.size()));
        }

        //Vecteur des temps LIDAR
        ArrayList<TimeVector> TLIDAR = new ArrayList<>();

        for (LasPoint lasPoint : lasPointList) {
            TLIDAR.add(new TimeVector(lasPoint.t, false, TLIDAR.size()));
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
        
        for (int i = 0; i < fusion.size(); i++) {
            if (!fusion.get(i).isGpsTime) {
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
    
    public void voxelise(Map<String, Shoot> shoots){
        
        VoxelAnalysis voxelAnalysis = new VoxelAnalysis();
                voxelAnalysis.init(parameters);
                
        for(Entry entry:shoots.entrySet()){
            
            Shoot shoot = (Shoot) entry.getValue();
            
            float[] rangesTemp = new float[7];
            rangesTemp[0] = (float) shoot.r1;
            rangesTemp[1] = (float) shoot.r2;
            rangesTemp[2] = (float) shoot.r3;
            rangesTemp[3] = (float) shoot.r4;
            rangesTemp[4] = (float) shoot.r5;
            rangesTemp[5] = (float) shoot.r6;
            rangesTemp[6] = (float) shoot.r7;
            
            float[] ranges = new float[shoot.n];
            
            System.arraycopy(rangesTemp, 0, ranges, 0, shoot.n);
            
            Shot shot = new Shot(shoot.n, 
                    new Point3f((float)shoot.xloc_s, (float)shoot.yloc_s, (float)shoot.zloc_s), 
                    new Vector3f((float)shoot.x_u, (float)shoot.y_u, (float)shoot.z_u),
                    ranges);
            
            voxelAnalysis.voxelise(shot);
        }
        
        voxelAnalysis.calculatePAD(0);
        voxelAnalysis.writeOutput(outputFile);
    }

    /*
    public void writeShootsFile(Map<String, Shoot> shoots) {

        if (shoots != null) {

            try {
                
                outputFile = new File("shoots_file");
                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

                writer.write("\"n\" \"xloc_s\" \"yloc_s\" \"zloc_s\" \"x_u\" \"y_u\" \"z_u\" \"r1\" \"r2\" \"r3\" \"r4\" \"r5\" \"r6\" \"r7\"\n");

                for (Map.Entry<String, Shoot> entry : shoots.entrySet()) {

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
                Logger.getLogger(LasVoxelisation.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    */

    public Map<String,Shoot> buildEchos(ArrayList<LasMixTrajectory> ALL) {

        double oldTime = -1;
        int oldN = -1;

        int compteur = 0;
        
        Map<String,Shoot> shoots = new HashMap<>();
        
        Shoot e = null;
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

                    e = new Shoot(all.lasPoint.n, all.xloc_s, all.yloc_s, all.zloc_s, all.x_u, all.y_u, all.z_u);
                    isNewExp = true;
                }

                switch (all.lasPoint.r) {

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
            oldN = all.lasPoint.n;

        }
        
        return shoots;
    }

    /*
    public Map<String, Shoot> generateEchosFile(final Mat4D transfMatrix, final String pointsFile, final String trajectoryFile) {

        setFinished(false);

        ArrayList<Las2> lasList = null;

        if (pointsFile.endsWith(".las")) {

            lasList = readLas(LasReader.read(pointsFile));

        } else if (pointsFile.endsWith(".txt")) {

            lasList = readTxt();

        } else if (pointsFile.endsWith(".rxp")) {

            System.err.println("rxp extension not supported yet");

        } else {
            System.err.println("extension file is not known");

            return null;
        }

        ArrayList<LasMixTrajectory> ALL = transformPoints(transfMatrix, lasList, trajectoryFile);
        buildEchos(ALL);

        File f = new File(pointsFile);
        writeEchosFile(f.getName() + "_echos");

        setFinished(true);

        return Collections.unmodifiableMap(echos);
    }
    */
}
