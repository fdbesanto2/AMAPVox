package fr.ird.jeeb.workspace.archimedes.geometry.shapes;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.geometry.HalfLine;
import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;
import fr.ird.jeeb.workspace.archimedes.geometry.Line;
import fr.ird.jeeb.workspace.archimedes.geometry.LineElement;
import fr.ird.jeeb.workspace.archimedes.geometry.LineSegment;
import fr.ird.jeeb.workspace.archimedes.geometry.Transformations;

/**
 * Triangle class
 * @author cresson, august 2012
 */	
@Deprecated
public class Triangle extends Shape {

	public static final float 	DEFAULT_FAR	= -Float.MAX_VALUE;
	
	private Point3f[] vertex;
	private Vector3f  normal;
	private boolean culling;
	
	public Triangle (Point3f[] points) {
		this(points, false);
	}
	
	public Triangle (Point3f[] points, boolean culling) {
		this.vertex = points;
		this.culling = culling;
		computeNormal();
	}
	
	private class IntersectionContext {
		public boolean intersect;
		public float   length;
		public IntersectionContext(boolean intersect, float length) {
			this.intersect=intersect;
			this.length=length;
		}
	}
	
	/**
	 *  Manage intersectionContext to return a correct Intersection 
	 * (depending on instance of lineElement, e.g. test for segment is different from test for half-line) 

	 * @param	lineElement					line element
	 * @param	allowNegativeLengths		allow intersection point to be back to the line element origin
	 * @return	computed intersection (null if empty)
	 */
	private Intersection computeIntersection (LineElement lineElement, boolean allowNegativeLengths) {
		IntersectionContext intersectionContext = computeIntersection(lineElement.getDirection(), lineElement.getOrigin (), culling, allowNegativeLengths);
		if (intersectionContext.intersect) {
			float distance = intersectionContext.length; 
			//boolean side = linel.getDirection ().dot (normal)>0;
			//return new Intersection(point,normal,side);
			if (lineElement instanceof LineSegment)
				if (lineElement.getLength () < intersectionContext.length)
					return null;
			return new Intersection(distance,normal);
		}
		return null;
	}
	
	/**
	 * Computes normal of the triangle
	 */
	private void computeNormal() {
		Vector3f vertex1 = new Vector3f(vertex[1].x - vertex[0].x,vertex[1].y - vertex[0].y,vertex[1].z - vertex[0].z);
		Vector3f vertex2 = new Vector3f(vertex[2].x - vertex[1].x,vertex[2].y - vertex[1].y,vertex[2].z - vertex[1].z);
		Vector3f normal  = new Vector3f();
		normal.cross (vertex1, vertex2);
		normal.normalize ();
		this.normal = normal;
	}

