/**
 *
 */
package fr.amap.amapvox.simulation.hemi;

import fr.amap.amapvox.commons.util.Filter;
import fr.amap.amapvox.commons.util.LidarScan;
import fr.amap.amapvox.commons.util.MatrixUtility;
import fr.amap.amapvox.commons.util.SphericalCoordinates;
import fr.amap.amapvox.io.tls.rsp.Rsp;
import fr.amap.amapvox.io.tls.rsp.Scans;
import fr.amap.amapvox.io.tls.rxp.RxpExtraction;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.amapvox.jeeb.raytracing.voxel.DirectionalTransmittance;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

import fr.amap.amapvox.jeeb.workspace.sunrapp.geometry.Transformations;
import fr.amap.commons.math.matrix.Mat3D;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec3D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.amapvox.simulation.transmittance.TransmittanceParameters;
import fr.amap.amapvox.simulation.transmittance.TransmittanceSim;
import fr.amap.amapvox.voxelisation.EchoFilter;
import fr.amap.amapvox.voxelisation.tls.RxpEchoFilter;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;
import org.jdom2.JDOMException;

/**
 * @author dauzat
 *
 */
public class HemiScanView {

    private final static Logger logger = Logger.getLogger(HemiScanView.class);
    
    private float skyLuminance = 1f;
    private float canopyLuminance = 0.1f;
    private Point3f rgbSky;
    private Point3f rgbCan;

    private int nbPixels;
    private int decimation; // the proportion of shots used for computation is 1/decimation

    private int minSampling;	// minimum number of shots sampling a sector for calculating its gap fraction

    private int nbAzimuts;
    private int nbZeniths;
    private Pixel[][] pixTab;
    private Sector[][] sectorTable;
    private boolean random;	// the color of pixels is drawn randomly depending on the gap fraction
    private Mat4D transformation;
    private Mat3D rotationFromTransf;
    
    private HemiParameters parameters;

    public class Pixel {

        int nbShots;
        float brightness;

        public Pixel() {
            
            this.nbShots = 0;
            this.brightness = 0;
        }

        protected void updatePixel(float luminance) {
            if (nbShots == 0) {
                brightness = luminance;
                nbShots++;
            } else {
                float newBrightness = (brightness * nbShots) + (luminance);
                nbShots++;
                brightness = newBrightness / (float) nbShots;
            }
        }

        public int getNbShots() {
            return nbShots;
        }

        public float getBrightness() {
            return brightness;
        }
        
    }

    private class Sector {

        int nbShots;
        float brightness;

        public Sector() {
            super();
            this.nbShots = 0;
            this.brightness = 0;
        }

        protected void updateSector(float luminance) {
            if (nbShots == 0) {
                brightness = luminance;
                nbShots++;
            } else {
                float newBrightness = (brightness * nbShots) + (luminance);
                nbShots++;
                brightness = newBrightness / (float) nbShots;
            }
        }
    }
    
    public HemiScanView(){
        
        decimation = 100;
        nbPixels = 800;
        nbZeniths = 9; //6;
        nbAzimuts = 36; //24;
        random = true;
        minSampling = 50;
        rgbSky = new Point3f(0, 0, 255);
        rgbCan = new Point3f(0, 255, 0);
        
        transformation = Mat4D.identity();
        rotationFromTransf = getRotationFromMatrix(transformation);

        initArrays();
    }
    
    public HemiScanView(HemiParameters parameters){
        
        this.parameters = parameters;
        init(parameters);
    }
    
    private void init(HemiParameters parameters){
        
        nbPixels = parameters.getPixelNumber();
        nbZeniths = parameters.getZenithsNumber(); //6;
        nbAzimuts = parameters.getAzimutsNumber(); //24;
        random = true;
        minSampling = 50;
        rgbSky = new Point3f(0, 0, 255);
        rgbCan = new Point3f(0, 255, 0);
        
        initArrays();
    }
    
    private void initArrays(){
        
        pixTab = new Pixel[nbPixels][];
        for (int x = 0; x < nbPixels; x++) {
            pixTab[x] = new Pixel[nbPixels];
            for (int y = 0; y < nbPixels; y++) {
                pixTab[x][y] = new Pixel();
            }
        }
        // table of azimuthAngle/zenith sectors
        sectorTable = new Sector[nbZeniths][];
        for (int z = 0; z < nbZeniths; z++) {
            sectorTable[z] = new Sector[nbAzimuts];
            for (int a = 0; a < nbAzimuts; a++) {
                sectorTable[z][a] = new Sector();
            }
        }
    }
    
