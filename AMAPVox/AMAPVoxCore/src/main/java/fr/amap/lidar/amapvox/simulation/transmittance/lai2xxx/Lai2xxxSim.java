/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.simulation.transmittance.lai2xxx;

import fr.amap.commons.math.util.SphericalCoordinates;
import fr.amap.commons.util.Statistic;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineElement;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineSegment;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.util.BoundingBox3d;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.DirectionalTransmittance;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.Scene;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.VoxelManager;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.VoxelManagerSettings;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.VoxelSpace;
import fr.amap.lidar.amapvox.simulation.transmittance.TransmittanceCfg;
import fr.amap.lidar.amapvox.simulation.transmittance.TransmittanceParameters;
import static fr.amap.lidar.amapvox.simulation.transmittance.lai2xxx.LAI2xxx.ViewCap.CAP_360;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxreader.VoxelFileReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import fr.amap.commons.util.Cancellable;

/**
 *
 * @author Julien
 */
public class Lai2xxxSim implements Cancellable{
    
    private final static Logger logger = Logger.getLogger(Lai2xxxSim.class);
    
    private final LAI2xxx lai2xxx;
    private TransmittanceParameters parameters;
    
    private DirectionalTransmittance direcTransmittance;
    private List<Point3d> positions;
    
    private final Vector3f[] directions;
    private VoxelSpace voxSpace;
    
    private Statistic transmittedStatistic;
    
    private boolean cancelled;
    
    //temporaire, pour test
    private final static boolean TRANSMITTANCE_NORMALISEE = true;

    public Lai2xxxSim(TransmittanceCfg cfg) {
        
        parameters = cfg.getParameters();
        transmittedStatistic = new Statistic();
        
        if(parameters.getMode() == TransmittanceParameters.Mode.LAI2000){
            lai2xxx = new LAI2000(parameters.getDirectionsNumber(), CAP_360, parameters.getMasks());
        }else{
            lai2xxx = new LAI2200(parameters.getDirectionsNumber(), CAP_360, parameters.getMasks());
        }

        logger.info("Computing directions...");
        
        lai2xxx.computeDirections();
        
        directions = lai2xxx.getDirections();
    }
    
    public void process() throws Exception{
        
        if(!TRANSMITTANCE_NORMALISEE){
            logger.info("===== " + parameters.getInputFile().getAbsolutePath() + " =====");

            if(direcTransmittance == null){
                direcTransmittance = new DirectionalTransmittance(parameters.getInputFile());
            }

            voxSpace = direcTransmittance.getVoxSpace();

            getSensorPositions();

            // TRANSMITTANCE
            logger.info("Computation of transmittance");

            lai2xxx.initPositions(positions.size());

            int positionID = 0;
            double transmitted;

            for (Point3d position : positions) {

                for (int t = 0; t < directions.length; t++) {
                    
                    if(cancelled){
                        return;
                    }

                    Vector3d dir = new Vector3d(directions[t]);
                    dir.normalize();

                    transmitted = direcTransmittance.directionalTransmittance(position, dir);
                    transmittedStatistic.addValue(transmitted);

                    if(!Double.isNaN(transmitted)){

                        int ring = lai2xxx.getRingIDFromDirectionID(t);
                        lai2xxx.addTransmittance(ring, positionID, (float) transmitted);
                    }              

                }

                positionID++;

                if (positionID % 1000 == 0) {
                    logger.info(positionID + "/" + positions.size());
                }
            }

            if(transmittedStatistic.getNbNaNValues() > 0){
                logger.warn("Some rays crossed NA voxels, count: "+transmittedStatistic.getNbNaNValues());
            }

            if(parameters.isGenerateTextFile()){
                writeTransmittance();
                logger.info("File "+parameters.getTextFile().getAbsolutePath()+" was written.");
            }

            logger.info("Simulation is finished.");
        }else{
            //*******début du test
            //lecture du fichier voxel
            VoxelFileReader voxReader = new VoxelFileReader(parameters.getInputFile());
            VoxelSpaceInfos infos = voxReader.getVoxelSpaceInfos();

            Iterator<Voxel> iterator = voxReader.iterator();
            Voxel voxels[][][] = new Voxel[infos.getSplit().x][infos.getSplit().y][infos.getSplit().z];

            //conversion de la liste de voxels en tableau 3d
            while (iterator.hasNext()) {
                Voxel voxel = iterator.next();
                voxels[voxel.i][voxel.j][voxel.k] = voxel;
            }
            //initialisation de la scène
            Scene scene = new Scene();
            scene.setBoundingBox(new BoundingBox3d(infos.getMinCorner(), infos.getMaxCorner()));


            //création d'un nouveau VoxelManager avec les paramètres du fichier voxel
            VoxelManager vm = new VoxelManager(scene, new VoxelManagerSettings(infos.getSplit(), 0));

            List<Double[]> l = new ArrayList<>();


            /*try (BufferedReader reader = new BufferedReader(new FileReader(new File("/media/forestview01/partageLidar/FTH2014_LAI2200/data/LAI_P9_M_fusion_new.txt")))) {
                reader.readLine();

                String line;

                while((line = reader.readLine()) != null){
                    String[] split = line.split("\t");
                    l.add(new Double[]{Double.valueOf(split[4]), Double.valueOf(split[5]), Double.valueOf(split[6]), Double.valueOf(split[7]), Double.valueOf(split[8])});
                }
            }*/

            /*Statistic[][] tranStattistics = new Statistic[l.size()][5];
            for(int i=0;i<tranStattistics.length;i++){
                for(int j=0;j<5;j++){
                    tranStattistics[i][j] = new Statistic();
                }
            }*/

            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(parameters.getTextFile().getAbsolutePath()+"_test.txt")));
            writer.write("position.ID"+" "+"position.x"+" "+"position.y"+" "+"position.z"+" "+"ring"+" "+"pathLength"+" "+"transmittance"+" "+"isOut"+" "+"azimut"+" "+"elevation"+" "+"cross_NA"+"\n");

