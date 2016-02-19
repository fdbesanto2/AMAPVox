/**
 *
 */
package fr.amap.lidar.amapvox.simulation.transmittance;

import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.DirectionalTransmittance;
import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.VoxelSpace;
import fr.amap.lidar.amapvox.jeeb.workspace.sunrapp.light.IncidentRadiation;
import fr.amap.lidar.amapvox.jeeb.workspace.sunrapp.light.SolarRadiation;
import fr.amap.lidar.amapvox.jeeb.workspace.sunrapp.light.Turtle;
import fr.amap.lidar.amapvox.jeeb.workspace.sunrapp.util.Colouring;
import fr.amap.lidar.amapvox.jeeb.workspace.sunrapp.util.Time;
import fr.amap.commons.util.Cancellable;
import fr.amap.lidar.amapvox.simulation.transmittance.util.Period;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Calendar;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;

/**
 * @author dauzat
 *
 */
public class TransmittanceSim implements Cancellable{

    private final static Logger logger = Logger.getLogger(TransmittanceSim.class);
    
    private TLSVoxel voxels[][][];
    private DirectionalTransmittance direcTransmittance;
    private double[][] transmissionPeriod;
    private int nbPeriods;
    private float mnt[][];
    private float mntZmax;
    private float mntZmin;
    private Turtle turtle;
    private List<File> outputBitmapFiles;

    private List<Point3d> positions;
    
    private TransmittanceParameters parameters;
    private TransmittanceCfg cfg;
    private VoxelSpace voxSpace;
    
    private boolean cancelled;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    private class TLSVoxel {

        float padBV;
    }
    
    public TransmittanceSim(){
        
        outputBitmapFiles = new ArrayList<>();
    }
    
    public void simulationProcess(TransmittanceCfg cfg) throws Exception{
        
        parameters = cfg.getParameters();
        init(parameters);
        process();

        if(parameters.isGenerateTextFile()){
            writeTransmittance();
        }

        if(parameters.isGenerateBitmapFile()){
            writeBitmaps();
        }
    }
    
    private void init(TransmittanceParameters parameters) throws IOException{
        
        this.parameters = parameters;
        
        turtle = new Turtle(parameters.getDirectionsNumber());
        
        logger.info("Turtle built with " + turtle.getNbDirections() + " directions");
        
        outputBitmapFiles = new ArrayList<>();
    }
    
    public void process() throws IOException, Exception{
            
        logger.info("===== " + parameters.getInputFile().getAbsolutePath() + " =====");

        direcTransmittance = new DirectionalTransmittance(parameters.getInputFile());
        voxSpace = direcTransmittance.getVoxSpace();
        mnt = direcTransmittance.getMnt();

        getSensorPositions();

        List<IncidentRadiation> solRad = new ArrayList<>();
        
        List<SimulationPeriod> simulationPeriods = parameters.getSimulationPeriods();
        
        for(SimulationPeriod period : simulationPeriods){
            
            Calendar c1 = period.getPeriod().startDate;
            Calendar c2 = period.getPeriod().endDate;
            
            Time time1 = new Time(c1.get(Calendar.YEAR), c1.get(Calendar.DAY_OF_YEAR), c1.get(Calendar.HOUR_OF_DAY), c1.get(Calendar.MINUTE));
            Time time2 = new Time(c2.get(Calendar.YEAR), c2.get(Calendar.DAY_OF_YEAR), c2.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.MINUTE));
            
            solRad.add(SolarRadiation.globalTurtleIntegrate(turtle, (float) Math.toRadians(parameters.getLatitudeInDegrees()), period.getClearnessCoefficient(), time1, time2));
        }        
        
        transmissionPeriod = new double[positions.size()][solRad.size()];
        for (int i = 0; i < positions.size(); i++) {
            for (int m = 0; m < solRad.size(); m++) {
                transmissionPeriod[i][m] = 0;
            }
        }
        
        nbPeriods = solRad.size();

        // TRANSMITTANCE
        logger.info("Computation of transmittance");
        
        int positionID = 0;
        double transmitted;

        IncidentRadiation ir = solRad.get(0);
        