    public void launchSimulation() throws Exception{
        
        init(parameters);
        
        //HemiScanView hemiScanView = new HemiScanView(parameters);
        
        switch(parameters.getMode()){
            case ECHOS:
                
                
                
                for(LidarScan scan : parameters.getRxpScansList()){
                
                    setTransformation(MatrixUtility.convertMatrix4dToMat4D(scan.matrix));
                    setScan(scan.file, parameters.getEchoFilter());
                }
                
                break;
                
            case PAD:
                hemiFromPAD(parameters.getVoxelFile(), parameters.getSensorPosition());
                break;
            default:
                return;
        }
        
        if(parameters.isGenerateBitmapFile()){
                
            switch(parameters.getBitmapMode()){
                case PIXEL:
                    writeHemiPhoto(parameters.getOutputBitmapFile());
                    break;
                case COLOR:
                    sectorTable(parameters.getOutputBitmapFile());
                    break;
            }
        }
        
    }
    
    public void setTransformation(Mat4D matrix){
        
        transformation = matrix;
        rotationFromTransf = getRotationFromMatrix(transformation);
    }
    
    public Mat3D getRotationFromMatrix(Mat4D matrix){
        
        Mat3D rotation = new Mat3D();
        
        rotation.mat = new double[]{
            transformation.mat[0],transformation.mat[1],transformation.mat[2],
            transformation.mat[4],transformation.mat[5],transformation.mat[6],
            transformation.mat[8],transformation.mat[9],transformation.mat[10]
        };
        
        return rotation;
    }
    
    public void setScan(File scan){
        setScan(scan, null);
    }
    
    public void setScan(File scan, EchoFilter filter){
        
        int count = 0;
        int totalShots = 0;
            
        RxpExtraction extraction = new RxpExtraction();
        
        try {
            extraction.openRxpFile(scan);
            
            Iterator<Shot> iterator = extraction.iterator();
            
            while(iterator.hasNext()){
                
                Shot shot = iterator.next();
                
                Vec4D locVector = Mat4D.multiply(transformation, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));
                Vec3D uVector = Mat3D.multiply(rotationFromTransf, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));

                shot.setOriginAndDirection(new Point3d(locVector.x, locVector.y, locVector.z), new Vector3d(uVector.x, uVector.y, uVector.z));
                
                boolean keepShot = true;
                
                if(shot.nbEchos > 0 && filter != null){
                    keepShot = filter.doFiltering(shot, shot.nbEchos-1);
                }
                
                if(keepShot){
                    
                    transform(shot);
                
                    count++;
                    totalShots++;
                    
                    if(count == 1000000){
                        logger.info("Shots processed : "+totalShots);
                        count = 0;
                    }
                }
                
            }
            
            logger.info("Total shots processed : "+totalShots);
            
            extraction.close();
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, Exception {

        HemiParameters parameters = new HemiParameters();
        parameters.setZenithsNumber(9);
        parameters.setAzimutsNumber(36);
        parameters.setPixelNumber(800);
        
        HemiScanView hemiScanView = new HemiScanView(parameters);
        hemiScanView.hemiFromPAD(new File("/home/calcul/Documents/Julien/Test_photos_hemispheriques/tmp/tls_paracou_2013_non_pondere.vox"), new Point3d(0, 80, 29));
        hemiScanView.writeHemiPhoto(new File("/home/calcul/Documents/Julien/Test_photos_hemispheriques/tmp/hemiFromPAD.png"));
    }

    private void sectorTable(File outputFile) {

        float radius = nbPixels / 2f;
        float zenithWidth = (float) ((Math.PI / 2) / nbZeniths);
        float azimWidth = (float) ((Math.PI * 2) / nbAzimuts);

        for (int z = 0; z < nbZeniths; z++) {
            System.out.println();
            for (int a = 0; a < nbAzimuts; a++) {
                System.out.print("\t" + sectorTable[z][a].brightness);
            }
        }

        for (int x = 0; x < nbPixels; x++) {
            for (int y = 0; y < nbPixels; y++) {
                Vector2f v = new Vector2f((x - radius) / radius, (y - radius) / radius);
                double zen = v.length() * Math.PI / 2;
                if (zen < Math.PI / 2) {
                    
                    double azim = xyAzimuthNW(v.x, v.y);
                    
                    int sx = (int) (zen / zenithWidth);
                    int sy = (int) (azim / azimWidth);

                    if (sectorTable[sx][sy].nbShots > minSampling) {
                        if (random) {
                            float gf = (sectorTable[sx][sy].brightness - canopyLuminance) / (skyLuminance - canopyLuminance);
                            if (Math.random() > gf) {
                                pixTab[x][y].brightness = canopyLuminance;
                            } else {
                                pixTab[x][y].brightness = skyLuminance;
                            }
                        } else {
                            pixTab[x][y].brightness = sectorTable[sx][sy].brightness;
                        }
                    }
                }
            }
        }

        writeHemiPhoto(outputFile);
    }
    
