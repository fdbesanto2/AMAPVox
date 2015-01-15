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
 * Bare bones 3D spatial vector.
 *
 * @author John E. Lloyd, Winter 2003 */
public class SpatialVector
{
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
	 * Constructs and initializes a SpatialVector to (0,0,0).
	 */
        public SpatialVector ()
	 {
	   this.x = 0;
	   this.y = 0;
	   this.z = 0;
	 }

	/**
	 * Constructs and initializes a SpatialVector to (x,y,z).
	 *
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param z the z coordinate
	 */
        public SpatialVector (double x, double y, double z)
	 {
	   this.x = x;
	   this.y = y;
	   this.z = z;
	 }

	/**
	 * Normalizes this vector so that it has unit length.
	 */
	public void normalize()
	 {
	   double mag = Math.sqrt(x*x + y*y + z*z);
	   x /= mag;
	   y /= mag;
	   z /= mag;
	 }

	/**
	 * Returns the length of this vector.
	 *
	 * @return the length of this vector.
	 */
	public double length()
	 {
	   return Math.sqrt(x*x + y*y + z*z);
	 }

	/**
	 * Returns the squared length of this vector.
	 *
	 * @return the squared length of this vector.
	 */
	public double lengthSquared()
	 {
	   return x*x + y*y + z*z;
	 }

	/**
	 * Returns the dot product of this vector and vector <code>v1</code>.
	 *
	 * @param v1 the other vector.
	 * @return the dot product of this and <code>v1</code>.
	 */
	public double dot (SpatialVector v)
	 {
	   return v.x*x + v.y*y + v.z*z;
	 }
}
