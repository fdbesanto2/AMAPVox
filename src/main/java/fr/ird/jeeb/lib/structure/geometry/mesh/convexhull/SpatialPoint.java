 /*
  * Copyright John E. Lloyd, 2003. All rights reserved. Permission
  * to use, copy, and modify, without fee, is granted for non-commercial 
  * and research purposes, provided that this copyright notice appears 
  * in all copies.
  *
  * This  software is distributed "as is", without any warranty, including 
  * any implied warranty of merchantability or fitness for a particular
  * use. The authors assume no responsibility for, and shall not be liable
  * for, any special, indirect, or consequential damages, or any damages
  * whatsoever, arising out of or in connection with the use of this
  * software.
  */

package fr.ird.jeeb.lib.structure.geometry.mesh.convexhull;

/**
 * A 3D spatial point, used
 * to represent both input points and hull vertices.
 *
 * @author John E. Lloyd, Winter 2003 */
public class SpatialPoint
{
	private int index;
	private int hullFaceIndex;
	private int mergedFaceIndex;

	/**
	 * The x coordinate.
	 */
	public double x;
	/**
	 * The y coordinate.
	 */
	public double y;
	/**
	 * The z coordinate.
	 */
	public double z;

	/**
	 * Constructs a SpatialPoint with the coordinates (x,y,z) and
	 * a point index given by <code>idx</code>.
	 * The point index should correspond to the location
	 * of the point within the input data.
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 * @param idx the point index 
	 */
	public SpatialPoint (double x, double y, double z, int idx)
	 {
	   this.x = x;
	   this.y = y;
	   this.z = z;
	   index = idx;
	 }

	/**
	 * Returns the value of the i-th coordinate of the point.
	 * 
	 * @param i coordinate index (with 0,1,2 corresponding to x,y,z).
	 * @return coordinate value
	 */
	public double getCoord (int i)
	 {
	   switch (i)
	    { case 0:
	       { return x;
	       }
	      case 1:
	       { return y;
	       }
	      case 2:
	       { return z;
	       }
	      default:
	       { throw new IndexOutOfBoundsException ("" + i);
	       }		 
	    }
	 }

	/**
	 * Sets the point index for this point. Not currently used.
	 *
	 * @param idx new point index
	 */
	public void setPointIndex (int idx)
	 {
	   index = idx;
	 }

	/**
	 * Gets the point index for this point. This is nominally the
	 * location of the point within the input point set.
	 *
	 * @return point index */
	public int getPointIndex ()
	 {
	   return index;
	 }

	/**
	 * Sets the hull face index for this point.
	 *
	 * @param idx new hull face index
	 * @see SpatialPoint#getHullFaceIndex
	 */
	public void setHullFaceIndex (int idx)
	 {
	   hullFaceIndex = idx;
	 }

	/**
	 * Gets the hull face index for this point.
	 *
	 * The hull face index is the location of the point
	 * within the hull face vertex list (i.e., the list returned by
	 * {@link ConvexHull3D#getHullFaceVertices
	 * ConvexHull3D.getHullFaceVertices()}. If this point is not
	 * associated with a hull face vertex, then -1 is returned.	 
	 *
	 * @return hull face index
	 * @see ConvexHull3D#getHullFaceVertices
	 */
	public int getHullFaceIndex ()
	 {
	   return hullFaceIndex;
	 }

	/**
	 * Sets the merged face index for this point.
	 *
	 * @param idx new merged face index
	 * @see SpatialPoint#getMergedFaceIndex
	 */
	public void setMergedFaceIndex (int idx)
	 {
	   mergedFaceIndex = idx;
	 }

	/**
	 * Gets the merged face index for this point.
	 *
	 * The merged face index is the location of the point
	 * within the merged face vertex list (i.e., the list returned by
	 * {@link ConvexHull3D#getMergedFaceVertices
	 * ConvexHull3D.getMergedFaceVertices()}. If this point is not
	 * associated with a merged face vertex, then -1 is returned.
	 *
	 * @return merged face index
	 * @see ConvexHull3D#getMergedFaceVertices
	 */
	public int getMergedFaceIndex ()
	 {
	   return mergedFaceIndex;
	 }

	/**
	 * Returns true if this point equals another point.
	 *
	 * @param pnt the other point
	 * @return true if the points are equal
	 */
	public boolean equals (SpatialPoint pnt)
	 { 
	   return (pnt.x == x && pnt.y == y && pnt.z == z);
	 }

	/**
	 * Returns true if this point equals another point
	 * within a prescribed tolerance.
	 *
	 * @param pnt the other point
	 * @param epsilon tolerance value
	 * @return true if the points are equal
	 */
	public boolean epsilonEquals (SpatialPoint pnt, double epsilon)
	 { 
	   return (Math.abs(pnt.x-x) <= epsilon &&
		   Math.abs(pnt.y-y) <= epsilon &&
		   Math.abs(pnt.z-z) <= epsilon);
	 }


	public String toString()
	 {
	   return "(" + x + ", " + y + ", " + z + ")";
	 }
}
