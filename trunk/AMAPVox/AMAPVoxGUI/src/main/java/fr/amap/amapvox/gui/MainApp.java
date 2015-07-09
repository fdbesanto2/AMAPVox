package fr.amap.amapvox.gui;

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
