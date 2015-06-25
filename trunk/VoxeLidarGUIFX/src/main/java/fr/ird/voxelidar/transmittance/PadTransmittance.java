/**
 *
 */
package fr.ird.voxelidar.transmittance;

import fr.ird.voxelidar.util.Period;
import fr.ird.voxelidar.voxelisation.raytracing.util.BoundingBox3d;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;

import fr.ird.voxelidar.voxelisation.raytracing.voxel.VoxelSpace;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.Scene;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Calendar;
import java.util.logging.Level;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;
/**
 * @author dauzat
 *
 */
public class PadTransmittance {

    private final static Logger logger = Logger.getLogger(PadTransmittance.class);
    
    private VoxelSpace voxSpace;
    private TLSVoxel voxels[][][];
    private final Point3d vsMin;
    private final Point3d vsMax;
    private final Point3i splitting;
    private double[][][] transmissionPeriod;
    private float mnt[][];
    private float mntZmax;
    private float mntZmin;
    private final Turtle turtle;

    private ArrayList<Point3d> positions;
    
    private final Parameters parameters;

    private class TLSVoxel {

        float padBV;
    }
    
    public PadTransmittance(Parameters parameters){
        
        this.parameters = parameters;
        
        vsMin = new Point3d();
        vsMax = new Point3d();
        splitting = new Point3i();
        
        int nbDirections = parameters.getDirectionsNumber();
        
        turtle = new Turtle(nbDirections);
        logger.info("Turtle built with " + turtle.getNbDirections() + " sectors");
    }
    
    public void process(){
            
        logger.info("===== " + parameters.getInputFile().getAbsolutePath() + " =====");

        // read data
        readData(parameters.getInputFile());

        getSensorPositions();

        ArrayList<IncidentRadiation> solRad = new ArrayList<>();
        
        List<SimulationPeriod> simulationPeriods = parameters.getSimulationPeriods();
        
        for(SimulationPeriod period : simulationPeriods){
            
            Calendar c1 = period.getPeriod().startDate;
            Calendar c2 = period.getPeriod().endDate;
            
            Time time1 = new Time(c1.get(Calendar.YEAR), c1.get(Calendar.DAY_OF_YEAR), c1.get(Calendar.HOUR_OF_DAY), c1.get(Calendar.MINUTE));
            Time time2 = new Time(c2.get(Calendar.YEAR), c2.get(Calendar.DAY_OF_YEAR), c2.get(Calendar.HOUR_OF_DAY), c2.get(Calendar.MINUTE));
            
            solRad.add(SolarRadiation.globalTurtleIntegrate(turtle, (float) Math.toRadians(parameters.getLatitudeRadians()), period.getClearnessCoefficient(), time1, time2));
        }

        int n = 0;
        double transmitted;

        transmissionPeriod = new double[splitting.x][][];
        for (int x = 0; x < splitting.x; x++) {
            transmissionPeriod[x] = new double[splitting.y][];
            for (int y = 0; y < splitting.y; y++) {
                transmissionPeriod[x][y] = new double[solRad.size()];
                for (int m = 0; m < solRad.size(); m++) {
                    transmissionPeriod[x][y][m] = 0;
                }
            }
        }

        // TRANSMITTANCE
        logger.info("Computation of transmittance");
        int month = 0;

        IncidentRadiation ir = solRad.get(month);
        
        for (Point3d pos : positions) {
            
            int i = (int) ((pos.x - vsMin.x) / voxSpace.getVoxelSize().x);
            int j = (int) ((pos.y - vsMin.y) / voxSpace.getVoxelSize().y);
            
            for (int t = 0; t < turtle.getNbDirections(); t++) {
                
                Vector3d dir = new Vector3d(ir.directions[t]);
                dir.normalize();

                List<Double> distances = distToVoxelWalls(new Point3d(pos), dir);
                transmitted = directionalTransmittance(new Point3d(pos), distances, dir);
                
                int m = 0;
                for(IncidentRadiation incidentRadiation : solRad){
                    transmissionPeriod[i][j][m] += transmitted * incidentRadiation.directionalGlobals[t];
                    m++;
                }
            }
            
            
            for(int m = 0 ; m<solRad.size() ; m++){
                transmissionPeriod[i][j][m] /= ir.global;
                
            }

            n++;

            if (n % 1000 == 0) {
                logger.info(n + "/" + positions.size());
            }
        }
        
    }
    
    
    
