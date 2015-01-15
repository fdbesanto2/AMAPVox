/**
 * 
 */
package fr.ird.jeeb.workspace.archimedes.geometry;

import javax.vecmath.Vector3f;

/**
 * information about intersection (distance and normal at the intersection)
 * @author DAUZAT/Cresson; August 2012
 */
public class Intersection implements java.lang.Comparable{

	private Vector3f	normal;
	public	float		distance;

	public Intersection (float distance, Vector3f normal){
		this.distance	= distance;
		this.normal		= normal;
	}
	
	public float getDistance () {
		return distance;
	}
	
	public Vector3f getNormal () {
		return normal;
	}

	@Override
	public int compareTo(Object o) {
		if (((Intersection) o).distance>distance)
			return -1;
		return 1;
	}

	
}
