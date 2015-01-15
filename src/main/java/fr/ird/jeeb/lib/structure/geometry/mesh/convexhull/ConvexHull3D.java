/**
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

import java.io.PrintStream;
import java.util.Iterator;
import java.util.Vector;

/**
 * Robustly computes the convex hull of a three dimensional set of points.
 *
 * <p> 
 * The computation is done using an insertion algorithm, with
 * modifications to make it numercially robust.  This includes making
 * sure that all the faces deemed ``visible'' from a newly added point
 * are connected (as described in Dey, Sugihara, and Bajaj, ``Delaunay
 * triangulations in three dimensions with finite precision
 * arithmetic'', Computer Aided Geometric Design 9 (1992)
 * pp. 457--470). Marginally visible faces are also pruned, if necessary,
 * to ensure that when the new point is added no faces with improper
 * orientation will be generated.
 *
 * <p> By default, the hull is composed of a set of triangular faces,
 * for which the associated vertices and faces may be obtained using
 * the methods
 * {@link #getHullFaceVertices getHullFaceVertices} and
 * {@link #getHullFaces getHullFaces}, respectively. However,
 * merged faces can also be obtained using the methods
 * {@link #getMergedFaceVertices getMergedFaceVertices} and
 * {@link #getMergedFaces getMergedFaces}.
 * These are formed by merging adjacent
 * faces whose vertices are coplanar to within numeric precision.
 *
 * @author John E. Lloyd, Winter 2003 */
public class ConvexHull3D
{
	double epsilon;
	SpatialPoint[] points;
	int numHullVertices;
	int numMergedVertices;
	Vector faces = new Vector(16);
	Vector mergedFaces = new Vector(16);
	Vector pointsRemaining = new Vector(16);

	/**
	 * Precision of a double.
	 */
	static public final double DBL_EPSILON = 2.2204460492503131e-16;

	private class Face
	 {
	   HalfEdge boundaryEdge0;
	   int mark;

	   Face (HalfEdge boundaryEdge0, int mark)
	    {
	      this.boundaryEdge0 = boundaryEdge0;
	      this.mark = mark;
	    }
	 }

	/**
	 * Constructs the convex hull of a set of 3D points given
	 * by <code>coords</code>.
	 *
	 * <p> After construction,
	 * other methods can be used to obtain the hull's vertices and faces.
	 * If only three input points are specified, then the resulting
	 * ``hull'' will consist of two back-to-back triangles.
	 *
	 * @param coords an array containing the x,y,z coordinates
	 * of each point. The length of this array will be three times
	 * the number of points.
	 * @throws IllegalArgumentException The number of input points
	 * is less than three.
	 */
	public ConvexHull3D (double[] coords)
	   throws IllegalArgumentException
	 {
	   if (coords.length < 9)
	    { throw new IllegalArgumentException (
		 "Less than three input points specified");
	    }
	   createInitialFaces (coords);
	   createHull();
	   markHullVertices();
	   createMergedFaces();
	 }

	/**
	 * Constructs a Hull object with explicitly loaded
	 * points and triangles. An actual hull is not constructed.
	 * For testing purposes only.
	 */
	ConvexHull3D (SpatialPoint[] pnts, Triangle[] tris)
	 {
	   this.points = pnts;
	   double inf = Double.POSITIVE_INFINITY;
	   double[] max = new double[] {-inf, -inf, -inf};
	   for (int i=0; i<points.length; i++)
	    { for (int j=0; j<3; j++)
	       { double val;
		 if ((val = points[i].getCoord(j)) > max[j])
		  { max[j] = val;
		  }
	       }	   
	    }
	   double maxsum = (Math.abs(max[0]) + 
			    Math.abs(max[1]) + 
			    Math.abs(max[2]));
	   epsilon = 3*DBL_EPSILON*maxsum*1.01;	      
	   for (int i=0; i<tris.length; i++)
	    { faces.add (tris[i]); 
	    }
	 }

