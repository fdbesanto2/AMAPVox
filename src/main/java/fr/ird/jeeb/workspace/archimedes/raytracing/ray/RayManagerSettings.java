package fr.ird.jeeb.workspace.archimedes.raytracing.ray;

/**
 * RayManagerSettings Class
 * Contains parameters for RayManager class
 * 	- maximumIteration:		maximum iteration for one intersection computing
 * 	- maximumLength:		maximum distance for one intersection computing
 * 	- absoluteCoordinates:	when set to false, all shot location are returned in relative
 * 							coordinates, i.e. modulo scene boundingbox coordinates. 
 * @author Cresson, Sept. 2012
 *
 */
public class RayManagerSettings {
	
	private int		maximumIteration;
	private float	maximumLength;
	private boolean	absoluteCoordinates;
	
	/*
	 * Constructors
	 */
	public RayManagerSettings(int maximumIteration, float maximumLength) {
		this(maximumIteration, maximumLength, true);
	}
	public RayManagerSettings (int maximumIteration) {
		this(maximumIteration, true);
	}
	public RayManagerSettings (float maximumLength) {
		this(maximumLength, true);
	}
	public RayManagerSettings () {
		this(true);
	}
	public RayManagerSettings (int number, boolean absoluteCoordinates) {
		this(number, Float.MAX_VALUE, absoluteCoordinates);
	}
	public RayManagerSettings (float length, boolean absoluteCoordinates) {
		this(Integer.MAX_VALUE, length, absoluteCoordinates);
	}
	public RayManagerSettings(boolean absoluteCoordinates) {
		this(Integer.MAX_VALUE, Float.MAX_VALUE, absoluteCoordinates);
	}
	public RayManagerSettings(int maximumIteration, float maximumLength, boolean absoluteCoordinates) {
		this.maximumIteration		= maximumIteration;
		this.maximumLength			= maximumLength;
		this.absoluteCoordinates	= absoluteCoordinates;
		
	}
	
	
	/*
	 * Getters
	 */
	public int getMaximumIterationNumber() {
		return maximumIteration;
	}
	public float getMaximumLength() {
		return maximumLength;
	}
	public boolean absoluteCoordinates() {
		return absoluteCoordinates;
	}
}
