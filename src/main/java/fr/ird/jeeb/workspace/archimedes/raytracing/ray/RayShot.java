package fr.ird.jeeb.workspace.archimedes.raytracing.ray;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.raytracing.scatteringmodel.ScatteringModel;

/**
 * RayShot class
 * Object which contains informations associated to the end of the propagation of a ray.
 * 	-	absorption	:	true if the resulting behavior of the shot is an absorption
 * 	-	scattering	:	true if the resulting behavior of the shot is a scattering
 * 	-	nothing		:	true when the ray encounters nothing
 * 	-	intensity	:	returned/absorbed intensity of the shot
 * 	-	distance	:	distance traveled by the ray (from the last scattering or from the source)
 * 	-	location	:	location of the shot
 * @author Cresson
 *
 */
public class RayShot{
	public static final float	NO_INTENSITY_RETURNING = Float.NaN;

	private boolean	absorption;
	private boolean	scattering;
	private boolean	nothing;
	private float	intensity;
	private float	distance;
	private Point3f	location;
	private Vector3f		scatteringNormal;
	private ScatteringModel	scatteringModel;
	
	
	/**
	 * Constructor #1 (default initialization)
	 */
	public RayShot() {
		this(false, false, false, Float.NaN, null, 0f);
	}
	
	/**
	 * Constructor #2 (returning results)
	 * @param absorbtion	true if the resulting behavior of the shot is an absorption
	 * @param scattering	true if the resulting behavior of the shot is a scattering
	 * @param nothing		true when the ray encounters nothing
	 * @param intensity		returned intensity of the shot
	 * @param shot			location of the shot
	 * @param distance		distance traveled by the ray 
	 */
	public RayShot(boolean absorbtion, boolean scattering, boolean nothing, float intensity, Point3f shot, float distance) {
		this.absorption = absorbtion;
		this.scattering	= scattering;
		this.nothing	= nothing;
		this.location	= shot;
		this.distance	= distance;
		this.intensity	= intensity;
		this.scatteringNormal	= null;	// scattering normal
		this.scatteringModel	= null;	// scattering model

	}
	
	/**
	 * @return true when the shot has not happen, false when the shot has happen, i.e. scattering, absorption or no encountered objects
	 */
	public boolean notEncountered() {
		return !scattering & !nothing & !absorption;
	}
	
	/**
	 * @return the distance between the last ray origin and the shot location
	 */
	public float getDistance(){
		return distance;
	}
	
	/**
	 * @return the returning intensity (when scattering) or the absorbed intensity (when absorption)
	 */
	public float getIntensity() {
		return intensity;
	}
	
	/**
	 * @return true if the ray shot is a scattering
	 */
	public boolean scattering(){
		return scattering;
	}
	
	/**
	 * @return true if the ray shot is an absorption
	 */
	public boolean absorption(){
		return absorption;
	}
	
	/**
	 * @return the shot location
	 */
	public Point3f getLocation(){
		return location;
	}

	/**
	 * Set a null shot: It happen when the ray exits the scene
	 */
	public void setNullShot(){
		
		this.absorption	= false;							// no absorption
		this.scattering	= false;							// no scattering
		this.nothing	= true; 							// nothing = yes (no artnode encountered)
		this.intensity	= NO_INTENSITY_RETURNING;			// no returning intensity
		this.location	= null;								// no location
		this.distance	= Float.MAX_VALUE	;				// infinite distance
	}
	
	/**
	 * Set an "absorption" shot: It happen when the ray is absorbed by a target
	 * @param location		shot location
	 * @param distance		distance between the shot location and the last ray origin
	 * @param intensity		intensity of the absorbed ray
	 */
	public void setAbsorbtionShot(Point3f location, float distance, float intensity) {
		this.absorption	= true;								// absorption = yes
		this.scattering	= false;							// no scattering
		this.nothing	= false;							// nothing = no (artnode encountered)
		this.intensity	= intensity;						// absorbed intensity
		this.location	= location;							// location of the shot
		this.distance	= distance;							// shot distance
	}
	
	/**
	 * Set a "scattering" shot: It happen when the ray is scattered by a target, or
	 * when the ray enter/leaves a medium. In this case, it isn't really a "true scattering"
	 * because the ray is not alterated/deviated.
	 * @param trueScattering		true when it's a real scattering, false if it's just an entering/exiting in a medium
	 * @param incomingRayIntensity	returning intensity of the scattered ray
	 * @param location				shot location
	 * @param distance				distance between the shot location and the last ray origin
	 */
	public void setScatteringShot(boolean trueScattering, float incomingRayIntensity, Point3f location, float distance,
			ScatteringModel scatteringModel, Vector3f scatteringNormal) {
		this.absorption	= false;							// no absorption
		this.scattering = trueScattering;					/*	Depends if the interception.getNormal is null
		 													 *	i.e. the ray is entering/leaving a medium node 
		 													 *	or if it's a scattering.
		 													 */
		this.nothing	= false;							// nothing = no (artnode encountered)
		this.intensity	= incomingRayIntensity;				// returning intensity
		this.location	= location;							// location of the shot
		if (trueScattering){
			this.distance = distance;						// shot distance
			this.scatteringModel 	= scatteringModel;
			this.scatteringNormal	= scatteringNormal;
		}
		else
			this.distance += distance;
		
		
	}
	
	public ScatteringModel getScatteringModel() {
		return scatteringModel;
	}
	
	public Vector3f getScatteringNormal(){
		return scatteringNormal;
	}

}
