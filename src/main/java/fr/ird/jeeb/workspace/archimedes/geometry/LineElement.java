package fr.ird.jeeb.workspace.archimedes.geometry;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Shape;

/**
 * Interface for handling intersections between line elements (line, half-line or line-segment) with "Shape" objects (polygon, sphere, mesh, ...)
 * @author Dauzat/Cresson - August 2012
 */
public interface LineElement {

	public Point3f	getOrigin ();
	public Point3f	getEnd ();
	public Vector3f	getDirection ();
	public float	getLength ();

	
	/**
	 * @param a Shape object (polygon, mesh, sphere...)
	 */
	public boolean doesIntersect (Shape shape);
	
	/**
	 * @param a Shape object (polygon, mesh, sphere...)
	 * @return a List of intersections (empty list if no intersection)
	 */
	public ArrayList<Intersection> getIntersections (Shape shape);

	/**
	 * @param a Shape object (polygon, mesh, sphere...)
	 * @return the nearest intersection from the point "origin" in the line element direction
	 */
	public Intersection getNearestIntersection (Shape shape);

	/**
	 * Translate the line element
	 * @param translation	translation vector
	 */
	public void translate(Vector3f translation);
}
