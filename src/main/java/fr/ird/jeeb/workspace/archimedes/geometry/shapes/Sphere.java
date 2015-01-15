/**
 * 
 */
package fr.ird.jeeb.workspace.archimedes.geometry.shapes;

import java.util.ArrayList;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.geometry.HalfLine;
import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;
import fr.ird.jeeb.workspace.archimedes.geometry.Line;
import fr.ird.jeeb.workspace.archimedes.geometry.LineElement;
import fr.ird.jeeb.workspace.archimedes.geometry.LineSegment;
import fr.ird.jeeb.workspace.archimedes.geometry.Transformations;

/**
 * Sphere class
 * @author Cresson/DAUZAT, august 2012
 *
 */
public class Sphere extends Shape implements VolumicShape{

	protected 	Point3f	center;
	private 	float	radius;
	
	/**
	 * Constructor with the center and the radius
	 */
	public Sphere (Point3f center, float radius) {
		this.center = center;
		this.radius = Math.abs (radius);
	}
	
	public Point3f getCenter () {
		return new Point3f (center);
	}
	
	public float getRadius () {
		return radius;
	}

	public void scale (double scale) {
		this.radius *= scale;
	}

	public void translate (Point3f translation) {

		this.center.add (translation);
	}
	
	public Object clone() {
	    Sphere s = null;
	    try {
	    	s = (Sphere) super.clone();
	    } catch(CloneNotSupportedException cnse) {
	    	System.err.println (System.err+"/"+cnse.getMessage ());
	    }
	    
	    s.center = new Point3f(center); 
	    s.radius = radius;
	    return s;
	}
	
	@Override
	/**	Tests if sphere is intersected by the specified line element
	 * 
	 * !!! This test returns the boolean of the VOLUMIC intersection !!!
	 * 
	 * @param linel					line element
	 * @return true if sphere is intersected by the line element. false if not.
	 */
	public boolean isIntersectedBy (LineElement linel) {
		float distanceToCenter = distanceToPoint(linel.getDirection (), linel.getOrigin ());
		if (distanceToCenter > radius) // distance line-center
			return false;
		// LINE
		if (linel instanceof Line) {
			return true;
		}
		else {
			Vector3f AC = new Vector3f(center); AC.sub(linel.getOrigin ());
			float k = AC.dot (linel.getDirection ());
			float[] d = computeIntersections(k,radius,distanceToCenter);
			if (0>d[0] & 0>d[1])
				return false;
			// HALF-LINE
			if (linel instanceof HalfLine)
				return true;
			// LINE-SEGMENT
			else if ((d[0]<linel.getLength () | d[1]<linel.getLength ()) & Math.abs (k)<(radius+linel.getLength()))
				return true;				
		}
		return false;
	}
	
	/*
	 * @param k					distance to the nearest point of the line element to the center of the sphere
	 * @param radius			sphere radius
	 * @param distanceToCenter	distance(center, line element)
	 * @return intersection distances (from the nearest point of the line element to the center of the sphere)
	 */
	private float[] computeIntersections(float k, float radius, float distanceToCenter) {
		float D = (float) Math.sqrt (radius*radius - distanceToCenter*distanceToCenter);
		float[] d = new float[2];
		d[0] = k - D;
		d[1] = k + D;
		return d;

	}
	
	/*
	 * @param d					float[]
	 * @return sorted d[]
	 */
	private float[] sort(float[] d) {
		if (d[0]>d[1]) {
			float tmp = d[0];
			d[0] = d[1];
			d[1] = tmp;
		}
		return d;
	}
	/*
	 * @return The shortest distance between the line and the point
	 */
	private float distanceToPoint (Vector3f direction, Point3f origin) {

		Vector3f vpp = new Vector3f (origin);
		vpp.sub (center);

		vpp.cross (vpp, direction);

		return vpp.length ();
	}

	@Override
	public void transform (Transformations transform) {
		
		transform.apply (center);
		
		// Mean square error between 3 axes norms
		float tol = 0.001f;
		Matrix4f transformMatrix = transform.getMatrix ();
		float[] norm = new float[3];
		norm[0] = (float)Math.sqrt (transformMatrix.m00*transformMatrix.m00 + transformMatrix.m10*transformMatrix.m10 + transformMatrix.m20*transformMatrix.m20);
		norm[1] = (float)Math.sqrt (transformMatrix.m01*transformMatrix.m01 + transformMatrix.m11*transformMatrix.m11 + transformMatrix.m21*transformMatrix.m21);
		norm[2] = (float)Math.sqrt (transformMatrix.m02*transformMatrix.m02 + transformMatrix.m12*transformMatrix.m12 + transformMatrix.m22*transformMatrix.m22);
		float mNorm = 0f;
		for (float n: norm)
			mNorm += n;
		mNorm /= 3;
		float err = 0f;
		for (float n: norm)
			err += (n-mNorm);
		if (err>tol)
			System.err.println ("Non isotropic scaling !!!");
		
		scale(mNorm);
	}

	@Override
	public ArrayList<Intersection> getIntersections (LineElement linel) {
		ArrayList<Intersection> intersections = new ArrayList<Intersection>();
		float distanceToCenter = distanceToPoint(linel.getDirection (), linel.getOrigin ());
		if (distanceToCenter > radius) // distance line-center
			return intersections;
		Vector3f AC = new Vector3f(center); AC.sub(linel.getOrigin ());
		float k = AC.dot (linel.getDirection ());
		float[] d = sort(computeIntersections(k,radius,distanceToCenter));
		Intersection i1 = null;
		Intersection i2 = null;
		
		// LINE
		if ((linel instanceof Line)) {
			i1 = computeIntersection(linel, d[0]);
			i2 = computeIntersection(linel, d[1]);
		}
		else {
			// HALF-LINE & LINE SEGMENT
			if (0>d[0] & 0>d[1])
				return intersections;
			if (linel instanceof LineSegment)
				if (!((d[0]<linel.getLength () | d[1]<linel.getLength ()) & Math.abs (k)<(radius+linel.getLength())))
					return intersections;
			if (d[0]>0 & d[0]<linel.getLength ())
				i1 = computeIntersection(linel, d[0]);
			if (d[1]>0 & d[1]<linel.getLength ())
				i2 = computeIntersection(linel, d[1]);
		}
		
		if (i1!=null)
			intersections.add(i1);
		if (i2!=null)
			intersections.add(i2);
		
		return intersections;
	}
	
	private Intersection computeIntersection(LineElement linel, float d) {
		
		// Intersection point
		Point3f p1 = new Point3f(linel.getDirection ()); 
		p1.scale (d); 
		p1.add (linel.getOrigin ());
		
		// Intersection normal
		Vector3f norm1 = new Vector3f (p1);
		norm1.sub(center);
		norm1.normalize ();

		return new Intersection(d,norm1);
	}
	
	@Override
	public Intersection getNearestIntersection (LineElement linel) {
		ArrayList<Intersection> intersections = getIntersections (linel);
		if (intersections.size ()!=0)
			return intersections.get (0);
		return null;
	}

	@Override
	public boolean contains (Point3f point) {
		return point.distance (center)<radius;
	}


}
