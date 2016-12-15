package fr.amap.lidar.amapvox.gui;

import fr.amap.lidar.amapvox.gui.task.TaskElement;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static javafx.application.Application.launch;

public class MainApp extends Application {

    private final static Logger LOGGER = LoggerFactory.getLogger(MainApp.class);
    
    @Override
    public void start(Stage stage) throws Exception {
                        
        ResourceBundle rb = ResourceBundle.getBundle("bundle_help", Locale.ENGLISH, new URLClassLoader(new URL[]{getClass().getResource("/strings/")}));
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainFrame.fxml"), rb);
        
        Parent root = loader.load();
        
        MainFrameController controller = loader.getController();
                
        Parameters parameters = getParameters();
        
        if(parameters.getUnnamed().contains("-h")){
            showHelp();
            System.exit(0);
        }
                
        Map<String, String> namedParameters = parameters.getNamed();
        if(!namedParameters.isEmpty()){
            
            String cfgFileList = namedParameters.get("execute-cfg");
            
            if(cfgFileList != null){
                String[] cfgFileArray = cfgFileList.split(";");
                List<TaskElement> taskList = new ArrayList<>();
                
                for(String filePath : cfgFileArray){
                    taskList.add(controller.addFileToTaskList(new File(filePath)));
                }
                
                int nbThreadsTLSVox;
                String multithreadingTLSVoxParam = namedParameters.get("T-TLS_VOX");
                if(multithreadingTLSVoxParam != null){
                    
                    if(multithreadingTLSVoxParam.equals("1C")){
                        nbThreadsTLSVox = Runtime.getRuntime().availableProcessors();
                    }else{
                        try{
                            nbThreadsTLSVox = Integer.valueOf(multithreadingTLSVoxParam);
                        }catch(Exception e){
                            System.err.println("Invalid multithreading value");
                            showHelp();
                            System.exit(0);
                            return;
                        }
                    }
                    
                }else{
                    nbThreadsTLSVox = 1;
                }
                
                controller.setTlsVoxNbThreads(nbThreadsTLSVox);
                
                int nbThreads;
                String multithreadingParam = namedParameters.get("T");
                if(multithreadingParam != null){
                    
                    if(multithreadingParam.equals("1C")){
                        nbThreads = Runtime.getRuntime().availableProcessors();
                    }else{
                        try{
                            nbThreads = Integer.valueOf(multithreadingParam);
                        }catch(Exception e){
                            System.err.println("Invalid multithreading value");
                            showHelp();
                            System.exit(0);
                            return;
                        }
                    }
                    
                    System.out.println("Execute task list in parallel, threads : "+nbThreads);
                    controller.executeTaskListInParallel(taskList, nbThreads);
                }else{
                    System.out.println("Execute task list sequentially");
                    controller.executeTaskListSequentially(taskList);
                }
                
            }else{
                showHelp();
                System.exit(0);
            }
            
        }else{
            
            ObservableList<Screen> screens = Screen.getScreensForRectangle(0, 0, 10, 10);

            if(screens != null && screens.size() > 0){
                stage.setWidth(screens.get(0).getBounds().getWidth());
                stage.setHeight(screens.get(0).getBounds().getHeight());
            }
            
            Scene scene = new Scene(root);
            
            scene.getStylesheets().add("/styles/Styles.css");

            stage.setTitle("AMAPVox");
            stage.setScene(scene);
            stage.getIcons().addAll(
                            new Image("/icons/icon_256x256.png"), 
                            new Image("/icons/icon_128x128.png"),
                            new Image("/icons/icon_64x64.png"));

            controller.setStage(stage);
            
            stage.addEventFilter(WindowEvent.WINDOW_HIDDEN, new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    System.exit(0);
                }
            });

            stage.show();
        }
    }
    
    private static void showHelp(){
        
        System.out.println("\nRun a task list from command line:\n"+
        "--execute-cfg=/home/file1.xml /home/file2.xml /home/file3.xml /home/file4.xml\n"+
                "Execute task list with 1 thread per cpu core : --T=1C\n"+
                "Execute task list with 4 threads : --T=4\n"+
                "Execute TLS voxelization task with 1 thread per cpu core : --T-TLS_VOX=1C\n"+
                "Execute TLS voxelization task with 8 threads : --T-TLS_VOX=8\n");
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
                
        for(String arg : args){
            if(arg.equals("-h")){
                showHelp();
                System.exit(0);
            }
        }
        launch(args);    
    }

}
