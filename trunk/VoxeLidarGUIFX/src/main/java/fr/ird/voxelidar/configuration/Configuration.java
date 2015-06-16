/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package fr.ird.voxelidar.configuration;

import java.io.File;

/**
 *
 * @author calcul
 */


public abstract class Configuration {
    
    protected ProcessMode processMode = ProcessMode.VOXELISATION_ALS;
    protected InputType inputType = InputType.LAS_FILE;
    
    public enum ProcessMode{
        
        VOXELISATION_ALS(0),
        VOXELISATION_TLS(1),
        MERGING(2),
        MULTI_VOXELISATION_ALS_AND_MULTI_RES(3),
        MULTI_RES(4),
        ;
        
        public final int mode;
        
        private ProcessMode(int mode){
            this.mode = mode;
        }
    }
    
    public enum InputType{
        
        LAS_FILE(0),
        LAZ_FILE(1),
        POINTS_FILE(2),
        SHOTS_FILE(3),
        RXP_SCAN(4),
        RSP_PROJECT(5);
        
        public int type;
        
        private InputType(int type){
            this.type = type;
        }
    }
    
    public abstract void readConfiguration(File inputParametersFile);
    public abstract void writeConfiguration(File outputParametersFile);
}
