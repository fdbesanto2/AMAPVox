package fr.amap.amapvox.jeeb.raytracing.geometry.shapes;

import fr.amap.amapvox.jeeb.raytracing.geometry.Intersection;
import fr.amap.amapvox.jeeb.raytracing.geometry.LineElement;
import java.util.ArrayList;


/**
 * Abstract class for simple geometry
 * @author Cresson/Dauzat, August 2012
 */
public abstract class Shape{
	
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

}