            //*******fin du test

            logger.info("===== " + parameters.getInputFile().getAbsolutePath() + " =====");

            direcTransmittance = new DirectionalTransmittance(parameters.getInputFile());
            voxSpace = direcTransmittance.getVoxSpace();

            getSensorPositions();

            // TRANSMITTANCE
            logger.info("Computation of transmittance");


            lai2xxx.initPositions(positions.size());

            int positionID = 0;
            double transmitted;

            Statistic NaNCounter = new Statistic();

            for (Point3d position : positions) {

                for (int t = 0; t < directions.length; t++) {

                    if(cancelled){
                        return;
                    }
                    
                    Vector3d dir = new Vector3d(directions[t]);
                    dir.normalize();

                    transmitted = direcTransmittance.directionalTransmittance(position, dir);

                    int ring = lai2xxx.getRingIDFromDirectionID(t);


                    //test
                    LineElement lineElement = new LineSegment(position, new Vector3d(dir), 99999999);
                    //distance cumulée
                    double distance = 0;


                    //dernière distance valide (sortie de canopée)
                    double lastValidDistance = 0;

                    //get the first voxel cross by the line
                    VoxelManager.VoxelCrossingContext context = vm.getFirstVoxel(lineElement);

                    double distanceToHit = lineElement.getLength();
                    boolean gotOneNaN = false;

                    boolean wasOutside = false;

                    SphericalCoordinates sc = new SphericalCoordinates();
                    sc.toSpherical(dir);

                    while ((context != null) && (context.indices != null)) {
                        
                        if(cancelled){
                            return;
                        }

                        //current voxel
                        Point3i indices = context.indices;
                        Voxel voxel = voxels[indices.x][indices.y][indices.z];

                        if(voxel.ground_distance < 0.0f){
                            break;
                        }

                        if(Double.isNaN(voxel.PadBVTotal)){
                            gotOneNaN = true;
                            break;
                        }

                        //distance from the last origin to the point in which the ray enter the voxel
                        double d1 = context.length;

                        context = vm.CrossVoxel(lineElement, indices);


                        if(context != null && context.indices == null){
                            if(voxel.k == infos.getSplit().z -1){
                                wasOutside = false;
                            }else{
                                wasOutside = true;
                            }

                        }

                        //distance from the last origin to the point in which the ray exit the voxel
                        double d2 = context.length;

                        if (d2 < distanceToHit) {

                            distance += (d2 - d1);

                        }else if (d1 >= distanceToHit) {

                        }else {
                            distance += (d2 - d1);
                        }

                        if(voxel.PadBVTotal > 0){
                            lastValidDistance = distance;
                        }
                    }

                    double pathLength = lastValidDistance;

                    if(Double.isNaN(transmitted)){
                        gotOneNaN = true;
                    }

                    //test
                    if(!gotOneNaN && pathLength != 0){

                        NaNCounter.addValue(transmitted);

                        //lai2xxx.addNormalizedTransmittance(ring, positionID, (float) (Math.pow(transmitted, 1/pathLength)), (float) pathLength);
                        lai2xxx.addTransmittance(ring, positionID, (float) transmitted);

                        //tranStattistics[positionID][ring].addValue((Math.pow(l.get(positionID)[ring], 1/pathLength)));

                    }else{
                        NaNCounter.addValue(Double.NaN);
                    }

                    writer.write(positionID+" "+position.x+" "+position.y+" "+position.z+" "+(ring+1)+" "+pathLength+" "+transmitted+" "+wasOutside+" "+(float)Math.toDegrees(sc.getAzimut())+" "+(float)Math.toDegrees(sc.getZenith())+" "+gotOneNaN+"\n");

                    //lai2xxx.addTransmittance(ring, positionID, (float) transmitted);

                }

                positionID++;

                if (positionID % 1000 == 0) {
                    logger.info(positionID + "/" + positions.size());
                }
            }