        System.out.println("\n\n\n");
        
        
        
        for (Point3d position : positions) {
            
            for (int t = 0; t < turtle.getNbDirections(); t++) {
                
                if(cancelled){
                    return;
                }
                
                Vector3d dir = new Vector3d(ir.directions[t]);
                dir.normalize();

                transmitted = direcTransmittance.directionalTransmittance(position, dir);
                
                if(!Double.isNaN(transmitted)){
                    
                    for(int m=0 ; m < solRad.size();m++){
                        ir = solRad.get(m);

                        //transmittance for the direction
                        double transmittance = transmitted * ir.directionalGlobals[t];

                        transmissionPeriod[positionID][m] += transmittance;
                    }
                }                
            }
            
            for(int m=0 ; m < solRad.size();m++){
                ir = solRad.get(m);
                transmissionPeriod[positionID][m] /= ir.global;
            }

            positionID++;

            if (positionID % 1000 == 0) {
                logger.info(positionID + "/" + positions.size());
            }
        }
        
    }
    
    public void writeBitmaps() throws IOException{
        
        float zoom = (1*direcTransmittance.getInfos().getResolution());
                
        parameters.getBitmapFile().mkdirs();
        
        for(int k=0;k<nbPeriods;k++){
            
            SimulationPeriod period = parameters.getSimulationPeriods().get(k);
            
            String periodString = period.getPeriod().toString().replaceAll(" ", "_");
            periodString = periodString.replaceAll("/", "-");
            periodString = periodString.replaceAll(":", "");
            
            File outputFile = new File(parameters.getBitmapFile()+File.separator+"period_"+periodString+".bmp");
            outputBitmapFiles.add(outputFile);
            
            logger.info("Writing file "+outputFile);
            
            BufferedImage bimg = new BufferedImage((int)(voxSpace.getSplitting().x * zoom), (int)(voxSpace.getSplitting().y * zoom), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bimg.createGraphics();

            // background
            g.setColor(new Color(80, 30, 0));
            g.fillRect(0, 0, (int)(voxSpace.getSplitting().x * zoom), (int)(voxSpace.getSplitting().y * zoom));
            
            for(int p = 0;p<positions.size();p++){
                
                int i = (int) ((positions.get(p).x - voxSpace.getBoundingBox().min.x) / voxSpace.getVoxelSize().x);
                int j = (int) ((positions.get(p).y - voxSpace.getBoundingBox().min.y) / voxSpace.getVoxelSize().y);
            
                float col = (float) (transmissionPeriod[p][k]/* / 0.1*/);
                col = Math.min(col, 1);
                Color c = Colouring.rainbow(col);
                g.setColor(c);
                int jj = voxSpace.getSplitting().y - j - 1;
                g.fillRect((int)(i * zoom), (int)(jj * zoom), 1, 1);
            }
            /*
            for(Point3d position : positions){
                                
                int i = (int) ((position.x - vsMin.x) / voxSpace.getVoxelSize().x);
                int j = (int) ((position.y - vsMin.y) / voxSpace.getVoxelSize().y);
                
                float col = (float) (transmissionPeriod[i][j][k] / 0.1);
                col = Math.min(col, 1);
                Color c = Colouring.rainbow(col);
                g.setColor(c);
                int jj = splitting.y - j - 1;
                g.fillRect(i * zoom, jj * zoom, zoom, zoom);
            }*/
/*
            for (int i = 0; i < splitting.x; i++) {
                for (int j = 0; j < splitting.y; j++) {

                    float col = (float) (transmissionPeriod[i][j][k] / 0.1);
                    col = Math.min(col, 1);
                    g.setColor(Colouring.rainbow(col));
                    int jj = splitting.y - j - 1;
                    g.fillRect(i * zoom, jj * zoom, zoom, zoom);
                }
            }
*/
            try {
                ImageIO.write(bimg, "bmp", outputFile);
                logger.info("File "+outputFile+" written");

            } catch (IOException ex) {
                throw ex;
            }
        }
        
    }

    private void getSensorPositions() {
        
        if(parameters.getPositions() != null){
            positions = parameters.getPositions();
        }else{
            
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
                        pos.add(new Point3d(tx, ty, mnt[i][j] + parameters.getCenterPoint().z));
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

        parameters.getTextFile().getParentFile().mkdirs();
        
        logger.info("Writing file "+parameters.getTextFile().getAbsolutePath());
            
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(parameters.getTextFile()))) {

            String metadata = "Voxel space\n"+
                              "  min corner:\t" + voxSpace.getBoundingBox().min + "\n"+
                              "  max corner:\t" + voxSpace.getBoundingBox().max + "\n"+
                              "  splitting:\t" + voxSpace.getSplitting() + "\n\n"+
                              "latitude (degrees)\t"+parameters.getLatitudeInDegrees()+"\n\n";

            bw.write(metadata);

            String header = "position X\tposition Y\tposition Z\t";
            String periodsInfos = "";
            String periodsInfosHeader = "period ID\tstart\tend\tclearness";

            int count = 1;
            for(SimulationPeriod period : parameters.getSimulationPeriods()){

                String periodName = "Period "+count;
                header += periodName+"\t";
                periodsInfos += periodName+"\t"+Period.getDate(period.getPeriod().startDate)+"\t"+Period.getDate(period.getPeriod().endDate)+"\t"+period.getClearnessCoefficient()+"\n";

                count++;
            }

            bw.write(periodsInfosHeader+"\n"+
                     periodsInfos+"\n");

            bw.write(header+"\n");

            float mean[] = new float[transmissionPeriod[0].length];

            int i =0;
            for(Point3d position : positions){

                bw.write(position.x + "\t" + position.y + "\t" + position.z);

                for (int m = 0; m < transmissionPeriod[i].length; m++) {
                    bw.write("\t" + transmissionPeriod[i][m]);
                    mean[m] += transmissionPeriod[i][m];
                }

                bw.write("\n");
                i++;
            }

            float yearlyMean = 0;
            bw.write("\nPERIOD\tMEAN");
            for (int m = 0; m < transmissionPeriod[0].length; m++) {
                mean[m] /= (float) positions.size();
                bw.write("\t" + mean[m]);
                yearlyMean += mean[m];
            }
            yearlyMean /= transmissionPeriod[0].length;
            bw.write("\nTOTAL\tMEAN\t" + yearlyMean);
            bw.write("\n");

        }catch(IOException ex){
            throw ex;
        }
    
        logger.info("File "+parameters.getTextFile().getAbsolutePath()+" written");
    }

    public void imageMNT() {

        int zoom = (int) (1*direcTransmittance.getInfos().getResolution());

        BufferedImage bimg = new BufferedImage(voxSpace.getSplitting().x * zoom, voxSpace.getSplitting().y * zoom, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bimg.createGraphics();

        mntZmin = mnt[0][0];
        mntZmax = mnt[0][0];
        for (int i = 0; i < voxSpace.getSplitting().x; i++) {
            for (int j = 0; j < voxSpace.getSplitting().y; j++) {
                mntZmin = Math.min(mntZmin, mnt[i][j]);
                mntZmax = Math.max(mntZmax, mnt[i][j]);
            }
        }

        logger.info("MNTmin:\t" + mntZmin + "\t(VSmin= " + voxSpace.getBoundingBox().min.z + ")");
        logger.info("MNTmax:\t" + mntZmax + "\t(VSmax= " + voxSpace.getBoundingBox().max.z + ")");

        // background
        g.setColor(new Color(80, 30, 0));
        g.fillRect(0, 0, voxSpace.getSplitting().x * zoom, voxSpace.getSplitting().y * zoom);

        for (int i = 0; i < voxSpace.getSplitting().x; i++) {
            for (int j = 0; j < voxSpace.getSplitting().y; j++) {
                float col = ((mnt[i][j] - mntZmin) / (mntZmax - mntZmin));
                col = Math.min(col, 1);
                g.setColor(Colouring.rainbow(col));
                int jj = voxSpace.getSplitting().y - j - 1;
                g.fillRect(i * zoom, jj * zoom, zoom, zoom);
            }
        }
    }

    public List<File> getOutputBitmapFiles() {
        return outputBitmapFiles;
    }

}
