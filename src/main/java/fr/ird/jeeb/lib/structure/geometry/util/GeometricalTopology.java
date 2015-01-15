package fr.ird.jeeb.lib.structure.geometry.util;

import javax.vecmath.Vector3d;

/**
 * 
 * @author JFB
 * 
 * 
 */
public interface GeometricalTopology {
	public abstract boolean in (Vector3d p);
	public abstract boolean out (Vector3d p);
	public abstract boolean on (Vector3d p);
}
