package fr.ird.jeeb.lib.structure.geometry.mesh;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import fr.ird.jeeb.lib.structure.geometry.util.BoundingBox3d;
import fr.ird.jeeb.lib.structure.geometry.util.BoundingBox3f;

/**
 * A description for a simple Mesh.
 * 
 * @author S. Griffon - April 2012
 */
public class SimpleMesh implements Serializable, Cloneable {

	static private final long serialVersionUID = 1L;

	protected Point3f[] points;
	protected Vector3f[] normals;

	protected boolean volumic = false; // true if the mesh is a closed volume, false means that the
										// mesh is a surface

	private Color color = Color.GREEN; 
	
	protected boolean triangulated = false;

	/**
	 * Index of the faces that specifies the order and vertices that define individual faces.First
	 * index indicates face number and the second indicates vertex indices.
	 * Clockwise order
	 */
	protected int[][] paths;

	protected BoundingBox3d box;

	public SimpleMesh () {
		points = new Point3f[0];
		normals = null;
		paths = null;
		box = null;
		volumic = false;
		color = null;
	} // Mesh constructor

	/**
	 * Creates a new <code>Mesh</code> instance to represent a simple path in 3D space.
	 * 
	 * @param pts The points along the simple path (in order of the path)
	 */
	public SimpleMesh (Point3f[] points) {
		this.points = points;
		normals = null;
		volumic = false;
		paths = new int[1][points.length];
		for (int i = 0; i < points.length; i++)
			paths[0][i] = i;
		color = null;
		triangulate ();
	}

	/**
	 * Creates a new <code>Mesh</code> instance with the given set of points and connections between
	 * them.
	 * 
	 * @param pts the collection of vertices for this mesh.
	 * @param connections an <code>int[][]</code> instance representing all the connections between
	 *        vertices in the mesh. Each entry in connections represents a closed polygon. Each
	 *        entry in a single entry of connections represents the index of a vertex. So
	 *        connections[0] is the first closed polygon described in this mesh. The entries of
	 *        connections[0] will be indexes into <code>pts</code>. The sequence of these entries
	 *        defines the closed path in 3D space. For example, if connections[0] = {1, 3, 4, 5},
	 *        then connections[0] represents a quadrilateral (in 3D space) whose vertices are given
	 *        by pts[1], pts[3], pts[4] and pts[5] in that order.
	 */
	public SimpleMesh (Point3f[] points, int[][] paths) {
		this.points = points;
		this.paths = paths;
		normals = null;
		volumic = false;
		color = null;
		triangulate ();
	}

	/**
	 * Creates a new <code>Mesh</code> instance with the given set of points and connections between
	 * them and normals.
	 * 
	 * @param pts the collection of vertices for this mesh.
	 * @param connections an <code>int[][]</code> instance representing all the connections between
	 *        vertices in the mesh. Each entry in connections represents a closed polygon. Each
	 *        entry in a single entry of connections represents the index of a vertex. So
	 *        connections[0] is the first closed polygon described in this mesh. The entries of
	 *        connections[0] will be indexes into <code>pts</code>. The sequence of these entries
	 *        defines the closed path in 3D space. For example, if connections[0] = {1, 3, 4, 5},
	 *        then connections[0] represents a quadrilateral (in 3D space) whose vertices are given
	 *        by pts[1], pts[3], pts[4] and pts[5] in that order.
	 * @param normals the collection of normal of each vertices for this mesh.
	 */
	public SimpleMesh (Point3f[] points, int[][] paths, Vector3f[] normals) {
		this.points = points;
		this.paths = paths;
		this.normals = normals;
		volumic = false;
		color = null;
		triangulate ();

	}

