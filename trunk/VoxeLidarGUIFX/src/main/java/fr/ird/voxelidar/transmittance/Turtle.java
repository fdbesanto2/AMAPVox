//package fr.ird.voxelidar.transmittance;
//
//import java.awt.Color;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.Set;
//import java.util.TreeSet;
//
//import javax.vecmath.Point2f;
//import javax.vecmath.Point3f;
//import javax.vecmath.Vector2f;
//import javax.vecmath.Vector3f;
//
//import jeeb.workspace.sunrapp.util.Colouring;
//
//public class Turtle {
//
//	public Vector3f[] directions;
//	public float[] elevation;
//	public float[] azimuth;
//
//	private GeometryWithAttribute turtleGWA;
//
//	public Turtle(int scale, boolean onlyUpward) {
//
//		Mesh mesh = buildTurtle(scale);
//		Point3f[] points = mesh.getPoints();
//
//		int nbSectors = points.length;
//		if (onlyUpward)
//			nbSectors /= 2;
//		directions = new Vector3f[nbSectors];
//		elevation = new float[nbSectors];
//		azimuth = new float[nbSectors];
//		int sector = 0;
//		for (int p = 0; p < points.length; p++) {
//			boolean keep = true;
//			if (onlyUpward && (points[p].z < 0.0))
//				keep = false;
//			if (keep) {
//				directions[sector] = new Vector3f(points[p]);
//				directions[sector].normalize(); // shouldn't be necessary
//				elevation[sector] = (float) Math.asin(points[p].z);
//				Vector2f proj = new Vector2f(points[p].x, points[p].y);
//				proj.normalize();
//				azimuth[sector] = (float) Math.acos(proj.x);
//				if (proj.y > 0) {
//					azimuth[sector] = (float) (Math.PI * 2.) - azimuth[sector];
//
//				}
//
//				azimuth[sector] -= Math.PI / 2;
//				if (azimuth[sector] < 0) {
//					azimuth[sector] += 2 * Math.PI;
//				}
//				sector++;
//			}
//		}
//	}
//
//	public float getElevationAngle(int d) {
//		return elevation[d];
//	}
//
//	public float getZenithAngle(int d) {
//		return (float) ((Math.PI / 2) - elevation[d]);
//	}
//
//	public float getAzimuthAngle(int d) {
//		return azimuth[d];
//	}	
//
//	public int getNbDirections() {
//		return directions.length;
//	}
//
//	/**
//	 * TODO: compute the radius of sectors; either individually or on average
//	 */
//	public void edgeLength() {
//
//		// edges length
//		// for (int sp=0; sp<sPath.size()-1; sp++) {
//		// Vector3f edge = new Vector3f(pts2[sPath.get(sp)]);
//		// edge.sub(pts2[sPath.get(sp+1)]);
//		// System.out.println(edge.length());
//		// }
//	}
//	
//
//	// /** To move in MeshUtils
//	// * @param mesh
//	// * @return
//	// */
//	// public SimpleMesh relistPointsIndices (SimpleMesh mesh) {
//	//
//	// TreeSet<Integer> sPtsIndices = new TreeSet<Integer>();
//	// int[][] paths = mesh.getPaths();
//	// for (int f=0; f<paths.length; f++) {
//	// for (int p=0; p<paths[f].length; p++) {
//	// sPtsIndices.add(paths[f][p]);
//	// }
//	// }
//	// Point3f[] points = mesh.getPoints();
//	// Point3f[] newPoints = new Point3f[sPtsIndices.size()];
//	// HashMap<Integer, Integer> newPtsIndices = new HashMap<Integer,
//	// Integer>();
//	// int n = 0;
//	// for (int i : sPtsIndices) {
//	// newPtsIndices.put(i, n);
//	// newPoints[n] = points[i];
//	// n ++;
//	// }
//	//
//	// int[][] newPaths = new int[paths.length][];
//	// for (int f=0; f<paths.length; f++) {
//	// newPaths[f] = new int[paths[f].length];
//	// for (int p=0; p<paths[f].length; p++) {
//	// newPaths[f][p] = newPtsIndices.get(paths[f][p]);
//	// }
//	// }
//	//
//	// SimpleMesh newMesh = new SimpleMesh(newPoints, newPaths);
//	// newMesh.computeNormals();
//	// newMesh.reverseNormals();
//	//
//	// return newMesh;
//	// }
//
//}
