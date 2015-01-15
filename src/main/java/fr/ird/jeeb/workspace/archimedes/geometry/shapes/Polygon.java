/**
 * 
 */
package fr.ird.jeeb.workspace.archimedes.geometry.shapes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.geometry.HalfLine;
import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;
import fr.ird.jeeb.workspace.archimedes.geometry.Line;
import fr.ird.jeeb.workspace.archimedes.geometry.LineElement;
import fr.ird.jeeb.workspace.archimedes.geometry.Transformations;


/**
 * 
 * @author Dauzat; August 2012
 */
@Deprecated
public class Polygon extends Shape {
	
	private Point3f[] vertices;
	
	
	public Polygon (Point3f[] points) {
		this.vertices = points;
	}

	@Override
	public boolean isIntersectedBy (LineElement linel) {
		
		if (getNearestIntersection (linel) == null)
			return false;
		
		return true;
	}

	@Override
	public ArrayList<Intersection> getIntersections (LineElement linel) {
		
		ArrayList<Intersection> inters = new ArrayList<Intersection>();
		inters.add (getNearestIntersection (linel));
		
		return inters;
	}

	@Override
	public Intersection getNearestIntersection (LineElement linel) {

		if (linel instanceof Line)
			return intersectionSegmentPolygon (linel, -Float.MAX_VALUE, Float.MAX_VALUE);
		
		if (linel instanceof HalfLine)
			return intersectionSegmentPolygon (linel, 0, Float.MAX_VALUE);
		
		return intersectionSegmentPolygon (linel, 0, linel.getLength ());
	}


	public Intersection intersectionSegmentPolygon (LineElement seg, float minDist, float maxDist) {
		
		double dPolSeg;
		double dist_to_plane;

		Vector3f polNormal = computeNormal (this.vertices);								//polygon's normal
		
		dPolSeg = polNormal.dot (seg.getDirection ());
		
		// the direction's vector is // to the plane
		if (Math.abs(dPolSeg) < 0.0000001){
			return null;
		}

		//calculate the length between the origin and the intersection
		Vector3f v0 = new Vector3f (vertices[0]);
		double plcst = v0.dot(polNormal);				//Constant D of Eq.: P*N-D=0 
		Vector3f vOrigin = new Vector3f(seg.getOrigin ());
		dist_to_plane = (-polNormal.dot (vOrigin) + plcst) / dPolSeg;


		if (dist_to_plane < minDist) {					//  discard if negative value (intersection before the origin)
			return null;
		}
		if (dist_to_plane > maxDist) {		// discard distance to plane > segment 
			return null;
		}

		// calculate the intersection point with polygon plane
		Point3f intersection= new Point3f (seg.getDirection ());
		intersection.scale ((float) dist_to_plane);
		intersection.add (seg.getOrigin ());
		
		if (isPointInsidePolygon (intersection, this.vertices))
			return new Intersection ((float) dist_to_plane, polNormal);

		return null;
	}

	
	/**
	 * Calculation of the normal to a polygon using Newell's Method 
	 */
	public static Vector3f computeNormal (Point3f[] points) {
		
		return computeNormal (Arrays.asList(points));
	}

	/**
	 * Calculation of the normal to a polygon using Newell's Method 
	 */
	public static Vector3f computeNormal (List<Point3f> points) {
		
		if (points.size () < 3)
			return null;
		
		Vector3f crossPro = new Vector3f ();
		for (int n=0; n<points.size ()-1; n++) {
			Vector3f v1 = new Vector3f (points.get (n+1));
			v1.sub (points.get(n));
			Vector3f v2 = new Vector3f (points.get((n+2)%points.size ()));
			v2.sub (points.get (n+1));
			
			crossPro.cross (v1, v2);

			if (crossPro.length () > 0) {
				crossPro.normalize ();
				return crossPro;
			}
		}
		
		return null;
	}



	@Override
	public void transform (Transformations transform) {
		
		for (int v=0; v<vertices.length; v++) {
			transform.apply (vertices[v]);
		}
	}

	
	public static boolean isPointInsidePolygon (Point3f point, Point3f[] pts) {
		
		Vector3f v1 = new Vector3f(pts[0]);
		v1.sub (point);
		v1.normalize ();
		double sumAngles = 0;
		for (int p=1; p<=pts.length; p++) {
			Vector3f v2 = new Vector3f(pts[p%pts.length]);
			v2.sub (point);
			v2.normalize ();

			double dot = Math.min (1,v1.dot (v2));
			sumAngles += Math.acos (dot);
			v1 = new Vector3f(v2);
		}

		if ((2*Math.PI) - sumAngles > 0.0001f)
			return false;

		return true;
	}


}
