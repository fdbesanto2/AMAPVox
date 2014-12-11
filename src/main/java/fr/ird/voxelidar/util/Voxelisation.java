/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

import fr.ird.voxelidar.io.stream.FluxViewer;
import fr.ird.voxelidar.math.matrix.Mat4D;
import fr.ird.voxelidar.math.vector.Vec3F;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Julien
 */
public class Voxelisation {
    
    public String args;
    private final static String VOXELISATION_PROGRAM = "VoxelAnalysis.jar";
    private final EventListenerList listeners;
    
    public Voxelisation(){
        listeners = new EventListenerList();
    }
    
    public void addVoxelisationListener(VoxelisationListener listener){
        listeners.add(VoxelisationListener.class, listener);
    }
    
    public void fireProgress(String progress, int ratio){
        
        for(VoxelisationListener listener :listeners.getListeners(VoxelisationListener.class)){
            
            listener.voxelisationStepProgress(progress, ratio);
        }
    }
    
    public void fireFinished(){
        
        for(VoxelisationListener listener :listeners.getListeners(VoxelisationListener.class)){
            
            listener.voxelisationFinished();
        }
    }
    
    public void voxelise(final Mat4D mat4x4, final String pointsFile, final String trajectoryFile, final Vec3F bottomCorner, final Vec3F topCorner, final int numberX, final int numberY, final int numberZ){
        
        final VoxelPreprocessing preprocessVox = new VoxelPreprocessing();
        
        
        SwingWorker sw = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                
                
                preprocessVox.addVoxelPreprocessingListener(new VoxelPreprocessingAdapter() {

                    @Override
                    public void voxelPreprocessingStepProgress(String progress, int ratio) {
                        fireProgress(progress, ratio);
                    }

                    @Override
                    public void voxelPreprocessingFinished() {
                        
                        System.out.println("voxelisation:");
                        fireProgress("voxelisation", 100);
        
                        String inputFilePath = preprocessVox.outputFile.getAbsolutePath();
                
                        String commande = "java -jar "+VOXELISATION_PROGRAM + " ";
                        commande += inputFilePath + " ";
                        commande += bottomCorner.x + " ";
                        commande += bottomCorner.y + " ";
                        commande += bottomCorner.z + " ";
                        commande += topCorner.x + " ";
                        commande += topCorner.y + " ";
                        commande += topCorner.z + " ";
                        commande += numberX + " ";
                        commande += numberY + " ";
                        commande += numberZ;

                        Process p;
                        try {
                            p = Runtime.getRuntime().exec(commande);
                            
                            FluxViewer fluxSortie = new FluxViewer(p.getInputStream());
                            FluxViewer fluxErreur = new FluxViewer(p.getErrorStream());
                            new Thread(fluxSortie).start();
                            new Thread(fluxErreur).start();

                            p.waitFor();

                        } catch (IOException | InterruptedException ex) {
                            Logger.getLogger(Voxelisation.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                        fireFinished();
                    }
                });
                
                preprocessVox.generateEchosFile(mat4x4, pointsFile, trajectoryFile);

                return null;
            }
        };
        
        sw.execute();
        
    }
    
    public void writeFile(String outputFilePath){
        
    }
}
