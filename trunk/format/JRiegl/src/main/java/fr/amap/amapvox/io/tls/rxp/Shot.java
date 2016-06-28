package fr.amap.amapvox.io.tls.rxp;

import fr.amap.commons.util.Filter;
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
public class Shot{

    public int nbEchos;
    public double time;
    public Point3d origin;
    public Vector3d direction;
    public double ranges[] = null;
    public float intensities[];
    public int classifications[];
    
    /**
     *This is the ratio of the received power to the power that would
    be received from a white diffuse target at the same distance
    expressed in dB. The reflectance represents a range independent
    property of the target.  The surface normal of this target is assumed
    to be in parallel to the laser beam direction.
     */
    public float reflectances[];
    public float deviations[];
    public float amplitudes[];
    public double times[];
    
    /**
     * Optional echoes attributes
     */
    public double[][] echoesAttributes;

    public double angle;

    public Shot() {

    }
    
    public Shot(Shot shot) {
        this.amplitudes = shot.amplitudes;
        this.angle = shot.angle;
        this.classifications = shot.classifications;
        this.deviations = shot.deviations;
        this.direction = shot.direction;
        this.intensities = shot.intensities;
        this.nbEchos = shot.nbEchos;
        this.time = shot.time;
        this.origin = shot.origin;
        this.ranges = shot.ranges;
        this.reflectances = shot.reflectances;
    }
    
    public Shot(int nbEchos,
            double originX, double originY, double originZ,
            double directionX, double directionY, double directionZ,
            double[] ranges) {

        this.nbEchos = nbEchos;
        
        this.origin = new Point3d(originX, originY, originZ);
        this.direction = new Vector3d(directionX, directionY, directionZ);
        
        this.ranges = ranges;
    }

    public Shot(int nbEchos, double time,
            double originX, double originY, double originZ,
            double directionX, double directionY, double directionZ,
            double[] ranges) {

        this.nbEchos = nbEchos;
        this.time = time;
        
        this.origin = new Point3d(originX, originY, originZ);
        this.direction = new Vector3d(directionX, directionY, directionZ);
        
        this.ranges = ranges;
    }
    
    public void setReflectances(float[] reflectances){
        this.reflectances = reflectances;
    }
    
    public void setDeviations(float[] deviations){
        this.deviations = deviations;
    }
    
    public void setAmplitudes(float[] amplitudes){
        this.amplitudes = amplitudes;
    }
    
    public void setIntensities(float[] intensities){
        this.intensities = intensities;
    }
    
    public void setTimes(double[] times){
        this.times = times;
    }

    public Shot(int nbEchos, Point3d origin, Vector3d direction, double[] ranges) {

        this.origin = origin;
        this.nbEchos = nbEchos;
        this.direction = direction;
        this.ranges = ranges;
    }

    public Shot(int nbEchos, Point3d origin, Vector3d direction, double[] ranges, int[] classifications, float[] intensities) {

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
        
        
        //attention ! : les angles sont les mêmes que z soit positif ou négatif
        this.angle = FastMath.toDegrees(FastMath.acos(direction.z));
    }

}