    private void hemiFromPAD(File inputFile, Point3d position) throws Exception{
        
        DirectionalTransmittance dt = new DirectionalTransmittance(inputFile);
        
        float center = nbPixels / 2;
        
        for (int i = 0; i < nbPixels; i++) {
            for (int j = 0; j < nbPixels; j++) {
                
                float deltaX = i + 0.5f - center;
                float deltaY = j + 0.5f - center;
                double distToCenter = Math.sqrt((deltaX * deltaX) + (deltaY * deltaY));
                
                if (distToCenter < center) {
                    
                    double zenithAngle = (distToCenter/center)*Math.PI/2;
                    double azimuthAngle = 0;
                    
                    if (deltaY != 0) {
                        azimuthAngle = Math.atan(deltaX / deltaY);
                        if (deltaY < 0) {
                            azimuthAngle += Math.PI;
                        } else if (deltaX < 0) {
                            azimuthAngle += Math.PI * 2;
                        }
                    } else if (deltaX < 0) {
                        azimuthAngle = Math.PI / 2;
                    }
                    
                    SphericalCoordinates sc = new SphericalCoordinates(azimuthAngle, zenithAngle);
                    Vector3f direction = new Vector3f(sc.toCartesian());
                    
                    /*Vector3f direction = new Vector3f(0,0,1);
                    Transformations transform = new Transformations();
                    transform.setRotationAroundX(zenithAngle);
                    transform.setRotationAroundZ(azimuthAngle);
                    transform.apply(direction);*/
                    
                    /*if(direction.x != rayDirection.x || direction.y != rayDirection.y || direction.z != rayDirection.z){
                        System.out.println("test");
                    }*/
                    double transmittance = dt.directionalTransmittance(position, new Vector3d(direction.x, direction.y, direction.z));
                    
                    pixTab[i][j].updatePixel((float)transmittance);
                }
            }
        }
    }

//    private void transform(String line, Transformations tr) {
//        String[] st = line.split(" ");
//        int echocount = Integer.valueOf(st[1]);
//        Point3d origin = new Point3d(Double.valueOf(st[2]), Double.valueOf(st[3]), Double.valueOf(st[4]));
//        Vector3d direction = new Vector3d(Double.valueOf(st[5]), Double.valueOf(st[6]), Double.valueOf(st[7]));
//        double range = -9999;
//        if (echocount > 0) {
//            range = Float.valueOf(st[8]);
//        }
//        tr.apply(direction);
//        tr.apply(origin);
//        direction.sub(origin);
//        direction.normalize();
//        float zenith = (float) FastMath.acos(direction.z);
//        double azimuth = xyAzimuthNW(direction.x, direction.y);
//        
//        SphericalCoordinates sc = new SphericalCoordinates();
//        sc.toSpherical(direction);
//
//        if (zenith < Math.PI / 2) {
//            updatePixTab(zenith, azimuth, range);
//            updateSectorTab(zenith, azimuth, range);
//        }
//    }

    public void transform(Shot shot) {

        double zenith = FastMath.acos(shot.direction.z);
        double azimut = xyAzimuthNW(shot.direction.x, shot.direction.y);

        double range = -9999;
        if (shot.nbEchos > 0) {
            range = shot.ranges[shot.nbEchos-1];
        }

        if (zenith < Math.PI / 2) {
            updatePixTab(zenith, azimut, range);
            updateSectorTab(zenith, azimut, range);
        }
    }

