package fr.ird.jeeb.workspace.archimedes.geometry.shapes;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 * Convex mesh class
 * @author Cresson
 *
 */
public class ConvexMesh extends TriangulatedMesh implements VolumicShape{

	public ConvexMesh (Point3f[] points, int[][] paths) {
		super (points, paths);
	}
	public ConvexMesh (Point3f[] points, int[][] paths, boolean culling) {
		super (points, paths, culling);
	}

	@Override
	public boolean contains(Point3f point) {
		for (int i = 0 ; i < paths.length ; i++) {
			Point3f p = new Point3f(points[paths[i][0]]);
			Vector3f v = new Vector3f(	point.x - p.x,
										point.y - p.y,
										point.z - p.z);

			if (normals[i].dot (v) >= 0)
				return false;
		}
		return true;
	}

}
