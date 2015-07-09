package fr.amap.amapvox.jeeb.raytracing.geometry.shapes;

import java.util.ArrayList;

import javax.vecmath.Point3f;

import fr.ird.voxelidar.voxelisation.raytracing.geometry.Intersection;
import fr.ird.voxelidar.voxelisation.raytracing.geometry.LineElement;
import javax.vecmath.Point3d;

/**
 * Volumic Shape Interface.
 * @author Cresson, Nov. 2012
 *
 */
public interface VolumicShape {
	/**
	 * Return true when the given point is inside the volumic shape
	 */
	boolean contains(Point3d point);
	
	/* Shape methods */
	boolean isIntersectedBy(LineElement lineElement);
	Intersection getNearestIntersection (LineElement lineElement); 
	ArrayList<Intersection> getIntersections (LineElement lineElement); 
	}
	
	