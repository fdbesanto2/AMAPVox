/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar;

import fr.ird.voxelidar.engine3d.math.point.Point3F;
import fr.ird.voxelidar.octree.Node;
import fr.ird.voxelidar.octree.Octree;
import fr.ird.voxelidar.voxelisation.extraction.tls.RxpExtraction;
import fr.ird.voxelidar.voxelisation.extraction.Shot;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class FXPrincipal extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        /*
        Octree octree = new Octree(50);
        octree.loadPointsFromFile(new File("G:\\2014-03-31.ID2-3.RISCAN\\pointclouds\\ID_61_full.txt"));
        //octree.loadPointsFromFile(new File("/media/calcul/IomegaHDD/2014-03-31.ID2-3.RISCAN/pointclouds/ID_61_full.txt"));
        octree.build();
        
        int count = 0;
        int count2 = 0;
        
        long startTime = System.currentTimeMillis();
            try (BufferedReader reader = new BufferedReader(new FileReader(new File("e:\\test.txt")))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(" ");
                    
                    
                    if (split.length == 3) {
                        try {
                            
                            if(count2 == 103){
                                System.out.println("test");
                            }
                            Point3F value = new Point3F(Float.valueOf(split[0]),Float.valueOf(split[1]),Float.valueOf(split[2]));
                            Point3F incrementalSearchNearestPoint = octree.searchNearestPoint(value, Octree.INCREMENTAL_SEARCH);
                            
                            
                            boolean test = false;
                            if(incrementalSearchNearestPoint != null){
                                float distance = value.distanceTo(incrementalSearchNearestPoint);
                                
                                if(distance < 0.01f){
                                    test = true;
                                }
                                
                                if (test) {
                                    count++;
                                }
                            }
                            
                            
                            
                        } catch (Exception e) {
                            System.err.println(e);
                        }

                        count2++;

                    }
                }

                long endTime = System.currentTimeMillis();
                System.out.println("temps de traitement: " + ((endTime - startTime) * Math.pow(10, -3)));
                System.out.println("max distance "+0.1);
                System.out.println("ratio: "+count+"/"+octree.getPoints().length);
                //System.out.println("exact values: "+ptCloudTool.exactValue);
                //System.out.println("approximate values: "+ptCloudTool.approximateValue);

            } catch (FileNotFoundException ex) {
                
            } catch (IOException ex) {
                
            }
        */
        //Point3F incrementalSearchNearestPoint = octree.incrementalSearchNearestPoint(new Point3F(-9.166f, 10.019f, -2.301f));
        //Point3F incrementalSearchNearestPoint = octree.incrementalSearchNearestPoint(new Point3F(-9.166f, 10.019f, -2.301f));
        
        /*
        Dtm dtm = DtmLoader.readFromAscFile(new File("C:\\Users\\Julien\\Desktop\\samples\\dtm\\ALSbuf_xyzirncapt_dtm.asc"));
        dtm.buildMesh();
        dtm.exportObj(new File("C:\\Users\\Julien\\Desktop\\samples\\dtm\\ALSbuf_xyzirncapt_dtm.obj"));
        */
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainFrame.fxml"));
        Parent root = loader.load();
        MainFrameController controller = loader.getController();
        
        Scene scene = new Scene(root);
        
        scene.getStylesheets().add("/styles/Styles.css");
        
        stage.setTitle("AMAPVox");
        stage.setScene(scene);
        
        controller.setStage(stage);
        
        stage.show();
        
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
