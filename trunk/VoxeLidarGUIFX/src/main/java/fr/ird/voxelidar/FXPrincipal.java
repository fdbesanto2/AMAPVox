/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar;

import fr.ird.voxelidar.engine3d.math.point.Point3F;
import fr.ird.voxelidar.octree.Octree;
import fr.ird.voxelidar.voxelisation.extraction.tls.RxpExtraction;
import fr.ird.voxelidar.voxelisation.extraction.Shot;
import java.io.File;
import java.util.Iterator;
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
        
        Octree octree = new Octree(50);
        octree.loadPointsFromFile(new File("/media/calcul/IomegaHDD/2014-03-31.ID2-3.RISCAN/pointclouds/ID_61_full.txt"));
        octree.build();
        
        Point3F incrementalSearchNearestPoint = octree.incrementalSearchNearestPoint(new Point3F(-17.0f, 20.0f, 10.0f));
        
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
