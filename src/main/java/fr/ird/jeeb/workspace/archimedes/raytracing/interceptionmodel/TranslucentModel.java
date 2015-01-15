package fr.ird.jeeb.workspace.archimedes.raytracing.interceptionmodel;

import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Shape;
import fr.ird.jeeb.workspace.archimedes.raytracing.ray.Ray;

/**
 * Translucent Model Class (intensity of the ray which passes through is decreased by a constant factor)
 * @author Cresson
 *
 */
public class TranslucentModel extends SurfacicInterceptionModel {

	private float transparency; // [0, 1], 0: opaque; 1: completely transparent

	/**
	 * Constructor
	 */
	public TranslucentModel (Shape shape, float transparency) {
		this.transparency = transparency;
	}

	@Override
	public Intersection interception(Ray ray, Shape shape) {
		float incomingRayIntensity = ray.getIntensity();
		float outcomingRayIntensity = incomingRayIntensity*transparency;
		ray.setIntensity(outcomingRayIntensity);
		Intersection firstIntersection = shape.getNearestIntersection(ray.getLineSupport());
		if (firstIntersection!=null)
			return new Intersection(firstIntersection.getDistance(), null);
		return null;
	}

}
