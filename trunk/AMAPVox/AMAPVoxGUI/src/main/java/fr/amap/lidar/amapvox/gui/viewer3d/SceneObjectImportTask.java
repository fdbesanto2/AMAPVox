/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.viewer3d;

import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import java.io.File;
import javafx.concurrent.Task;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public abstract class SceneObjectImportTask extends Task<SceneObject>{

    protected final static Logger LOGGER = Logger.getLogger(SceneObjectImportTask.class);
    
    protected File file;
    protected Mat4D transfMatrix;
    
    public SceneObjectImportTask(File file){
        this.file = file;
        this.transfMatrix = Mat4D.identity();
    }
    
    public abstract void showImportFrame(Stage stage) throws Exception;
    
    @Override
    protected abstract SceneObject call() throws Exception;

    public Mat4D getTransfMatrix() {
        return transfMatrix;
    }
}
