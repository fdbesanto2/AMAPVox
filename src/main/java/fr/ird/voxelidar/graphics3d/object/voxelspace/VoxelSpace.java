/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.object.voxelspace;

import com.jogamp.common.nio.Buffers;
import fr.ird.voxelidar.graphics2d.image.ScaleGradient;
import fr.ird.voxelidar.graphics3d.mesh.Attribut;
import fr.ird.voxelidar.graphics3d.mesh.Grid;
import fr.ird.voxelidar.graphics3d.mesh.Mesh;
import fr.ird.voxelidar.graphics3d.mesh.MeshFactory;
import fr.ird.voxelidar.graphics3d.object.terrain.Terrain;
import fr.ird.voxelidar.graphics3d.shader.Shader;
import fr.ird.voxelidar.io.file.FileManager;
import fr.ird.voxelidar.math.point.Point2F;
import fr.ird.voxelidar.math.vector.Vec3F;
import fr.ird.voxelidar.util.ColorGradient;
import fr.ird.voxelidar.util.Settings;
import fr.ird.voxelidar.util.StandardDeviation;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import static java.lang.Float.NaN;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.media.opengl.GL3;
import javax.swing.SwingWorker;
import javax.swing.event.EventListenerList;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class VoxelSpace {
    
    final static Logger logger = Logger.getLogger(VoxelSpace.class);
    
    public static final int FLOAT_SIZE = Buffers.SIZEOF_FLOAT;
    public static final int INT_SIZE = Buffers.SIZEOF_INT;
    public static final int SHORT_SIZE = Buffers.SIZEOF_SHORT;
    
    public static final int VOXELSPACE_FORMAT1 = 1;
    public static final int VOXELSPACE_FORMAT2 = 2;
    
    public enum VoxelFormat{
        VOXELSPACE_FORMAT1(1), VOXELSPACE_FORMAT2(2);
        
        private final int format;
        VoxelFormat(int format){
            this.format = format;
        }
    }
    
    private ArrayList<Voxel> voxelList;
    public Mesh cube;
    private float cubeSize;
    private Shader simpleShader;
    private String attributToVisualize;
    public int nX, nY, nZ;
    private float startPointX, startPointY, startPointZ;
    public float widthX, widthY, widthZ;
    private float resolution;
    private boolean fileLoaded;
    public float attributValueMax;
    public float attributValueMin;
    public float min;
    public float max;
    private float instancePositions[];
    private float instanceColors[];
    public boolean arrayLoaded = false;
    private Settings settings;
    private Map<String,Attribut> mapAttributs;
    
    private FloatBuffer instancePositionsBuffer;
    private FloatBuffer instanceColorsBuffer;   
    
    private int vboId, vaoId, iboId;
    private int gridVaoId;
    private int gridVboId;
    private int gridIboId;
    private Grid grid;
    
    public float centerX;
    public float centerY;
    public float centerZ;
    
    private boolean updateValue;
    private Attribut attribut;
    
    private Color[] gradient = ColorGradient.GRADIENT_HEAT;
    private ColorGradient colorGradient;
    
    private final int shaderId;
    private boolean gradientUpdated = false;
    private boolean cubeSizeUpdated;
    
    private int readFileProgress; 
    
    private VoxelSpaceFormat voxelSpaceFormat;
    
    private final EventListenerList listeners;

    public void setReadFileProgress(int progress) {
        this.readFileProgress = progress;
        fireReadFileProgress(progress);
    }
    
    public void fireReadFileProgress(int progress){
        
        for(VoxelSpaceListener listener :listeners.getListeners(VoxelSpaceListener.class)){
            
            listener.voxelSpaceCreationProgress(progress);
        }
    }

    public void setFileLoaded(boolean fileLoaded) {
        this.fileLoaded = fileLoaded;
        
        if(fileLoaded){
            firefileLoaded();
        }
    }

    public boolean isFileLoaded() {
        return fileLoaded;
    }

    public Color[] getGradient() {
        return gradient;
    }
    
    public void firefileLoaded(){
        
        for(VoxelSpaceListener listener :listeners.getListeners(VoxelSpaceListener.class)){
            
            listener.voxelSpaceCreationFinished();
        }
    }
    
    public void addVoxelSpaceListener(VoxelSpaceListener listener){
        listeners.add(VoxelSpaceListener.class, listener);
    }
    
    public int getShaderId() {
        return shaderId;
    }
    
    public File file;

    public float getCenterX() {
        return centerX;
    }

    public float getCenterY() {
        return centerY;
    }

    public float getCenterZ() {
        return centerZ;
    }

    public ArrayList<Voxel> getVoxelList() {
        return voxelList;
    }

    public boolean isGradientUpdated() {
        return gradientUpdated;
    }

    public void setAttributToVisualize(String attributToVisualize) {
        this.attributToVisualize = attributToVisualize;
    }
    
    public VoxelSpace(Settings settings){
        
        voxelList = new ArrayList<>();
        listeners = new EventListenerList();
        fileLoaded = false;
        this.shaderId = 0;
    }
    
    public VoxelSpace(GL3 gl, int shaderId, Settings settings){
        
        voxelList = new ArrayList<>();
        listeners = new EventListenerList();
        fileLoaded = false;
        this.shaderId = shaderId;
        this.settings = settings;
    }
    
    private void setMetadata(String metadataLine){
        
        String[] metadata = metadataLine.split(" ");

        nX = Integer.valueOf(metadata[0]);
        nY = Integer.valueOf(metadata[1]);
        nZ = Integer.valueOf(metadata[2]);
        resolution = Float.valueOf(metadata[3]);

        startPointX = Float.valueOf(metadata[4]);
        startPointY = Float.valueOf(metadata[5]);
        startPointZ = Float.valueOf(metadata[6]);
    }
    
    private void setWidth(){
        
        if(voxelList.size() > 0){
            
            widthX = (voxelList.get(voxelList.size()-1).x) - (voxelList.get(0).x);
            widthY = (voxelList.get(voxelList.size()-1).y) - (voxelList.get(0).y);
            widthZ = (voxelList.get(voxelList.size()-1).z) - (voxelList.get(0).z);
        }
    }
    
    private void setCenter(){
        
        if(voxelList.size() > 0){
            
            centerX = ((voxelList.get(voxelList.size()-1).x) - (voxelList.get(0).x))/2.0f;
            centerY = ((voxelList.get(voxelList.size()-1).y) - (voxelList.get(0).y))/2.0f;
            centerZ = ((voxelList.get(voxelList.size()-1).z) - (voxelList.get(0).z))/2.0f;
        }
    }
    
    
    
    private void readVoxelFormat1(File f){
        
        String header = FileManager.readHeader(f.getAbsolutePath());
        
        if(header.equals("VOXEL SPACE")){
            
            VoxelSpaceFormat1 voxelSpace = new VoxelSpaceFormat1();
            
            int count = FileManager.getLineNumber(file.getAbsolutePath());

            /******read file*****/

            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(file));
                
                //VOXEL SPACE
                reader.readLine();
                
                Map<String, Point2F> minMax = new HashMap<>();
                
                //min corner
                String minCornerLine = reader.readLine();
                String[] minCornerLineSplit = minCornerLine.substring(minCornerLine.indexOf("(")+1, minCornerLine.indexOf(")")).split(",");
                voxelSpace.xMinCorner = Double.valueOf(minCornerLineSplit[0].trim());
                voxelSpace.yMinCorner = Double.valueOf(minCornerLineSplit[1].trim());
                voxelSpace.zMinCorner = Double.valueOf(minCornerLineSplit[2].trim());
                
                //max corner
                String maxCornerLine = reader.readLine();
                String[] maxCornerLineSplit = maxCornerLine.substring(maxCornerLine.indexOf("(")+1, maxCornerLine.indexOf(")")).split(",");
                voxelSpace.xMaxCorner = Double.valueOf(maxCornerLineSplit[0].trim());
                voxelSpace.yMaxCorner = Double.valueOf(maxCornerLineSplit[1].trim());
                voxelSpace.zMaxCorner = Double.valueOf(maxCornerLineSplit[2].trim());
                
                //splitting
                String splittingLine = reader.readLine();
                String[] splittingLineSplit = splittingLine.substring(splittingLine.indexOf("(")+1, splittingLine.indexOf(")")).split(",");
                voxelSpace.xSplit = Integer.valueOf(splittingLineSplit[0].trim());
                voxelSpace.ySplit = Integer.valueOf(splittingLineSplit[1].trim());
                voxelSpace.zSplit = Integer.valueOf(splittingLineSplit[2].trim());
                
                
                String[] columnsNames = reader.readLine().split("\t");
                
                for(int i=0;i<columnsNames.length;i++){
                    columnsNames[i] = columnsNames[i].replaceAll(" ", "");
                    columnsNames[i] = columnsNames[i].replaceAll("#", "");
                }
                
                int lineNumber = 0;
                String line;                
                
                //start reading voxels
                while ((line = reader.readLine())!= null) {

                    String[] voxel = line.split("\t");
                    
                    int indiceX = Integer.valueOf(voxel[0]);
                    int indiceZ = Integer.valueOf(voxel[1]);
                    int indiceY = Integer.valueOf(voxel[2]);

                    Map<String,Float> mapAttributs = new HashMap<>();

                    for (int i=0;i<voxel.length;i++) {
                        
                        float value = Float.valueOf(voxel[i]);
                        
                        mapAttributs.put(columnsNames[i], value);
                        
                        Point2F minMaxPoint;
                        
                        if((minMaxPoint = minMax.get(columnsNames[i]))!=null){
                            
                            float min = minMaxPoint.x;
                            float max = minMaxPoint.y;
                            
                            if(value < min){
                                min = value;
                            }
                            
                            if(value > max){
                                max = value;
                            }
                            
                            minMaxPoint = new Point2F(min, max);
                            minMax.put(columnsNames[i], minMaxPoint);
                            
                        }else{
                            minMax.put(columnsNames[i], new Point2F(value, value));
                        }
                    }

                    float posX = indiceX+startPointX-(resolution/2.0f);
                    float posY = indiceY+startPointY-(resolution/2.0f);
                    float posZ = indiceZ+startPointZ-(resolution/2.0f);

                    voxelList.add(new Voxel(indiceX, indiceY, indiceZ, posX, posY, posZ, mapAttributs, 1.0f));


                    lineNumber++;

                    setReadFileProgress((lineNumber * 100) / count);
                }
                
                voxelSpace.setMinMax(minMax);

                reader.close();

            } catch (FileNotFoundException ex) {
                logger.error(null, ex);
            } catch (IOException ex) {
                logger.error(null, ex);
            }
            
        }
    }
    
    private void readVoxelFormat2(File f){
        
        String header = FileManager.readHeader(f.getAbsolutePath());
        
        if(header.split(" ").length == 10){
            
            VoxelSpaceFormat2 voxelSpace = new VoxelSpaceFormat2();
            
            int count = FileManager.getLineNumber(file.getAbsolutePath());

            /******read file*****/

            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(file));
                
                Map<String, Point2F> minMax = new HashMap<>();
                
                String[] columnsNames = reader.readLine().split(" ");
                
                String[] infos = reader.readLine().split(" ");
                voxelSpace.xNumberVox = Integer.valueOf(infos[0]);
                voxelSpace.yNumberVox = Integer.valueOf(infos[1]);
                voxelSpace.zNumberVox = Integer.valueOf(infos[2]);
                voxelSpace.resolution = Float.valueOf(infos[3]);
                
                int lineNumber = 0;
                String line;                
                
                //start reading voxels
                while ((line = reader.readLine())!= null) {

                    String[] voxel = line.split(" ");
                    
                    int indiceX = Integer.valueOf(voxel[0]);
                    int indiceZ = Integer.valueOf(voxel[1]);
                    int indiceY = Integer.valueOf(voxel[2]);

                    Map<String,Float> mapAttributs = new HashMap<>();

                    for (int i=0;i<voxel.length;i++) {
                        
                        float value = Float.valueOf(voxel[i]);
                        
                        mapAttributs.put(columnsNames[i], value);
                        
                        Point2F minMaxPoint;
                        
                        if((minMaxPoint = minMax.get(columnsNames[i]))!=null){
                            
                            float min = minMaxPoint.x;
                            float max = minMaxPoint.y;
                            
                            if(value < min){
                                min = value;
                            }
                            
                            if(value > max){
                                max = value;
                            }
                            
                            minMaxPoint = new Point2F(min, max);
                            minMax.put(columnsNames[i], minMaxPoint);
                            
                        }else{
                            minMax.put(columnsNames[i], new Point2F(value, value));
                        }
                    }

                    float posX = indiceX+startPointX-(resolution/2.0f);
                    float posY = indiceY+startPointY-(resolution/2.0f);
                    float posZ = indiceZ+startPointZ-(resolution/2.0f);

                    voxelList.add(new Voxel(indiceX, indiceY, indiceZ, posX, posY, posZ, mapAttributs, 1.0f));

                    lineNumber++;

                    setReadFileProgress((lineNumber * 100) / count);
                }
                
                voxelSpace.setMinMax(minMax);
                
                reader.close();

            } catch (FileNotFoundException ex) {
                logger.error(null, ex);
            } catch (IOException ex) {
                logger.error(null, ex);
            }
            
        }
    }
    
    public void loadFromFile(File f, final VoxelFormat format){
        
            
        setFileLoaded(false);

        this.file =f;

        //final JProgressLoadingFile progress = new JProgressLoadingFile(parent);
        //progress.setVisible(true);
        SwingWorker sw = new SwingWorker() {


            @Override
            protected Object doInBackground() {

                switch(format){
                    case VOXELSPACE_FORMAT1:
                        readVoxelFormat1(file);
                        break;
                    case VOXELSPACE_FORMAT2:
                        readVoxelFormat2(file);
                        break;
                }

                setFileLoaded(true);

                setCenter();
                setWidth();

                return null;

            }
        };

        sw.execute();   
    }
    
    
    public void updateValue(Attribut att){
        
        Attribut attribut = mapAttributs.get(attributToVisualize);
        
        this.updateValue= true;
        
        float[] values = new float[voxelList.size()];
        
        int count = 0;
        for(Voxel voxel:voxelList){
                    
            float attributValue;
            
            Map<String, Float> attributs = voxel.getAttributs();
            
            for(Entry entry : attributs.entrySet()){
                
                String name = (String)entry.getKey();
                double value = (Float)entry.getValue();
                attribut.getExpression().setVariable(name, value);
            }

             try{
                attributValue = (float) attribut.getExpression().evaluate();
            }catch(Exception e){
                attributValue = 0;
            }

            voxel.attributValue = attributValue;
            //voxel.color = getColorFromValue(attributValue);

            //initialize minimum and maximum attributs values
            if(voxel ==  voxelList.get(0)){

                attributValueMax = attributValue;
                attributValueMin = attributValue;
            }

            //set maximum attribut value
            if(attributValue>attributValueMax){

                attributValueMax = attributValue;
            }

            //set minimum attribut value
            if(attributValue < attributValueMin){

                attributValueMin = attributValue;
            }

            boolean drawVoxel = !(Float.isNaN(voxel.attributValue) || voxel.attributValue == -1.0f || (!settings.drawNullVoxel && voxel.attributValue == 0));
            
            if(!drawVoxel){
                voxel.alpha = 0;
            }else{
                voxel.alpha = 1;
            }
            
            values[count] = voxel.attributValue;
            count++;
            
            
            
        }
        
        //calculate standard deviation
        StandardDeviation sd = new StandardDeviation();
        float sdValue = sd.getFromFloatArray(values);
        float average = sd.getAverage();
        
        min = average - (2*sdValue);
        max = average + (2*sdValue);
        
        //colorGradient = new ColorGradient(min, max);
        setGradientColor(gradient, min, max);
        
        /*
        colorGradient = new ColorGradient(attributValueMin, attributValueMax);
        setGradientColor(gradient, attributValueMin, attributValueMax);
        */
        gradientUpdated = false;
        
        setFileLoaded(true);
    }
    
    public void updateColorValue(Color[] gradient){
        setGradientColor(gradient, min, max);
    }
    
    
    public void loadFromFile(File f, final VoxelFormat format, Map<String,Attribut> mapAttributs){
        
        setFileLoaded(false);
        
        //attribut is a custom equation defined by user
        this.mapAttributs = mapAttributs;
        
        attribut = mapAttributs.get(attributToVisualize);
        
        this.file =f;
        
        SwingWorker sw = new SwingWorker() {
            
            
            @Override
            protected Object doInBackground() {
                
                switch(format){
                    case VOXELSPACE_FORMAT1:
                        readVoxelFormat1(file);
                        break;
                    case VOXELSPACE_FORMAT2:
                        readVoxelFormat2(file);
                        break;
                }
                
                
                updateValue(settings.attribut);
                
                setCenter();
                setWidth();
                
                return null;

            }
        };

        sw.execute();
    }
    
    /*
    private boolean isVoxelSpaceFile(File f){
        
        String header = FileManager.readHeader(f.getAbsolutePath());
        
        return true;
        //return header.equals("VOXEL SPACE");
    }
    */
    
