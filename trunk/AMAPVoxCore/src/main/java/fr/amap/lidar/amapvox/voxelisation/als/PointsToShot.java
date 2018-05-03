/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.als;

import fr.amap.amapvox.als.LasHeader;
import fr.amap.amapvox.als.LasPoint;
import fr.amap.amapvox.als.las.LasReader;
import fr.amap.amapvox.als.las.PointDataRecordFormat;
import fr.amap.amapvox.als.laz.LazExtraction;
import fr.amap.commons.util.io.file.FileManager;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.util.Process;
import fr.amap.commons.util.io.file.CSVFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.vecmath.Vector3d;
import fr.amap.commons.util.Cancellable;
import fr.amap.lidar.amapvox.shot.Shot;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Comparator;

/**
 * This class merge trajectory file with point file (las, laz)
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class PointsToShot extends Process implements IterableWithException<AlsShot>, Cancellable{
    
    private final File inputFile;
    private final CSVFile trajectoryFile;
    
    private final Mat4D vopMatrix;
    
    private List<Trajectory> trajectoryList;
    private List<LasPoint> lasPointList;
    
    private boolean cancelled;
    
    public PointsToShot(CSVFile trajectoryFile, File inputFile, Mat4D vopMatrix){

        this.trajectoryFile = trajectoryFile;
        this.vopMatrix = vopMatrix;
        this.inputFile = inputFile;
    }

    public void init() throws FileNotFoundException, IOException, Exception {
                    
        /***reading las***/

        lasPointList = new ArrayList<>();

        LasHeader header;

        switch(FileManager.getExtension(inputFile)){
            case ".las":

                LasReader lasReader = new LasReader();
                try {
                    lasReader.open(inputFile);
                } catch (IOException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw ex;
                }
                header = lasReader.getHeader();

                long maxIterations = header.getNumberOfPointrecords();

                int iterations = 0;

                for (PointDataRecordFormat p : lasReader) {

                    fireProgress("Reading *.las", iterations, maxIterations);
                    
                    if(isCancelled()){
                        return;
                    }

                    /*if(p.isHasQLineExtrabytes()){
                        QLineExtrabytes qLineExtrabytes = p.getQLineExtrabytes();
                        //logger.info("QLineExtrabytes" + qLineExtrabytes.getAmplitude()+" "+qLineExtrabytes.getPulseWidth());
                    }*/
                    Vector3d location = new Vector3d((p.getX() * header.getxScaleFactor()) + header.getxOffset(), (p.getY() * header.getyScaleFactor()) + header.getyOffset(), (p.getZ() * header.getzScaleFactor()) + header.getzOffset());


                    LasPoint point = new LasPoint(location.x, location.y, location.z, p.getReturnNumber(), p.getNumberOfReturns(), p.getIntensity(), p.getClassification(), p.getGpsTime());
                    lasPointList.add(point);


                    iterations++;
                }
                break;

            case ".laz":

                LazExtraction laz = new LazExtraction();
                try {
                    laz.openLazFile(inputFile);
                } catch (Exception ex) {
                    throw ex;
                }

                header = laz.getHeader();

                long numberOfPointrecords = header.getNumberOfPointrecords();

                int count = 0;

                for (LasPoint p : laz) {

                    fireProgress("Reading *.laz", count, numberOfPointrecords);
                    
                    if(isCancelled()){
                        return;
                    }

                    p.x = (p.x * header.getxScaleFactor()) + header.getxOffset();
                    p.y = (p.y * header.getyScaleFactor()) + header.getyOffset();
                    p.z = (p.z * header.getzScaleFactor()) + header.getzOffset();


                    lasPointList.add(p);
                    count++;
                }
                laz.close();

                break;
        }

        /***sort las by time***/
        //lasPointList.sort(null);
        Collections.sort(lasPointList);

        double minTime = lasPointList.get(0).t;
        double maxTime = lasPointList.get(lasPointList.size()-1).t;
        
        minTime -= 0.1; 
        maxTime += 0.1;
        
        if(minTime == maxTime){
            //logger.error("ALS file doesn't contains time relative information, minimum and maximum time = "+minTime);
            return;
        }

        trajectoryList = new ArrayList<>();

        try {            

            BufferedReader reader = new BufferedReader(new FileReader(trajectoryFile));

            String line;
            
            int lineNumber = FileManager.getLineNumber(trajectoryFile.getAbsolutePath());
            int count = 0;
            
            if(trajectoryFile.containsHeader()){
                reader.readLine();
                count++;
            }

            for(long l = 0; l < trajectoryFile.getNbOfLinesToSkip();l++){
                reader.readLine();
                count++;
            }
            
            Map<String, Integer> columnAssignment = trajectoryFile.getColumnAssignment();
            Integer timeIndex = columnAssignment.get("Time");
            
            if(timeIndex == null){
                timeIndex = 3;
            }
            
            Integer eastingIndex = columnAssignment.get("Easting");
            if(eastingIndex == null){
                eastingIndex = 0;
            }
            
            Integer northingIndex = columnAssignment.get("Northing");
            if(northingIndex == null){
                northingIndex = 1;
            }
            
            Integer elevationIndex = columnAssignment.get("Elevation");
            if(elevationIndex == null){
                elevationIndex = 2;
            }

            
            while ((line = reader.readLine()) != null) {
                
                fireProgress("Reading trajectory file", count, lineNumber);

                String[] lineSplit = line.split(trajectoryFile.getColumnSeparator());

                double time = Double.valueOf(lineSplit[timeIndex]);

                //discard unused values
                if(time >= minTime && time <= maxTime){

                    Trajectory traj = new Trajectory(Double.valueOf(lineSplit[eastingIndex]), Double.valueOf(lineSplit[northingIndex]),
                        Double.valueOf(lineSplit[elevationIndex]), time);

                    trajectoryList.add(traj);
                }
                
                count++;
            }
            
            fireProgress("Sorting trajectory file", 99, 100);
            
            Collections.sort(trajectoryList, new Comparator<Trajectory>() {
                @Override
                public int compare(Trajectory o1, Trajectory o2) {
                    return Double.compare(o1.t, o2.t);
                }
            });
            
            fireProgress("Sorting trajectory file finished", 100, 100);

        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
        
    }

    @Override
    public PointsToShotIterator iterator() {
        
        return new PointsToShotIterator(trajectoryList, lasPointList, vopMatrix);
    }
    

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
    
    public void write(File outputFile) throws IOException, Exception{
        
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){
            
            writer.write("xOrigin yOrigin zOrigin xDirection yDirection zDirection nbEchoes r1 r2 r3 r4 r5 r6 r7 c1 c2 c3 c4 c5 c6 c7\n");
            
            PointsToShotIterator iterator = iterator();
            
            AlsShot shot;
            
            while((shot = iterator.next()) != null){
                
                String line = shot.origin.x+" "+shot.origin.y+" "+shot.origin.z+" "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z+" "+shot.getEchoesNumber();
                
                for (int i = 0; i < shot.ranges.length; i++) {
                    line += " "+ shot.ranges[i];
                }
                
                for (int i = shot.ranges.length; i < 7; i++) {
                    line += " "+ "NaN";
                }
                
                for (int i = 0; i < shot.classifications.length; i++) {
                    line += " "+ shot.classifications[i];
                }
                
                for (int i = shot.classifications.length; i < 7; i++) {
                    line += " "+ "NaN";
                }
                
                line += "\n";
                
                writer.write(line);
            }
        }
    }
}
