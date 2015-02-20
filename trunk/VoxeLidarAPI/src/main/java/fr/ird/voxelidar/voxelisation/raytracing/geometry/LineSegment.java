package fr.ird.voxelidar.voxelisation.raytracing.geometry;

import java.util.ArrayList;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import fr.ird.voxelidar.voxelisation.raytracing.geometry.shapes.Shape;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * Oriented line segment defined by 2 points: origin and end
 * @author Dauzat/Cresson; August 2012
 */
public class LineSegment implements Cloneable, LineElement {
	
	private Point3d		origin;
	private Point3d		end;
	private Vector3d	direction;
	private double		length;

	/**
	 * Constructor with 2 points
	 * @param origin	origin of the line segment
	 * @param end		end of the line segment
	 */
	public LineSegment (Point3d origin, Point3d end) {
		direction = new Vector3d (end);
		direction.sub (origin);
		this.length		= direction.length ();
		if (direction.length()!=0f) direction.normalize ();
		this.end		= new Point3d(end);
		this.origin		= new Point3d(origin);
	}
	
	/**
	 * Constructor with 1 point, a direction and a length
	 * @param origin	origin of the line segment
	 * @param direction	direction of the line segment
	 * @param length	length of the line segment
	 */
	public LineSegment (Point3d origin, Vector3d direction, double length) {
		end = new Point3d (0f,0f,0f);
		end.scaleAdd (length, direction, origin);
		this.direction 	= new Vector3d (direction);
		this.origin		= new Point3d(origin);
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
	public Point3d getEnd () {
		return end;
	}

	@Override
	public double getLength () {
		return length;
	}

	@Override
	public Point3d getOrigin () {
		return origin;
	}
	
	@Override
	public Vector3d getDirection () {
		return direction;
	}

	public void setLength(double newLength) {
		Point3d endPoint = new Point3d(direction);
		endPoint.scale (newLength);
		endPoint.add (origin);
		end		= endPoint;
		length	= newLength;
	}
	
	@Override
	public void translate (Vector3d translation) {
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
	    
	    l.direction = new Vector3d(direction);
	    l.end		= new Point3d(end);
	    l.length	= length;
	    l.origin	= new Point3d(origin);
	    
	    return l;
	}

}
