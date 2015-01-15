package fr.ird.jeeb.workspace.archimedes.raytracing.interceptionmodel;

import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Shape;
import fr.ird.jeeb.workspace.archimedes.raytracing.ray.Ray;

/**
 * Interception Model Abstract Class
 * @author Cresson, Jan 2013
 *
 */
public abstract class InterceptionModel {
	public abstract Intersection interception(Ray ray, Shape shape);
}