	/**
	 * Creates the first two initial faces, which
	 * are simply two triangles, with the same vertices,
	 * placed back to back.
	 */
	private void createInitialFaces (double[] coords)
	   throws IllegalArgumentException
	 {
	   // Initialize point set, and compute maximum and
	   // minimum point values.

	   SpatialPoint[] maxv = new SpatialPoint[3];
	   SpatialPoint[] minv = new SpatialPoint[3];
	   double inf = Double.POSITIVE_INFINITY;
	   double[] max = new double[] {-inf, -inf, -inf};
	   double[] min = new double[] { inf,  inf,  inf};
	   int numPnts = 0;
	   
	   SpatialPoint[] tmpPnts = new SpatialPoint[coords.length/3];

	   // first, we need an epsilon, so we need max coordinate values
	   for (int i=0; i<coords.length-2; i+=3)
	    { for (int j=0; j<3; j++)
	       { double abs = Math.abs (coords[i+j]);
		 if (abs > max[j])
		  { max[j] = abs;
		  }
	       }
	    }
	   // this epsilon formula comes from QuickHull, and I'm
	   // not about to argue.
	   epsilon = 3*DBL_EPSILON*(max[0]+max[1]+max[2])*1.01;

	   // reset max for the sequal
	   for (int i=0; i<3; i++)
	    { max[i] = -inf;
	    }

	   for (int i=0; i<coords.length-2; i+=3)
	    { 
	      SpatialPoint newPnt =
		 new SpatialPoint (coords[i],coords[i+1],coords[i+2],i/3);

	      // make sure the new point is unique
	      int j;
	      for (j=0; j<numPnts; j++)
	       { if (newPnt.epsilonEquals (tmpPnts[j], epsilon))
		  { break;
		  }
	       }
	      if (j<numPnts)
	       { continue; // the point was not unique, so pass it over
	       }
	      for (j=0; j<3; j++)
	       { double val;
		 if ((val = newPnt.getCoord(j)) < min[j])
		  { min[j] = val;
		    minv[j] = newPnt;
		  }
		 else if (val > max[j])
		  { max[j] = val;
		    maxv[j] = newPnt;
		  }
	       }
	      tmpPnts[numPnts++] = newPnt;
	      pointsRemaining.add (newPnt);
	    }

	   if (numPnts < 3)
	    { throw new IllegalArgumentException (
		 "Less than three unique input points specified");
	    }
	   points = new SpatialPoint[numPnts];
	   for (int i=0; i<numPnts; i++)
	    { points[i] = tmpPnts[i];
	    }

	   // Create initial simplex as simply two back-to-back
	   // triangles. Choose the first two vertices to correspond
	   // to the largest coorinate axis separation.
	   SpatialPoint[] vtxs = new SpatialPoint[3];
	   double d, dmax = -1;
	   for (int i=0; i<3; i++)
	    { if ((d = max[i]-min[i]) > dmax)
	       { vtxs[0] = minv[i];
		 vtxs[1] = maxv[i];
		 dmax = d;
	       }
	    }

	   // Choose third vertex to be the one that produces
	   // the largest triangle

	   SpatialVector areaVec = new SpatialVector();
	   double maxAreaSquared = 0;
	   double aSquared;
 	   for (int i=0; i<points.length; i++)
	    { SpatialPoint vx = points[i];
	      if (vx != vtxs[0] && vx != vtxs[1])
	       { Triangle.areaVector (areaVec, vx, vtxs[0], vtxs[1]);
		 if ((aSquared = areaVec.lengthSquared()) > maxAreaSquared)
		  { maxAreaSquared = aSquared;
		    vtxs[2] = vx;
		  }
	       }
	    }

	   Triangle tri0 = new Triangle (vtxs[0], vtxs[1], vtxs[2]);
	   Triangle tri1 = new Triangle (vtxs[0], vtxs[2], vtxs[1]);

	   // ``Sew'' the two triangles together
	   for (int i=0; i<3; i++)
	    { int k = (4-i)%3;
	      tri0.getEdge(i).setOpposite(tri1.getEdge(k));
	      tri1.getEdge(k).setOpposite(tri0.getEdge(i));
	    }

	   faces.add (tri0);
	   faces.add (tri1);
	   for (int i=0; i<3; i++)
	    { pointsRemaining.remove (vtxs[i]);
	    }

	   // Small hack: place the point which is furthest 
	   // from the triangles to the front of the queue
	   dmax = -1;
	   SpatialPoint vmax = null;
	   for (Iterator it=pointsRemaining.iterator(); it.hasNext(); ) 
	    { SpatialPoint vx = (SpatialPoint)it.next();
	      if ((d = Math.abs(tri0.distanceToPlane(vx))) > dmax)
	       { vmax = vx;
		 dmax = d;
	       }
	    }
	   if (vmax != null)
	    { pointsRemaining.remove(vmax);
	      pointsRemaining.add (0, vmax);
	    }
	   
	 }