    public void writeBitmaps(){
        
        int zoom = 2;
        
        for(int k=0;k<transmissionPeriod[0][0].length;k++){
            
            File outputFile = new File(parameters.getBitmapFile()+File.separator+"period_"+(k+1)+".bmp");
            logger.info("Writing file "+outputFile);
            
            BufferedImage bimg = new BufferedImage(splitting.x * zoom, splitting.y * zoom, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = bimg.createGraphics();

            // background
            g.setColor(new Color(80, 30, 0));
            g.fillRect(0, 0, splitting.x * zoom, splitting.y * zoom);

            for (int i = 0; i < splitting.x; i++) {
                for (int j = 0; j < splitting.y; j++) {

                    float col = (float) (transmissionPeriod[i][j][k] / 0.1);
                    col = Math.min(col, 1);
                    g.setColor(Colouring.rainbow(col));
                    int jj = splitting.y - j - 1;
                    g.fillRect(i * zoom, jj * zoom, zoom, zoom);
                }
            }

            try {
                ImageIO.write(bimg, "bmp", outputFile);
                logger.info("File "+outputFile+" written");

            } catch (IOException ex) {
                logger.error("Cannot write transmittance bitmap image", ex);
            }
        }
        
    }
    
    /*
    public void processFileList(List<File> files){
        
        for (File file : files) {
            process(file);
        }
    }*/

    private void readData(File inputFile) {


        try(BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            
            logger.info("header: " + reader.readLine());
            voxSpace = parseHeader(reader);
            
            String line;
            int nbScans = 0;
            logger.info("Data parsing");
            
            //columns names
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {

                parseLineData(line);
                nbScans++;
                if (nbScans % 100000 == 0) {
                    logger.info(" " + nbScans + " shots");
                }
            }
            logger.info(nbScans + "scans\n");
            
        }catch (IOException e) {
            logger.error("Can't open file: " + inputFile.getAbsolutePath(), e);
        }catch (Exception e) {
            logger.error("Error happened when reading file : "+inputFile.getAbsolutePath(), e);
        }
    }

    private void parseLineData(String line) throws IOException {

        String[] temps = line.split(" ");

        int i = Integer.valueOf(temps[0]);
        int j = Integer.valueOf(temps[1]);
        int k = Integer.valueOf(temps[2]);
        if (k == 0) {
            mnt[i][j] = mntZmin - Float.valueOf(temps[7]);
        }

        if (temps[3].contains("NaN")) {
            voxels[i][j][k].padBV = 0;
        } else {
            voxels[i][j][k].padBV = Float.valueOf(temps[3]);
        }
    }

    private VoxelSpace parseHeader(BufferedReader reader) throws IOException {

        String line;
        StringTokenizer str;
        String delimiter = " ";

        do {
            line = reader.readLine();
            if (line.startsWith("#min_corner")) {
                str = new StringTokenizer(line, delimiter);
                str.nextToken();
                vsMin.x = Float.parseFloat(str.nextToken());
                vsMin.y = Float.parseFloat(str.nextToken());
                vsMin.z = Float.parseFloat(str.nextToken());
            }
            if (line.startsWith("#max_corner")) {
                str = new StringTokenizer(line, delimiter);
                str.nextToken();
                vsMax.x = Float.parseFloat(str.nextToken());
                vsMax.y = Float.parseFloat(str.nextToken());
                vsMax.z = Float.parseFloat(str.nextToken());
            }
            if (line.startsWith("#split")) {
                str = new StringTokenizer(line, delimiter);
                str.nextToken();
                splitting.x = Integer.parseInt(str.nextToken());
                splitting.y = Integer.parseInt(str.nextToken());
                splitting.z = Integer.parseInt(str.nextToken());
            }
        } while (line.startsWith("#type") != true);
        
        logger.info("VOXEL SPACE");
        logger.info("Min corner: " + vsMin);
        logger.info("Max corner: " + vsMax);
        logger.info("Splitting: " + splitting);

        createVoxelTable();
        allocateMNT();

        Scene scene = new Scene();
        scene.setBoundingBox(new BoundingBox3d(vsMin, vsMax));

        return new VoxelSpace(new BoundingBox3d(vsMin, vsMax), splitting, 0);
    }

    public List<Double> distToVoxelWalls(Point3d origin, Vector3d direction) {

        // coordinates in voxel units
        Point3d min = new Point3d(voxSpace.getBoundingBox().min);
        Point3d max = new Point3d(voxSpace.getBoundingBox().max);
        Point3d voxSize = new Point3d();
        voxSize.x = (max.x - min.x) / (double) voxSpace.getSplitting().x;
        voxSize.y = (max.y - min.y) / (double) voxSpace.getSplitting().y;
        voxSize.z = (max.z - min.z) / (double) voxSpace.getSplitting().z;

        // point where the ray exits form the top of the bounding box
        Point3d exit = new Point3d(direction);
        double dist = (max.z - origin.z) / direction.z;
        exit.scale(dist);
        exit.add(origin);

        Point3i o = new Point3i((int) ((origin.x - min.x) / voxSize.x), (int) ((origin.y - min.y) / voxSize.y), (int) ((origin.z - min.z) / voxSize.z));
        Point3i e = new Point3i((int) ((exit.x - min.x) / voxSize.x), (int) ((exit.y - min.y) / voxSize.y), (int) ((exit.z - min.z) / voxSize.z));

        List<Double> distances = new ArrayList<>();

        Vector3d oe = new Vector3d(exit);
        oe.sub(origin);
        distances.add(0.0);
        distances.add(oe.length());

        // voxel walls in X
        int minX = Math.min(o.x, e.x);
        int maxX = Math.max(o.x, e.x);
        for (int m = minX; m < maxX; m++) {
            double dx = (m + 1) * voxSize.x;
            dx += min.x - origin.x;
            dx /= direction.x;
            distances.add(dx);
        }

        // voxel walls in Y
        int minY = Math.min(o.y, e.y);
        int maxY = Math.max(o.y, e.y);
        for (int m = minY; m < maxY; m++) {
            double dy = (m + 1) * voxSize.y;
            dy += min.y - origin.y;
            dy /= direction.y;
            distances.add(dy);
        }

        // voxel walls in Z
        int minZ = Math.min(o.z, e.z);
        int maxZ = Math.max(o.z, e.z);
        for (int m = minZ; m < maxZ; m++) {
            double dz = (m + 1) * voxSize.z;
            dz += min.z - origin.z;
            dz /= direction.z;
            distances.add(dz);
        }

        Collections.sort(distances);

        return distances;
    }

    public double directionalTransmittance(Point3d origin, List<Double> distances, Vector3d direction) {

        Point3d min = new Point3d(voxSpace.getBoundingBox().min);
        Point3d max = new Point3d(voxSpace.getBoundingBox().max);
        Point3d voxSize = new Point3d();
        voxSize.x = (max.x - min.x) / (double) voxSpace.getSplitting().x;
        voxSize.y = (max.y - min.y) / (double) voxSpace.getSplitting().y;
        voxSize.z = (max.z - min.z) / (double) voxSpace.getSplitting().z;
        double dMoy;
        Point3d pMoy;

        double d1 = 0;
        double transmitted = 1;
        for (Double d2 : distances) {
            double pathLength = d2 - d1;
            dMoy = (d1 + d2) / 2.0;
            pMoy = new Point3d(direction);
            pMoy.scale(dMoy);
            pMoy.add(origin);
            pMoy.sub(min);
            int i = (int) (pMoy.x / voxSize.x);
            int j = (int) (pMoy.y / voxSize.y);
            int k = (int) (pMoy.z / voxSize.z);

            // no toricity option (rajouter des modulo pour g\E9rer l'option "torique"
            if (i < 0 || j < 0 || k < 0 || i >= splitting.x || j >= splitting.y || k >= splitting.z) {
                break;
            }

            // Test if current voxel is below the ground level
            if (pMoy.z < mnt[i][j]) {
                transmitted = 0;
            } else {
                transmitted *= Math.exp(-0.5 * voxels[i][j][k].padBV * pathLength);
            }
            d1 = d2;
        }

        return transmitted;
    }

    private void getSensorPositions() {
        
        positions = new ArrayList<>();
        
        if(parameters.isUseScanPositionsFile()){
            
            File pointPositionsFile = parameters.getPointsPositionsFile();
            
            try {
                BufferedReader reader = new BufferedReader(new FileReader(pointPositionsFile));
                
                String line;
                
                while((line = reader.readLine()) != null){
                    
                    line = line.replaceAll(" ", ",");
                    line = line.replaceAll("\t", ",");
                    
                    String[] split = line.split(",");
                    
                    positions.add(new Point3d(Double.valueOf(split[0]), Double.valueOf(split[1]), Double.valueOf(split[2])));
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
            //int middleX = (splitting.x / 2);
            //int middleY = (splitting.y / 2);

            int xMin = middleX - size;
            int yMin = middleY - size;

            int xMax = middleX + size;
            int yMax = middleY + size;
            
            if(xMin < 0){
                xMin = 0;
            }
            
            if(yMin < 0){
                yMin = 0;
            }
            
            if(xMax >= voxSpace.getSplitting().x){
                xMax = voxSpace.getSplitting().x -1;
            }
            
            if(yMax >= voxSpace.getSplitting().y){
                yMax = voxSpace.getSplitting().y -1;
            }

            for (int i = xMin; i < xMax; i++) {

                double tx = (0.5f + (double) i) * voxSpace.getVoxelSize().x;

                for (int j = yMin; j < yMax; j++) {

                    double ty = (0.5f + (double) j) * voxSpace.getVoxelSize().y;
                    Point3d pos = new Point3d(vsMin);
                    pos.add(new Point3d(tx, ty, mnt[i][j] + 0.1f));
                    positions.add(pos);
                }
            }
        }
        
        
        
        
        logger.info("nb positions= " + positions.size());
    }

    public void createVoxelTable() {

        voxSpace = new VoxelSpace(new BoundingBox3d(vsMin, vsMax), splitting, 0);

        // allocate voxels
        logger.info("allocate Voxels");
        voxels = new TLSVoxel[splitting.x][][];
        for (int x = 0; x < splitting.x; x++) {
            voxels[x] = new TLSVoxel[splitting.y][];
            for (int y = 0; y < splitting.y; y++) {
                voxels[x][y] = new TLSVoxel[splitting.z];
                for (int z = 0; z < splitting.z; z++) {
                    voxels[x][y][z] = new TLSVoxel();
                }
            }
        }
    }

    public void allocateMNT() {

        // allocate MNT
        logger.info("allocate MNT");
        mnt = new float[splitting.x][];
        for (int x = 0; x < splitting.x; x++) {
            mnt[x] = new float[splitting.y];
            for (int y = 0; y < splitting.y; y++) {
                mnt[x][y] = (float) vsMin.z;
            }
        }
    }

    public void writeTransmittance(){

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(parameters.getTextFile()))) {
            
            logger.info("Writing file "+parameters.getTextFile().getAbsolutePath());
            
            bw.write("Voxel space\n");
            bw.write("  min corner:\t" + vsMin + "\n");
            bw.write("  max corner:\t" + vsMax + "\n");
            bw.write("  splitting:\t" + splitting + "\n\n");
            
            bw.write("latitude (degrees)\t"+parameters.getLatitudeRadians()+"\n\n");
            
            String header = "index X\tindex Y\t";
            String periodsInfos = "";
            String periodsInfosHeader = "period\tclearness coefficient";
            
            int count = 1;
            for(SimulationPeriod period : parameters.getSimulationPeriods()){
                
                String periodTimeRange = Period.getDate(period.getPeriod().startDate)+"->"+Period.getDate(period.getPeriod().endDate)+"\t";
                header += periodTimeRange;
                periodsInfos += "Period "+count+" : "+periodTimeRange+period.getClearnessCoefficient()+"\n";
                        
                count++;
            }
            
            bw.write(periodsInfosHeader+"\n");
            bw.write(periodsInfos+"\n");
            bw.write(header+"\n");
            //bw.write("index X\tindex Y\tJanuary\tFebruary\tMarch\tApril\tMay\tJune\tJuly\tAugust\tSeptember\tOctober\tNovember\tDecember\n");
            
            float mean[] = new float[transmissionPeriod[0][0].length];
            int size = (int)parameters.getWidth();
            int n = 0;
            /*
            for(int i = 0;i<positions.size();i++){
                
                int idI = 
                bw.write("\t" + transmissionPeriod[i][j][m]);
                (positions.get(i).x)/splitting.x;
            }*/
            
            for (int i = (splitting.x / 2) - size; i < (splitting.x / 2) + size; i++) {
                for (int j = (splitting.y / 2) - size; j < (splitting.y / 2) + size; j++) {
                    bw.write(i + "\t" + j);
                    n++;
                    
                    for (int m = 0; m < transmissionPeriod[i][j].length; m++) {
                        bw.write("\t" + transmissionPeriod[i][j][m]);
                        mean[m] += transmissionPeriod[i][j][m];
                    }
                    bw.write("\n");
                    
                }
            }
            
            float yearlyMean = 0;
            bw.write("\nPERIOD\tMEAN");
            for (int m = 0; m < transmissionPeriod[0][0].length; m++) {
                mean[m] /= (float) n;
                bw.write("\t" + mean[m]);
                yearlyMean += mean[m];
            }
            yearlyMean /= transmissionPeriod[0][0].length;
            bw.write("\nTOTAL\tMEAN\t" + yearlyMean);
            bw.write("\n");
            
            logger.info("File "+parameters.getTextFile().getAbsolutePath()+" written");
            
        }catch(IOException ex){
            logger.error("Cannot write text file "+parameters.getTextFile().getAbsolutePath(), ex);
        }
    }

    public void imageMNT() {

        int zoom = 2;

        BufferedImage bimg = new BufferedImage(splitting.x * zoom, splitting.y * zoom, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bimg.createGraphics();

        mntZmin = mnt[0][0];
        mntZmax = mnt[0][0];
        for (int i = 0; i < splitting.x; i++) {
            for (int j = 0; j < splitting.y; j++) {
                mntZmin = Math.min(mntZmin, mnt[i][j]);
                mntZmax = Math.max(mntZmax, mnt[i][j]);
            }
        }

        logger.info("MNTmin:\t" + mntZmin + "\t(VSmin= " + vsMin.z + ")");
        logger.info("MNTmax:\t" + mntZmax + "\t(VSmax= " + vsMax.z + ")");

        // background
        g.setColor(new Color(80, 30, 0));
        g.fillRect(0, 0, splitting.x * zoom, splitting.y * zoom);

        for (int i = 0; i < splitting.x; i++) {
            for (int j = 0; j < splitting.y; j++) {
                float col = ((mnt[i][j] - mntZmin) / (mntZmax - mntZmin));
                col = Math.min(col, 1);
                g.setColor(Colouring.rainbow(col));
                int jj = splitting.y - j - 1;
                g.fillRect(i * zoom, jj * zoom, zoom, zoom);
            }
        }
    }

}
