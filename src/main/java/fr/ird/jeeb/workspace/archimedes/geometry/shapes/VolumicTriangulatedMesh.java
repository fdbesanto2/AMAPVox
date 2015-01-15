package fr.ird.jeeb.workspace.archimedes.geometry.shapes;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.geometry.HalfLine;
import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;

/**
 * Volumic Triangulated Mesh (A TriangulatedMesh which is volumic)
 * @author Cresson, Oct. 2012
 *
 */
public class VolumicTriangulatedMesh extends TriangulatedMesh implements VolumicShape{
	/**
	 * Constructor with points array and paths
	 * @param points	points array
	 * @param paths		paths
	 */
	public VolumicTriangulatedMesh (Point3f[] points, int[][] paths) {
		super (points, paths);
	}
	
	/**
	 * Constructor with points array, paths and culling option
	 * @param points	points array
	 * @param paths		paths
	 * @param culling	culling opt. When sets to true, the intersection happens only when face_normal[dot]ray >0
	 */
	public VolumicTriangulatedMesh (Point3f[] points, int[][] paths, boolean culling) {
		super (points, paths, culling);
	}
	
	@Override
	public boolean contains (Point3f point) {
		
		boolean[]	inside	= {	false, 
								false,
								false};
		Vector3f[]	dir		= new Vector3f[] {	new Vector3f(1,0,0),
												new Vector3f(0,1,0),
												new Vector3f(0,0,1)};
		for (int i = 0 ; i < 3 ; i++){
			ArrayList<Intersection> intersections = this.getIntersections (new HalfLine(point,dir[i]));
			inside[i] = (intersections.size ()%2==1);
		}
			
		if (inside[0] & inside[1] & inside[2])
			return true;
		if (inside[0] | inside[1] | inside[2])
			System.err.println ("Ambiguous case, 'outside' is returned");
		return false;
	}

	
}
