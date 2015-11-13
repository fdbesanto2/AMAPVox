package fr.amap.amapvox.jeeb.raytracing.voxel;

import javax.vecmath.Point3i;

/**
 * VoxelManagerSettings class
 * Container for RayManager initialization parameters
 * @author Cresson, Dec. 2012
 *
 */
public class VoxelManagerSettings {

    public static final int NON_TORIC_FINITE_BOX_TOPOLOGY = 0;
    public static final int TORIC_FINITE_BOX_TOPOLOGY = 1;
    public static final int TORIC_INFINITE_BOX_TOPOLOGY = 2;

    private Point3i splitting;
    private int topology;

    public VoxelManagerSettings(Point3i splitting) {
        this.splitting = splitting;
        this.topology = TORIC_INFINITE_BOX_TOPOLOGY;
    }

    /**
     * Constructor for a simple voxel manager (no user-specified plotBox)
     *
     * @param splitting	number of subdivision in space (i,j,k)
     * @param topology	voxel topology (0: box, 1: infinite space)
     */
    public VoxelManagerSettings(Point3i splitting, int topology) {
        this.splitting = splitting;
        this.topology = topology;
    }

    public Point3i getSplitting() {
        return splitting;
    }

    public int getTopology() {
        return topology;
    }

}