	/**
	 * Creates a new <code>Mesh</code> instance from a existing Mesh
	 * 
	 * @param mesh The mesh to copy into this
	 */
	public SimpleMesh (SimpleMesh mesh) {
		if (mesh.getPoints () != null) {
			points = new Point3f[mesh.getPoints ().length];
			int i = 0;
			for (Point3f p : mesh.getPoints ()) {
				points[i] = new Point3f (p);
				i++;
			}
		}

		if (mesh.getNormals () != null) {
			normals = new Vector3f[mesh.getNormals ().length];
			int i = 0;
			for (Vector3f v : mesh.getNormals ()) {
				normals[i] = new Vector3f (v);
				i++;
			}
		}

		if (mesh.getPaths () != null) {
			int[][] currentPath = mesh.getPaths ();
			if (currentPath.length > 0 && currentPath[0].length > 0) {
				// paths = new int[currentPath.length][currentPath[0].length];
				paths = new int[currentPath.length][];
				for (int i = 0; i < mesh.getPaths ().length; i++) {
					paths[i] = new int[currentPath[i].length];
					for (int j = 0; j < currentPath[i].length; j++) {
						paths[i][j] = currentPath[i][j];
					}
				}
			}
		}

		volumic = mesh.volumic;
		triangulated = mesh.triangulated;
		color = mesh.color;

	}

	public void copyFrom (SimpleMesh mesh) {

		if (mesh.getPoints () != null) {
			points = new Point3f[mesh.getPoints ().length];
			int i = 0;
			for (Point3f p : mesh.getPoints ()) {
				points[i] = new Point3f (p);
				i++;
			}
		}

		if (mesh.getNormals () != null) {
			normals = new Vector3f[mesh.getNormals ().length];
			int i = 0;
			for (Vector3f v : mesh.getNormals ()) {
				normals[i] = new Vector3f (v);
				i++;
			}
		}

		if (mesh.getPaths () != null) {
			int[][] currentPath = mesh.getPaths ();
			if (currentPath.length > 0 && currentPath[0].length > 0) {
				// paths = new int[currentPath.length][currentPath[0].length];
				paths = new int[currentPath.length][];
				for (int i = 0; i < mesh.getPaths ().length; i++) {
					paths[i] = new int[currentPath[i].length];
					for (int j = 0; j < currentPath[i].length; j++) {
						paths[i][j] = currentPath[i][j];
					}
				}
			}
		}

		volumic = mesh.volumic;
		triangulated = mesh.triangulated;
		color = mesh.color;
	}

	public boolean isVolume () {
		return volumic;
	}

	public boolean isSurface () {
		return !volumic;
	}

	/**
	 * Check wether or not all the mesh faces are convex.
	 * 
	 * @return true faces are convex
	 */
	public boolean isConvex() {
		
		for (int f = 0; f < this.paths.length; f++) {
			if(!isFaceConvex(f))
				return false;
			
		}	
		
		return true;
	}
	
	private boolean isFaceConvex (int faceIndex) {
		int [] face = this.paths[faceIndex];
		int l = face.length;
		
		Vector3f prevCrossvector = null;
		float signum = 0;
		
		for ( int i = 0; i < face.length; i++ ) {
			Point3f p1 = points[face[i]];
			Point3f p2 = points[face[(i+1)%l]];
			Point3f p3 = points[face[(i+2)%l]];

			
			Point3f ps = new Point3f(p2);
			ps.sub (p1);
			
			Point3f pm = new Point3f(p3);
			pm.sub (p2);
			
			
			
			Vector3f v1 = new Vector3f(ps);
			Vector3f v2 = new Vector3f(pm);

			
			v1.cross (v1, v2);
			
			if(v1.length () > 0.000001) //not colinear
			{					
				if(prevCrossvector != null) {
					float signumTmp = Math.signum (v1.dot (prevCrossvector));
					if(signum != 0 && signumTmp != signum) {
						return false;
					}
					signum = signumTmp;
				}					
				prevCrossvector = v1;
				
			}			
			
		}
		
		return true;
	}
	
	
	/*
	 * Setter for default color. Must not be null.
	 */
	public void setColor(Color color) {
		if(color == null)
			return;
		this.color = color;
	}
	
