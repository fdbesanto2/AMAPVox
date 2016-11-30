/**
 *
 */
package fr.amap.lidar.amapvox.simulation.transmittance;

import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.DirectionalTransmittance;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.VoxelSpace;
import fr.amap.lidar.amapvox.jeeb.archimed.mmr.IncidentRadiation;
import fr.amap.lidar.amapvox.jeeb.archimed.mmr.SolarRadiation;
import fr.amap.lidar.amapvox.jeeb.archimed.mmr.Turtle;
import fr.amap.lidar.amapvox.jeeb.util.Colouring;
import fr.amap.lidar.amapvox.jeeb.util.Time;
import fr.amap.lidar.amapvox.simulation.transmittance.util.Period;
import fr.amap.commons.util.Process;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.vecmath.Vector3d;

import java.util.Calendar;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;
import fr.amap.commons.util.Cancellable;

/**
 * @author dauzat
 *
 */
public class TransmittanceSim extends Process implements Cancellable{

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

        if(cancelled){
            return;
        }
        
        if(parameters.isGenerateTextFile()){
            writeTransmittance();
        }

        if(parameters.isGenerateBitmapFile()){
            writeBitmaps();
        }
    }
    
    private void init(TransmittanceParameters parameters) throws IOException{
        
        this.parameters = parameters;
        
        turtle = new Turtle(parameters.getDirectionsNumber(), parameters.getDirectionsRotation());        
        
        logger.info("Turtle built with " + turtle.getNbDirections() + " directions");
        
        outputBitmapFiles = new ArrayList<>();
    }
    
    public void process() throws IOException, Exception{
            
        logger.info("===== " + parameters.getInputFile().getAbsolutePath() + " =====");

        direcTransmittance = new DirectionalTransmittance(parameters.getInputFile());
        direcTransmittance.setToricity(parameters.isToricity());
        
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
        
        int count = 0;
        
        for (Point3d position : positions) {
            
            fireProgress("Compute transmittance", positionID, positions.size());
            
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
            count++;

            if (count == 1000) {
                logger.info(positionID + "/" + positions.size());
                count = 0;
            }
        }
        
    }
    
    public void writeBitmaps() throws IOException{
        
        int zoom = 1;
        float resolution = direcTransmittance.getInfos().getResolution();
        //float zoom = (1*direcTransmittance.getInfos().getResolution());
        
        int nbXPixels = (int) (voxSpace.getSplitting().x * resolution);
        int nbYPixels = (int) (voxSpace.getSplitting().y * resolution);
        
        parameters.getBitmapFile().mkdirs();
        
        for(int k=0;k<nbPeriods;k++){
            
            SimulationPeriod period = parameters.getSimulationPeriods().get(k);
            
            String periodString = period.getPeriod().toString().replaceAll(" ", "_");
            periodString = periodString.replaceAll("/", "-");
            periodString = periodString.replaceAll(":", "");
            
            File outputFile = new File(parameters.getBitmapFile()+File.separator+periodString+"_"+period.getClearnessCoefficient()+".bmp");
            outputBitmapFiles.add(outputFile);
            
            logger.info("Writing file "+outputFile);
            
            BufferedImage bimg = new BufferedImage(nbXPixels, nbYPixels, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bimg.createGraphics();

            // background
            g.setColor(new Color(80, 30, 0));
            g.fillRect(0, 0, nbXPixels, nbYPixels);
            
            for(int p = 0;p<positions.size();p++){
                
                int i = (int) ((positions.get(p).x - voxSpace.getBoundingBox().min.x) /*/ voxSpace.getVoxelSize().x*/);
                int j = (int) ((positions.get(p).y - voxSpace.getBoundingBox().min.y)/* / voxSpace.getVoxelSize().y*/);
            
                float col = (float) (transmissionPeriod[p][k]/* / 0.1*/);
                col = Math.min(col, 1);
                Color c = Colouring.rainbow(col);
                g.setColor(c);
                int jj = nbYPixels - j - 1;
                g.fillRect((int)(i * zoom), (int)(jj * zoom), zoom, zoom);
            }
            
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
            logger.info("nb positions= " + positions.size());
        }
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

    public List<File> getOutputBitmapFiles() {
        return outputBitmapFiles;
    }

}
