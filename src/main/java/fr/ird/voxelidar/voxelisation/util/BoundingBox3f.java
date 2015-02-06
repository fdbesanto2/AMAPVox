package fr.ird.voxelidar.voxelisation.util;

import java.io.Serializable;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * Class to create a bounding box
 * 
 *@author GRIFFON Sebastien & STEFAS Mickael
 */
public class BoundingBox3f implements Serializable, GeometricalTopology {
	public Point3d min;
	public Point3d max;

	// **************************************************************************************
	// ********************************CONSTRUCTOR*******************************************
	// **************************************************************************************
	public BoundingBox3f() {
		min = new Point3d(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		max = new Point3d(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
	}
	
	public BoundingBox3f(BoundingBox3d box3d) {
		min = new Point3d (box3d.min);
		max = new Point3d(box3d.max);
	}

	/**
	 * Constructor
	 * 
	 * @param inf
	 *            inferior point that define the box
	 * @param sup
	 *            superior point that define the box
	 */
	public BoundingBox3f(Point3d inf, Point3d sup) {
		min = inf;
		max = sup;
	}

	// **************************************************************************************
	// **********************************GETTERS*********************************************
	// **************************************************************************************
	/**
	 * getter for the inferior point
	 * 
	 * @return Point3d the inferior point
	 */
	public Point3d getMin() {
		return min;
	}

	/**
	 * getter for the superior point
	 * 
	 * @return Point3d the superior point
	 */
	public Point3d getMax() {
		return max;
	}

	/**
	 * method to return the minimum between the main BoundingBox and the given
	 * one
	 * 
	 * @param bb
	 *            the given bounding box.
	 * @return Point3d the minimum of the two bounding box.
	 */
	public Point3d getMin(BoundingBox3f bb) {
		Point3d temp = new Point3d(0, 0, 0);

		if (getMin().x < bb.getMin().x)
			temp.x=(getMin().x);
		else
			temp.x=(bb.getMin().x);

		if (getMin().y < bb.getMin().y)
			temp.y=(getMin().y);
		else
			temp.y=(bb.getMin().y);

		if (getMin().z < bb.getMin().z)
			temp.z=(getMin().z);
		else
			temp.z=(bb.getMin().z);

		return temp;
	}

	/**
	 * method to return the maximum between the main BoundingBox and the given
	 * one
	 * 
	 * @param bb
	 *            the given bounding box.
	 * @return Point3d the maximum point of the two bounding box.
	 */
	public Point3d getMax(BoundingBox3f bb) {
		Point3d temp = new Point3d(0, 0, 0);

		if (getMax().x > bb.getMax().x)
			temp.x=(getMax().x);
		else
			temp.x=(bb.getMax().x);

		if (getMax().y > bb.getMax().y)
			temp.y=(getMax().y);
		else
			temp.y=(bb.getMax().y);

		if (getMax().z > bb.getMax().z)
			temp.z=(getMax().z);
		else
			temp.z=(bb.getMax().z);

		return temp;
	}

	// **************************************************************************************
	// **********************************SETTERS*********************************************
	// **************************************************************************************
	/**
	 * setter for the inferior point
	 * 
	 * @param p
	 *            the new inferior point
	 */
	public void setMin(Point3d p) {
		min = p;
	}

	/**
	 * setter for the superior point
	 * 
	 * @param p
	 *            the new inferior point
	 */
	public void setMax(Point3d p) {
		max = p;
	}

	public void update(Point3d p) {

		min.x=(Math.min(min.x, p.x));
		min.y=(Math.min(min.y, p.y));
		min.z=(Math.min(min.z, p.z));

		max.x=(Math.max(max.x, p.x));
		max.y=(Math.max(max.y, p.y));
		max.z=(Math.max(max.z, p.z));

	}

	// **************************************************************************************
	// **********************************METHODS*********************************************
	// **************************************************************************************
	/**
	 * method used to translate a bounding box
	 * 
	 * @param x
	 * @param y
	 * @param z
	 */
	public void translateBB(double x, double y, double z) {
		getMax().add(new Vector3d(x, y, z));
		getMin().add(new Vector3d(x, y, z));
	}

	/**
	 * method that show in the console the minimum and the maximum bounding box
	 * point.
	 * 
	 */
	public void show() {
		System.out.println(getMin().toString());
		System.out.println(getMax().toString());
	}
	
	public boolean in (Vector3d p){
		if (   p.x > min.x
			&& p.y > min.y
			&& p.z > min.z
			&& p.x < max.x
			&& p.y < max.y
			&& p.z < max.z){
			return true;
		}
		return false;
	}
	public boolean out (Vector3d p){
		if (   p.x < min.x
			|| p.y < min.y
			|| p.z < min.z
			|| p.x > max.x
			|| p.y > max.y
			|| p.z > max.z){
			return true;
		}
		return false;
	}
	public boolean on (Vector3d p){
		if (   p.x == min.x
			|| p.y == min.y
			|| p.z == min.z
			|| p.x == max.x
			|| p.y == max.y
			|| p.z == max.z){
			return true;
		}
		return false;
	}
}
