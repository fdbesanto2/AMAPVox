/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

/**
 *
 * @author calcul
 */
public abstract class TaskAdapter implements TaskListener{
    
    @Override
    public void onSucceeded(){}
    
    @Override
    public void onCancelled(){}
    
    @Override
    public void onFailed(Exception ex){}
}