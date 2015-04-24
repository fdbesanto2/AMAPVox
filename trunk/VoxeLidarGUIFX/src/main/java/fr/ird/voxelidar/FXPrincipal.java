/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar;

import fr.ird.voxelidar.engine3d.object.scene.Dtm;
import fr.ird.voxelidar.engine3d.object.scene.DtmLoader;
import fr.ird.voxelidar.voxelisation.extraction.tls.RxpExtraction;
import fr.ird.voxelidar.voxelisation.extraction.tls.Shot;
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
        /*
        RxpExtraction rxpExtraction = new RxpExtraction();
        rxpExtraction.openRxpFile(new File("/media/forestview01/BDLidar/TLS/Paracou2014/FTH2014.RiSCAN/SCANS/ScanPos002/SINGLESCANS/850215_172246.mon.rxp"));
        
        Iterator<Shot> iterator = rxpExtraction.iterator();
        
        int count = 0;
        while(iterator.hasNext()){
            Shot shot = iterator.next();
            count++;
        }
        
        rxpExtraction.close();
        */
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
