package fr.amap.amapvox.gui;

import fr.amap.amapvox.chart.ChartViewer;
import fr.amap.amapvox.chart.VoxelsToChart;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApp extends Application {

    final static Logger logger = LoggerFactory.getLogger(MainApp.class);
    
    @Override
    public void start(Stage stage) throws Exception {
        
        
        /*File alsParacou2013 = new File("/home/calcul/Documents/Julien/comparaisons ALS_multires_TLS_paracou_2013/als_multires.vox");
        File tlsParacou2013 = new File("/home/calcul/Documents/Julien/comparaisons ALS_multires_TLS_paracou_2013/tls_non_pondere.vox");
        
        ChartViewer chartViewer = new ChartViewer("Hello world chart", 1200, 500);
        
        chartViewer.setChart(VoxelsToChart.createVegetationProfileChartMulti(alsParacou2013, tlsParacou2013));
        
        chartViewer.show();*/
        
        /*FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RingsMaskingSetupFrame.fxml"));
        Parent root = loader.load();
        loader.getController();
        Scene scene = new Scene(root);
        stage.setScene(scene);

        stage.show();*/
        
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainFrame.fxml"));
        Parent root = loader.load();
        MainFrameController controller = loader.getController();
                
        Parameters parameters = getParameters();
        
        Map<String, String> namedParameters = parameters.getNamed();
        if(!namedParameters.isEmpty()){
            String cfgFileList = namedParameters.get("execute-cfg");
            String[] cfgFileArray = cfgFileList.split(";");
            List<File> taskList = new ArrayList<>();
            for(String filePath : cfgFileArray){
                taskList.add(new File(filePath));
            }
            //controller.addTasksToTaskList(taskList);
            controller.executeTaskList(taskList);
            
        }else{
            
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
