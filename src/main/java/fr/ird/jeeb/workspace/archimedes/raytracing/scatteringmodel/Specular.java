package fr.ird.jeeb.workspace.archimedes.raytracing.scatteringmodel;

import javax.vecmath.Vector3f;


/**
 * Model mirror like reflection
 * @author Cresson, Dauzat; August 2012
 */
public class Specular extends ScatteringModel {

	private float reflectance;
	private float transmittance;
	
	public Specular() {
		reflectance		= 1.0f;
		transmittance	= 0.0f;
	}
	
	public Specular(float reflectance, float transmittance) {
		this.reflectance	= reflectance;
		this.transmittance	= transmittance;
	}
	
	@Override
	public Vector3f getRandomScatteringDirection (Vector3f incomingDirection, Vector3f normal) {
		
		// outDir = inDir - 2*prodScal(ray,norm)*norm;
		Vector3f tmp = new Vector3f (normal);
		tmp.scaleAdd (-2*normal.dot (incomingDirection),incomingDirection);

		return tmp;
	}

	@Override
	public float getDirectionalReflectanceFactor (Vector3f normal, Vector3f direction)	{
		// TODO multiply by mormalisation factor
		return 0.0f;
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
		// TODO Auto-generated method stub
		return 0.8f;
	}

}
