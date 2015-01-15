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
 * Represents the half-edges that surround each
 * triangular face in a counter-clockwise direction.
 *
 * @author John E. Lloyd, Winter 2003 */
public class HalfEdge
{
	/**
	 * The vertex associated with the head of this half-edge.
	 */
	SpatialPoint vertex;

	/**
	 * Triangular face associated with this half-edge.
	 */
	private Triangle face;

	/**
	 * Next half-edge in the triangle.
	 */
	private HalfEdge next;

	/**
	 * Previous half-edge in the triangle.
	 */
	private HalfEdge prev;

	/**
	 * Half-edge associated with the opposite triangle
	 * adjacent to this edge.
	 */
	private HalfEdge opposite;

	/**
	 * Constructs a HalfEdge with head vertex <code>v</code> and
	 * left-hand triangular face <code>f</code>.
	 *
	 * @param v head vertex
	 * @param f left-hand triangular face
	 */
	public HalfEdge (SpatialPoint v, Triangle f)
	 {
	   vertex = v;
	   face = f;
	 }

	/**
	 * Sets the value of the next edge adjacent
	 * (counter-clockwise) to this one within the triangle.
	 *
	 * @param edge next adjacent edge */
	public void setNext (HalfEdge edge)
	 {
	   next = edge;
	 }
	
	/**
	 * Gets the value of the next edge adjacent
	 * (counter-clockwise) to this one within the triangle.
	 *
	 * @return next adjacent edge */
	public HalfEdge getNext()
	 {
	   return next;
	 }

	/**
	 * Sets the value of the previous edge adjacent (clockwise) to
	 * this one within the triangle.
	 *
	 * @param edge previous adjacent edge */
	public void setPrev (HalfEdge edge)
	 {
	   prev = edge;
	 }
	
	/**
	 * Gets the value of the previous edge adjacent (clockwise) to
	 * this one within the triangle.
	 *
	 * @return previous adjacent edge
	 */
	public HalfEdge getPrev()
	 {
	   return prev;
	 }

	/**
	 * Returns the triangular face located to the left of this
	 * half-edge.
	 *
	 * @return left-hand triangular face
	 */
	public Triangle getFace()
	 {
	   return face;
	 }

	/**
	 * Returns the half-edge opposite to this half-edge.
	 *
	 * @return opposite half-edge
	 */
	public HalfEdge getOpposite()
	 {
	   return opposite;
	 }

	/**
	 * Sets the half-edge opposite to this half-edge.
	 *
	 * @param edge opposite half-edge
	 */
	public void setOpposite (HalfEdge edge)
	 {
	   opposite = edge;
	 }

	/**
	 * Returns the head vertex associated with this half-edge.
	 *
	 * @return head vertex
	 */
	public SpatialPoint head()
	 {
	   return vertex;
	 }

	/**
	 * Returns the tail vertex associated with this half-edge.
	 *
	 * @return tail vertex
	 */
	public SpatialPoint tail()
	 {
	   return prev != null ? prev.vertex : null;
	 }

	/**
	 * Returns the opposite triangular face associated with this
	 * half-edge.
	 *
	 * @return opposite triangular face
	 */
	public Triangle oppositeFace()
	 {
	   return opposite != null ? opposite.face : null;
	 }

	/**
	 * Produces a string identifying this half-edge by the point
	 * index values of its tail and head vertices.
	 *
	 * @return identifying string
	 */
	public String vertexString()
	 {
	   if (tail() != null)
	    { return "" +
		 tail().getPointIndex() + "-" +
		 head().getPointIndex();
	    }
	   else
	    { return "?-" + head().getPointIndex(); 
	    }
	 }
}