	private HalfEdge firstBoundaryEdge;

	/**
	 * Starting with a triangle tri, find all triangular faces
	 * which form a connected region and are visible from a point
	 * px. Faces which are unambiguously visible are marked
	 * VISIBLE, while faces which are coplanar with px (within
	 * numeric precision) are marked as COPLANAR.
	 */
	private void markVisibleRegion (Triangle tri, SpatialPoint px, int mark)
	 {
	   HalfEdge edge = tri.getEdge(0);
	   do
	    { Triangle nbr = edge.oppositeFace();
	      // assume initially neighbour not invisible unless nbr==null
	      boolean neighbourInvisible = (nbr == null ? true : false);
	      if (nbr != null && nbr.getMark() == Triangle.NO_MARK)
	       { double d = nbr.distanceToPlane(px);
		 if (d > epsilon && mark == Triangle.VISIBLE)
		  { nbr.setMark (Triangle.VISIBLE);
		    markVisibleRegion (nbr, px, Triangle.VISIBLE); 
		  }
		 else if (d >= -epsilon)
		  { nbr.setMark (Triangle.COPLANAR);
		    markVisibleRegion (nbr, px, Triangle.COPLANAR);
		  }
		 else
		  { neighbourInvisible = true; 
		  }
	       }
	      if (firstBoundaryEdge == null && neighbourInvisible)
	       { firstBoundaryEdge = edge;
	       }
	      edge = edge.getNext();
	    }
	   while (edge != tri.getEdge(0));
	 }

	/**
	 * Starting with a triangle tri, find a connected region
	 * of triangles whose vertices are coplanar with the
	 * given reference triangle refTri. All triangles
	 * within the region are marked with the given mark value.
	 * This routine is used to form merged faces.
	 */
	private void markCoplanarRegion (Triangle tri,
					 Triangle refTri, int mark)
	 {
	   HalfEdge edge = tri.getEdge(0);
	   tri.setMark (mark);
	   do
	    { Triangle nbr = edge.oppositeFace();
	      // assume initially neighbour coplanar unless nbr==null
	      boolean neighbourCoplanar = (nbr == null ? false : true);
	      if (nbr != null && nbr.getMark() != mark)
	       { if (Math.abs (refTri.distanceToPlane(
		    edge.getOpposite().getNext().head())) <= epsilon)
		  { markCoplanarRegion (nbr, refTri, mark);
		  }
		 else
		  { neighbourCoplanar = false;
		  }
	       }
	      if (!neighbourCoplanar && firstBoundaryEdge == null)
	       { firstBoundaryEdge = edge;
	       }
	      edge = edge.getNext();
	    }
	   while (edge != tri.getEdge(0));
	 }

	/**
	 * Starting with triangle <code>tri</code>, recursively remark
	 * all connected triangles whose current mark equals or exceeds
	 * <code>refMark</code>. Note that in order for this to terminate
	 * properly, <code>newMark</code> must be less than
	 * <code>refMark</code>.
	 */
	void remarkRegion (Triangle tri, int refMark, int newMark)
	 {
	   HalfEdge edge = tri.getEdge(0);
	   tri.setMark (newMark);
	   do
	    { Triangle nbr = edge.oppositeFace();
	      if (nbr != null && nbr.getMark() >= refMark)
	       { remarkRegion (nbr, refMark, newMark);
	       }
	      edge = edge.getNext();
	    }
	   while (edge != tri.getEdge(0));   
	 }

