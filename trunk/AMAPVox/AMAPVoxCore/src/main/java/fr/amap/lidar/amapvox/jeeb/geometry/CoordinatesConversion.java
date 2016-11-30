package fr.amap.lidar.amapvox.jeeb.geometry;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

/**
 * Converts from polar to cartesian coordinates or from cartesian coordinates to
 * polar coordinates
 *
 * @author dauzat
 */
public class CoordinatesConversion {

    /**
     * Converts polar to cartesian coordinates
     *
     * @param zenith zenith
     * @param azimuth azimuth
     * @return cartesian coordinates
     */
    public static Vector3f polarToCartesian(float zenith, float azimuth) {

        Point3f dir = new Point3f();
        dir.z = (float) Math.cos(zenith);
        dir.x = dir.y = (float) Math.sin(zenith);
        dir.x *= -Math.sin(azimuth);
        dir.y *= Math.cos(azimuth);

        Vector3f direction = new Vector3f(dir);
        return direction;
    }

    public static Point2f cartesianToPolar(Vector3f normalizedDirection) {
        float zenith = (float) Math.acos(normalizedDirection.z);
        Vector2f projection = new Vector2f(normalizedDirection.x, normalizedDirection.y);
        projection.normalize();
        float azimuth = projection.angle(new Vector2f(0, 1));

        return new Point2f(zenith, azimuth);
    }

}