	public Color getColor() {
		return color;
	}
		
	public void setVolumic (boolean volumic) {
		this.volumic = volumic;
	}

	/**
	 * Get the Points value.
	 * 
	 * @return the Points value.
	 */
	public Point3f[] getPoints () {
		return points;
	}

	/**
	 * Set the Points value. triangulate() must be called after
	 * 
	 * @param newPoints The new Points value.
	 */
	public void setPoints (Point3f[] newPoints) {
		this.points = newPoints;
		triangulated = false;
	}

	public Vector3f[] getNormals () {
		return normals;
	}

	/*
	 * Return normal for a given face index (compute the normalized sum of each point's normal of
	 * the face)
	 */
	public Vector3f getFaceNormal (int faceIndex) {
		Vector3f faceNormal = new Vector3f ();

		for (int j = 0; j < paths[faceIndex].length; j++) {

			faceNormal.x += normals[paths[faceIndex][j]].x;
			faceNormal.y += normals[paths[faceIndex][j]].y;
			faceNormal.z += normals[paths[faceIndex][j]].z;

		}

		faceNormal.normalize ();
		return faceNormal;
	}

	public void setNormals (Vector3f[] normals) {
		this.normals = normals;
		// Changing normals doesn't affect the current triangulation algorithm
	}

	public void reverseNormals () {
		for (Vector3f normal : normals) {
			normal.negate ();
		}
		// Changing normals doesn't affect the current triangulation algorithm
	}

	/**
	 * Add a face to the path
	 * 
	 */
	public void addFace (int[] face) {

		if (paths == null)
			paths = new int[1][];
		else {
			paths = Arrays.copyOf (paths, paths.length + 1);
		}
		paths[paths.length - 1] = face.clone ();
		triangulated = false;
	}

	/**
	 * Get the Paths value.
	 * 
	 * @return the Paths value.
	 */
	public int[][] getPaths () {
		return paths;
	}

	/**
	 * Set the Paths value.
	 * 
	 * @param newPaths The new Paths value.
	 */
	public void setPaths (int[][] newPaths) {
		this.paths = newPaths;
		triangulated = false;
	}

	public void setBBox (BoundingBox3d box) {
		this.box = box;
	}

	public BoundingBox3d getBBox () {
		if (box == null) {
			computeBBox ();
		}
		return box;
	}

	public BoundingBox3f getBBox3f () {
		if (box == null) {
			computeBBox ();
		}

		return new BoundingBox3f (box);
	}

