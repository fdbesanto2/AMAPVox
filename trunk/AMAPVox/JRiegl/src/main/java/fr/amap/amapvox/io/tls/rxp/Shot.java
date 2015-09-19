package fr.amap.amapvox.io.tls.rxp;

import fr.amap.amapvox.commons.util.Filter;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.commons.math3.util.FastMath;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Shot {

    public int nbEchos;

    public Point3d origin;
    public Vector3d direction;
    public double ranges[];
    public int intensities[];
    public int classifications[];
    
    /**
     *This is the ratio of the received power to the power that would
    be received from a white diffuse target at the same distance
    expressed in dB. The reflectance represents a range independent
    property of the target.  The surface normal of this target is assumed
    to be in parallel to the laser beam direction.
     */
    public float reflectances[];
    
    //public float deviations[];

    public double angle;
    private static List<Filter> filters;

    public Shot() {

    }

    public static void setFilters(List<Filter> filters) {
        Shot.filters = filters;
    }

    public Shot(int nbEchos,
            double originX, double originY, double originZ,
            double directionX, double directionY, double directionZ,
            double[] ranges) {

        this.origin = new Point3d(originX, originY, originZ);
        this.nbEchos = nbEchos;
        this.direction = new Vector3d(directionX, directionY, directionZ);
        this.ranges = ranges;
    }
    
    public Shot(int nbEchos,
            double originX, double originY, double originZ,
            double directionX, double directionY, double directionZ,
            double[] ranges, float[] reflectances/*, float[] deviations*/) {

        this.origin = new Point3d(originX, originY, originZ);
        this.nbEchos = nbEchos;
        this.direction = new Vector3d(directionX, directionY, directionZ);
        this.ranges = ranges;
        this.reflectances = reflectances;
        //this.deviations = deviations;
    }


    public Shot(int nbEchos, Point3d origin, Vector3d direction, double[] ranges) {

        this.origin = origin;
        this.nbEchos = nbEchos;
        this.direction = direction;
        this.ranges = ranges;
    }

    public Shot(int nbEchos, Point3d origin, Vector3d direction, double[] ranges, int[] classifications, int[] intensities) {

        this.origin = origin;
        this.nbEchos = nbEchos;
        this.direction = direction;
        this.ranges = ranges;
        this.classifications = classifications;
        this.intensities = intensities;
    }

    public void setOriginAndDirection(Point3d origin, Vector3d direction) {

        this.origin = new Point3d(origin);
        this.direction = new Vector3d(direction);

        calculateAngle();
    }

    public void calculateAngle() {
        this.angle = FastMath.toDegrees(FastMath.acos(direction.z));
    }

    public boolean doFilter() {

        if (filters != null) {

            for (Filter f : filters) {

                switch (f.getVariable()) {
                    case "Angle":
                        switch (f.getCondition()) {

                            case Filter.EQUAL:
                                if (angle != f.getValue()) {
                                    return false;
                                }
                                break;
                            case Filter.GREATER_THAN:
                                if (angle <= f.getValue()) {
                                    return false;
                                }
                                break;
                            case Filter.GREATER_THAN_OR_EQUAL:
                                if (angle < f.getValue()) {
                                    return false;
                                }
                                break;
                            case Filter.LESS_THAN:
                                if (angle >= f.getValue()) {
                                    return false;
                                }
                                break;
                            case Filter.LESS_THAN_OR_EQUAL:
                                if (angle > f.getValue()) {
                                    return false;
                                }
                                break;
                            case Filter.NOT_EQUAL:
                                if (angle == f.getValue()) {
                                    return false;
                                }
                                break;
                        }

                        break;
                }
            }
        }else{
            System.err.println("shot filters not initialized");
        }

        return true;
    }

}
