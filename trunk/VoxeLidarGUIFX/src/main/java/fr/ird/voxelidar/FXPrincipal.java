/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar;

import fr.ird.voxelidar.gui.MainFrameController;
import fr.ird.voxelidar.update.Updater;
import java.nio.charset.Charset;
import java.util.SortedMap;
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
        Quaternion quat = new Quaternion();
        quat.setIdentity();
        float[] resultMatrix = new float[16];
        quat.toMatrix(resultMatrix, 0);
        BufferedImage image = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        int colorRed = new Color(255, 0, 0).getRGB();
        Vec3F pivot = new Vec3F(0, 0, 0);
        Vec3F location = new Vec3F(1, 0, 0);
        float r = 1;
        Vec3F viewXAxis;
        Vec3F viewYAxis;
        Vec3F viewZAxis;
        Vec3F viewUpAxis = new Vec3F(0, 0, 1);
        Vec3F orientation;
        for(int i=0;i<100;i++){
        orientation = Vec3F.substract(pivot, location);
        float angle = -i/100.0f;
        float angleSinus = (float)Math.sin(angle);
        float angleCosinus = (float)Math.cos(angle);
        viewXAxis = Vec3F.normalize(orientation);
        viewYAxis = Vec3F.cross(orientation, viewUpAxis);
        viewZAxis = Vec3F.cross(viewYAxis, viewXAxis);
        viewXAxis = Vec3F.normalize(viewXAxis);
        viewYAxis = Vec3F.normalize(viewYAxis);
        viewZAxis = Vec3F.normalize(viewZAxis);
        //float x = pivot.x + (-viewXAxis.x * r * angleCosinus) + (viewYAxis.x * r * angleSinus);
        //float y = pivot.y + (-viewXAxis.y * r * angleCosinus) + (viewYAxis.y * r * angleSinus);
        //float z = pivot.z + (-viewXAxis.z * r * angleCosinus) + (viewYAxis.z * r * angleSinus);
        float x = pivot.x + (-viewXAxis.x * r * angleCosinus) + (viewZAxis.x * r * angleSinus);
        float y = pivot.y + (-viewXAxis.y * r * angleCosinus) + (viewZAxis.y * r * angleSinus);
        float z = pivot.z + (-viewXAxis.z * r * angleCosinus) + (viewZAxis.z * r * angleSinus);
        int indiceX = (((int)(x*500))/2)+249;
        int indiceZ = (((int)(z*500))/2)+249;
        System.out.println(indiceX + " "+ indiceZ);
        image.setRGB(indiceX, indiceZ, colorRed);
        location = new Vec3F(x, y, z);
        }
        ImageIO.write(image, "png", new File("/home/calcul/Documents/Julien/test.png"));*/
        
        
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
