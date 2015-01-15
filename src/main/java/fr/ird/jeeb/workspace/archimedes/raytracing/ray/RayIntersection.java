package fr.ird.jeeb.workspace.archimedes.raytracing.ray;

import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;
import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.VoxelManager.ArtNodeVoxelID;

/**
 * RayIntersection class
 * Object which contains an intersection, and the associated target identifier
 * @author Cresson, Oct. 2012
 *
 */
public class RayIntersection implements Cloneable {
	public	ArtNodeVoxelID				artNodeVoxelID;
	public	Intersection				intersection;
	
	/**
	 * Characterize the intersection between a ray and a node
	 * @param artNodeVoxelID		node identifier (node id, shape id) which characterize the encountered object
	 * @param intersection			intersection (distance, normal)
	 */
	public RayIntersection(ArtNodeVoxelID artNodeVoxelID, Intersection intersection) {
		this.artNodeVoxelID		= artNodeVoxelID;
		this.intersection		= intersection;
	}
	
	/**
	 * Cloning
	 */
	public Object clone() {
		RayIntersection rayInter = null;
		try {
			rayInter = (RayIntersection) super.clone();
		} catch (CloneNotSupportedException cnse) {
			System.err.println(System.err+"/"+cnse.getMessage ());
		}
		rayInter.artNodeVoxelID = (ArtNodeVoxelID) artNodeVoxelID.clone();
		rayInter.intersection = new Intersection(
				intersection.getDistance(),
				new Vector3f(intersection.getNormal()));
		return rayInter;
	}
	
}