	SpatialVector u0 = new SpatialVector();
	SpatialVector u1 = new SpatialVector();
	SpatialVector xprod = new SpatialVector();

	/**
	 * Returns true if the triangle formed by px, the tail
	 * of edge, and the head of edge, is unambiguously
	 * oriented counter-clockwise with respect to the
	 * face associated with edge.
	 */
	private boolean leftTurnOnFace (HalfEdge edge, SpatialPoint px)
	 {
	   // edge should form a left turn on the triangle face
	   // WRT (tail-px)
	   if (Triangle.areaVectorDot (px, edge.tail(), edge.head(),
				       edge.getFace().getNormal()) < epsilon)
	    { return false;
	    }
	   else
	    { return true;
	    }
	 }

	/**
	 * Find the points in the input set which are used as
	 * vertices within the final hull, and assign each
	 * one an index value.
	 */
	private void markHullVertices()
	 {
	   for (int i=0; i<points.length; i++)
	    { points[i].setHullFaceIndex (-1);
	    }
	   for (Iterator fi=faces.iterator(); fi.hasNext(); )
	    { Triangle tri = (Triangle)fi.next();
	      for (int i=0; i<3; i++)
	       { tri.getEdge(i).head().setHullFaceIndex (1);
	       }
	    }
	   int idx = 0;
	   for (int i=0; i<points.length; i++)
	    { if (points[i].getHullFaceIndex() != -1)
	       { points[i].setHullFaceIndex(idx++);
	       }
	    }	   
	   numHullVertices = idx;
	 }

	/**
	 * Merge adjacent faces whose vertices are coplanar
	 * within numeric precision.
	 */
	private void createMergedFaces ()
	 {
	   // unmark all faces
	   for (Iterator fi=faces.iterator(); fi.hasNext(); )
	    { ((Triangle)fi.next()).setMark (Triangle.NO_MARK);
	    }
	   int mark = 1;
	   for (Iterator fi=faces.iterator(); fi.hasNext(); )
	    { Triangle tri = (Triangle)fi.next();
	      while (tri.getMark() == Triangle.NO_MARK)
	       { createMergedRegion (tri, mark);
		 mark++;
	       }
	    }   
	   for (int i=0; i<points.length; i++)
	    { points[i].setMergedFaceIndex (-1);
	    }
	   for (Iterator fi=mergedFaces.iterator(); fi.hasNext(); )
	    { Face face = (Face)fi.next();
	      HalfEdge edge = face.boundaryEdge0;
	      do
	       { edge.head().setMergedFaceIndex (1);
		 edge = Triangle.nextBoundaryEdge(edge, face.mark);
	       }
	      while (edge != face.boundaryEdge0);
	    }
	   int idx = 0;
	   for (int i=0; i<points.length; i++)
	    { if (points[i].getMergedFaceIndex() != -1)
	       { points[i].setMergedFaceIndex(idx++);
	       }
	    }
	   numMergedVertices = idx;
	 }

	/**
	 * Merge coplanar faces surrounding <code>tri</code> to create
	 * a single convex merged region with the indicated <code>mark</code>.
	 */
	private void createMergedRegion (Triangle tri, int mark)
	 {
	   firstBoundaryEdge = null;
	   markCoplanarRegion (tri, tri, mark+2);
	   HalfEdge edge0;
	   edge0 = unmarkNonconvexFaces (firstBoundaryEdge, tri.getNormal(),
					 mark+2);
	   remarkRegion (edge0.getFace(), mark+2, mark);
	   remarkNonconvexRegion (edge0, mark);
	   mergedFaces.add (new Face (edge0, mark));
	 }

	void remarkNonconvexRegion (HalfEdge edge0, int mark)
	 {
	   HalfEdge edge = edge0;
	   do
	    { Triangle nbr = edge.oppositeFace();
	      if (nbr != null && nbr.getMark() == mark+1)
	       { remarkRegion (nbr, mark+1, Triangle.NO_MARK);
	       }
	      edge = Triangle.nextBoundaryEdge (edge, mark);
	    }
	   while (edge != edge0);	   
	 }

