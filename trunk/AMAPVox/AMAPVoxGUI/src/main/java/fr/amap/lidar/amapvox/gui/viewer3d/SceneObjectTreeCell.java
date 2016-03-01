/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.viewer3d;

import fr.amap.lidar.amapvox.gui.SceneObjectWrapper;
import javafx.scene.control.TreeCell;

/**
 *
 * @author calcul
 */
public class SceneObjectTreeCell extends TreeCell<SceneObjectWrapper>{
    
    public SceneObjectTreeCell() {
        
    }
    
    @Override
        public void updateItem(SceneObjectWrapper item, boolean empty) {
            
            super.updateItem(item, empty);
            if (empty) {
                setText(null);
                setGraphic(null);
            } else {
                if(item == null){
                    setText("Scene");
                    setGraphic(null);
                }else{
                    setGraphic(item);
                    setDisable(empty);
                    setText("");
                }
            }
        }
}
