package fr.ird.jeeb.workspace.archimedes.raytracing.ray;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;
import fr.ird.jeeb.workspace.archimedes.geometry.LineSegment;
import fr.ird.jeeb.workspace.archimedes.geometry.utils.VectorUtils;
import fr.ird.jeeb.workspace.archimedes.raytracing.scatteringmodel.ScatteringModel;

/**
 * Ray Class
 * @author Cresson, Aug. 2012
 *
 */
public class Ray implements Cloneable {

	private LineSegment				lineSupport;
	private float					intensity;
	private float					frequency;
	private RaySurroundingContext 	surroundingContext;
	private RayAntecedents			antecedents;
	
	/*
	 * Used to store some informations about the global ray path
	 */
	private class RayAntecedents implements Cloneable {
		private float	pathLength;
		private int		pathOrder;
		public RayAntecedents() {
			pathLength	= 0f;
			pathOrder	= -1;
		}
		private void updatePathLength(float length) {
			pathLength+=length;
		}
		private void updatePathOrder(){
			pathOrder++;
		}
		public Object Clone() {
			RayAntecedents r = null;
			try {
				r = (RayAntecedents) super.clone();
			} catch(CloneNotSupportedException cnse) {
				System.err.println (System.err+"/"+cnse.getMessage ());
			}
			r.pathLength	= pathLength;
			r.pathOrder		= pathOrder;
			return r;
		}
	}
	
	/**
	 * Constructor, with a line support and an intensity
	 * @param lineSupport	ray line support
	 * @param intensity		ray intensity
	 */
	public Ray(LineSegment lineSupport, float intensity) {
		this(lineSupport, intensity, new RaySurroundingContext(false, null, null));
	}
	
	/**
	 * Constructor, with a line support, intensity and the ray surrounding.
	 * Can be used when the ray starts in a partiular medium, e.g. turbid
	 * @param lineSupport			ray line support
	 * @param intensity				ray intensity
	 * @param surroundingContext	ray surrounding informations
	 */
	public Ray(LineSegment lineSupport, float intensity, RaySurroundingContext surroundingContext) {
		this.lineSupport		= lineSupport;
		this.intensity			= intensity;
		this.surroundingContext = surroundingContext;
		this.antecedents		= new RayAntecedents();
	}
	
	public LineSegment getLineSupport() {
		return lineSupport;
	}
	
	public float getIntensity() {
		return intensity;
	}
	
	public float getFrequency() {
		return frequency;
	}
	
	public float getPathLength() {
		return antecedents.pathLength;
	}
	
	public int getPathOrder() {
		return antecedents.pathOrder;
	}
	
	public Object clone() {
	    Ray r = null;
	    try {
	    	r = (Ray) super.clone();
	    } catch(CloneNotSupportedException cnse) {
	      	System.err.println (System.err+"/"+cnse.getMessage ());
	    }
	    
	    r.frequency = frequency;
	    r.intensity = intensity;
	    r.lineSupport			= (LineSegment) lineSupport.clone ();
	    r.surroundingContext	= (RaySurroundingContext) surroundingContext.clone();
	    r.antecedents			= (RayAntecedents) antecedents.Clone();
	    
	    return r;
	}
	
	/**
	 * "Hi-Level" scatering Computation
	 * 	-	00	:	no scatering, no traversal
	 * 	-	01	:	no scatering, but traversal (entering/leaving a medium)
	 * 	-	10	:	scattering, no traversal (true scattering)
	 * 	-	11	:	------------------------------------------------------
	 * @param model	Scattering model
	 * @param inter	Ray-Shape intersection
	 * @return
	 */
	public boolean[] scatter(ScatteringModel model, Intersection inter) {
		
		// Computes the total path length
		antecedents.updatePathLength(inter.getDistance());
		
		// Computes scattering...
		float random = (float) Math.random();
		if (random<model.getTransmittance() | inter.getNormal()==null) {
			// Transmit. (or traversal of a medium)
			return scat( model,  inter, true);
		} else if (random<model.getTransmittance()+model.getReflectance()) {
			// Reflect.
			return scat( model,  inter, false);
		}
		
		// ... or absorption
		return absorb(inter);
				
	}

	/**
	 * Set intensity of the ray
	 * @param value	intensity value to set
	 */
	public void setIntensity(float value) {
		intensity = value;
	}
	
	/**
	 * Set a null intensity
	 */
	public void setNullIntensity() {
		setIntensity(0f);
	}
	
	/**
	 * Returns a boolean[] relative to absorption process
	 */
	private boolean[] absorb(Intersection inter) {
		// The ray origin is set to the intersection point.
		// WARNING: The ray length is obsolete here !
		lineSupport = new LineSegment(
				getLocation(inter),
				lineSupport.getDirection (),
				Float.NaN	);
		return new boolean[] {false,false};
	}
	
	/*
	 * Low-Level Scattering computation
	 * @param model		Scattering Model
	 * @param inter		Ray-Shape intersection
	 * @param transmit	True when transmit, False when reflect 
	 * @return a boolean[] relative to the scattering process (see Hi-Level scattering computation)
	 */
	private boolean[] scat(ScatteringModel model, Intersection inter, boolean transmit) {
		Vector3f	incomingDirection	= lineSupport.getDirection ();
		Point3f		intersectionPoint	= getLocation(inter);
		if (inter.getNormal ()==null) { 
			// This happen only when ray encountered a medium,
			// and is not intercepted by this medium
			
			// Shift origin in incoming direction
			VectorUtils.shift(intersectionPoint, incomingDirection);
			
			// The ray is simply translated
			lineSupport = new LineSegment(	intersectionPoint,
											incomingDirection,
											Float.MAX_VALUE	);
			
			// No intensity is returned to the sensor
			return new boolean[] {false,true};

		}
		// Else (everything below), Computes the scattering
		
		Vector3f	normal				= inter.getNormal ();
		Vector3f	scatteringDirection = model.getRandomScatteringDirection (incomingDirection, normal);
//		boolean		face				= normal.dot(incomingDirection)>0;
				
		if (transmit) {
			// Transmission
			Vector3f tmp = new Vector3f (normal);
			tmp.scaleAdd (-2*normal.dot (scatteringDirection),scatteringDirection);
			scatteringDirection = tmp;
		}
			
		// Shift origin in scattering direction
		VectorUtils.shift(intersectionPoint, scatteringDirection);
		
		// Child ray
		lineSupport = new LineSegment(	intersectionPoint,
										scatteringDirection,
										Float.MAX_VALUE	);
		intensity *= model.getScatteringFactor(	incomingDirection,
												normal	);
		
		// Update path order
		antecedents.updatePathOrder();
		
		// scattering = true
		return new boolean[] {true,false};

	}

	public RaySurroundingContext getSurroundingContext() {
		return surroundingContext;
	}
	
	/**
	 * Locate a point from a given Intersection 
	 */
	public Point3f getLocation(Intersection inter) {
		Point3f intersectionPoint = new Point3f(lineSupport.getDirection ());
		intersectionPoint.scale(inter.distance);
		intersectionPoint.add (lineSupport.getOrigin ());
		return intersectionPoint;
	}
	
}
