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
 * Basic triangular face used to form the hull.
 *
 * <p>The information stored for each triangle consists of a planar
 * normal, a planar offset, and a doubly-linked list of three <a
 * href=HalfEdge>HalfEdges</a> which surround the triangle in a
 * counter-clockwise direction.
 *
 * @author John E. Lloyd, Winter 2003 */
public class Triangle
{
	private HalfEdge[] edges;
	private SpatialVector normal;
	private double offset;

	/**
	 * The triangle is not marked.
	 */
	static public final int NO_MARK = 0;
	/**
	 * Triangle is marked as visible to a particular point.
	 */
	static public final int VISIBLE = 1;

	/**
	 * Triangle is marked as coplanar (i.e., marginally visible)
	 * with respect to a particular point.
	 */
	static public final int COPLANAR = 2;

	private int mark = NO_MARK;

	/**
	 * Computes the cross product of the vectors formed from
	 * (p1-p0) and (p2-p0) and stores this in the vector v.
	 * The length of v is then the area of the triangle
	 * formed by p0, p1, p2.
	 *
	 * @param v used to return the cross product
	 * @param p0 first point
	 * @param p1 second point
	 * @param p2 third point
	 */
	static public void areaVector (
	   SpatialVector v, SpatialPoint p0, SpatialPoint p1, SpatialPoint p2)
	 {
	   double u1x = p1.x - p0.x;
	   double u1y = p1.y - p0.y;
	   double u1z = p1.z - p0.z;

	   double u2x = p2.x - p0.x;
	   double u2y = p2.y - p0.y;
	   double u2z = p2.z - p0.z;

	   v.x = u1y*u2z - u1z*u2y;
	   v.y = u1z*u2x - u1x*u2z;
	   v.z = u1x*u2y - u1y*u2x;
	 }

	/**
	 * Computes the cross product of the vectors formed from
	 * (p1-p0) and (p2-p0), and then takes the dot product of
	 * this with the vector v.
	 *
	 * @param p0 first point
	 * @param p1 second point
	 * @param p2 third point
	 * @param v used to form dot product
	 * @return [(p1-p0) X (p2-p0)] . v
	 */
	static public double areaVectorDot (
	   SpatialPoint p0, SpatialPoint p1, SpatialPoint p2, SpatialVector v)
	 {
	   double u1x = p1.x - p0.x;
	   double u1y = p1.y - p0.y;
	   double u1z = p1.z - p0.z;

	   double u2x = p2.x - p0.x;
	   double u2y = p2.y - p0.y;
	   double u2z = p2.z - p0.z;

	   return ((u1y*u2z - u1z*u2y)*v.x +
		   (u1z*u2x - u1x*u2z)*v.y +
		   (u1x*u2y - u1y*u2x)*v.z);
	 }

	/**
	 * Constructs a Triangle from points p0, p1, and p2.
	 *
	 * @param p0 first point
	 * @param p1 second point
	 * @param p2 third point
	 */
	public Triangle (SpatialPoint p0, SpatialPoint p1, SpatialPoint p2)
	 {
	   edges = new HalfEdge[3];
	   edges[0] = new HalfEdge (p0, this);
	   edges[1] = new HalfEdge (p1, this);
	   edges[2] = new HalfEdge (p2, this);

	   // link up the edges
	   for (int i=0; i<3; i++)
	    { edges[i].setNext (edges[(i+1)%3]);
	      edges[i].setPrev (edges[(i+2)%3]);
	    }

	   // compute the normal and offset
	   normal = new SpatialVector();
	   areaVector (normal, p0, p1, p2);
	   normal.normalize();
	   offset = -(normal.x*p0.x + normal.y*p0.y + normal.z*p0.z);
	 }

	/**
	 * Sets the marking associated with this triangle.
	 *
	 * @param m the mark value
	 */
	public void setMark (int m)
	 { mark = m;
	 }

