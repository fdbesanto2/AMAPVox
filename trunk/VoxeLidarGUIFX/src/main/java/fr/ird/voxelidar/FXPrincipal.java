/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar;

import fr.ird.voxelidar.gui.MainFrameController;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.Voxel;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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
        //génération d'un fichier voxel test
        
        Voxel voxels[][][] = new Voxel[100][100][50];
        
        
        
        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/home/calcul/Documents/Julien/test2.vox")));
            
            writer.write("VOXEL SPACE" + "\n");
            writer.write("#min_corner: " + 0 + " " + 0 + " " + 0 + "\n");
            writer.write("#max_corner: " + 100 + " " + 100 + " " + 50 + "\n");
            writer.write("#split: " + 100 + " " + 100 + " " + 50 + "\n");

            String metadata = "";
            String type = "";
            
            metadata += "#res: "+"1"+" ";
            metadata += "#MAX_PAD: "+"5";
            
            type += "#type: " +"ALS"+ " ";
            type += metadata+"\n";
            writer.write(type);

            writer.write(Voxel.getHeader(Voxel.class) + "\n");

            for (int x = 0; x < 100; x++) {
                for (int y = 0; y < 100; y++) {
                    for (int z = 0; z < 50; z++) {

                        if(x>70 && x<80 && y>30 && y < 90){
                            voxels[x][y][z] = new Voxel(x, y, z);
                            voxels[x][y][z].PadBVTotal = 5;
                        }else{
                            voxels[x][y][z] = new Voxel(x, y, z);
                            voxels[x][y][z].PadBVTotal = 0;
                        }

                        voxels[x][y][z].ground_distance = z;
                        writer.write(voxels[x][y][z].toString() + "\n");
                    }
                }
            }

            writer.close();

        } catch (FileNotFoundException e) {
            
        } catch (Exception e) {
            
        }*/
        
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
