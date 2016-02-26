/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import java.io.File;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author calcul
 */
public class SceneObjectWrapper extends VBox{
        
        private SceneObject sceneObject;
        private final Label label;
        private final ProgressBar progressBar;
        private final CheckBox checkbox;
        private final HBox hbox;
        
        //properties
        private final String name;
        private final String path;
        
        private Mat4D transfMatrix;
        

        public SceneObjectWrapper(File file, ProgressBar progressBar) {
            
            label = new Label(file.getName());
            path = file.getAbsolutePath();
            name = file.getName();
            this.progressBar = progressBar;
            checkbox = new CheckBox();
            checkbox.setSelected(true);
            hbox = new HBox();
            hbox.getChildren().add(checkbox);
            hbox.getChildren().add(label);
            
            super.setSpacing(5.0);
            super.getChildren().add(hbox);
            super.getChildren().add(this.progressBar);
            
            //addColumn(0, labelWrapper);
            //addColumn(1, progressBarWrapper);
            
            /*ColumnConstraints columnConstraints1 = new ColumnConstraints();
            columnConstraints1.setPercentWidth(50);
            
            ColumnConstraints columnConstraints2 = new ColumnConstraints();
            columnConstraints1.setPercentWidth(50);
            
            this.getColumnConstraints().addAll(columnConstraints1, columnConstraints2);*/
            
            //this.prefWidthProperty().bind(listviewTreeSceneObjects.widthProperty());
        }

        public Label getLabel() {
            return label;
        }

        public ProgressBar getProgressBar() {
            return progressBar;
        }

        public SceneObject getSceneObject() {
            return sceneObject;
        }

        public void setSceneObject(SceneObject sceneObject) {
            this.sceneObject = sceneObject;
        }

        public Mat4D getTransfMatrix() {
            return transfMatrix;
        }

        public void setTransfMatrix(Mat4D transfMatrix) {
            this.transfMatrix = transfMatrix;
        }
        
        public boolean isSelected(){
            return checkbox.isSelected();
        }
        
        public void setSelected(boolean selected){
            checkbox.setSelected(selected);
        }
        
    }