	/**
	 * method to get the Mesh's bounding box
	 * 
	 * @return BoundingBox the Mesh's bounding box
	 */
	public BoundingBox3d computeBBox () {
		Point3d min = new Point3d (Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
		// Point3d max = new Point3d(Double.MIN_VALUE, Double.MIN_VALUE,
		// Double.MIN_VALUE);
		Point3d max = new Point3d (-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE);

		for (Point3f point : points) {

			min.x = Math.min (min.x, point.x);
			min.y = Math.min (min.y, point.y);
			min.z = Math.min (min.z, point.z);

			max.x = Math.max (max.x, point.x);
			max.y = Math.max (max.y, point.y);
			max.z = Math.max (max.z, point.z);

		}
		box = new BoundingBox3d (min, max);
		return box;

	}

	/**
	 * Doesn't need triangulation
	 * 
	 * @return area of the mesh (faces must be convex)
	 */
	public float getArea () {
		float meshArea = 0;
		for (int[] face : paths) {
			if (face.length > 2) {
				Vector3f vec1 = new Vector3f (points[face[1]]);
				vec1.sub (points[face[0]]);
				for (int p = 2; p < face.length; p++) {
					Vector3f vec2 = new Vector3f (points[face[p]]);
					vec2.sub (points[face[0]]);

					Vector3f vec3 = new Vector3f ();
					vec3.cross (vec1, vec2);
					meshArea += vec3.length () / 2.0f;

					vec1 = vec2;
				}

			}
		}
		return meshArea;
	}

	public static float signedVolumeOfTriangle (Point3f points1, Point3f points2, Point3f points3) {
		float v321 = points3.x * points2.y * points1.z;
		float v231 = points2.x * points3.y * points1.z;
		float v312 = points3.x * points1.y * points2.z;
		float v132 = points1.x * points3.y * points2.z;		
		float v213 = points2.x * points1.y * points3.z;
		float v123 = points1.x * points2.y * points3.z;
		return (1.0f / 6.0f) * (-v321 + v231 + v312 - v132 - v213 + v123);
	}

	// Compute the volume of a triangulated mesh
	public float getVolume () {

		if (!triangulated) triangulate ();

		float volume = 0;
		for (int[] face : paths) {
			volume += signedVolumeOfTriangle (points[face[0]], points[face[1]], points[face[2]]);
		}
		return Math.abs (volume);
	}

	public SimpleMesh transform (Matrix4f t) {
		// t a transformation matrix. Each row is a point.

		Matrix3f rotation = new Matrix3f ();
		t.getRotationScale (rotation);

		for (int i = 0; i < points.length; i++) {

			t.transform (points[i], points[i]);

			if (normals != null) {

				rotation.transform (normals[i], normals[i]);
			}

		}

		return this;
	}

	public SimpleMesh multiply (Matrix3f t) {
		// t a transformation matrix. Each row is a point.

		Matrix3f nt = new Matrix3f (t);

		for (int i = 0; i < points.length; i++) {

			t.transform (points[i], points[i]);

			if (normals != null) {
				nt.transform (normals[i], normals[i]);
			}

		}

		return this;
	}

	/** translates the vertices in this polygon by the position */
	public Object clone () {
		Point3f[] result = new Point3f[points.length];
		Vector3f[] normal = null;
		for (int i = 0; i < points.length; i++) {
			result[i] = (Point3f) points[i].clone ();
		}

		if (normals != null) {
			normal = new Vector3f[normals.length];
			for (int i = 0; i < normals.length; i++) {
				normal[i] = (Vector3f) normals[i].clone ();
			}
		}
		SimpleMesh mesh = new SimpleMesh (result, paths, normal);
		mesh.volumic = volumic;
		mesh.triangulated = triangulated;
		mesh.color = color;
		return mesh;
	}

	/** Return a translated copy of this mesh according to the given vector */
	public SimpleMesh getTranslatedCopy (Point3f pos) {
		Point3f[] result = new Point3f[points.length];
		for (int i = 0; i < points.length; i++) {
			result[i] = (Point3f) points[i].clone ();
			result[i].add (new Vector3f (pos));
		}
		return new SimpleMesh (result, paths);
	}
	
	/** Translate the mesh according to the given vector */
	public void translate (Point3f pos) {
		for (int i = 0; i < points.length; i++) {			
			points[i].add (new Vector3f (pos));
		}
	}
	
	public void taper (double baseWidth, double topWidth, double baseHeight, double topHeight) {
		TaperTransform taper = new TaperTransform (baseWidth, topWidth, baseHeight, topHeight);
		taper.transform (this);
	}

	public void scale (float scale) {
		Matrix3f scaleMat = new Matrix3f ();
		scaleMat.set (scale);
		multiply (scaleMat);
	}

	/** Remove useless vertices, needed by the triangulation */
	private void purify () {
		//SimpleMesh newMesh = (SimpleMesh) this.clone ();

		ArrayList<Integer> newFace;
		ArrayList<ArrayList<Integer>> faceList = new ArrayList<ArrayList<Integer>> ();
		for (int f = 0; f < this.paths.length; f++) {
			int[] face = this.paths[f];

			//0 - If the original face doesn't have at least 3 points, ignore it
			if (face.length < 3) break;
			
			//1-If two consecutive identical points, remove the second 
			newFace = new ArrayList<Integer> ();
			Point3f pt1 = this.points[face[0]];
			newFace.add (face[0]);
			for (int p = 1; p < face.length - 1; p++) {
				Point3f pt2 = this.points[face[p]];
				if (pt2 != pt1) {
					newFace.add (face[p]);
					pt1 = pt2;
				}
			}

			//2-If the first and the last points are identical, remove the last 
			pt1 = this.points[face[0]];
			Point3f pt2 = this.points[face[face.length - 1]];
			if (pt2 != pt1) {
				newFace.add (face[face.length - 1]);
			}
			
			//3 - If the purified face doesn't have at least 3 points, ignore it
			if (newFace.size () >= 3) {
				faceList.add (newFace);
			}
		}

		
		//Copy the list in the path array
		int[][] meshPaths = new int[faceList.size ()][];
		int faceIndex = 0;
		int pointIndex = 0;
		for (ArrayList<Integer> face : faceList) {
			pointIndex = 0;
			Object[] currentFace = face.toArray ();
			meshPaths[faceIndex] = new int[currentFace.length];
			for (Object index : currentFace) {
				meshPaths[faceIndex][pointIndex] = (Integer) index;
				pointIndex++;
			}
			faceIndex++;
		}
		this.paths = meshPaths;
		
	}
	
////	//TODO 
//	public void simplify () {
//		
//		LinkedHashMap<Integer, Integer> mapPoints = new LinkedHashMap<Integer, Integer>();
//		for(int i=0; i<points.length; i++) {
//			Point3f pnt = points[i];
//			
//			boolean foundSamePoint = false;
//			for (int j=0; j<points.length; j++) {
//				Point3f pnt2 = points[j];
//				if(pnt != pnt2) {
//					if(pnt.equals(pnt2)) {
//						mapPoints.put(i, j);
//						foundSamePoint = true;
//						System.out.println("Found same point : " + pnt);
//						break;
//					}
//				}
//			}
//			
//			if(!foundSamePoint) {
//				mapPoints.put(i, i);
//			}
//		}
//		
//		points = (Point3f[]) mapPoints.values().toArray();
//		
//		for (int f = 0; f < this.paths.length; f++) {
//			
//			int np = paths[f].length;			
//			for (int j = 0; j < np; j++) {
//				if(paths[f][j]
//			}
//		}
//		
//		
//	}
	
//	public static void main(String[] args) {
//		AmapPatternsMeshLoader loader = new AmapPatternsMeshLoader("/home/griffon/workspace/amapstudio/samples/lines/smb/nentn125.smb");
//		Mesh mesh = loader.load();
//		mesh.simplify();
//		System.out.println("//////////////////////CLEANER ///////////////////////");
//		MeshCleaner cleaner = new MeshCleaner(mesh);
//		mesh = cleaner.getMesh();
//		mesh.simplify();
//	}

	/** Triangulation of the mesh */
	public void triangulate () {
		purify ();
					
		ArrayList<Integer> newFace;
		ArrayList<ArrayList<Integer>> faceList = new ArrayList<ArrayList<Integer>> ();
		for (int f = 0; f < this.paths.length; f++) {
			
//			if(!isFaceConvex(f)) {
//				//System.out.println ("This mesh has a non-convex face, then triangulation is currently not available for this face");
//				//continue;
//			}
			
			int[] face = this.paths[f];
			if (face.length >= 3) {
				int vertex = 0;
				while (vertex < face.length - 2) {
					newFace = new ArrayList<Integer> ();
					newFace.add (face[0]);
					for (int i = 1; i < 3; i++) {
						newFace.add (face[(vertex + i) % face.length]);
					}
					faceList.add (newFace);
					vertex += 1;
				}
			}
		}
		int[][] meshPaths = new int[faceList.size ()][];
		int faceIndex = 0;
		int pointIndex = 0;
		for (ArrayList<Integer> face : faceList) {
			pointIndex = 0;
			Object[] currentFace = face.toArray ();
			meshPaths[faceIndex] = new int[currentFace.length];
			for (Object index : currentFace) {
				meshPaths[faceIndex][pointIndex] = (Integer) index;
				pointIndex++;
			}
			faceIndex++;
		}
		this.paths = meshPaths;
//		this.purify (); // We think it is not needed.
		this.triangulated = true;
		
	}
	
	public void triangulateOld () {
		purify ();

		ArrayList<Integer> newFace;
		ArrayList<ArrayList<Integer>> faceList = new ArrayList<ArrayList<Integer>> ();
		for (int f = 0; f < this.paths.length; f++) {
			int[] face = this.paths[f];
			if (face.length >= 3) {
				int vertex = 0;
				while (vertex < face.length - 1) {
					newFace = new ArrayList<Integer> ();
					newFace.add (face[vertex]);
					for (int i = 1; i < 3; i++) {
						newFace.add (face[(vertex + i) % face.length]);
					}
					faceList.add (newFace);
					vertex += 2;
				}
			}
		}
		int[][] meshPaths = new int[faceList.size ()][];
		int faceIndex = 0;
		int pointIndex = 0;
		for (ArrayList<Integer> face : faceList) {
			pointIndex = 0;
			Object[] currentFace = face.toArray ();
			meshPaths[faceIndex] = new int[currentFace.length];
			for (Object index : currentFace) {
				meshPaths[faceIndex][pointIndex] = (Integer) index;
				pointIndex++;
			}
			faceIndex++;
		}
		this.paths = meshPaths;
		//this.purify (); We think it is not needed
		this.triangulated = true;
		
	}

	/** Compute normals according to points and paths, doesn't need triangulation */
	public void computeNormalsOld () {
	
		Vector3f faceNormals[] = new Vector3f[paths.length];
		Vector3f A = new Vector3f ();
		Vector3f B = new Vector3f ();

		normals = new Vector3f[points.length];

		Vector4f vertNormals[] = new Vector4f[points.length];
		// each entry contains accumulated values + count of normals to compute
		// avg.

		// first compute normals of faces.
		for (int i = 0; i < paths.length; i++) {
			// for each face

			A.x = points[paths[i][0]].x - points[paths[i][1]].x;
			B.x = points[paths[i][1]].x - points[paths[i][2]].x;

			A.y = points[paths[i][0]].y - points[paths[i][1]].y;
			B.y = points[paths[i][1]].y - points[paths[i][2]].y;

			A.z = points[paths[i][0]].z - points[paths[i][1]].z;
			B.z = points[paths[i][1]].z - points[paths[i][2]].z;

			A.cross (A, B);
			faceNormals[i] = (Vector3f) A.clone ();

		}

		for (int i = 0; i < vertNormals.length; i++)
			vertNormals[i] = new Vector4f (0, 0, 0, 0);

		for (int i = 0; i < paths.length; i++) {
			for (int j = 0; j < paths[i].length; j++) {

				vertNormals[paths[i][j]].x += faceNormals[i].x;
				vertNormals[paths[i][j]].y += faceNormals[i].y;
				vertNormals[paths[i][j]].z += faceNormals[i].z;
				vertNormals[paths[i][j]].w += 1;
			}
		}

		for (int i = 0; i < vertNormals.length; i++)
			if (vertNormals[i].w != 0) {
				// if normals were contributed .. compute avg
				normals[i] = new Vector3f (-vertNormals[i].x / vertNormals[i].w, -vertNormals[i].y / vertNormals[i].w,
						-vertNormals[i].z / vertNormals[i].w);

			}

	}

	/** Compute normals according to points and paths, doesn't need triangulation */
	public void computeNormals () {

		normals = new Vector3f[points.length];
		for (int i = 0; i < normals.length; i++)
			normals[i] = new Vector3f ();

		Vector3f normal = new Vector3f();
		for (int i = 0; i < paths.length; i++) {

			int np = paths[i].length;
			Vector3f edge1 = new Vector3f(points[paths[i][np-1]]);
			edge1.sub(points[paths[i][0]]);
			edge1.normalize();
			for (int j = 0; j < np; j++) {
				Vector3f edge2 = new Vector3f(points[paths[i][(j+1)%np]]);
				edge2.sub(points[paths[i][j]]);
				edge2.normalize();
				normal.cross(edge1, edge2);
				
				float angle = (float) Math.asin(Math.min(1.0, normal.length()));	
				
				normal.normalize();
				normal.scale(angle);
					
				normals[paths[i][j]].add(normal);

				edge1.negate(edge2);
			}
		}

		
		for (int i = 0; i < normals.length; i++) {
			normals[i].normalize();		
			
		}
		
		
	}

	
	public String toString () {
		int[] path;
		String result = "[";
		for (int i = 0; i < points.length; i++)
			result = result + points[i].toString () + " ";
		result = result + "]:[";
		for (int p = 0; p < paths.length; p++) {
			path = paths[p];
			if (path.length > 0) {
				result = result + "[" + path[0];
				for (int i = 1; i < path.length; i++) {
					result = result + ", " + path[i];
				}
				result = result + "]";
			}
		}
		result = result + "]";
		return result;
	}


	/**
	 * Create a single SimpleMesh by concatenating all the ones given in the list. Normals are
	 * copied only if present for all meshes in the list and exactly one normal per point is
	 * provided.
	 */
	public static SimpleMesh concatenate (List<SimpleMesh> meshList) throws Exception { // fc-29.6.2012
		int nPoints = 0;
		int nPaths = 0;
		int nNormals = 0;
		boolean ignoreNormals = false;

		for (SimpleMesh m : meshList) {
			m.computeBBox ();

			// points: required for all meshes in the list
			if (m.points == null)
				throw new Exception ("SimpleMesh.concatenate (): error, one mesh in the list has no points");
			nPoints += m.points.length;

			// paths: required for all meshes in the list
			if (m.paths == null)
				throw new Exception ("SimpleMesh.concatenate (): error, one mesh in the list has no paths");
			nPaths += m.paths.length;

			// If one mesh has no normals, no normals in the resulting mesh
			if (m.normals == null) {
				ignoreNormals = true;
			} else {
				nNormals += m.normals.length;
			}
		}
		// Normals are copies only if all mesh in the list have normals and if there is a normal for
		// each point
		if (nPoints != nNormals) ignoreNormals = true;

		Point3f[] r_points = new Point3f[nPoints];
		int[][] r_paths = new int[nPaths][];

		Vector3f[] r_normals = null;
		if (!ignoreNormals) r_normals = new Vector3f[nNormals];

		int p_shift = 0;
		int f_shift = 0;

		for (SimpleMesh m : meshList) {
			// Copy the points
			for (int i = 0; i < m.points.length; i++) {

				r_points[p_shift + i] = (Point3f) m.points[i].clone ();

				if (!ignoreNormals) r_normals[p_shift + i] = m.normals[i];

			}

			// Copy the paths (faces)
			for (int i = 0; i < m.paths.length; i++) {

				r_paths[f_shift + i] = new int[m.paths[i].length];

				for (int j = 0; j < m.paths[i].length; j++) {
					int f = m.paths[i][j];
					r_paths[f_shift + i][j] = f + p_shift;
				}

			}

			p_shift += m.points.length;
			f_shift += m.paths.length;
		}

		return new SimpleMesh (r_points, r_paths, r_normals);

	}

} // SimpleMesh
