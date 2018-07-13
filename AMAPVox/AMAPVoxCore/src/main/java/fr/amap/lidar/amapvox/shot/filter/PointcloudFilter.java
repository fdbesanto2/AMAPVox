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
package fr.amap.lidar.amapvox.shot.filter;

import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.structure.octree.Octree;
import fr.amap.commons.math.point.Point3D;
import fr.amap.commons.util.filter.Filter;
import fr.amap.commons.util.io.file.CSVFile;
import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.lidar.amapvox.util.Util;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class PointcloudFilter implements Filter<Shot.Echo> {

    private final CSVFile pointcloudFile;
    private final float pointcloudErrorMargin;
    private final Behavior behavior;
    private final boolean retain;
    private final Mat4D vop;
    private Octree octree;
    //
     private final static Logger LOGGER = Logger.getLogger(PointcloudFilter.class);

    public PointcloudFilter(CSVFile pointcloudFile, float pointcloudErrorMargin, Behavior behavior, Mat4D vop) {
        this.pointcloudFile = pointcloudFile;
        this.pointcloudErrorMargin = pointcloudErrorMargin;
        this.behavior = behavior;
        retain = behavior.equals(Behavior.RETAIN);
        this.vop = vop;
    }

    public CSVFile getPointcloudFile() {
        return pointcloudFile;
    }

    public float getPointcloudErrorMargin() {
        return pointcloudErrorMargin;
    }

    public Behavior behavior() {
        return behavior;
    }

    @Override
    public void init() throws Exception {
        LOGGER.info("Loading point cloud filter " + pointcloudFile.getAbsolutePath());
        octree = Util.loadOctree(pointcloudFile, vop);
    }

    @Override
    public boolean accept(Shot.Echo echo) throws Exception {

        Point3D location = new Point3D(
                echo.location.x,
                echo.location.y,
                echo.location.z);

        boolean inside = octree.isPointBelongsToPointcloud(location, pointcloudErrorMargin, Octree.INCREMENTAL_SEARCH);
        return retain ? inside : !inside;
    }
}
