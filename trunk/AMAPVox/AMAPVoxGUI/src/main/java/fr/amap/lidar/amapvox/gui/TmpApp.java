package fr.amap.lidar.amapvox.gui;

import fr.amap.lidar.amapvox.gui.viewer3d.Viewer3DPanelController;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TmpApp extends Application {

    final static Logger logger = LoggerFactory.getLogger(MainApp.class);
    
    @Override
    public void start(Stage stage) throws Exception {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Viewer3DPanel.fxml"));
        Parent root = loader.load();
        Viewer3DPanelController controller = loader.getController();
        Scene scene = new Scene(root);

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
