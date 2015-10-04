package jeeb.workspace.sunrapp.geometry.utils;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

/**
 * Converts from polar to cartesian coordinates or from cartesian coordinates to polar coordinates
 * @author dauzat
 */
public class CoordinatesConversion {

	/**
	 * Converts polar to cartesian coordinates
	 * @param zenith
	 * @param azimuth
	 * @return cartesian coordinates
	 */
	static public Vector3f polarToCartesian (float zenith, float azimuth) {
		Point3f dir = new Point3f ();
		dir.z = (float) Math.cos (zenith);
		dir.x = dir.y = (float) Math.sin (zenith);
		// dir.x *= Math.sin(azimuth);
		// dir.y *= Math.cos(azimuth);
		// float az= (float) (azimuth-(Math.PI/2));
		dir.x *= -Math.sin (azimuth);
		dir.y *= Math.cos (azimuth);
		// dir= dir.multiply(-1);

		// direction.setVectorPoint(dir);
		Vector3f direction = new Vector3f (dir);
		return direction;
	}
	/**
	 * Converts cartesian to polar coordinates.
	 * @return zenith and azimuth in radians
	 */
	static public Point2f cartesianToPolar (Vector3f normalizedDirection) {
		float zenith = (float) Math.acos (normalizedDirection.z);
		Vector2f projection= new Vector2f (normalizedDirection.x, normalizedDirection.y);
		projection.normalize();
		float azimuth = projection.angle (new Vector2f (0, 1)); 
		
		return new Point2f (zenith, azimuth);
	}
	
}
