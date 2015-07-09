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

package fr.amap.amapvox.commons.util;

import java.io.File;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class PointcloudFilter {
    
    private File pointcloudFile;
    private float pointcloudErrorMargin;
    private boolean keep;


    public PointcloudFilter(File pointcloudFile, float pointcloudErrorMargin, boolean keep) {
        this.pointcloudFile = pointcloudFile;
        this.pointcloudErrorMargin = pointcloudErrorMargin;
        this.keep = keep;
    }

    public File getPointcloudFile() {
        return pointcloudFile;
    }

    public void setPointcloudFile(File pointcloudFile) {
        this.pointcloudFile = pointcloudFile;
    }

    public float getPointcloudErrorMargin() {
        return pointcloudErrorMargin;
    }

    public void setPointcloudErrorMargin(float pointcloudErrorMargin) {
        this.pointcloudErrorMargin = pointcloudErrorMargin;
    }

    public boolean isKeep() {
        return keep;
    }

    public void setKeep(boolean keep) {
        this.keep = keep;
    }
    
}