	/**
	 * Returns an array of all the points from which
	 * this hull was constructed.
	 *
	 * @return points from which this hull was constructed
	 */
	public SpatialPoint[] getPoints()
	 {
	   return points;
	 }

	/**
	 * Returns an array of all the vertices used by
	 * the triangular faces of this hull.
	 *
	 * @return array of vertices
	 * @see ConvexHull3D#getHullFaces
	 */
	public SpatialPoint[] getHullFaceVertices()
	 {
	   SpatialPoint[] vtxs = new SpatialPoint[numHullVertices];
	   int idx = 0;
	   for (int i=0; i<points.length; i++)
	    { if (points[i].getHullFaceIndex() != -1)
	       { vtxs[idx++] = points[i];
	       }
	    }	   
	   return vtxs;
	 }

	/**
	 * Returns the triangular faces associated
	 * with this hull.
	 *
	 * <p>Each face is represented as an integer
	 * array, which lists, in counter-clockwise order, the indices
	 * of its vertices (with respect to the array returned by
	 * getHullFaceVertices()).
	 *
	 * @return array of integer arrays, with each one listing the vertex
	 * indices for a particular face.
	 * @see ConvexHull3D#getHullFaceVertices
	 */
	public int[][] getHullFaces()
	 {
	   int[][] idxs = new int[faces.size()][3];
	   int k = 0;
	   for (Iterator fi=faces.iterator(); fi.hasNext(); )
	    { Triangle tri = (Triangle)fi.next();
	      for (int i=0; i<3; i++)
	       { idxs[k][i] = tri.getEdge(i).head().getHullFaceIndex();
	       }
	      k++;
	    }	   
	   return idxs;
	 }

	/**
	 * Returns an array of all the vertices found
	 * in the merged faces of this hull.
	 *
	 * @return array of vertices
	 * @see ConvexHull3D#getMergedFaces
	 */
	public SpatialPoint[] getMergedFaceVertices()
	 {
	   SpatialPoint[] vtxs = new SpatialPoint[numMergedVertices];
	   int idx = 0;
	   for (int i=0; i<points.length; i++)
	    { if (points[i].getMergedFaceIndex() != -1)
	       { vtxs[idx++] = points[i];
	       }
	    }	   
	   return vtxs;
	 }

	/**
	 * Returns the merged faces associated with this hull.
	 *
	 * <p> Merged
	 * faces are formed by merging adjacent triangular faces which
	 * are co-planar within numeric precision.
	 * Each face is represented as an integer array,
	 * which lists, in counter-clockwise order, the indices of its
	 * vertices (with respect to the array returned by
	 * getMergedFaceVertices()).
	 *
	 * @return array of integer arrays, with each one listing the vertex
	 * indices for a particular face.
	 * @see ConvexHull3D#getMergedFaceVertices
	 */
	public int[][] getMergedFaces()
	 {
	   int[][] idxs = new int[ mergedFaces.size() ][];
	   int k = 0;
	   for ( Iterator fi=mergedFaces.iterator(); fi.hasNext(); )
	    { Face face = (Face)fi.next();
	      HalfEdge edge = face.boundaryEdge0;
	      int numv = 0;
	      do
	       { numv++;
		 edge = Triangle.nextBoundaryEdge(edge, face.mark);
	       }
	      while (edge != face.boundaryEdge0);
	      idxs[k] = new int[numv];
	      edge = face.boundaryEdge0;
	      int i = 0;
	      do
	       { idxs[k][i++] = edge.head().getMergedFaceIndex();
		 edge = Triangle.nextBoundaryEdge(edge, face.mark);
	       }
	      while (edge != face.boundaryEdge0);
	      k++;
	    }	   
	   return idxs;
	 }

