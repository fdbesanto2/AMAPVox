
package fr.ird.jeeb.workspace.archimedes.raytracing.scatteringmodel;

import javax.vecmath.Vector3f;

/**
 * Lambertian model
 * @author Dauzat, Cresson; August 2012
 */
public class Lambertian extends ScatteringModel {

	private float reflectance;
	private float transmittance;
	
	public Lambertian(){
		reflectance = 1f;
		transmittance = 0f;
	}
	
	public Lambertian(float reflectance, float transmittance) {
		this.reflectance	= reflectance;
		this.transmittance	= transmittance;
	}

	@Override
	public Vector3f getRandomScatteringDirection (Vector3f incomingDirection, Vector3f normal) {	

		/* Code Jean #1 */
		
//		// angle from normal
//		double angle = Math.asin((Math.random()));
//		Vector3d rotationAxis = VectorUtils.getOrthogonalVector (normal);
//		Vector3d scatteringDirection = Transformations.rotateAroundAxis3d (normal, rotationAxis, angle);
//
//		// azimuth angle
//		double azimuth = 2 * Math.PI * Math.random ();
//		scatteringDirection = Transformations.rotateAroundAxis3d (scatteringDirection, normal, azimuth);

		/* Code Jean #2 */
		
//		Transformations t = new Transformations ();
//		
//		// angle from normal
//		float angle = (float) Math.asin((Math.random()));
//		Vector3f rotationAxis = VectorUtils.getOrthogonalVector (normal);
//		t.setRotationAroundAxis (rotationAxis, angle);
//		Vector3f scatteringDirection = new Vector3f (normal);
//
//		// azimuth angle
//		float azimuth = (float) (2 * Math.PI * Math.random ());
//		t.setRotationAroundAxis (normal, azimuth);
//
//		t.apply (scatteringDirection);
//
//		return scatteringDirection;
		
		/* Code Remi */
		Vector3f v = new Vector3f();
		while(true){
			v.x = 2*((float)Math.random())-1f;
			v.y = 2*((float)Math.random())-1f;
			v.z = 2*((float)Math.random())-1f;
			if (v.length()<1)
				break;
		}
		v.normalize();
		v.add(normal);
		v.normalize();
		return v;
	}
	
	@Override
	public float getDirectionalReflectanceFactor (Vector3f normal, Vector3f scatteringDirection)	{
		/* 
		 * Warning: 
		 * incoming direction [dot] surface normal IS NOT the zenithal angle 
		 * must take: -1*incoming direction [dot] surface normal
		*/
		float cosTheta = normal.dot(scatteringDirection);
		float returningIntensity = 	Math.abs(cosTheta);
		if (cosTheta<0)
			return transmittance*returningIntensity;
		else
			return reflectance*returningIntensity;

	}

	@Override
	public void setReflectance(float reflectance) {
		this.reflectance = reflectance;
	}

	@Override
	public void setTransmittance(float transmittance) {
		this.transmittance = transmittance;
	}

	@Override
	public float getReflectance() {
		return reflectance;
	}

	@Override
	public float getTransmittance() {
		return transmittance;
	}

	@Override
	public float getScatteringFactor(Vector3f normal, Vector3f direction) {
		return (transmittance+reflectance);
	}


}
