package fr.amap.lidar.amapvox.gui;

import javafx.scene.Parent;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Julien Heurtebize
 */
public class FxmlContext<T>{
        
    private final T controller;
    private final Parent root;

    public FxmlContext(T controller, Parent root) {
        this.controller = controller;
        this.root = root;
    }

    public T getController() {
        return controller;
    }

    public Parent getRoot() {
        return root;
    }
}