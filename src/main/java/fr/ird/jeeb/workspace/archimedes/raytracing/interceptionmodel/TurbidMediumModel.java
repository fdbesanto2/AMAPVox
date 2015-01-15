package fr.ird.jeeb.workspace.archimedes.raytracing.interceptionmodel;

import java.util.ArrayList;
import java.util.Collections;

import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.geometry.HalfLine;
import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Shape;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.VolumicShape;
import fr.ird.jeeb.workspace.archimedes.raytracing.ray.Ray;

/**
 * Turbid Medium Class (Volumic Interception Model)
 * @author Cresson, Jan. 2013
 *
 */
public class TurbidMediumModel extends VolumicInterceptionModel {
	
	private float			interceptionCoefficient;
	
	public TurbidMediumModel(Float interceptionCoefficient) {
		this.interceptionCoefficient	= interceptionCoefficient;
	}

	@Override
	public Intersection interception(Ray ray, Shape shape) {
		
		VolumicShape volumicShape = (VolumicShape) shape;
		
		// Get the intersections with the shape
		HalfLine hl = new HalfLine(	
				ray.getLineSupport().getOrigin(),
				ray.getLineSupport().getDirection());
		ArrayList<Intersection> intersections = volumicShape.getIntersections(hl);

		// Check the number of intersections
		int n = intersections.size();
		if (n==0)
			// If no intersection, return null
			return null;

		// Number of intersection
		int intersectionNumberParity = n%2;

		// Sort intersections (by length)
		Collections.sort(intersections);

		// Base length : Distance between ray origin and first intersection
		float d0 = 0;

		// Section length: Distance of the ray path inside the shape
		float sectionLength = 0;

		if (intersectionNumberParity==1){
			// ORIGIN IS INSIDE THE MEDIUM (odd intersection number)
			sectionLength = intersections.get(0).getDistance();
		}
		else {
			// ORIGIN IS OUTISDE THE MEDIUM (pair intersection number)
			d0 = intersections.get(0).getDistance();
			sectionLength = intersections.get(1).getDistance() - d0;
		}

		// Computes the random interception by the medium
		// Random variable between 0 and 1
		float X = (float) Math.random();

		// Random distance
		float randomLength = (float) -Math.log(1-X)/interceptionCoefficient;

		// Returns the interception point
		if (randomLength<sectionLength){

			// there is an interception in the medium
			Vector3f normal = randomNormal(hl.getDirection());

			//			System.out.println("\tthe ray is intercepted in the medium ("+(randomLength+d0)+")");
			return new Intersection(randomLength+d0,normal);
		}
		// ... or not (no interception in the medium)
		//		System.out.println("\tthe ray leaves the medium without interception ("+(sectionLength+d0)+")");
		return new Intersection(sectionLength+d0,null);
		
	}

	/*
	 * Computes the normal of the infinitesimal intercepted element, given the ray direction
	 * @param direction	ray direction
	 * @return normal
	 */
	private Vector3f randomNormal(Vector3f direction) {
		Vector3f normal = new Vector3f(	
				(float) Math.random()-0.5f,
				(float) Math.random()-0.5f,
				(float) Math.random()-0.5f);
		normal.normalize();
		return normal;
	}

}
