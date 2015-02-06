package fr.ird.voxelidar.voxelisation.raytracing.voxel;

import fr.ird.voxelidar.voxelisation.util.BoundingBox3f;

/**
 * Scene class
 * Contains artNodes
 * Can be used to store light values in the nodes
 * 
 * @author Cresson, Nov 2012
 *
 */
public class Scene {
	
	//TODO added Dauzat Feb 2013
	private BoundingBox3f bbox = null;
	
	/**
	 * Computes the global bounding box of artNodes
	 */
	public BoundingBox3f getBoundingBox() {
		// added Dauzat Feb. 2013
                return bbox;
	}
	
	// added Dauzat, Feb. 2013
	public void setBoundingBox(BoundingBox3f boundingBox) {
		bbox = new BoundingBox3f(boundingBox.getMin(), boundingBox.getMax());
	}

}