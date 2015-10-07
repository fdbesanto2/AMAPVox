/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.object.scene;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import fr.amap.amapvox.commons.math.geometry.AABB;
import fr.amap.amapvox.commons.math.geometry.Plane;
import fr.amap.amapvox.commons.math.point.Point3F;
import fr.amap.amapvox.commons.math.vector.Vec3F;
import fr.amap.amapvox.commons.util.BoundingBox3F;
import fr.amap.amapvox.commons.util.ColorGradient;
import fr.amap.amapvox.commons.util.CombinedFilter;
import fr.amap.amapvox.commons.util.CombinedFilters;
import fr.amap.amapvox.commons.util.Filter;
import fr.amap.amapvox.commons.util.StandardDeviation;
import fr.amap.amapvox.voxcommons.RawVoxel;
import fr.amap.amapvox.voxcommons.VoxelSpaceInfos;
import fr.amap.amapvox.voxreader.VoxelFileRawReader;
import fr.amap.amapvox.voxviewer.mesh.GLMesh;
import fr.amap.amapvox.voxviewer.mesh.GLMeshFactory;
import fr.amap.amapvox.voxviewer.mesh.InstancedGLMesh;
import fr.amap.amapvox.voxviewer.misc.Attribut;
import java.awt.Color;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.event.EventListenerList;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VoxelSpaceSceneObject extends SceneObject{
    
    public static final int FLOAT_SIZE = Buffers.SIZEOF_FLOAT;
    public static final int INT_SIZE = Buffers.SIZEOF_INT;
    
    public static final int VOXELSPACE_FORMAT1 = 1;
    public static final int VOXELSPACE_FORMAT2 = 2;

    
    
    public enum Format{
        VOXELSPACE_FORMAT2(2);
        
        private final int format;
        Format(int format){
            this.format = format;
        }
    }
    
    private float cubeSize = 1.0f;
    
    public float widthX, widthY, widthZ;
    private boolean fileLoaded;
    private float attributValueMax;
    private float attributValueMin;
    public float attributValueMaxClipped;
    public float attributValueMinClipped;
    private boolean useClippedRangeValue;
    public float min;
    public float max;
    
    private File voxelsFile;
    
    public boolean arrayLoaded;
    private Map<String,Attribut> mapAttributs;
    private Set<String> variables;
    private String currentAttribut;
    
    public float centerX;
    public float centerY;
    public float centerZ;
    
    private Color[] gradient = ColorGradient.GRADIENT_HEAT;
    private ColorGradient colorGradient;
    
    private boolean gradientUpdated;
    private boolean cubeSizeUpdated = true;
    private boolean instancesUpdated;
    
    private int voxelNumberToDraw = 0;
    
    
    private boolean stretched;
    
    public VoxelSpaceData data;
    
    //private Set<Filter> filteredValues;
    private CombinedFilters combinedFilters;
    private boolean displayValues;
    
    private final EventListenerList listeners;
    float sdValue;
    float average;
    
    //handle cutting plane
    private boolean isCuttingInit;
    private Vec3F lastRightVector = new Vec3F();
    private Vec3F loc;
    private float cuttingIncrementFactor = 1.0f;
    
    private final PropertyChangeSupport props = new PropertyChangeSupport(this);
    
    public VoxelSpaceSceneObject(){
        
        //filteredValues = new TreeSet<>();
        //filteredValues.add(new Filter("x", Float.NaN, Filter.EQUAL));
        //filteredValues.add(new Filter("x", 0.0f, Filter.EQUAL));
        combinedFilters = new CombinedFilters();
        combinedFilters.addFilter(new CombinedFilter(new Filter("x", Float.NaN, Filter.EQUAL), null, CombinedFilter.AND));
        combinedFilters.addFilter(new CombinedFilter(new Filter("x", 0.0f, Filter.EQUAL), null, CombinedFilter.AND));
        mapAttributs = new LinkedHashMap<>();
        variables = new TreeSet<>();
        listeners = new EventListenerList();
        fileLoaded = false;
    }
    
    public VoxelSpaceSceneObject(File voxelSpace){
        
        //filteredValues = new TreeSet<>();
        //filteredValues.add(new Filter("x", Float.NaN, Filter.EQUAL));
        //filteredValues.add(new Filter("x", 0.0f, Filter.EQUAL));
        combinedFilters = new CombinedFilters();
        combinedFilters.addFilter(new CombinedFilter(new Filter("x", Float.NaN, Filter.EQUAL), null, CombinedFilter.AND));
        combinedFilters.addFilter(new CombinedFilter(new Filter("x", 0.0f, Filter.EQUAL), null, CombinedFilter.AND));
        mapAttributs = new LinkedHashMap<>();
        variables = new TreeSet<>();
        listeners = new EventListenerList();
        fileLoaded = false;
        
        this.voxelsFile = voxelSpace;
    }
    
    public void addPropertyChangeListener(String propName, PropertyChangeListener l) {
        props.addPropertyChangeListener(propName, l);
    }

    public void setMapAttributs(Map<String, Attribut> mapAttributs) {
        this.mapAttributs = mapAttributs;
    }

    public void setVariables(TreeSet<String> variables) {
        this.variables = variables;
    }

    public float getCubeSize() {
        return cubeSize;
    }
    
    public void addExtendedMapAttributs(Map<String,Attribut> extendedMapAttributs){
        for(Entry entry : extendedMapAttributs.entrySet()){
            
            Attribut a = (Attribut) entry.getValue();
            this.variables.add(a.getName());
            addAttribut(a.getName(), a.getExpressionString());
        }
    }

    public void setCurrentAttribut(String attributToVisualize) {
        
        this.currentAttribut = attributToVisualize;
    }
    
    public void load() throws IOException, Exception{
        
        loadFromFile(voxelsFile);
        
        cubeSize = (float) (data.getVoxelSpaceInfos().getResolution()/2.0f);
        
        int instanceNumber = data.voxels.size();    
        mesh = new InstancedGLMesh(GLMeshFactory.createCube(cubeSize), instanceNumber);
    }

    public Map<String, Attribut> getMapAttributs() {
        return mapAttributs;
    }    
    
    public final void addAttribut(String name, String expression){
        
        if(!mapAttributs.containsKey(name)){            
            this.variables.add(name);
            mapAttributs.put(name, new Attribut(name, expression, variables));
        }
    }
    
    public void setReadFileProgress(int progress) {
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
    
    public void setStretched(boolean stretched){
        this.stretched = stretched;
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

    public boolean isGradientUpdated() {
        return gradientUpdated;
    }
    
    public void setGradientUpdated(boolean value){
        props.firePropertyChange("gradientUpdated", gradientUpdated, value);
        gradientUpdated = value;
    }

    public void changeCurrentAttribut(String attributToVisualize) {
        
        this.currentAttribut = attributToVisualize;
        updateValue();
    }
    
    public void setFilterValues(Set<CombinedFilter> values, boolean display){
        combinedFilters = new CombinedFilters();
        combinedFilters.setFilters(values);
        this.displayValues = display;
    }
    
    private void setWidth(){
        
        if(data.voxels.size() > 0){
            
            VoxelObject lastVoxel = ((VoxelObject)data.getLastVoxel());
            VoxelObject firstVoxel = ((VoxelObject)data.getFirstVoxel());
            
            Point3f lastVoxelPosition = getVoxelPosition(lastVoxel.$i, lastVoxel.$j, lastVoxel.$k);
            Point3f firstVoxelPosition = getVoxelPosition(firstVoxel.$i, firstVoxel.$j, firstVoxel.$k);
            
            widthX = lastVoxelPosition.x - firstVoxelPosition.x;
            widthY = lastVoxelPosition.y - firstVoxelPosition.y;
            widthZ = lastVoxelPosition.z - firstVoxelPosition.z;
        }
    }
    
    private void setCenter(){
        
        
        if(data.voxels.size() > 0){
            
            VoxelObject firstVoxel = (VoxelObject) data.getFirstVoxel();
            VoxelObject lastVoxel = (VoxelObject) data.getLastVoxel();
            
            Point3f lastVoxelPosition = getVoxelPosition(lastVoxel.$i, lastVoxel.$j, lastVoxel.$k);
            Point3f firstVoxelPosition = getVoxelPosition(firstVoxel.$i, firstVoxel.$j, firstVoxel.$k);
            
            centerX = (firstVoxelPosition.x + lastVoxelPosition.x)/2.0f;
            centerY = (firstVoxelPosition.y + lastVoxelPosition.y)/2.0f;
            centerZ = (firstVoxelPosition.z + lastVoxelPosition.z)/2.0f;
            
            position = new Point3F(centerX, centerY, centerZ);
            
        }
        
    }
    
    public void initAttributs(String[] columnsNames){
        
        for(String name : columnsNames){
            variables.add(name);
        }
        
        for(String name : columnsNames){
            mapAttributs.put(name, new Attribut(name, name, variables));
        }
    }
    
    
    
    
    private void readVoxelFormat(File f) throws IOException, Exception{
        
        try {
            
            VoxelFileRawReader reader = new VoxelFileRawReader(f, false);
            
            VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();
            
            data = new VoxelSpaceData(infos);
            initAttributs(infos.getColumnNames());

            int count = infos.getSplit().x * infos.getSplit().y * infos.getSplit().z;

            int lineNumber = 0;

            for(RawVoxel voxel : reader){

                float[] mapAttrs = new float[voxel.attributs.length+3];
                mapAttrs[0] = voxel.$i;
                mapAttrs[1] = voxel.$j;
                mapAttrs[2] = voxel.$k;

                for (int i=3;i<mapAttrs.length;i++) {

                    mapAttrs[i] = voxel.attributs[i-3];
                }

                Point3i indice = new Point3i(voxel.$i, voxel.$j, voxel.$k);               

                data.voxels.add(new VoxelObject(indice, mapAttrs, 1.0f));

                lineNumber++;

                setReadFileProgress((lineNumber * 100) / count);
            }
            
        } catch (Exception ex) {
            throw ex;
        }
    }
    
    private Point3f getVoxelPosition(int i, int j, int k){
        
        VoxelSpaceInfos infos = data.getVoxelSpaceInfos();
        double posX = infos.getMinCorner().x + (infos.getResolution() / 2.0d) + (i * infos.getResolution());
        double posY = infos.getMinCorner().y + (infos.getResolution() / 2.0d) + (j * infos.getResolution());
        double posZ = infos.getMinCorner().z + (infos.getResolution() / 2.0d) + (k * infos.getResolution());
        
        return new Point3f((float)posX, (float)posY, (float)posZ);
    }
    
    public final void loadFromFile(File f) throws IOException, Exception{
        
        setFileLoaded(false);
        
        //attribut is a custom equation defined by user
        //this.mapAttributs = mapAttributs;
        
        this.file =f;
        
        readVoxelFormat(file);
        //updateValue();

        setCenter();
        setWidth();

        setFileLoaded(true);
    }
    
    public void setAttributValueRange(float minClipped, float maxClipped){
        
        useClippedRangeValue = true;
        attributValueMinClipped = minClipped;
        attributValueMaxClipped = maxClipped;
    }
    
    public void resetAttributValueRange(){
        useClippedRangeValue = false;
    }

    public boolean isUseClippedRangeValue() {
        return useClippedRangeValue;
    }
    
    public void updateValue(){
        
        if(currentAttribut == null){
            currentAttribut = mapAttributs.entrySet().iterator().next().getKey();
        }
        
        Attribut attribut = mapAttributs.get(currentAttribut);
        
        //float[] values = new float[data.voxels.size()];
        
        int count = 0;
        boolean minMaxInit = false;
        
        StandardDeviation sd = new StandardDeviation();
        
        for(int v=0;v<data.voxels.size();v++){
            
            VoxelObject voxel = (VoxelObject)data.voxels.get(v);
            
            float attributValue;
            
            float[] attributs = voxel.getAttributs();
            
            for(int i=0; i< attributs.length;i++){
                
                String name = data.getVoxelSpaceInfos().getColumnNames()[i];
                double value = attributs[i];
                attribut.getExpression().setVariable(name, value);
            }

             try{
                attributValue = (float) attribut.getExpression().evaluate();
            }catch(Exception e){
                attributValue = 0;
            }

            voxel.attributValue = attributValue;
            //voxel.color = getColorFromValue(attributValue);

            if (!Float.isNaN(attributValue)){
                
                if(!minMaxInit){

                    attributValueMax = attributValue;
                    attributValueMin = attributValue;

                    minMaxInit = true;
                }else{

                    //set maximum attribut value
                    if(attributValue>attributValueMax){

                        attributValueMax = attributValue;
                    }

                    //set minimum attribut value
                    if(attributValue < attributValueMin){

                        attributValueMin = attributValue;
                    }
                }
            }
            

            
            voxel.setAlpha(255);
            
            //values[count] = voxel.attributValue;
            
            if(stretched){
                if(useClippedRangeValue){
                    if(voxel.attributValue < attributValueMinClipped){
                        sd.addValue(attributValueMinClipped);
                    }else if(voxel.attributValue > attributValueMaxClipped){
                        sd.addValue(attributValueMaxClipped);
                    }else{
                        sd.addValue(voxel.attributValue); 
                   }
                }else{
                    sd.addValue(voxel.attributValue);
                }
            }
            
            count++;
            
        }
        
        //calculate standard deviation
        
        if(stretched){
            sdValue = sd.getStandardDeviation();
            average = sd.getAverage();
            
            min = average-(2*sdValue);
            max = average+(2*sdValue);
            
            if(useClippedRangeValue){
                if(min < attributValueMinClipped){
                    min = attributValueMinClipped;
                }

                if(max > attributValueMaxClipped){
                    max = attributValueMaxClipped;
                }
            }else{
                if(min < attributValueMin){
                    min = attributValueMin;
                }

                if(max > attributValueMax){
                    max = attributValueMax;
                }
            }

            setGradientColor(gradient, min, max);
            
        }else{
            if(useClippedRangeValue){
                setGradientColor(gradient, attributValueMinClipped, attributValueMaxClipped);
            }else{
                setGradientColor(gradient, attributValueMin, attributValueMax);
            }
        }
        
    }

    public boolean isStretched() {
        return stretched;
    }
    
    
    public void updateColorValue(Color[] gradient){
        if(stretched){
            setGradientColor(gradient, min, max);
        }else{
            if(useClippedRangeValue){
                setGradientColor(gradient, attributValueMinClipped, attributValueMaxClipped);
            }else{
                setGradientColor(gradient, attributValueMin, attributValueMax);
            }
            
        }
        
    }
    
    public Vec3F getColorFromValue(float value){
        
        Color c = colorGradient.getColor(value);
        
        return new Vec3F(c.getRed()/255.0f, c.getGreen()/255.0f, c.getBlue()/255.0f);
    }
    
    
    public void setGradientColor(Color[] gradientColor, float valMin, float valMax){
        
        this.gradient = gradientColor;
        
        ColorGradient color = new ColorGradient(valMin, valMax);
        color.setGradientColor(gradientColor);
        //ArrayList<Float> values = new ArrayList<>();
        voxelNumberToDraw = 0;
        
        for(int i=0;i<data.voxels.size();i++){
            
            VoxelObject voxel = (VoxelObject)data.voxels.get(i);
            
            //float ratio = voxel.attributValue/(attributValueMax-attributValueMin);
            //float value = valMin+ratio*(valMax-valMin);
            //Color colorGenerated = color.getColor(value);
            Color colorGenerated = color.getColor(voxel.attributValue);
            
            voxel.setColor(colorGenerated.getRed(), colorGenerated.getGreen(), colorGenerated.getBlue());
            //values.add(voxel.attributValue);
            
            boolean isFiltered = combinedFilters.doFilter(voxel.attributValue);
            
            if(isFiltered && displayValues){
                voxel.setAlpha(1);
                voxelNumberToDraw++;
            }else if(isFiltered && !displayValues){
                voxel.setAlpha(0);
            }else if(!isFiltered && displayValues){
                voxel.setAlpha(0);
            }else{
                voxel.setAlpha(1);
                voxelNumberToDraw++;
            }
        }
        //System.out.println("test");
        //voxelList = ImageEqualisation.scaleHistogramm(voxelList);
        //voxelList = ImageEqualisation.voxelSpaceFormatEqualisation(voxelList);
        
        
    }
    
    
    public void updateInstanceColorBuffer(){
        
        setGradientUpdated(false);
        
    }
    
    public void updateCubeSize(GL3 gl, float size){
        
        cubeSize = size;
        cubeSizeUpdated = false;
    }
    
    
    public void setCuttingPlane(Plane plane){
        
        Vec3F normale = plane.getNormale();
        Vec3F point = new Vec3F(plane.getPoint().x, plane.getPoint().y, plane.getPoint().z);
        
        for(int i=0;i<data.voxels.size();i++){
            
            VoxelObject voxel = (VoxelObject)data.voxels.get(i);
            
            Point3f pt = getVoxelPosition(voxel.$i, voxel.$j, voxel.$k);
            Vec3F position = new Vec3F(pt.x, pt.y, pt.z);
            
            float side = Vec3F.dot(Vec3F.substract(position, point), normale);
            
            voxel.isHidden = side > 0;
        }
        
    }
    
    public void clearCuttingPlane(){
        
        for(int i=0;i<data.voxels.size();i++){
            
            VoxelObject voxel = (VoxelObject)data.voxels.get(i);
            
            voxel.isHidden = false;
        }
    }
    
    public void resetCuttingPlane(){
        
        clearCuttingPlane();
        updateVao();
        
        isCuttingInit = false;
        lastRightVector = new Vec3F();
    }
    
    public void setCuttingIncrementFactor(float cuttingIncrementFactor) {
        this.cuttingIncrementFactor = cuttingIncrementFactor;
    }
    
    public void setCuttingPlane(boolean increase, Vec3F forwardVector, Vec3F rightVector, Vec3F upVector, Vec3F cameraLocation){
        
        rightVector = Vec3F.normalize(rightVector);
        upVector = Vec3F.normalize(upVector);
        
        if(lastRightVector.x != rightVector.x || lastRightVector.y != rightVector.y || lastRightVector.z != rightVector.z){
            isCuttingInit = false;
        }
        
        lastRightVector = rightVector;
        
        //init
        if(!isCuttingInit){
            
            loc = cameraLocation;
            Point3d bottomCorner = data.getVoxelSpaceInfos().getMinCorner();
            Point3d topCorner = data.getVoxelSpaceInfos().getMaxCorner();
            AABB aabb = new AABB(new BoundingBox3F(new Point3F((float)bottomCorner.x,(float)bottomCorner.y,(float)bottomCorner.z),
                                               new Point3F((float)topCorner.x,(float)topCorner.y,(float)topCorner.z)));
            
            Point3F nearestPoint = aabb.getNearestPoint(new Point3F(loc.x, loc.y, loc.z));
            loc = new Vec3F(nearestPoint.x, nearestPoint.y, nearestPoint.z);
            isCuttingInit = true;
            
        }else{
            Vec3F forward = forwardVector;
            Vec3F direction = Vec3F.normalize(forward);
            
            if(increase){
                loc = Vec3F.add(loc, Vec3F.multiply(direction, cuttingIncrementFactor));
            }else{
                loc = Vec3F.substract(loc, Vec3F.multiply(direction, cuttingIncrementFactor));
            }
            
        }
        
        
        Plane plane = new Plane(rightVector, upVector, new Point3F(loc.x, loc.y, loc.z));
        //System.out.println(loc.x+" "+loc.y+" "+loc.z);
        
        
        setCuttingPlane(plane);
        updateVao();
    }

    public boolean isInstancesUpdated() {
        return instancesUpdated;
    }

    public void setInstancesUpdated(boolean instancesUpdated) {
        this.instancesUpdated = instancesUpdated;
    }
    
    @Override
    public void initVao(GL3 gl){
        
        //generate vao
        int[] tmp2 = new int[1];
        gl.glGenVertexArrays(1, tmp2, 0);
        vaoId = tmp2[0];
        
        gl.glBindVertexArray(vaoId);
        
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, mesh.getVboId());

                gl.glEnableVertexAttribArray(shader.attributeMap.get("position"));
                gl.glVertexAttribPointer(shader.attributeMap.get("position"), 3, GL3.GL_FLOAT, false, 0, 0);
                
                gl.glEnableVertexAttribArray(shader.attributeMap.get("instance_position"));
                gl.glVertexAttribPointer(shader.attributeMap.get("instance_position"), 3, GL3.GL_FLOAT, false, 0, mesh.vertexBuffer.capacity()*FLOAT_SIZE);
                gl.glVertexAttribDivisor(shader.attributeMap.get("instance_position"), 1);
                
                gl.glEnableVertexAttribArray(shader.attributeMap.get("instance_color"));
                gl.glVertexAttribPointer(shader.attributeMap.get("instance_color"), 4, GL3.GL_FLOAT, false, 0, (mesh.vertexBuffer.capacity()+((InstancedGLMesh)mesh).instancePositionsBuffer.capacity())*FLOAT_SIZE);
                gl.glVertexAttribDivisor(shader.attributeMap.get("instance_color"), 1);
                 
            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, mesh.getIboId());
            
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
            
        gl.glBindVertexArray(0);
        
        setGradientUpdated(true);
    }
    
    public void updateVao(){
        
        //List<Float> instancePositionsList = new ArrayList<>();
        //List<Float> instanceColorsList = new ArrayList<>();
        
        float[] instancePositions = new float[voxelNumberToDraw*3];
        float[] instanceColors = new float[voxelNumberToDraw*4];

        int positionCount = 0;
        int colorCount = 0;
        
        for(int i=0;i<data.voxels.size();i++){
            
            VoxelObject voxel = (VoxelObject)data.voxels.get(i);
            
            if(voxel.getAlpha() != 0 && !voxel.isHidden){
                
                if(positionCount < instancePositions.length && colorCount < instanceColors.length){
                    
                    Point3f position = getVoxelPosition(voxel.$i, voxel.$j, voxel.$k);
                    
                    instancePositions[positionCount] = position.x;
                    instancePositions[positionCount+1] = position.y;
                    instancePositions[positionCount+2] = position.z;

                    instanceColors[colorCount] = voxel.getRed();
                    instanceColors[colorCount+1] = voxel.getGreen();
                    instanceColors[colorCount+2] = voxel.getBlue();
                    instanceColors[colorCount+3] = voxel.getAlpha();

                    positionCount += 3;
                    colorCount += 4;
                }
                
                
                /*
                instancePositionsList.add(voxel.position.x);
                instancePositionsList.add(voxel.position.y);
                instancePositionsList.add(voxel.position.z);
                
                instanceColorsList.add(voxel.getRed());
                instanceColorsList.add(voxel.getGreen());
                instanceColorsList.add(voxel.getBlue());
                instanceColorsList.add(voxel.getAlpha());
                */
                
            }
        }
        
        
        /*
        for(int i=0;i<instancePositionsList.size();i++){
            instancePositions[i] = instancePositionsList.get(i);
        }
        
        for(int i=0;i<instanceColorsList.size();i++){
            instanceColors[i] = instanceColorsList.get(i);
        }*/
        
        ((InstancedGLMesh)mesh).instancePositionsBuffer = Buffers.newDirectFloatBuffer(instancePositions);
        ((InstancedGLMesh)mesh).instanceColorsBuffer = Buffers.newDirectFloatBuffer(instanceColors);
        
        ((InstancedGLMesh)mesh).setInstanceNumber(instancePositions.length/3);
        
        instancesUpdated = false;
    }
    
    @Override
    public void initBuffers(GL3 gl){
        
        
        //mesh = new SimpleGLMesh(gl);
        
        int maxSize = (mesh.vertexBuffer.capacity()*GLMesh.FLOAT_SIZE)+(data.voxels.size()*3*GLMesh.FLOAT_SIZE)+(data.voxels.size()*4*GLMesh.FLOAT_SIZE);
        mesh.initBuffers(gl, maxSize);
        
        updateVao();
    }
    
    @Override
    public void draw(GL3 gl) {
        
        if(!instancesUpdated){
            
            mesh.updateBuffer(gl, 1, ((InstancedGLMesh)mesh).instancePositionsBuffer);
            mesh.updateBuffer(gl, 2, ((InstancedGLMesh)mesh).instanceColorsBuffer);
            
            gl.glBindVertexArray(vaoId);
        
                gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, mesh.getVboId());

                    gl.glEnableVertexAttribArray(shader.attributeMap.get("position"));
                    gl.glVertexAttribPointer(shader.attributeMap.get("position"), 3, GL3.GL_FLOAT, false, 0, 0);

                    gl.glEnableVertexAttribArray(shader.attributeMap.get("instance_position"));
                    gl.glVertexAttribPointer(shader.attributeMap.get("instance_position"), 3, GL3.GL_FLOAT, false, 0, mesh.vertexBuffer.capacity()*FLOAT_SIZE);
                    gl.glVertexAttribDivisor(shader.attributeMap.get("instance_position"), 1);

                    gl.glEnableVertexAttribArray(shader.attributeMap.get("instance_color"));
                    gl.glVertexAttribPointer(shader.attributeMap.get("instance_color"), 4, GL3.GL_FLOAT, false, 0, (mesh.vertexBuffer.capacity()+((InstancedGLMesh)mesh).instancePositionsBuffer.capacity())*FLOAT_SIZE);
                    gl.glVertexAttribDivisor(shader.attributeMap.get("instance_color"), 1);

                gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, mesh.getIboId());

                gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);

            gl.glBindVertexArray(0);
            
            instancesUpdated = true;
        }
        
        if(!gradientUpdated){
            
            float instanceColors[] = new float[data.voxels.size()*4];
            
            int count0 = 0;
            
            for (int i=0, j=0;i<data.voxels.size();i++, j+=4) {
                
                VoxelObject voxel = (VoxelObject) data.voxels.get(i);

                if(voxel.getAlpha() != 0 && !voxel.isHidden){
                
                    instanceColors[count0] = voxel.getRed();
                    instanceColors[count0+1] = voxel.getGreen();
                    instanceColors[count0+2] = voxel.getBlue();
                    instanceColors[count0+3] = voxel.getAlpha();
                    count0 += 4;
                }
            }

            ((InstancedGLMesh)mesh).instanceColorsBuffer = Buffers.newDirectFloatBuffer(instanceColors, 0, count0);
            
            mesh.updateBuffer(gl, 2, ((InstancedGLMesh)mesh).instanceColorsBuffer);
            
            setGradientUpdated(true);
        }
        
        if(!cubeSizeUpdated){
            
            GLMesh cube = GLMeshFactory.createCube(cubeSize);
            mesh.updateBuffer(gl, 0, cube.vertexBuffer);
            
            cubeSizeUpdated = true;
        }
        
        gl.glBindVertexArray(vaoId);
            if(texture != null){
                gl.glBindTexture(GL3.GL_TEXTURE_2D, textureId);
            }
            mesh.draw(gl);

            if(texture != null){
                gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
            }
        gl.glBindVertexArray(0);
    }

    public Set<String> getVariables() {
        return variables;
    }
    
    public float getRealAttributValueMax() {
        return attributValueMax;
    }
    
    public float getRealAttributValueMin() {
        return attributValueMin;
    }

    public float getAttributValueMax() {
        
        if(useClippedRangeValue){
            return attributValueMaxClipped;
        }else{
            return attributValueMax;
        }
        
    }

    public float getAttributValueMin() {
        if(useClippedRangeValue){
            return attributValueMinClipped;
        }else{
            return attributValueMin;
        }
    }
    
}
