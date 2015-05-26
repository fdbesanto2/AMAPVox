/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar;

import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4F;
import fr.ird.voxelidar.engine3d.math.point.Point3F;
import fr.ird.voxelidar.engine3d.math.vector.Vec4;
import fr.ird.voxelidar.engine3d.math.vector.Vec4D;
import fr.ird.voxelidar.gui.MainFrameController;
import fr.ird.voxelidar.lidar.format.als.LasHeader;
import fr.ird.voxelidar.lidar.format.als.LasReader;
import fr.ird.voxelidar.lidar.format.als.PointDataRecordFormat0;
import fr.ird.voxelidar.lidar.format.tls.Rsp;
import fr.ird.voxelidar.octree.Octree;
import fr.ird.voxelidar.octree.OctreeFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.Iterator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class FXPrincipal extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        
        /*
        Octree octree = OctreeFactory.createOctreeFromPointFile(new File("/media/calcul/IomegaHDD/2014-03-31.ID2-3.RISCAN/pointclouds/ID_61_Vf.txt"), 50, false);
        octree.build();
        
        File directory = new File("/media/calcul/IomegaHDD/2014-03-31.ID2-3.RISCAN/ascii");
        
        File[] fileList = directory.listFiles();
        
        Rsp rsp = new Rsp();
        rsp.read(new File("/media/calcul/IomegaHDD/2014-03-31.ID2-3.RISCAN/project.rsp"));
        Mat4D popMatrix = rsp.getPopMatrix();
        
        
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/media/calcul/IomegaHDD/2014-03-31.ID2-3.RISCAN/ascii/result.txt")))){
            
            for(File file : fileList){
                
                if(file.getName().contains(".las")){
                    
                    LasReader reader = new LasReader();
                    reader.open(file);

                    LasHeader header = reader.getHeader();

                    for (PointDataRecordFormat0 p : reader) {


                        Point3d location = new Point3d(((p.getX() * header.getxScaleFactor()) + header.getxOffset()),
                                                        ((p.getY() * header.getyScaleFactor()) + header.getyOffset()),
                                                        ((p.getZ() * header.getzScaleFactor()) + header.getzOffset()));

                        Vec4D transformedLocation = Mat4D.multiply(popMatrix, new Vec4D(location.x, location.y, location.z, 1));

                        boolean pointBelongsToPointcloud = octree.isPointBelongsToPointcloud(new Point3F((float)transformedLocation.x, (float)transformedLocation.y, (float)transformedLocation.z), 0.0025f, Octree.INCREMENTAL_SEARCH);

                        if(pointBelongsToPointcloud){
                            writer.write(transformedLocation.x+" "+transformedLocation.y+" "+transformedLocation.z+" "+p.getNumberOfReturns()+" "+p.getReturnNumber()+" "+p.getIntensity()+"\n");
                        }

                    }
                }
                
                
            }
            
        }
        
        */
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainFrame.fxml"));
        Parent root = loader.load();
        MainFrameController controller = loader.getController();
        
        Scene scene = new Scene(root);
            
        scene.getStylesheets().add("/styles/Styles.css");
        
        stage.setTitle("AMAPVox");
        stage.setScene(scene);
        stage.getIcons().addAll(new Image("/icons/icon_512x512.png"), 
                        new Image("/icons/icon_256x256.png"), 
                        new Image("/icons/icon_128x128.png"),
                        new Image("/icons/icon_64x64.png"));
        
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
