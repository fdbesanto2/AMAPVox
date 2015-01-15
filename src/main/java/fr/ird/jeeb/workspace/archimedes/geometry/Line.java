package fr.ird.jeeb.workspace.archimedes.geometry;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Shape;

/**
 * Straight line in 3D
 * @author Dauzat/Cresson; August 2012
 */
public class Line implements LineElement, Transformable {

	protected Point3f	onePoint;
	protected Vector3f	direction;

	/**
	 * Constructor with 1 point and 1 direction
	 * @param onePoint		one point on the line
	 * @param direction		direction of the line
	 */
	public Line (Point3f onePoint, Vector3f direction) {
		this.onePoint	= new Point3f (onePoint);
		this.direction	= new Vector3f (direction);
		this.direction.normalize ();
	}

	/**
	 * Constructor with 2 points
	 * @param point1		one point on the line
	 * @param point2		one other point on the line
	 */
	public Line (Point3f point1, Point3f point2) {
		onePoint	= new Point3f (point1);
		direction	= new Vector3f (point2);
		direction.sub (point1);
		direction.normalize ();
	}

	//---------------------- Intersections ----------------------//
	@Override
	public boolean doesIntersect (Shape shape) {
		
		return shape.isIntersectedBy (this);
	}
	@Override
	public ArrayList<Intersection> getIntersections (Shape shape) {
		
		return shape.getIntersections (this);
	}
	@Override
	public Intersection getNearestIntersection (Shape shape) {
		
		return shape.getNearestIntersection (this);
	}

	
	//--------------------------- Getters ------------------------//
	@Override
	public Point3f getOrigin () {
		return new Point3f (onePoint);
	}

	@Override
	public Vector3f getDirection () {
		return new Vector3f (direction);
	}
	
	@Override
	public Point3f getEnd () {
		return null;
	}

	@Override
	public float getLength () {
		return Float.MAX_VALUE;
	}


	//---------------------- Transformations ----------------------//
	@Override
	public void translate (Vector3f translation) {
		onePoint.add (translation);
	}

	@Override
	public void transform (Transformations t) {
		t.apply (onePoint);
		t.apply (direction);
	}
	
	
	
	
	//---------------------- Old stuffs -------------------------//
//	/**
//	 * @return The shortest distance between the line and the point
//	 */
//	public float distanceToPoint (Tuple3f tuple3f) {
//
//		Vector3f vpp = new Vector3f (origin);
//		vpp.sub (tuple3f);
//
//		vpp.cross (vpp, direction);
//		
//		return vpp.length ();
//	}
//	
//	public boolean doesIntersectSphere (Sphere sphere) {
//		
//		if (distanceToPoint(sphere.getCentre()) > sphere.getRadius ())
//			return false;
//		
//		return true;
//	}
//
//	public List<Point3f> intersectionsWithSphere (Sphere sphere) {
//		
//		ArrayList<Point3f> intersections = new ArrayList<Point3f>();
//		
//		Vector3f dir = getDirection ();
//		Vector3f voc = new Vector3f (sphere.getCentre ());
//		voc.sub (getOrigin ());
//
//		float dop = voc.dot (dir);	// distance to projected point
//
//		Point3f projection = new Point3f (dir);
//		projection.scaleAdd (dop, getOrigin ());
//		
//		Vector3f vcp = new Vector3f (projection);
//		vcp.sub (sphere.getCentre ());
//
//		float dist2 = vcp.lengthSquared ();
//		float radius2 = sphere.getRadius()*sphere.getRadius();
//		
//		// no intersection
//		if (dist2 > radius2)
//			return intersections;
//		
//		float dip = (float) Math.sqrt (radius2-dist2);
//		
//		// one intersection
//		if (dip == 0) {
//			intersections.add (projection);
//			return intersections;
//		}
//		
//		// two intersections
//		Vector3f vip = new Vector3f (getDirection ());
//		vip.scale (dip);
//		Point3f nearest = new Point3f (projection);
//		nearest.sub (vip);
//		intersections.add (nearest);
//		Point3f furthest = new Point3f (projection);
//		furthest.add (vip);
//		intersections.add (furthest);
//
//		return intersections;
//	}
//	
//	@Override
//	public void translate (Tuple3f translation) {
//		// TODO Auto-generated method stub
//		
//	}
//
////	@Override
//	public void rotX (double angle) {
//		// TODO Auto-generated method stub
//		
//	}
//
////	@Override
//	public void rotY (double angle) {
//		// TODO Auto-generated method stub
//		
//	}
//
////	@Override
//	public void rotZ (double angle) {
//		// TODO Auto-generated method stub
//		
//	}
//
////	@Override
//	public void rotAroundAxis (double angle, Vector3f axis) {
//		// TODO Auto-generated method stub
//		
//	}

	
}