    public void writeHemiPhoto(File outputFile) {

        int border = 30;
        int nbPixImage = pixTab.length + (2 * border); //= 600;
        float center = nbPixImage / 2;
        float radius = center - border;

        BufferedImage bimg = new BufferedImage(nbPixImage, nbPixImage, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bimg.createGraphics();

        // background
        g.setColor(new Color(80, 30, 0));
        g.fillRect(0, 0, nbPixImage, nbPixImage);

        // black sky vault
        g.setColor(new Color(0, 0, 0));
        g.fillOval((int) (center - radius), (int) (center - radius), (int) (2 * radius), (int) (2 * radius));

        // draw points
        for (int x = 0; x < pixTab.length; x++) {
            for (int y = 0; y < pixTab[x].length; y++) {
                int yn = pixTab.length - 1 - y; // North in Y+
                if (pixTab[x][y].brightness > 0) {
                    float gf = (pixTab[x][y].brightness - canopyLuminance) / (skyLuminance - canopyLuminance);
                    Point3f rgbr = new Point3f(rgbCan);
                    rgbr.scale(1 - gf);
                    Point3f rgbb = new Point3f(rgbSky);
                    rgbb.scale(gf);
                    Point3f rgb = new Point3f(rgbb);
                    rgb.add(rgbr);
                    rgb.x = Math.max(rgb.x, 0);
                    rgb.y = Math.max(rgb.y, 0);
                    rgb.z = Math.max(rgb.z, 0);
                    rgb.x = Math.min(rgb.x, 255);
                    rgb.y = Math.min(rgb.y, 255);
                    rgb.z = Math.min(rgb.z, 255);
                    Point3i c = new Point3i((int) rgb.x, (int) rgb.y, (int) rgb.z);
                    g.setColor(new Color(c.x, c.y, c.z));
                    g.drawRect(x + border, yn + border, 1, 1);
                }
            }
        }

        // parallels
        g.setColor(new Color(220, 240, 255));
        double rad = radius / nbZeniths;
        for (int i = 1; i <= nbZeniths; i++) {
            g.drawOval((int) (center - rad * i), (int) (center - rad * i), (int) (2 * rad * i), (int) (2 * rad * i));
        }
        // meridians
        for (int i = 1; i <= nbAzimuts; i++) {
            double azimuth = i * (Math.PI * 2 / nbAzimuts);
            double x = radius * Math.sin(azimuth);
            double y = radius * Math.cos(azimuth);
            g.drawLine((int) (center - x), (int) (center - y), (int) (center + x), (int) (center + y));
        }

        // cardinal points
        g.drawString("N", (int) center, border / 2);
        g.drawString("S", (int) center, nbPixImage - (border / 2));
        g.drawString("E", nbPixImage - (border / 2), center);
        g.drawString("W", border / 2, center);
        
        try {
            ImageIO.write(bimg, "png", outputFile);
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    /**
     * Get azimuth angle from 2D coordinates
     * @param x 2D coordinates
     * @param y 2D coordinates
     * @return	azimuthAngle	[radian] clockwise from Y axis
     */
    public static double xyAzimuthNW(double x, double y) {

        double azimuth = 0;
        if(y != 0) {
            
            azimuth = FastMath.atan(x/y);
            if(y < 0){
                azimuth += Math.PI;
            }
            else if(x < 0){
                azimuth += Math.PI*2;
            }
        }
        else if (x < 0){
            azimuth = (Math.PI / 2);
        }
        
        return azimuth;
    }

    private void updatePixTab(double zenith, double azimut, double distance) {
        
        double radius = nbPixels / 2f;
        
        //normalization from 0 to radius
        double normalizedRadius = ((zenith / (Math.PI / 2)) * radius);
        
        double x = normalizedRadius * Math.cos(azimut);
        double y = normalizedRadius * Math.sin(azimut);
        
        int indexX = (int) (x + radius);
        int indexY = (int) (y + radius);
        
        indexX = Math.min(indexX, nbPixels - 1);
        indexY = Math.min(indexY, nbPixels - 1);

        if (distance < 0) {
            pixTab[indexX][indexY].updatePixel(skyLuminance);
        } else {
            pixTab[indexX][indexY].updatePixel(canopyLuminance);
        }
    }
    
    public static Point2i getPixelIndicesFromDirection(int nbPixels, Vector3f direction){
        
        double zenith = FastMath.acos(direction.z);        
        double azimut = xyAzimuthNW(direction.x, direction.y);
                
        double radius = nbPixels / 2f;
        
        //normalization from 0 to radius
        double normalizedRadius = ((zenith / (Math.PI / 2)) * radius);
        
        double x = (normalizedRadius * Math.cos(azimut));
        double y = (normalizedRadius * Math.sin(azimut));
        
        int indexX = (int) (x + radius);
        int indexY = (int) (y + radius);
        
        indexX = Math.min(indexX, nbPixels - 1);
        indexY = Math.min(indexY, nbPixels - 1);
        
        return new Point2i(indexX, indexY);
    }
    
    public static Vector3d getDirectionFromPixel(int nbPixels, int i, int j){
        
        double radius = nbPixels/2;
        
        int centerX = nbPixels/2;
        int centerY = centerX;
        
        double x = i - centerX;
        double y = j - centerY;
        
        double normalizedRadius = Math.sqrt((x*x)+(y*y));
        
        
        double azimut = Math.acos(x/normalizedRadius);
        double zenith = (normalizedRadius / radius) * Math.PI / 2.0;
        
        SphericalCoordinates sc = new SphericalCoordinates(azimut, zenith);
        Vector3d direction = new Vector3d(sc.toCartesian());
        return direction;
    }

    private void updateSectorTab(double zenith, double azimuth, double distance) {

        int indexZn = (int) ((zenith * nbZeniths) / (Math.PI / 2));
        int indexAz = (int) ((azimuth * nbAzimuts) / (Math.PI * 2));

        if (distance < 0) {
            sectorTable[indexZn][indexAz].updateSector(skyLuminance);
        } else {
            sectorTable[indexZn][indexAz].updateSector(canopyLuminance);
        }
    }

    public int getNbPixels() {
        return nbPixels;
    }

    public Pixel[][] getPixTab() {
        return pixTab;
    }

    
}
