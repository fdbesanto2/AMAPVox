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

package fr.amap.amapvox.voxelisation;

import fr.amap.amapvox.datastructure.octree.Octree;
import fr.amap.amapvox.math.point.Point3D;
import fr.amap.amapvox.math.point.Point3F;
import java.io.File;
import javax.vecmath.Point3d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class PointcloudFilter {
    
    private File pointcloudFile;
    private float pointcloudErrorMargin;
    private boolean keep;
    private Octree octree;


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

    public Octree getOctree() {
        return octree;
    }

    public void setOctree(Octree octree) {
        this.octree = octree;
    }
    
    public boolean doFiltering(Point3d point){
        
        boolean test;

        test = octree.isPointBelongsToPointcloud(new Point3D(point.x, point.y, point.z), pointcloudErrorMargin, Octree.INCREMENTAL_SEARCH);
        
        if(keep){
            return test;
        }else{
            return !test;
        }
    }
}
