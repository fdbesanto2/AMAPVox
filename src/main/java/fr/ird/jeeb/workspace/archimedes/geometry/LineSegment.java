package fr.ird.jeeb.workspace.archimedes.geometry;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Shape;

/**
 * Oriented line segment defined by 2 points: origin and end
 * @author Dauzat/Cresson; August 2012
 */
public class LineSegment implements Cloneable, Transformable, LineElement {
	
	private Point3f		origin;
	private Point3f		end;
	private Vector3f	direction;
	private float		length;

	/**
	 * Constructor with 2 points
	 * @param origin	origin of the line segment
	 * @param end		end of the line segment
	 */
	public LineSegment (Point3f origin, Point3f end) {
		direction = new Vector3f (end);
		direction.sub (origin);
		this.length		= direction.length ();
		if (direction.length()!=0f) direction.normalize ();
		this.end		= new Point3f(end);
		this.origin		= new Point3f(origin);
	}
	
	/**
	 * Constructor with 1 point, a direction and a length
	 * @param origin	origin of the line segment
	 * @param direction	direction of the line segment
	 * @param length	length of the line segment
	 */
	public LineSegment (Point3f origin, Vector3f direction, float length) {
		end = new Point3f (0f,0f,0f);
		end.scaleAdd (length, direction, origin);
		this.direction 	= new Vector3f (direction);
		this.origin		= new Point3f(origin);
		this.length		= length;
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

	//---------------------- Getters & Setter -------------------//
	@Override
	public Point3f getEnd () {
		return end;
	}

	@Override
	public float getLength () {
		return length;
	}

	@Override
	public Point3f getOrigin () {
		return origin;
	}
	
	@Override
	public Vector3f getDirection () {
		return direction;
	}

	public void setLength(float newLength) {
		Point3f endPoint = new Point3f(direction);
		endPoint.scale (newLength);
		endPoint.add (origin);
		end		= endPoint;
		length	= newLength;
	}	
	
	//---------------------- Transformations ----------------------//
	@Override
	public void transform (Transformations t) {
		t.apply (end);
		t.apply (origin);
		Vector3f dir = new Vector3f (end);
		dir.sub (getOrigin());
		length		= dir.length ();
		direction	= dir;
	}
	
	@Override
	public void translate (Vector3f translation) {
		origin.add (translation);
		end.add (translation);
	}
	
	public Object clone() {
	    LineSegment l = null;
	    try {
	    	l = (LineSegment) super.clone();
	    } catch(CloneNotSupportedException cnse) {
	      	System.err.println (System.err+"/"+cnse.getMessage ());
	    }
	    
	    l.direction = new Vector3f(direction);
	    l.end		= new Point3f(end);
	    l.length	= length;
	    l.origin	= new Point3f(origin);
	    
	    return l;
	}

}
