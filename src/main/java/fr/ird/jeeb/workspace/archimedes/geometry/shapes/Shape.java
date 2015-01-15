package fr.ird.jeeb.workspace.archimedes.geometry.shapes;

import java.util.ArrayList;

import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;
import fr.ird.jeeb.workspace.archimedes.geometry.LineElement;
import fr.ird.jeeb.workspace.archimedes.geometry.Transformable;

/**
 * Abstract class for simple geometry
 * @author Cresson/Dauzat, August 2012
 */
public abstract class Shape implements Transformable, Cloneable {
	
	/**
	 * Check if the shape is intersected by a line element.
	 * Can be faster than getNearestIntersection when the intersection isn't needed.
	 */
	public abstract boolean isIntersectedBy (LineElement linel);
	
	/**
	 * Get all intersections between the given linel and the shape
	 */
	public abstract ArrayList<Intersection> getIntersections (LineElement linel);
	
	/**
	 * Get the nearest intersection between the given linel and the shape
	 */
	public abstract Intersection getNearestIntersection (LineElement linel);

	/**
	 * Cloning
	 */
	public Object clone() throws CloneNotSupportedException {
        return super.clone();
	}
	
}