	/**
	 * Prints the triangular faces (and associated vertices)
	 * of this hull to the stream ps.
	 *
	 * <p>
	 * The information is printed using the Alias Wavefront .obj file
	 * format, with the vertices printed first (each preceding by
	 * the letter <code>v</code>), followed by the face index sets (each
	 * preceded by the letter <code>f</code>).
	 *
	 * @param ps stream used for printing
	 * @param notZeroIndexed if false, then the index values for the
	 * face vertices are started at zero, rather than one. Indexing
	 * from one is standard for the .obj format.
	 * @see ConvexHull3D#getHullFaces
	 */
	public void printHull (PrintStream ps, boolean notZeroIndexed)
	 {
	   for (int i=0; i<points.length; i++)
	    { if (points[i].getHullFaceIndex() != -1)
	       { ps.println ("v " +
			     points[i].x + " " +
			     points[i].y + " " +
			     points[i].z);
	       }
	    }
	   for (Iterator fi=faces.iterator(); fi.hasNext(); )
	    { Triangle tri = (Triangle)fi.next();
	      ps.print ("f");
	      for (int i=0; i<3; i++)
	       { int idx = tri.getEdge(i).head().getHullFaceIndex();
		 if (notZeroIndexed)
		  { idx += 1;
		  }
		 ps.print (" " + idx);
	       }
	      ps.println ("");
	    }
	 }

	/**
	 * Prints the merged faces (and associated vertices)
	 * of this hull to the stream ps.
	 *
	 * <p>
	 * The information is printed using the Alias Wavefront .obj file
	 * format, with the vertices printed first (each preceding by
	 * the letter <code>v</code>), followed by the face index sets (each
	 * preceded by the letter <code>f</code>).
	 *
	 * @param ps stream used for printing
	 * @param notZeroIndexed if false, then the index values for the
	 * face vertices are started at zero, rather than one. Indexing
	 * from one is standard for the .obj format.
	 * @see ConvexHull3D#getMergedFaces
	 */
	public void printMergedHull (PrintStream ps, boolean notZeroIndexed)
	 {
	   for (int i=0; i<points.length; i++)
	    { if (points[i].getMergedFaceIndex() != -1)
	       { ps.println ("v " +
			     points[i].x + " " +
			     points[i].y + " " +
			     points[i].z);
	       }
	    }
	   for (Iterator fi=mergedFaces.iterator(); fi.hasNext(); )
	    { Face face = (Face)fi.next();
	      HalfEdge edge = face.boundaryEdge0;
	      ps.print ("f");
	      do
	       { int idx = edge.head().getMergedFaceIndex();
		 if (notZeroIndexed)
		  { idx += 1; 
		  }
		 ps.print (" " + idx);
		 edge = Triangle.nextBoundaryEdge(edge, face.mark);
	       }
	      while (edge != face.boundaryEdge0);
	      ps.println ("");
	    }
	 }

	/**
	 * Of those faces which are coplanar with px, unmark those
	 * for which the addition of px will result in the creation
	 * of an improperly oriented face.
	 */
	HalfEdge unmarkImproperFaces (HalfEdge edge0, SpatialPoint px)
	 { 
	   HalfEdge edge = edge0;
	   do
	    { 
	      Triangle tri = edge.getFace();
	      if (tri.getMark() == Triangle.COPLANAR &&
		  !leftTurnOnFace (edge, px))
	       { do
		  { edge = Triangle.prevBoundaryEdge (edge);
		  }
		 while (edge.getFace() == tri);
		 tri.setMark (Triangle.NO_MARK);
		 edge0 = edge;
	       }
	      edge = Triangle.nextBoundaryEdge (edge);
	    }
	   while (!edge.equals (edge0));
	   return edge0;
	 }

	/**
	 * If necessary, remove boundary faces from a coplanar region
	 * to ensure that the region is convex.
	 */
	HalfEdge unmarkNonconvexFaces (
	   HalfEdge edge0, SpatialVector normal, int mark)
	 { 
	   HalfEdge edge = edge0;
	   do
	    {
	      HalfEdge nextEdge = Triangle.nextBoundaryEdge(edge, mark);
	      while (Triangle.areaVectorDot (
		 edge.tail(), edge.head(), nextEdge.head(), normal) < -epsilon)
	       { nextEdge.getFace().setMark (mark-1);
		 nextEdge = Triangle.nextBoundaryEdge(edge, mark);
		 edge0 = edge;
	       }
	      edge = nextEdge;
	    }
	   while (!edge.equals (edge0));
	   return edge0;
	 }

