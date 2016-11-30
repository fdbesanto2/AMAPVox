package fr.amap.lidar.amapvox.jeeb.archimed.raytracing.util;

import java.io.Serializable;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Class to create a bounding box
 * 
 * @author GRIFFON Sebastien and STEFAS Mickael
 */
public class BoundingBox3d implements Serializable {
	public Point3d min;
	public Point3d max;

	// **************************************************************************************
	// ********************************CONSTRUCTOR*******************************************
	// **************************************************************************************
	public BoundingBox3d() {
		min = new Point3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
//		max = new Point3d(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
		max = new Point3d(-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);
	}

	/**
	 * Constructor
	 * 
	 * @param inf
	 *            inferior point that define the box
	 * @param sup
	 *            superior point that define the box
	 */
	public BoundingBox3d(Point3d inf, Point3d sup) {
		min = inf;
		max = sup;
	}

	// **************************************************************************************
	// **********************************GETTERS*********************************************
	// **************************************************************************************
	/**
	 * getter for the inferior point
	 * 
	 * @return Point3D the inferior point
	 */
	public Point3d getMin() {
		return min;
	}

	/**
	 * getter for the superior point
	 * 
	 * @return Point3D the superior point
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
	 * @return Point3D the minimum of the two bounding box.
	 */
	public Point3d getMin(BoundingBox3d bb) {
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
	 * @return Point3D the maximum point of the two bounding box.
	 */
	public Point3d getMax(BoundingBox3d bb) {
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
	 * @param x x
	 * @param y y
	 * @param z z
	 */
	public void translateBB(float x, float y, float z) {
		getMax().add(new Vector3d(x, y, z));
		getMin().add(new Vector3d(x, y, z));
	}
        
        public boolean contains(Point3d point3d){
            
            return (point3d.x >= min.x && point3d.x <= max.x && 
                    point3d.y >= min.y && point3d.y <= max.y && 
                    point3d.z >= min.z && point3d.z <= max.z );
        }

	/**
	 * method that show in the console the minimum and the maximum bounding box
	 * point.
	 * 
	 */
	public void show() {
		System.out.println(toString ());		
	}
	
	@Override
	public String toString () {
		return "Min =\t" + getMin().toString() + "\nMax =\t" + getMax().toString();
		
	}
}
