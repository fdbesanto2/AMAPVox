/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar;

import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.lidar.format.dart.DartWriter;
import fr.ird.voxelidar.lidar.format.dtm.DtmLoader;
import fr.ird.voxelidar.lidar.format.dtm.RegularDtm;
import fr.ird.voxelidar.gui.MainFrameController;
import java.io.File;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class FXPrincipal extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        /*
        VoxelSpace voxelSpace = new VoxelSpace();
        voxelSpace.loadFromFile(new File("/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/las.vox"));
        
        DartWriter dartWriter = new DartWriter();
        dartWriter.setGenerateTrianglesFile(true);
        dartWriter.setTrianglesFile(new File("/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/triangles.txt"));
        dartWriter.setDtmFile(new File("/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/ALSbuf_xyzirncapt_dtm.asc"));
        dartWriter.writeFromVoxelSpace(voxelSpace.data, new File("/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/maket.txt"));
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
