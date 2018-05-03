/**
 * 
 */
package fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel;


import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.util.BoundingBox3d;
import javax.vecmath.Point3i;

import javax.vecmath.Point3d;


/**
 * 
 * @author DAUZAT/Cresson sept.2012
 *
 */
public class VoxelSpace {

    private BoundingBox3d boundingBox;
    private Point3d boundingBoxSize;
    private Point3d voxelSize;
    private Point3i splitting;
    private boolean toric = false;
    private boolean finite = false;
    private int topology;

    public VoxelSpace(BoundingBox3d bb, Point3i splitting, int topology) {

        this.boundingBox = bb;
        this.boundingBoxSize = new Point3d(bb.max);
        this.boundingBoxSize.sub(bb.min);
        this.splitting = splitting;
        this.voxelSize = new Point3d(
                boundingBoxSize.x / splitting.x,
                boundingBoxSize.y / splitting.y,
                boundingBoxSize.z / splitting.z);
        setTopology(topology);
    }

    public VoxelSpace(BoundingBox3d bb, int numberOfVoxels, int topology) {

        this.boundingBox = bb;
        this.boundingBoxSize = new Point3d(bb.max);
        this.boundingBoxSize.sub(bb.min);
        setDefaultSpliting(numberOfVoxels);
        this.voxelSize = new Point3d(
                boundingBoxSize.x / splitting.x,
                boundingBoxSize.y / splitting.y,
                boundingBoxSize.z / splitting.z);
        setTopology(topology);
    }

    private void setTopology(int topology) {
        this.topology = topology;
        switch (topology) {
            case VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY:
                toric = false;
                finite = true;
                break;
            case VoxelManagerSettings.TORIC_FINITE_BOX_TOPOLOGY:
                toric = true;
                finite = false;
                break;
            case VoxelManagerSettings.TORIC_INFINITE_BOX_TOPOLOGY:
                toric = true;
                finite = false;
                break;
        }
    }

    public void setDefaultSpliting(int numberOfVoxels) {

        double volumeVoxel = (boundingBoxSize.x * boundingBoxSize.y * boundingBoxSize.z) / numberOfVoxels;
        double edge = Math.cbrt(volumeVoxel);		// average voxel edge

        int nVx = (int) ((boundingBoxSize.x / edge) + 0.5);
        int nVy = (int) ((boundingBoxSize.y / edge) + 0.5);
        int nVz = (int) ((boundingBoxSize.z / edge) + 0.5);

        splitting = new Point3i(nVx, nVy, nVz);
    }

    /**
     * Return scene indices of a given point
     *
     * @param point 3d point
     * @return scene indices
     */
    public Point3i getSceneIndices(Point3d point) {
        Point3d pt = new Point3d(point);
        pt.sub(boundingBox.getMin());

        double x = pt.x / boundingBoxSize.x;
        double y = pt.y / boundingBoxSize.y;

        return new Point3i((int) Math.floor(x), (int) Math.floor(y), 0);
    }

    /**
     * @param point 3d point
     * @return the x, y and z indices of the voxel including the point under the
     * Point3i format or null if the point is outside the voxel space except if
     * the voxel space is toric. In such case the returned index is modulo the
     * number of splitting along each coordinate
     */
    public Point3i getVoxelIndices(Point3d point) {

        // shift to scene Min
        Point3d pt = new Point3d(point);
        pt.sub(boundingBox.getMin());

        if ((pt.z < 0) || (pt.z >= boundingBoxSize.z)) {

            return null;

                    //System.out.println("deltaZ:"+Math.abs(pt.z-boundingBoxSize.z));
        }

        if (toric == false) {
            if ((pt.x < 0) || (pt.x >= boundingBoxSize.x)) {

                return null;
                //System.out.println("deltaC:"+Math.abs(pt.x-boundingBoxSize.x));

            }
            if ((pt.y < 0) || (pt.y >= boundingBoxSize.y)) {

                return null;
                //System.out.println("deltaY:"+Math.abs(pt.y-boundingBoxSize.y));

            }
        }
        pt.x /= voxelSize.x;
        pt.y /= voxelSize.y;
        pt.z /= voxelSize.z;

        Point3i indices = new Point3i();

        // voxel indexes
        indices.x = (int) Math.floor((double) (pt.x % splitting.x));
        if (indices.x < 0) {
            indices.x += splitting.x;
        }
        indices.y = (int) Math.floor((double) (pt.y % splitting.y));
        if (indices.y < 0) {
            indices.y += splitting.y;
        }
        indices.z = (int) Math.min(pt.z, splitting.z - 1);

        return indices;

    }

