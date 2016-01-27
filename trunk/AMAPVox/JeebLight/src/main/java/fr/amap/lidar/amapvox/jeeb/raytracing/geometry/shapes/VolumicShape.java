package fr.amap.lidar.amapvox.jeeb.raytracing.geometry.shapes;

import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.Intersection;
import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.LineElement;
import java.util.ArrayList;


import javax.vecmath.Point3d;

/**
 * Volumic Shape Interface.
 * @author Cresson, Nov. 2012
 *
 */
public interface VolumicShape {
    
	boolean contains(Point3d point);
	
	/* Shape methods */
	boolean isIntersectedBy(LineElement lineElement);
	Intersection getNearestIntersection (LineElement lineElement); 
	ArrayList<Intersection> getIntersections (LineElement lineElement); 
	}
	
	