	/**
	 * Gets the marking associated with this triangle.
	 *
	 * @return mark value
	 */
	public int getMark()
	 { return mark;
	 }

	/**
	 * Gets the i-th half-edge associated with the triangle.
	 * 
	 * @param i the half-edge index, in the range 0-2.
	 * @return the half-edge
	 */
	public HalfEdge getEdge(int i)
	 {
	   return edges[i];
	 }
	
	/**
	 * Gets the next half-edge, adjacent to edge0 in a
	 * counter-clockwise direction, which lies
	 * along the boundary of a region of triangles marked
	 * {@link #VISIBLE VISIBLE} or {@link #COPLANAR COPLANAR}
	 *
	 * @param edge0 original edge
	 * @return adjacent half-edge
	 */
	static public HalfEdge nextBoundaryEdge (HalfEdge edge0)
	 {
	   HalfEdge edge = edge0.getNext();
	   Triangle f = edge.oppositeFace();
	   while (f != null && f.mark != NO_MARK)
	    { edge = edge.getOpposite().getNext();
	      f = edge.oppositeFace();
	    }
	   return edge;
	 }

	/**
	 * Gets the next half-edge, adjacent to edge0 in a
	 * counter-clockwise direction, which lies along the boundary
	 * of a region of triangles marked with the value <code>mark</code>.
	 *
	 * @param edge0 original edge
	 * @param mark marking value for the region
	 * @return adjacent half-edge
	 */
	static public HalfEdge nextBoundaryEdge (HalfEdge edge0, int mark)
	 {
	   HalfEdge edge = edge0.getNext();
	   Triangle f = edge.oppositeFace();
	   while (f != null && f.mark == mark)
	    { edge = edge.getOpposite().getNext();
	      f = edge.oppositeFace();
	    }
	   return edge;
	 }

	/**
	 * Gets the next half-edge, adjacent to edge0 in a clockwise
	 * direction, which lies along the boundary of a region of
	 * triangles which are marked 
	 * {@link #VISIBLE VISIBLE} or {@link #COPLANAR COPLANAR}
	 *
	 * @param edge0 original edge
	 * @return adjacent half-edge
	 */
	static public HalfEdge prevBoundaryEdge (HalfEdge edge0)
	 {
	   HalfEdge edge = edge0.getPrev();
	   Triangle f = edge.oppositeFace();
	   while (f != null && f.mark != NO_MARK)
	    { edge = edge.getOpposite().getPrev();
	      f = edge.oppositeFace();
	    }
	   return edge;
	 }

	/**
	 * Finds the half-edge within this triangle which has
	 * tail <code>vt</code> and head <code>vh</code>.
	 *
	 * @param vt tail point
	 * @param vh head point
	 * @return the half-edge, or null if none is found.
	 */
	public HalfEdge findEdge (SpatialPoint vt, SpatialPoint vh)
	 {
	   for (int i=0; i<3; i++)
	    { if (edges[i].head() == vh && edges[i].tail() == vt)
	       { return edges[i]; 
	       }
	    }
	   return null;
	 }

	/**
	 * Produces a string identifying this triangle by the
	 * point index values of its vertices.
	 *
	 * @return identifying string
	 */
        public String vertexString ()
         {
	   String s = "";
	   for (int i=0; i<3; i++)
	    { s += edges[i].vertex.getPointIndex();
	      if (i < 2)
	       { s += " "; 
	       }
	    }
	   return s;
	 }

	/**
	 * Computes the distance from a point p to the plane of
	 * this triangle.
	 *
	 * @param p the point
	 * @return distance from the point to the plane
	 */
	public double distanceToPlane (SpatialPoint p)
	 {
	   return normal.x*p.x + normal.y*p.y + normal.z*p.z + offset;
	 }

	/**
	 * Returns the normal of the plane associated with this triangle.
	 *
	 * @return the planar normal
	 */
	public SpatialVector getNormal ()
	 {
	   return normal;
	 }
}