	/**
	 * Computes intersection Triangle-Line.
	 * 
	 * @param	direction					direction of the line element
	 * @param	origin						origin of the line element
	 * @param	cullingDesired				true if result depends of triangle orientation (i.e. if triangle is back, there is no intersection). Faster code.
	 * @param	allowNegativeLengths		true if result depends of line orientation (i.e. allow intersection point to be back to the line element origin).
	 * @return	IntersectionContext (length: distance between ray origin and ray impact, boolean: true if there is an intersection, false if not)
	 */
	private IntersectionContext computeIntersection (Vector3f direction, Point3f origin, boolean cullingDesired, boolean allowNegativeLengths) {
		// *************************************************************************************
		// Following code inspired by the paper :
		// "Fast, Minimum Storage Ray/Triangle intersection" from Thomas Möller and Ben Trumbore
		// (Prosolvia Clarus AB, Chalmers University of Technology / 
		//  Program of Computer Graphics, Cornell University)
		//
		// Link: http://www.graphics.cornell.edu/pubs/1997/MT97.html
		// *************************************************************************************
		
		// default length
		float length = DEFAULT_FAR;
		
		// find vectors for two edges sharing vertex0
		Vector3f edge1 = new Vector3f(vertex[1].x-vertex[0].x,vertex[1].y-vertex[0].y,vertex[1].z-vertex[0].z);
		Vector3f edge2 = new Vector3f(vertex[2].x-vertex[0].x,vertex[2].y-vertex[0].y,vertex[2].z-vertex[0].z);

		// begin computing determinant - also used to calculate u
		Vector3f pvec  = new Vector3f();
		pvec.cross(direction, edge2);

		// if determinant is zero, ray lies in plane of triangle
		float det = edge1.dot (pvec);
		if (cullingDesired == true) { // if culling is desired
			if (det<0)
				return new IntersectionContext(false,length);

			// calculate distance from vert0 to ray origin
			Vector3f tvec = new Vector3f();
			tvec.sub (origin, vertex[0]);

			// calculate u and test bounds
			float u = tvec.dot (pvec);
			if (u<0 | u>det)
				return new IntersectionContext(false,length);

			// prepare to test v
			Vector3f qvec = new Vector3f();
			qvec.cross (tvec, edge1);

			// computing v and test bounds
			float v = qvec.dot (direction);
			if (v<0 | u+v>det)
				return new IntersectionContext(false,length);

			// calculate length, ray intersects triangle
			length  = edge2.dot (qvec);
			length /= det;
		}
		else { // the non-culling branch
			if (det==0)
				return new IntersectionContext(false,length);

			// invert of determinant
			float inv_det = 1.0f/det;

			// calculate distance from vert0 to ray origin
			Vector3f tvec = new Vector3f();
			tvec.sub (origin, vertex[0]);

			// calculate u and test bounds
			float u = tvec.dot (pvec);
			u *= inv_det;
			if (u<0.0f | u>1.0f)
				return new IntersectionContext(false,length);

			// prepare to test v
			Vector3f qvec = new Vector3f();
			qvec.cross (tvec, edge1);

			// calculate v and test bounds
			float v = qvec.dot (direction);
			v *= inv_det;
			if (v<0.0f | u+v>1.0f)
				return new IntersectionContext(false,length);

			// calculate length, ray intersects triangle
			length = edge2.dot (qvec);
			length *= inv_det;
		}
		if (allowNegativeLengths==true)
			return new IntersectionContext(true,length);
		else
			if (length>0.0f)
				return new IntersectionContext(true,length);
		return new IntersectionContext(false,length);
		
	}

	
	@Override
	/**	
	 * Tests if triangle is intersected by the specified line element
	 * 
	 * @param	lineElement				line element
	 * @return	true if triangle is intersected by the line element. false if not.
	*/
	public boolean isIntersectedBy (LineElement lineElement) {
		
		if (lineElement instanceof Line) {
			// allow negative distances
			return computeIntersection(lineElement.getDirection(), lineElement.getOrigin (), culling, true).intersect;
		}
		else if (lineElement instanceof HalfLine) {
			// dont allow negative distances
			return computeIntersection(lineElement.getDirection(), lineElement.getOrigin (), culling, false).intersect;
		}
		// dont allow negative distance and compare length vs distance
		IntersectionContext intersectionContext = computeIntersection(lineElement.getDirection(), lineElement.getOrigin (), culling, false);
		if (lineElement.getLength () > intersectionContext.length & intersectionContext.intersect == true)
			return true;
		return false;
	}

	@Override
	/**	
	 * Get triangle intersections with the specified line element
	 * 
	 * @param	lineElement					line element
	 * @return	ArrayList of Intersection. 	(faked, because Triangles's maximum intersection number never > 1). Use getNearestIntersection instead.
	 * @see		getNearestIntersection
	*/
	public ArrayList<Intersection> getIntersections (LineElement lineElement) {
		ArrayList<Intersection> array = new ArrayList<Intersection>();
		array.add (computeIntersection(lineElement,true));
		return array;
	}

	@Override
	/**
	 * Get the nearest Intersection with the specified line element
	 * 
	 * @param	linel						line element
	 * @return	intersection
	 */
	public Intersection getNearestIntersection (LineElement lineElement) {
		if (lineElement instanceof LineSegment | lineElement instanceof HalfLine) {
			// dont allow negative distances
			return computeIntersection(lineElement,false);
		}
		// allow negative distances
		return computeIntersection(lineElement,true);	
	}

	@Override
	/**
	 * transform the shape's position
	 * 
	 * @param transform						transformation
	 */
	public void transform (Transformations transform) {
		
		for (int v=0; v<3; v++) {
			transform.apply (vertex[v]);
		}

		computeNormal();
	}

	/**
	 * get the barycentre of the triangle
	 * 
	 * @return barycentre
	 */
	public Point3f getBarycentre () {
		Point3f barycentre = new Point3f(0f,0f,0f);
		for (int v=0; v<3; v++) {
			barycentre.add (vertex[v]);
		}
		barycentre.scale (1.0f/3.0f);
		return barycentre;
	}
}