	/**
	 * Returns true if a spatial point is on or inside the convex hull. 
	 * This is the same as the method {@link
	 * #containedInHull(SpatialPoint,double) 
	 * containedInHull (SpatialPoint, double)},
	 * except that the numeric tolerance is computed automatically.
	 *
	 * @param px point to test
	 * @return true if the point is on or inside the hull
	 * @see #containedInHull(SpatialPoint,double)
	 */
	public boolean containedInHull (SpatialPoint px)
	 {
	   return containedInHull (px, epsilon);
	 }

	/**
	 * Returns true if a spatial point is on or inside the convex
	 * hull.  This is considered to be the case if the point is
	 * "inside" each of the hull's faces, or, more precisely, if
	 * its directed distance to each face (along the face's
	 * normal) is less than or equal to a user-supplied numeric
	 * tolerance. It is not unreasonable to set this tolerance
	 * to 0.
	 *
	 * @param px point to test
	 * @param tol numeric tolerance 
	 * @return true if the point is on or inside the hull
	 * @see #containedInHull(SpatialPoint)
	 */
	public boolean containedInHull (SpatialPoint px, double tol)
	 {
	   for (Iterator fi=faces.iterator(); fi.hasNext(); ) 
	    { if (((Triangle)fi.next()).distanceToPlane(px) > tol)
	       { return false;
	       }
	    }
	   return true;
	 }

	private void createHull ()
	 {
	   for (Iterator it=pointsRemaining.iterator(); it.hasNext(); ) 
	    { SpatialPoint px = (SpatialPoint)it.next();

	      // Find the face which is most visible to the point;
	      // i.e., the one for whom the distance to the face's
	      // plane has the greatest positive value. If the
	      // point is not more than epsilon away from any
	      // face plane, then the point is considered to be
	      // on or inside all the faces, and is therefore
	      // not visible and is ignored.
	      double dmax = 0;
	      double d;
	      Triangle trimax = null;
	      for (Iterator fi=faces.iterator(); fi.hasNext(); ) 
	       { Triangle tri = (Triangle)fi.next();
		 if ((d = tri.distanceToPlane(px)) > epsilon)
		  { dmax = d;
		    trimax = tri;
		  }
		 tri.setMark(Triangle.NO_MARK); // unmark all triangles
	       }
	      if (trimax != null)
	       { 
		 // SpatialPoint is visible WRT trimax, so mark accordingly
		 trimax.setMark(Triangle.VISIBLE);

		 // Recursively find all adjoining triangles for
		 // which the point is either visible or unknown.
		 firstBoundaryEdge = null;

		 markVisibleRegion (trimax, px, Triangle.VISIBLE);

		 // Go around the outside boundary of the visible region,
		 // unmarking COPLANAR faces which would result in
		 // improperly oriented faces
		 HalfEdge edge0 =
		    unmarkImproperFaces(firstBoundaryEdge, px);

		 // Go around the outside boundary again, this time
		 // adding new triangles
		 HalfEdge edge = edge0;
		 Triangle tri, lastTri = null;
		 Triangle firstTri = null;
		 do
		  { tri = new Triangle (edge.head(), px, edge.tail());
		    if (lastTri != null)
		     { lastTri.getEdge(1).setOpposite(tri.getEdge(2));
		       tri.getEdge(2).setOpposite(lastTri.getEdge(1));
		     }
		    else
		     { firstTri = tri;
		     }
		    tri.getEdge(0).setOpposite(edge.getOpposite());
		    edge.getOpposite().setOpposite(tri.getEdge(0));
		    edge = Triangle.nextBoundaryEdge (edge);
		    lastTri = tri;
		    faces.add (tri);
		  }
		 while (!edge.equals (edge0));
		 lastTri.getEdge(1).setOpposite(firstTri.getEdge(2));
		 firstTri.getEdge(2).setOpposite(lastTri.getEdge(1));

		 // Delete all marked faces
		 for (Iterator fi=faces.iterator(); fi.hasNext(); ) 
		  { tri = (Triangle)fi.next();
		    if (tri.getMark() != Triangle.NO_MARK)
		     { fi.remove(); 
		     }
		  }
	       }
	    }
	 }
}