//    public void loadFromFile(File f) throws Exception{
//        
//        if(isVoxelSpaceFile(f)){
//            
//            setFileLoaded(false);
//        
//            this.file =f;
//
//            //final JProgressLoadingFile progress = new JProgressLoadingFile(parent);
//            //progress.setVisible(true);
//            SwingWorker sw = new SwingWorker() {
//
//
//                @Override
//                protected Object doInBackground() {
//
//                    try {
//
//                        int count = FileManager.getLineNumber(file.getAbsolutePath());
//
//                        /******read file*****/
//
//                        BufferedReader reader = new BufferedReader(new FileReader(file));
//                        String parameters[] = reader.readLine().split(" ");
//
//                        //metadata
//                        setMetadata(reader.readLine());
//
//                        //first values line
//                        String line = reader.readLine();
//
//                        int lineNumber = 0;
//
//                        while (line != null) {
//
//                            String[] attributs = line.split(" ");
//
//                            //create voxel
//                            if(!updateValue){
//
//                                int indiceX = Integer.valueOf(attributs[0]);
//                                int indiceZ = Integer.valueOf(attributs[1]);
//                                int indiceY = Integer.valueOf(attributs[2]);
//
//                                Map<String,Float> mapAttributs = new HashMap<>();
//
//                                for (int i=0;i<attributs.length;i++) {
//                                    mapAttributs.put(parameters[i], Float.valueOf(attributs[i]));
//                                }
//
//                                float posX = indiceX+startPointX-(resolution/2.0f);
//                                float posY = indiceY+startPointY-(resolution/2.0f);
//                                float posZ = indiceZ+startPointZ-(resolution/2.0f);
//
//                                voxelList.add(new Voxel(indiceX, indiceY, indiceZ, posX, posY, posZ, mapAttributs, 1.0f));
//
//                            }
//
//                            line = reader.readLine();
//
//                            lineNumber++;
//
//                            setReadFileProgress((lineNumber * 100) / count);
//                        }
//
//                        reader.close();
//
//                        setFileLoaded(true);
//
//                        setCenter();
//                        setWidth();
//
//                    } catch (FileNotFoundException ex) {
//                        logger.error("cannot load voxel space from file", ex);
//                    } catch (IOException ex) {
//                        logger.error("cannot load voxel space from file", ex);
//                    }
//
//                    return null;
//
//                }
//            };
//
//            sw.execute();
//        }else{
//            throw new Exception("Not a voxel space file (header must be VOXEL");
//        }     
//    }
    

