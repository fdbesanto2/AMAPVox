package fr.ird.jeeb.workspace.archimedes.geometry.shapes;

import java.util.ArrayList;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;
import fr.ird.jeeb.workspace.archimedes.geometry.Line;
import fr.ird.jeeb.workspace.archimedes.geometry.LineElement;
import fr.ird.jeeb.workspace.archimedes.geometry.LineSegment;
import fr.ird.jeeb.workspace.archimedes.geometry.Transformations;

/**
 * Plane class: Infinite plane
 * @author Cresson
 *
 */
public class Plane extends Shape {
	private static final float eps		= 0.00001f;
	
	private Point3f		point;
	private Vector3f	normal;
	private boolean 	oriented;
	
	/**
	 * Constructor with a point and the normal, and specifying if the plane is oriented
	 */
	public Plane(Point3f point, Vector3f normal, boolean oriented) {
		this.point	= point;
		normal.normalize ();
		this.normal	= normal;
		this.oriented = oriented;
	}

	/**
	 * Constructor for an oriented plane, given a point and the normal
	 */
	public Plane(Point3f point, Vector3f normal) {
		this(point,normal,true);
	}

	@Override
	public void transform (Transformations transform) {
		transform.apply (point);
		Matrix3f rot = new Matrix3f();
		transform.getMatrix().getRotationScale(rot);
		rot.transform(normal);
		normal.normalize();
	}

	@Override
	public boolean isIntersectedBy (LineElement lineElement) {
		if (lineElement instanceof LineSegment){
			
			Vector3f p = new Vector3f(lineElement.getOrigin());
			p.sub(point);
			boolean p1 = normal.dot(p)<0;
			if (oriented)
				if (p1)
					return false;
			p = new Vector3f(lineElement.getEnd());
			p.sub(point);
			boolean p2 = normal.dot(p)<0;
			
			return (p1!=p2);
		}
		
		float ps = normal.dot (lineElement.getDirection ());
		if (ps==0) // ray is parallel to plane
			return false;

		if (lineElement instanceof Line) {
			return true;
		}
		else {
			if (oriented & ps > 0)
				return false;
			Vector3f v1 = new Vector3f(point);
			v1.sub (lineElement.getOrigin ());
			float k1 = v1.dot (lineElement.getDirection ());
			float k2 = v1.dot (normal)/ps;
			if (k1<=0 || k2<=eps) // intersection is back to the origin of the line
				return false;
			
		}
		return true;
	}

	@Override
	public ArrayList<Intersection> getIntersections (LineElement lineElement) {
		ArrayList<Intersection> intersections = new ArrayList<Intersection>();
		Intersection intersection = getNearestIntersection (lineElement);
		if (intersection!=null)
			intersections.add (intersection);
		return intersections;
	}

	@Override
	public Intersection getNearestIntersection (LineElement lineElement) {
		float ps = normal.dot (lineElement.getDirection ());
		if (ps==0) {// ray is parallel to plane
			return null;
		}
		else {
			Vector3f d = new Vector3f (point);
			d.sub (lineElement.getOrigin ());
			float ps2 = normal.dot (d);
			float k = ps2/ps;
			if (oriented & ps<0 | !oriented) {
				if (k>eps) {
					if (lineElement instanceof LineSegment)
						if (k>((LineSegment) lineElement).getLength())
							return null;
					Intersection intersection = new Intersection(k,normal);
					return intersection;
				}
			}
		}
		return null;

	}
	
	@Override
	public Object clone() {
	    Plane p = null;
	    try {
	    	p = (Plane) super.clone();
	    } catch(CloneNotSupportedException cnse) {
	      	System.err.println (System.err+"/"+cnse.getMessage ());
	    }
	    
	    p.point = new Point3f(point);
	    p.oriented = oriented;
	    p.normal = new Vector3f(normal);
	    
	    return p;
	}
	
	public Point3f getPoint(){
		return point;
	}
	
	public Vector3f getNormal(){
		return normal;
	}
}
