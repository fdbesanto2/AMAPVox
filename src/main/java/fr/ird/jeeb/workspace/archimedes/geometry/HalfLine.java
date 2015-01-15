package fr.ird.jeeb.workspace.archimedes.geometry;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Shape;


/**
 * Half-line defined by a point and a direction
 * @author Dauzat/Cresson; August 2012
 */
public class HalfLine implements LineElement, Transformable {

	private Vector3f	direction;
	private Point3f		origin;

	/**
	 * Constructor with 1 point, 1 direction
	 * @param origin		origin of the half-line
	 * @param direction		direction of the half-line
	 */
	public HalfLine (Point3f origin, Vector3f direction) {
		this.direction	= new Vector3f(direction);
		this.origin		= new Point3f(origin);
	}

	/**
	 * Constructor with 2 points
	 * @param startPoint	origin of the half-line
	 * @param onePoint		one point on the half-line
	 */
	public HalfLine (Point3f startPoint, Point3f onePoint) {
		Vector3f direction = new Vector3f (onePoint);
		direction.sub (startPoint);
		direction.normalize ();
		this.direction 	= new Vector3f(direction);
		this.origin		= new Point3f(startPoint);
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

	//---------------------- Transformations ----------------------//
	@Override
	public void transform (Transformations t) {
		t.apply (origin);
		t.apply (direction);
	}
	
	@Override
	public void translate (Vector3f translation) {
		origin.add (translation);
	}
	
	//--------------------------- Getters ------------------------//
	@Override
	public Point3f getOrigin () {
		return new Point3f(origin);
	}

	@Override
	public Point3f getEnd () {
		return null;
	}

	@Override
	public Vector3f getDirection () {
		return new Vector3f(direction);
	}

	@Override
	public float getLength () {
		return Float.MAX_VALUE;
	}

	
	
	//--------------------------- Old stuffs ------------------------//
//	/**
//	 * @return The shortest distance between 
//	 * <li>the point and the line 
//	 * <li>or the distance between the point and the half-line origin
//	 */
////	@Override
//	public float distanceToPoint (Point3f point) {
//		
//		Vector3f dir= super.getDirection ();
//
//		Vector3f vop= new Vector3f (point);
//		vop.sub (super.getOrigin ());
//
//		float dop = vop.dot (dir);	// distance to projected point
//
//		if (dop < 0)
//			return vop.length ();
//
//		Vector3f vpn = new Vector3f (dir);
//		dir.scaleAdd (dop, getOrigin ());
//		vpn.sub(point);
//		
//		return vpn.length ();
//	}
//	
//	public List<Point3f> intersectionsWithSphere (Sphere sphere) {
//		
//		ArrayList<Point3f> intersections = new ArrayList<Point3f>();
//		
//		return intersections;
//	}
//
//	@Override
//	public boolean doesIntersectSphere (Sphere sphere) {
//
//		Vector3f dir= super.getDirection ();
//
//		Vector3f vop= new Vector3f (sphere.getCentre ());
//		vop.sub (super.getOrigin ());
//
//		float dop = vop.dot (dir);	// distance to projected point
//
//		if (dop < 0)
//			return false;
//
//		Vector3f vcn = new Vector3f (dir);
//		vcn.scaleAdd (dop, getOrigin ());
//		vcn.sub (sphere.getCentre ());
//
//		float dist = vcn.length ();
//
//		if (dist > sphere.getRadius ())
//			return false;
//
//		return true;
//	}
//
//	public String toString () {
//		
//		return ("origin: "+getOrigin ()+"\tdirection: "+getDirection ());
//	}

}
