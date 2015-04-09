/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.scene;

import fr.ird.voxelidar.engine3d.math.point.Point2F;
import fr.ird.voxelidar.engine3d.object.mesh.Attribut;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceData.Type;
import fr.ird.voxelidar.io.file.FileManager;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.ALSVoxel;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.TLSVoxel;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.Voxel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.EventListenerList;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

/**
 *
 * @author Julien
 */
public class VoxelSpaceLoader{
    
    private final VoxelSpaceData data;
    private boolean fileLoaded;
    private File voxelsFile;
    public File file;
    
    private final EventListenerList listeners;
    
    public VoxelSpaceLoader(){
        
        data = new VoxelSpaceData();
        listeners = new EventListenerList();
        fileLoaded = false;
    }
    
    public VoxelSpaceLoader(File voxelSpaceFile){
        
        data = new VoxelSpaceData();
        listeners = new EventListenerList();
        fileLoaded = false;
        
        this.voxelsFile = voxelSpaceFile;
    }
    
    public VoxelSpaceData load(){
        
        loadFromFile(voxelsFile);
        
        return data;
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

    public boolean isFileLoaded() {
        return fileLoaded;
    }
    
    public void firefileLoaded(){
        
        for(VoxelSpaceListener listener :listeners.getListeners(VoxelSpaceListener.class)){
            
            listener.voxelSpaceCreationFinished();
        }
    }
    
    public void addVoxelSpaceListener(VoxelSpaceListener listener){
        listeners.add(VoxelSpaceListener.class, listener);
    }

    
    private void readVoxelFormat(File f){
        
        String header = FileManager.readHeader(f.getAbsolutePath());
        
        
        if(header.equals("VOXEL SPACE")){
            
            
            int count = FileManager.getLineNumber(file.getAbsolutePath());

            /******read file*****/

            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(file));
                
                Map<String, Point2F> minMax = new HashMap<>();
                
                //header
                reader.readLine();
                
                
                String[] minC = reader.readLine().split(" ");
                data.bottomCorner.x =  Double.valueOf(minC[1]);
                data.bottomCorner.y =  Double.valueOf(minC[2]);
                data.bottomCorner.z =  Double.valueOf(minC[3]);
                
                String[] maxC = reader.readLine().split(" ");
                data.topCorner.x =  Double.valueOf(maxC[1]);
                data.topCorner.y =  Double.valueOf(maxC[2]);
                data.topCorner.z =  Double.valueOf(maxC[3]);
                
                String[] split = reader.readLine().split(" ");
                
                data.split = new Point3i(Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]));
                
                data.resolution.x = (data.topCorner.x - data.bottomCorner.x) / data.split.x;
                data.resolution.y = (data.topCorner.y - data.bottomCorner.y) / data.split.y;
                data.resolution.z = (data.topCorner.z - data.bottomCorner.z) / data.split.z;
                
                
                //offset
                String type = reader.readLine().split(" ")[1];
                
                if(type.equals("ALS")){
                    data.type = Type.ALS;
                }else{
                    data.type = Type.TLS;
                }
                
                String[] columnsNames = reader.readLine().split(" ");
                
                data.attributsNames.addAll(Arrays.asList(columnsNames));
                
                
                int lineNumber = 0;
                String line;                
                
                //start reading voxels
                while ((line = reader.readLine())!= null) {

                    String[] voxelLine = line.split(" ");
                    
                    
                    Point3i indice = new Point3i(Integer.valueOf(voxelLine[0]), 
                            Integer.valueOf(voxelLine[1]),
                            Integer.valueOf(voxelLine[2]));

                    float[] mapAttrs = new float[data.attributsNames.size()];

                    for (int i=0;i<voxelLine.length;i++) {
                        
                        float value = Float.valueOf(voxelLine[i]);
                        
                        mapAttrs[i] = value;
                        
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
                    
                    Point3f position = new Point3f((float) (data.bottomCorner.x+(indice.x*(data.resolution.x))),
                                                    (float) (data.bottomCorner.z+(indice.y*(data.resolution.y))),
                                                    (float) (data.bottomCorner.y+(indice.z*(data.resolution.z))));
                    
                    if(lineNumber == 0){
                        data.minY = position.y;
                        data.maxY = position.y;
                    }else{
                        if(data.minY > position.y){
                            data.minY = position.y;
                        }

                        if(data.maxY < position.y){
                            data.maxY = position.y;
                        }
                    }
                    
                    Voxel vox;
                    
                    if(type.equals("ALS")){
                        vox = new ALSVoxel(indice.x, indice.y, indice.z);
                    }else{
                        vox = new TLSVoxel(indice.x, indice.y, indice.z);
                    }
                    
                    for(int i=3;i<mapAttrs.length;i++){
                        vox.setFieldValue(vox.getClass(), columnsNames[i], vox, mapAttrs[i]);
                    }

                    data.voxels.add(vox);

                    lineNumber++;

                    setReadFileProgress((lineNumber * 100) / count);
                }
                
                reader.close();

            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
            }
            
        }
    }
    
    public final void loadFromFile(File f){
        
        setFileLoaded(false);
        
        //attribut is a custom equation defined by user
        //this.mapAttributs = mapAttributs;
        
        this.file =f;
        
        readVoxelFormat(file);


        //updateValue();

        setFileLoaded(true);
    }
    
    public void write(File output) {
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {

            writer.write("VOXEL SPACE" + "\n");
            writer.write("#min_corner: " + data.bottomCorner.x + " " + data.bottomCorner.y + " " + data.bottomCorner.z + "\n");
            writer.write("#max_corner: " + data.topCorner.x + " " + data.topCorner.y + " " + data.topCorner.z + "\n");
            writer.write("#split: " + data.split.x + " " + data.split.y + " " + data.split.z + "\n");

            if (data.type.equals(Type.TLS)) {
                writer.write("#type: " +"TLS"+ "\n");
                writer.write(Voxel.getHeader(TLSVoxel.class) + "\n");
            } else {
                writer.write("#type: " +"ALS"+ "\n");
                writer.write(Voxel.getHeader(ALSVoxel.class) + "\n");
            }

            for (int i = 0; i < data.split.x; i++) {
                for (int j = 0; j < data.split.y; j++) {
                    for (int k = 0; k < data.split.z; k++) {

                        Voxel vox = data.getVoxel(i, j, k);
                        
                        if(vox instanceof ALSVoxel){
                            writer.write(vox.toString() + "\n");
                        }else{
                            writer.write(vox.toString() + "\n");
                        }
                    }
                }
            }

            writer.close();

        } catch (FileNotFoundException e) {
        } catch (Exception e) {
        }

    }
    
}