    private Point3d recoverPoint(Point3d point) {
        Point3d recovered = new Point3d(point);

        recovered.sub(boundingBox.getMin());
        if (recovered.x <= 0) {
            recovered.x = (+voxelSize.x / 2);
        } else if (recovered.x >= boundingBoxSize.x) {
            recovered.x = (boundingBoxSize.x - voxelSize.x / 2);
        }
        if (recovered.y <= 0) {
            recovered.y = (+voxelSize.y / 2);
        } else if (recovered.y >= boundingBoxSize.y) {
            recovered.y = (boundingBoxSize.y - voxelSize.y / 2);
        }

        recovered.add(boundingBox.getMin());

        return recovered;
    }

    /**
     * @param voxel	specified voxel
     * @return the inf corner of the specified voxel
     */
    public Point3d getVoxelInfCorner(Point3i voxel) {
        Point3d corner = new Point3d(voxel.x * voxelSize.x, voxel.y * voxelSize.y, voxel.z * voxelSize.z);
        corner.add(boundingBox.getMin());
        return corner;
    }

    /**
     * @param voxel	specified voxel
     * @return the sup corner of the specified voxel
     */
    public Point3d getVoxelSupCorner(Point3i voxel) {
        Point3d corner = new Point3d((voxel.x + 1) * voxelSize.x, (voxel.y + 1) * voxelSize.y, (voxel.z + 1) * voxelSize.z);
        corner.add(boundingBox.getMin());
        return corner;
    }

    public Point3d getRelativePoint(Point3d point) {
        Point3d relativePoint = new Point3d(point);
        relativePoint.sub(boundingBox.getMin());
        relativePoint.x = (int) Math.floor((double) (relativePoint.x % boundingBoxSize.x));
        if (relativePoint.x < 0) {
            relativePoint.x += boundingBoxSize.x;
        }
        relativePoint.y = (int) Math.floor((double) (relativePoint.y % boundingBoxSize.y));
        if (relativePoint.y < 0) {
            relativePoint.y += boundingBoxSize.y;
        }
        relativePoint.add(boundingBox.getMin());
        return relativePoint;
    }

    /**
     * Get the bounding box of the voxel space
     *
     * @return bounding box of the voxel space
     */
    public BoundingBox3d getBoundingBox() {
        return boundingBox;
    }

    /**
     * Get the splitting of the voxel space
     *
     * @return splitting of the voxel space
     */
    public Point3i getSplitting() {
        return splitting;
    }

    /**
     * Get the toricity of the voxel space
     *
     * @return toricity
     */
    public boolean isToric() {
        return toric;
    }

    /**
     * Returns if the voxelspace is finite
     *
     * @return finite
     */
    public boolean isFinite() {
        return finite;
    }

    /**
     * Get bounding box size
     *
     * @return bounding box size
     */
    public Point3d getBoundingBoxSize() {
        return this.boundingBoxSize;
    }

    /**
     * Get voxel space
     *
     * @return voxel size
     */
    public Point3d getVoxelSize() {
        return voxelSize;
    }

    public int getTopology() {
        return topology;
    }

    public static void main(String[] args) {

        VoxelSpace voxelSpace = new VoxelSpace(
                new BoundingBox3d(new Point3d(-20, -20, -20), new Point3d(0, 0, 0)),
                new Point3i(20, 20, 20), VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY);

        Point3i indice1 = voxelSpace.getVoxelIndices(new Point3d(0, 0, 0));
            //Point3i indice2 = voxelSpace.getVoxelIndices(new Point3d(20, 20, 19));

        System.out.println("test");

    }
}