//    public void loadFromFile(File f, Map<String,Attribut> mapAttributs, Terrain ground,boolean update){
//        
//        setFileLoaded(false);
//        
//        this.updateValue= update;
//        
//        attribut = mapAttributs.get(attributToVisualize);
//        final Terrain terrain = ground;
//        
//        this.file =f;
//        
//        SwingWorker sw = new SwingWorker() {
//            
//            
//            @Override
//            protected Object doInBackground() {
//
//                /******count line number*****/
//                int compteur = 0;
//                MultiKeyMap mapTerrainXY = null;
//                
//                if(terrain !=null){
//                    
//                    mapTerrainXY = terrain.getXYStructure();
//                }
//                
//                
//                try {
//                    
//                    /****count line number****/
//                    int count = FileManager.getLineNumber(file.getAbsolutePath());
//
//                    /******read file*****/
//                    BufferedReader reader = new BufferedReader(new FileReader(file));
//                    String parametersLine[] = reader.readLine().split(" ");
//                    
//                    
//                    //metadata
//                    setMetadata(reader.readLine());
//                    
//                    //first values line
//                    String line = reader.readLine();
//                    
//                    int lineNumber = 0;
//                    
//                    boolean valMinMaxInit = false;
//
//                    while (line != null) {
//                        
//                        String[] attributs = line.split(" ");
//                        
//                        for(int i=0;i<parametersLine.length;i++){
//                            attribut.getExpression().setVariable(parametersLine[i], Float.valueOf(attributs[i]));
//                        }
//                        
//                        float attributValue =0;
//                        
//                        try{
//                            attributValue = (float) attribut.getExpression().evaluate();
//                        }catch(Exception e){
//                            attributValue = NaN;
//                        }
//                        
//                        
//                        //initialize minimum and maximum attributs values
//                        if(lineNumber >= 0 && !valMinMaxInit && attributValue != 0){
//
//                            attributValueMax = attributValue;
//                            attributValueMin = attributValue;
//
//                            valMinMaxInit = true;
//                        }
//                        
//                        //edit voxel value
//                        if(updateValue && voxelList.size() > 0){
//                            voxelList.get(compteur).attributValue = attributValue;
//                            
//                        }
//                        
//                        //set maximum attribut value
//                        if(attributValue>attributValueMax){
//
//                            attributValueMax = attributValue;
//                        }
//
//                        //set minimum attribut value
//                        if(attributValue < attributValueMin){
//
//                            attributValueMin = attributValue;
//                        }
//                        
//                        Voxel voxel;
//                                
//                        //create voxel
//                        if(!updateValue){
//                            
//                            int indiceX = Integer.valueOf(attributs[0]);
//                            int indiceZ = Integer.valueOf(attributs[1]);
//                            int indiceY = Integer.valueOf(attributs[2]);
//                            
//                            float posX = indiceX+startPointX-(resolution/2.0f);
//                            float posY = indiceY+startPointZ-(resolution/2.0f);
//                            float posZ = indiceZ+startPointY-(resolution/2.0f);
//                            
//                            voxel = new Voxel(indiceX, indiceY, indiceZ, posX, posY, posZ, attributValue);
//                        }else{
//                            voxel = voxelList.get(compteur);
//                        }
//                        
//                        boolean drawVoxel;
//
//                        drawVoxel = !(Float.isNaN(voxel.attributValue) || voxel.attributValue == -1.0f || (!settings.drawNullVoxel && voxel.attributValue == 0));
//
//                        //if a terrain was loaded
//                        if(mapTerrainXY != null){
//
//                            float hauteurTerrain = 0;
//                            try{
//                                hauteurTerrain = (float) mapTerrainXY.get(voxel.x, voxel.z);
//                            }catch(Exception e){
//                                logger.error(null, e);
//                            }
//
//                            if((voxel.y<hauteurTerrain) && !settings.drawVoxelUnderground ){
//                                drawVoxel = false;
//                            }
//                        }
//
//                        if(drawVoxel){
//                            voxel.alpha = 1.0f;
//                        }else{
//                            voxel.alpha = 0.0f;
//                        }
//                        
//                        if(!updateValue){
//                            voxelList.add(voxel);
//                        }else{
//                            compteur++;
//                        }
//
//                        line = reader.readLine();
//                        
//                        lineNumber++;
//                        
//                        setReadFileProgress((lineNumber * 100) / count);
//                    }
//                    
//                    reader.close();
//                    
//                    setGradientColor(gradient);
//                    
//                    if(updateValue){
//                        
//                        updateInstanceColorBuffer();
//                    }
//                    
//                    setCenter();
//                    setWidth();
//                    
//                    setFileLoaded(true);
//
//                } catch (FileNotFoundException ex) {
//                    logger.error(null, ex);
//                } catch (IOException ex) {
//                    logger.error(null, ex);
//                }
//
//                return null;
//
//            }
//        };
//
//        sw.execute();
//    }
    
    public Vec3F getColorFromValue(float value){
        
        Color c = colorGradient.getColor(value);
        
        return new Vec3F(c.getRed()/255.0f, c.getGreen()/255.0f, c.getBlue()/255.0f);
    }
    /*
    public void setGradientColor(Color[] gradientColor){
        
        this.gradient = gradientColor;
        
        ColorGradient color = new ColorGradient(attributValueMin, attributValueMax);
        color.setGradientColor(gradientColor);

        for (Voxel voxel : voxelList) {
            
            Color colorGenerated = color.getColor(voxel.attributValue);
            if (voxel.alpha == 0) {
                voxel.color = new Vec3F(colorGenerated.getRed()/255.0f, colorGenerated.getGreen()/255.0f, colorGenerated.getBlue()/255.0f);
            } else {
                voxel.color = new Vec3F(colorGenerated.getRed()/255.0f, colorGenerated.getGreen()/255.0f, colorGenerated.getBlue()/255.0f);
            }
        }
        //voxelList = ImageEqualisation.scaleHistogramm(voxelList);
        //voxelList = ImageEqualisation.voxelSpaceEqualisation(voxelList);
        
        
    }
    */
    
    public void setGradientColor(Color[] gradientColor, float valMin, float valMax){
        
        this.gradient = gradientColor;
        
        ColorGradient color = new ColorGradient(valMin, valMax);
        color.setGradientColor(gradientColor);

        for (Voxel voxel : voxelList) {
            
            Color colorGenerated = color.getColor(voxel.attributValue);
            if (voxel.alpha == 0) {
                voxel.color = new Vec3F(colorGenerated.getRed()/255.0f, colorGenerated.getGreen()/255.0f, colorGenerated.getBlue()/255.0f);
            } else {
                voxel.color = new Vec3F(colorGenerated.getRed()/255.0f, colorGenerated.getGreen()/255.0f, colorGenerated.getBlue()/255.0f);
            }
        }
        //voxelList = ImageEqualisation.scaleHistogramm(voxelList);
        //voxelList = ImageEqualisation.voxelSpaceEqualisation(voxelList);
        
        
    }
    
    
    public void updateInstanceColorBuffer(){
        
        gradientUpdated = false;
        
    }
    
    public BufferedImage createScaleImage(int width, int height){
        
        return ScaleGradient.generateScale(gradient, attributValueMin, attributValueMax, width, height);
    }
    
    public void updateCubeSize(GL3 gl, float size){
        
        cubeSize = size;
        cubeSizeUpdated = false;
    }
    
    public void initBuffer(GL3 gl, Shader shader){
        
        cubeSize = 0.5f;
        cube = MeshFactory.createCube(cubeSize);
        
        instancePositions = new float[voxelList.size()*3];
        instanceColors = new float[voxelList.size()*4];

        for (int i=0, j=0, k=0;i<voxelList.size();i++, j+=3 ,k+=4) {

            instancePositions[j] = voxelList.get(i).x;
            instancePositions[j+1] = voxelList.get(i).y;
            instancePositions[j+2] = voxelList.get(i).z;

            instanceColors[k] = voxelList.get(i).color.x;
            instanceColors[k+1] = voxelList.get(i).color.y;
            instanceColors[k+2] = voxelList.get(i).color.z;
            instanceColors[k+3] = voxelList.get(i).alpha;
        }
        
        instancePositionsBuffer = Buffers.newDirectFloatBuffer(instancePositions);
        instanceColorsBuffer = Buffers.newDirectFloatBuffer(instanceColors);
        
        
        //generate vbo and ibo buffers
        IntBuffer tmp = IntBuffer.allocate(2);
        gl.glGenBuffers(2, tmp);
        vboId=tmp.get(0);
        iboId=tmp.get(1);
        
        
        
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboId);
        
            //allocate total memory
            gl.glBufferData(GL3.GL_ARRAY_BUFFER, (cube.vertexBuffer.capacity()*FLOAT_SIZE)+(instancePositionsBuffer.capacity()*FLOAT_SIZE)+(instanceColorsBuffer.capacity()*FLOAT_SIZE), null, GL3.GL_STATIC_DRAW);
            
            /***set buffers in global buffer (int target, long offset, long size, Buffer buffer)****/
            
            //set vertex buffer
            gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, 0, cube.vertexBuffer.capacity()*FLOAT_SIZE, cube.vertexBuffer);
            
            //set instances positions buffer
            gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, cube.vertexBuffer.capacity()*FLOAT_SIZE, instancePositionsBuffer.capacity()*FLOAT_SIZE, instancePositionsBuffer);
            
            //set instances colors buffer
            gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, (cube.vertexBuffer.capacity()+instancePositionsBuffer.capacity())*FLOAT_SIZE, instanceColorsBuffer.capacity()*FLOAT_SIZE, instanceColorsBuffer);
            
            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, iboId);
                gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, cube.indexBuffer.capacity()*SHORT_SIZE, cube.indexBuffer, GL3.GL_STATIC_DRAW);
            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, 0);
        
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
        
        //generate vao
        int[] tmp2 = new int[1];
        gl.glGenVertexArrays(1, tmp2, 0);
        vaoId = tmp2[0];
        
        gl.glBindVertexArray(vaoId);
        
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboId);

                gl.glEnableVertexAttribArray(shader.attributeMap.get("position"));
                gl.glVertexAttribPointer(shader.attributeMap.get("position"), 3, GL3.GL_FLOAT, false, 0, 0);
                
                gl.glEnableVertexAttribArray(shader.attributeMap.get("instance_position"));
                gl.glVertexAttribPointer(shader.attributeMap.get("instance_position"), 3, GL3.GL_FLOAT, false, 0, cube.vertexBuffer.capacity()*FLOAT_SIZE);
                gl.glVertexAttribDivisor(shader.attributeMap.get("instance_position"), 1);
                
                gl.glEnableVertexAttribArray(shader.attributeMap.get("instance_color"));
                gl.glVertexAttribPointer(shader.attributeMap.get("instance_color"), 4, GL3.GL_FLOAT, false, 0, (cube.vertexBuffer.capacity()+instancePositionsBuffer.capacity())*FLOAT_SIZE);
                gl.glVertexAttribDivisor(shader.attributeMap.get("instance_color"), 1);
                 
            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, iboId);
            
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
            
        gl.glBindVertexArray(0);
        
        gradientUpdated = true;
    }
    
    public void render(GL3 gl){
        

        //draw voxels
        gl.glBindVertexArray(vaoId);
            gl.glDrawElementsInstanced(GL3.GL_TRIANGLES, cube.vertexCount, GL3.GL_UNSIGNED_SHORT, 0, voxelList.size());
        gl.glBindVertexArray(0);
        
        if(!gradientUpdated){
            
            instanceColors = new float[voxelList.size()*4];

            for (int i=0, j=0;i<voxelList.size();i++, j+=4) {

                instanceColors[j] = voxelList.get(i).color.x;
                instanceColors[j+1] = voxelList.get(i).color.y;
                instanceColors[j+2] = voxelList.get(i).color.z;
                instanceColors[j+3] = voxelList.get(i).alpha;
            }

            instanceColorsBuffer = Buffers.newDirectFloatBuffer(instanceColors);
   
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboId);

                gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, (cube.vertexBuffer.capacity()+instancePositionsBuffer.capacity())*FLOAT_SIZE, instanceColorsBuffer.capacity()*FLOAT_SIZE, instanceColorsBuffer);

            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
            
            gradientUpdated = true;
        }
        
        if(!cubeSizeUpdated){
            
            cube = MeshFactory.createCube(cubeSize);
        
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboId);

                gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, 0, cube.vertexBuffer.capacity()*FLOAT_SIZE, cube.vertexBuffer);

            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
            
            cubeSizeUpdated = true;
        }               
    }
}
