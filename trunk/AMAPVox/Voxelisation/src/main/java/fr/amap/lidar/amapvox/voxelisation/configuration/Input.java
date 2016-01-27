
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

package fr.amap.lidar.amapvox.voxelisation.configuration;

import java.io.File;
import java.util.List;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class Input {
    
    public VoxelParameters voxelParameters;
    public File inputFile;
    public File dtmFile;
    public File outputFile;
    public List<Input> multiResList;
    public File outputFileMultiRes;

    public Input(VoxelParameters voxelParameters, File inputFile, File dtmFile, File outputFile, List<Input> multiResList, File outputFileMultiRes) {
        this.voxelParameters = voxelParameters;
        this.inputFile = inputFile;
        this.dtmFile = dtmFile;
        this.outputFile = outputFile;
        this.multiResList = multiResList;
        this.outputFileMultiRes = outputFileMultiRes;
    }
    
}
