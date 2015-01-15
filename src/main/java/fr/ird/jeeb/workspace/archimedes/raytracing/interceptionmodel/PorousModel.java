package fr.ird.jeeb.workspace.archimedes.raytracing.interceptionmodel;

import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Shape;
import fr.ird.jeeb.workspace.archimedes.raytracing.ray.Ray;

/**
 * Porous Model
 * @author Cresson, Jan. 2013
 *
 */
public class PorousModel extends SurfacicInterceptionModel{

	private float transparency; // [0, 1], 0: opaque

	/**
	 * Constructor with transparency
	 */
	public PorousModel (float transparency) {
		this.transparency	= transparency;
	}

	@Override
	public Intersection interception(Ray ray, Shape shape) {
		float a = (Math.random () < transparency) ? 1 : 0; 
		float incomingRayIntensity = ray.getIntensity();
		float outcomingRayIntensity = incomingRayIntensity*a;
		ray.setIntensity(outcomingRayIntensity);
		Intersection firstIntersection = shape.getNearestIntersection(ray.getLineSupport());
		if (firstIntersection!=null)
			return new Intersection(firstIntersection.getDistance(), null);
		return null;
	}
}
