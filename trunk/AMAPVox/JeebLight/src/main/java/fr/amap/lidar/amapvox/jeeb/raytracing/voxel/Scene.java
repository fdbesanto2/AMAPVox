package fr.amap.lidar.amapvox.jeeb.raytracing.voxel;

import fr.amap.lidar.amapvox.jeeb.raytracing.util.BoundingBox3d;


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
	private BoundingBox3d bbox = null;
	
	public BoundingBox3d getBoundingBox() {
		// added Dauzat Feb. 2013
                return bbox;
	}
	
	// added Dauzat, Feb. 2013
	public void setBoundingBox(BoundingBox3d boundingBox) {
		bbox = new BoundingBox3d(boundingBox.getMin(), boundingBox.getMax());
	}

}