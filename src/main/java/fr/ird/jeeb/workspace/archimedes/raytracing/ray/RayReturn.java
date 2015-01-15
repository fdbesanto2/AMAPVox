package fr.ird.jeeb.workspace.archimedes.raytracing.ray;

import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.VoxelManager.ArtNodeVoxelID;

/**
 * Used to return the final intensity and distance, and the last encountered ArtNode
 * @author Cresson, Nov. 2012
 *
 */
public class RayReturn implements java.lang.Comparable {
	public static final RayReturn NO_RETURN		= new RayReturn(RayShot.NO_INTENSITY_RETURNING, Float.NaN, null, -10);
	public static final RayReturn OUT_OF_FOV	= new RayReturn(RayShot.NO_INTENSITY_RETURNING, Float.NaN, null, -20);
	
	public float 			intensity;
	public float			distance;
	public ArtNodeVoxelID	ArtNodeId;
	private int				returnOrder;
	
	public RayReturn(float intensity, float distance, ArtNodeVoxelID id, int returnOrder) {
		this.intensity		= intensity;
		this.distance		= distance;
		this.ArtNodeId		= id;
		this.returnOrder	= returnOrder;
	}

	@Override
	public int compareTo(Object o) {
		if (((RayReturn) o).distance>distance)
			return -1;
		return 1;
	}
	
	public int getReturnOrder() {
		return returnOrder;
	}
	
	public boolean isOutOfFOV() {
		return (returnOrder==-20);
	}

	public boolean isReachingSensor() {
		return (!Float.isNaN(intensity));
	}
}
