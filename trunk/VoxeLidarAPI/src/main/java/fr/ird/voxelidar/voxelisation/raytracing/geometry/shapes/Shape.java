package fr.ird.voxelidar.voxelisation.raytracing.geometry.shapes;

import java.util.ArrayList;

import fr.ird.voxelidar.voxelisation.raytracing.geometry.Intersection;
import fr.ird.voxelidar.voxelisation.raytracing.geometry.LineElement;

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
