/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.als.las;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class LasToTxtAdapter implements LasToTxtListener{

    @Override
    public void LasToTxtProgress(int progress) {}
    
    @Override
    public void LasToTxtFinished(){}
    
}
