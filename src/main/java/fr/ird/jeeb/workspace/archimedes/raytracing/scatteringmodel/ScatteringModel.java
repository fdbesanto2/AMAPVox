package fr.ird.jeeb.workspace.archimedes.raytracing.scatteringmodel;

import javax.vecmath.Vector3f;


public abstract class ScatteringModel {


	/**
	 * @param normal		normal of the surface intersecting the ray
	 * @return A random scattering direction 
	 */
	public abstract Vector3f getRandomScatteringDirection (Vector3f incomingDirection, Vector3f normal);
	
	/**
	 * @param normal		normal of the surface intersecting the ray
	 * @param scatteringDirection		direction of the scattered ray (shot->sensor direction)
	 * @return the returned amount of intensity to the sensor
	 */
	public abstract float getDirectionalReflectanceFactor (Vector3f normal, Vector3f scatteringDirection);
	
	/**
	 * 
	 * @param normal		normal of the surface intersecting the ray
	 * @param direction		direction of the incoming ray
	 * @return the child ray amount of intensity
	 */
	public abstract float getScatteringFactor (Vector3f normal, Vector3f direction);
	
	public abstract void setReflectance(float reflectance);
	public abstract void setTransmittance(float transmittance);
	public abstract float getReflectance();
	public abstract float getTransmittance();

}
