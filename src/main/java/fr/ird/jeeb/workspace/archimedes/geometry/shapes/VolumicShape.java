package fr.ird.jeeb.workspace.archimedes.geometry.shapes;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;
import fr.ird.jeeb.workspace.archimedes.geometry.LineElement;

/**
 * Volumic Shape Interface.
 * @author Cresson, Nov. 2012
 *
 */
public interface VolumicShape {
	/**
	 * Return true when the given point is inside the volumic shape
	 */
	boolean contains(Point3f point);
	
	/* Shape methods */
	boolean isIntersectedBy(LineElement lineElement);
	Intersection getNearestIntersection (LineElement lineElement); 
	ArrayList<Intersection> getIntersections (LineElement lineElement); 
	}
	
	