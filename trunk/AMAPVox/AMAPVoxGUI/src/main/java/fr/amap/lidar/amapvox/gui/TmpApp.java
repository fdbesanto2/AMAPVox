package fr.amap.lidar.amapvox.gui;

import fr.amap.commons.javafx.io.TextFileParserFrameController;
import java.io.File;
import java.util.List;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TmpApp extends Application {

    final static Logger logger = LoggerFactory.getLogger(MainApp.class);
    
    @Override
    public void start(Stage stage) throws Exception {
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TextFileParserFrame.fxml"));
        Parent root = loader.load();
        TextFileParserFrameController controller = loader.getController();
        Scene scene = new Scene(root);

        scene.getStylesheets().add("/styles/Styles.css");

        stage.setTitle("AMAPVox");
        stage.setScene(scene);
        controller.setStage(stage);
        
        controller.setColumnAssignmentValues("Ignore", "X", "Y", "Z", "T");
        
        controller.setColumnAssignmentDefaultSelectedIndex(0, 1);
        controller.setColumnAssignmentDefaultSelectedIndex(1, 2);
        controller.setColumnAssignmentDefaultSelectedIndex(2, 3);
        controller.setColumnAssignmentDefaultSelectedIndex(3, 4);
        
        controller.setTextFile(new File("/home/calcul/Documents/Julien/csv_file_parsing_test.txt"));

        stage.show();
        
        stage.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                
                List<String> assignedColumnsItems = controller.getAssignedColumnsItems();
                
                System.out.println("Columns assignment: ");
                for(String s : assignedColumnsItems){
                    System.out.println(s);
                }
                
                System.out.println("Separator: "+controller.getSeparator());
                
            }
        });
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
