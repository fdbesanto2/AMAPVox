package fr.ird.jeeb.workspace.archimedes.raytracing.ray;

import fr.ird.jeeb.workspace.archimedes.raytracing.RadiativeModel;

/**
 * RaySurroundingContext class
 * Object which contains informations of a ray surrounding
 * 	-	insideMedium	:	true if the ray is inside a medium (e.g. turbid), false if not
 * 	-	rayInter		:	the first rayIntersection encountered by the ray
 * 	-	radiativeModel	:	the radiativeModel of the first entity encountered by the ray (e.g. medium, or simple scatterer)  
 * @author cresson, nov 2012
 *
 */
public class RaySurroundingContext implements Cloneable{
	public boolean						insideMedium;
	public RayIntersection				rayInter;
	public RadiativeModel				radiativeModel;
	
	/**
	 * Constructor #1
	 * @param insideMedium		true if the ray intersection is in a medium
	 * @param rayInter			ray intersection (can be null, for init. or when the ray encounter no object)
	 * @param radiativeModel	radiative model of the medium (can be null, for init. or when the 
	 * 							encountered object has no radiative model, e.g. when scattering inside turbid medium)
	 */
	public RaySurroundingContext(boolean insideMedium, RayIntersection rayInter, RadiativeModel radiativeModel) {
		this.insideMedium		= insideMedium;
		this.rayInter			= rayInter;
		this.radiativeModel		= radiativeModel;
	}
	
	/**
	 * Cloning
	 */
	public Object clone() {
		RaySurroundingContext context = null;
		try {
			context = (RaySurroundingContext) super.clone();
		} catch (CloneNotSupportedException cnse) {
			System.err.println(System.err+"/"+cnse.getMessage ());
		}
		
		context.insideMedium	= insideMedium;
		context.radiativeModel	= radiativeModel; // Just passing a reference for the RM
		if (rayInter!=null)
			context.rayInter = (RayIntersection) rayInter.clone();
		else
			context.rayInter = null;
		return context;
	}
}