            //test
            System.out.println("Nb values : "+NaNCounter.getNbValues());
            System.out.println("Nb NaN values : "+NaNCounter.getNbNaNValues());

            writer.close();

    //        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/media/forestview01/partageLidar/FTH2014_LAI2200/data/tests/normalisation_mesure/methode2/transmittances.txt")))) {
    //            
    //            for(int i=0;i<tranStattistics.length;i++){
    //                
    //                String line = i+"";
    //                
    //                for(int j=0;j<5;j++){
    //                    line += "\t"+tranStattistics[i][j].getMean();
    //                }
    //                
    //                writer.write(line+"\n");
    //                
    //            }
    //        }


            if(parameters.isGenerateTextFile()){
                
                if(cancelled){
                    return;
                }

                writeTransmittance();
                logger.info("File "+parameters.getTextFile().getAbsolutePath()+" was written.");
            }

            logger.info("Simulation is finished."); 
        }
        
    }
    
    //doublon (même méthode dans TransmittanceSim, à nettoyer)
    private void getSensorPositions() {
        
        positions = parameters.getPositions();
        
        if(positions == null){ //to remove in the future, keep compatibility with deprecated functions
            
            positions = new ArrayList<>();
        
            if(parameters.isUseScanPositionsFile()){

                File pointPositionsFile = parameters.getPointsPositionsFile();

                try {
                    BufferedReader reader = new BufferedReader(new FileReader(pointPositionsFile));

                    String line;
                    boolean firstLineParsed = false;

                    while((line = reader.readLine()) != null){

                        line = line.replaceAll(" ", ",");
                        line = line.replaceAll("\t", ",");

                        String[] split = line.split(",");

                        if(split != null && split.length >= 3){

                            if(split.length > 3 && !firstLineParsed){
                                logger.info("Sensor position file contains more than three columns, parsing the three first");
                            }

                            Point3d position = new Point3d(Double.valueOf(split[0]), Double.valueOf(split[1]), Double.valueOf(split[2]));

                            int i = (int) ((position.x - voxSpace.getBoundingBox().min.x) / voxSpace.getVoxelSize().x);
                            int j = (int) ((position.y - voxSpace.getBoundingBox().min.y) / voxSpace.getVoxelSize().y);

                            if(i < voxSpace.getSplitting().x && i >= 0 && j < voxSpace.getSplitting().y && j >= 0){
                                positions.add(position);
                            }else{
                                logger.warn("Position "+position.toString() +" ignored because out of voxel space!");
                            }
                        }

                        if(!firstLineParsed){
                            firstLineParsed = true;
                        }
                    }

                } catch (FileNotFoundException ex) {
                    logger.error("File "+ parameters.getPointsPositionsFile()+" not found", ex);
                } catch (IOException ex) {
                    logger.error("An error occured when reading file", ex);
                }

            }else{
                // Smaller plot at center
                int size = (int)parameters.getWidth();

                int middleX = (int)parameters.getCenterPoint().x;
                int middleY = (int)parameters.getCenterPoint().y;

                int xMin = middleX - size;
                int yMin = middleY - size;

                int xMax = middleX + size;
                int yMax = middleY + size;

                xMin = Integer.max(xMin, 0);
                yMin = Integer.max(yMin, 0);

                xMax = Integer.min(xMax, voxSpace.getSplitting().x -1);
                yMax = Integer.min(yMax, voxSpace.getSplitting().y -1);

                for (int i = xMin; i < xMax; i++) {

                    double tx = (0.5f + (double) i) * voxSpace.getVoxelSize().x;

                    for (int j = yMin; j < yMax; j++) {

                        double ty = (0.5f + (double) j) * voxSpace.getVoxelSize().y;
                        Point3d pos = new Point3d(voxSpace.getBoundingBox().min);
                        pos.add(new Point3d(tx, ty, direcTransmittance.getMnt()[i][j] + parameters.getCenterPoint().z));
                        positions.add(pos);
                    }
                }

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(parameters.getTextFile().getParentFile()+File.separator+"positions.txt")))) {

                    for(Point3d position : positions){
                        writer.write(position.x + " " + position.y + " " + position.z + "\n");
                    }

                    writer.close();
                }catch (IOException ex) {
                logger.error("Cannot write positions.txt file in output directory", ex);
                }
            }
        }
        
        logger.info("nb positions= " + positions.size());
    }
    
    public void writeTransmittance() throws IOException{
        
        if(parameters.isGenerateLAI2xxxTypeFormat()){
            lai2xxx.writeOutput(parameters.getTextFile());
        }else{

            lai2xxx.computeValues();

            try (BufferedWriter bw = new BufferedWriter(new FileWriter(parameters.getTextFile()))) {

                //bw.write("posX\tposY\tposZ\tLAI\tGAP[1]\tGAP[2]\tGAP[3]\tGAP[4]\tGAP[5]\n");
                //test
                bw.write("posX\tposY\tposZ\tLAI\tGAP[1]\tGAP[2]\tGAP[3]\tGAP[4]\tGAP[5]"/*
                        + "\tGAP[1]_normalized\tGAP[2]_normalized\tGAP[3]_normalized\tGAP[4]_normalized\tGAP[5]_normalized"
                        + "\tGAP[1]_mean_pathLength\tGAP[2]_mean_pathLength\tGAP[3]_mean_pathLength\tGAP[4]_mean_pathLength\tGAP[5]_mean_pathLength*/+"\n");
                
                for(int i =0 ; i<positions.size() ; i++){

                    Point3d position = positions.get(i);

                    String line = position.x + "\t" + position.y + "\t" + position.z+"\t" + lai2xxx.getByPosition_LAI()[i];

                    for(int r=0;r<lai2xxx.getRingNumber();r++){
                        line += "\t"+lai2xxx.transmittances[r][i];
                    }
//                    
//                    //test
//                    for(int r=0;r<lai2xxx.getRingNumber();r++){
//                        line += "\t"+lai2xxx.getNormalizedTransmittances()[r][i];
//                    }
//                    
//                    //test
//                    for(int r=0;r<lai2xxx.getRingNumber();r++){
//                        line += "\t"+lai2xxx.getPathLengths()[r][i];
//                    }

                    bw.write(line+"\n");

                }
            }catch(IOException ex){
                throw ex;
            }

        }
    }

    public void setParameters(TransmittanceParameters parameters) {
        this.parameters = parameters;
    }

    public LAI2xxx getLai2xxx() {
        return lai2xxx;
    }

    public void setQuiet(boolean quiet) {
        
        if(quiet){
            logger.setLevel(Level.ERROR);
        }else{
            logger.setLevel(Level.INFO);
        }        
    }    

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
