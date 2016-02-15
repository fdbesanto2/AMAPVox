/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

/**
 *
 * @author calcul
 */
public class SceneObjectWrapper extends GridPane{
        
        private SceneObject sceneObject;
        private final Label label;
        private final ProgressBar progressBar;
        
        //properties
        private String name;
        private String path;
        
        private Mat4D transfMatrix;
        

        public SceneObjectWrapper(File file, ProgressBar progressBar) {
            
            this.label = new Label(file.getName());
            this.path = file.getAbsolutePath();
            this.name = file.getName();
            this.progressBar = progressBar;
            
            HBox labelWrapper = new HBox(this.label);
            HBox progressBarWrapper = new HBox(this.progressBar);
            
            labelWrapper.setAlignment(Pos.CENTER_LEFT);
            progressBarWrapper.setAlignment(Pos.CENTER_RIGHT);
            
            this.addColumn(0, labelWrapper);
            this.addColumn(1, progressBarWrapper);
            
            ColumnConstraints columnConstraints1 = new ColumnConstraints();
            columnConstraints1.setPercentWidth(50);
            
            ColumnConstraints columnConstraints2 = new ColumnConstraints();
            columnConstraints1.setPercentWidth(50);
            
            this.getColumnConstraints().addAll(columnConstraints1, columnConstraints2);
            
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
        
    }
