package fr.amap.lidar.amapvox.jeeb.raytracing.geometry.shapes;

import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.Intersection;
import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.LineElement;
import java.util.ArrayList;


/**
 * Abstract class for simple geometry
 * @author Cresson/Dauzat, August 2012
 */
public abstract class Shape{
	
	public abstract boolean isIntersectedBy (LineElement linel);
	
	public abstract ArrayList<Intersection> getIntersections (LineElement linel);
	
	public abstract Intersection getNearestIntersection (LineElement linel);

}
