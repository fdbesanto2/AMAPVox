/*
 * Copyright (C) 2016 UMR AMAP (botAnique et Modélisation de l'Architecture des Plantes et des végétations.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.amap.amapvox.io.tls.rxp;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

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
        
        this.angle = Math.toDegrees(Math.acos(direction.z));
    }

}
