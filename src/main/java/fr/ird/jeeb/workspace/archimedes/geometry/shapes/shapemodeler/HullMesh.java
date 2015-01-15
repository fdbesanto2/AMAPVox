/**
 * 
 */
package fr.ird.jeeb.workspace.archimedes.geometry.shapes.shapemodeler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.vecmath.Point3f;

import fr.ird.jeeb.lib.structure.ArchiNode;
import fr.ird.jeeb.lib.structure.MeshGeometry;
import fr.ird.jeeb.lib.structure.SimpleGeometry;
import fr.ird.jeeb.lib.structure.geometry.mesh.Mesh;
import fr.ird.jeeb.lib.structure.geometry.mesh.convexhull.ConvexHull3D;
import fr.ird.jeeb.lib.structure.geometry.mesh.convexhull.SpatialPoint;


/**
 * Computes the convex hull ({@link ConvexHull3D}) of {@link Point3f} list or {@link ArchiNode}
 * @author Dauzat - August 2012
 */
public class HullMesh {

	/**
	 * Computes the convex hull ({@link ConvexHull3D}) of a {@link Point3f} List.
	 * <ul>
	 * Because the algorithm doesn't work for too large point lists, 
	 * several hulls are calculated for sublists if needed and then merged
	 * @return Mesh ({@link Mesh})
	 */
	public static Mesh getMesh (List<Point3f> points) {
		int nbMaxPoints = 2000; // found to give shorter computations
		
		int nbMeshes = (points.size () / nbMaxPoints) + 1;

		List <Point3f> meshesPoints = new ArrayList<Point3f>();
		
		for (int m=0; m<nbMeshes; m++) {
			List<Point3f> subListPoints = new ArrayList<Point3f>();
			for (int p=0; p<points.size (); p++) {
				if (m == (p % nbMeshes)) {
					subListPoints.add (points.get (p));
				}
			}
			Point3f[] pts = getHull (subListPoints).getPoints ();
			for (int p=0; p<pts.length; p++) {
				meshesPoints.add (pts[p]);
			}
		}

		Mesh mesh = getHull (meshesPoints);
		
		return mesh;
	}

	
	/**
	 * Computes the convex hull ({@link ConvexHull3D}) of an {@link ArchiNode} collection.
	 * <ul>
	 * Because the algorithm doesn't work for too large point lists, 
	 * several hulls are calculated for sublists of points if needed and then merged
	 * @return Mesh ({@link Mesh})
	 */
	public static Mesh getMesh (Collection<ArchiNode> plantNodes) {
		List<Point3f> points= new ArrayList<Point3f>();

		for (ArchiNode node : plantNodes) {
			SimpleGeometry sg = node.getGeometry();
			if (sg != null) {
				if (sg instanceof MeshGeometry) {
					Point3f[] pts = ((MeshGeometry) sg).getTransformedMesh ().getPoints ();
					for (int p=0; p<pts.length; p++)
						points.add (pts[p]);
				}
			}
		}
	
		return getMesh (points);
	}
	
	/**
	 * Computes the convex hull ({@link ConvexHull3D}) of a {@link Point3f} List.
	 * <ul>
	 * Because the algorithm doesn't work for too large point lists, 
	 * several hulls are calculated for sublists if needed and then merged
	 * @return Mesh ({@link Mesh})
	 */
	public static Mesh getMesh (Point3f[] points) {
		
		return getHull (Arrays.asList(points));
	}

	
	private static Mesh getHull (List<Point3f> points) {
		// creation of ptArray
		double[] ptArray = new double[points.size()*3];
		for (int p=0; p<points.size(); p++) {
			int c= p* 3;
			ptArray[c]  = points.get(p).x;
			ptArray[c+1]= points.get(p).y;
			ptArray[c+2]= points.get(p).z;
		}
		
		ConvexHull3D cvHull= new ConvexHull3D(ptArray); 
		int[][] faces;
		faces= cvHull.getHullFaces();
		SpatialPoint[] spts= cvHull.getHullFaceVertices();
		Point3f[] pts= new Point3f[spts.length];
		for (int p=0; p<spts.length; p++) {
			pts[p]= new Point3f((float)spts[p].x,(float)spts[p].y,(float)spts[p].z);
		}
		
		Mesh mesh= new Mesh(pts, faces);
		mesh.computeNormals ();
		mesh.reverseNormals ();

		return mesh;
	}
	
}
