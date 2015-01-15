package fr.ird.jeeb.lib.structure.geometry.mesh;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.lib.math.InlineMath;

/**
 * An object to build simple mesh instances.
 * 
 * @author F. de Coligny - June 2012, February 2013
 */
public class SimpleMeshFactory {

	/**
	 * Creates a mesh to represent the given cylinder.
	 * 
	 * @author F. de Coligny
	 */
	static public SimpleMesh createCylinder (double x0, double y0, double z0, double radius, double length,
			int nSectors, int nSlices) throws Exception {
		return createCylinder(x0,y0,z0,radius,length,nSectors,nSlices,false);
	}	
	
	static public SimpleMesh createCylinder (double x0, double y0, double z0, double radius, double length,	
					int nSectors, int nSlices, boolean closed) throws Exception {
		if (nSectors < 3)
			throw new Exception ("SimpleMeshFactory.createCylinder() Cannot create a cylinder, nSectors: " + nSectors
					+ " must be >= 3.");

		Point3f[] points = new Point3f[nSectors * (nSlices + 1)];
		int[][] faces = null;
		
		if(closed)
			faces = new int[nSectors * nSlices * 2 + 2][]; // i.e. triangles
		else
			faces = new int[nSectors * nSlices * 2][]; // i.e. triangles
		
		
		int[] topFace = new int [nSectors];
		int[] bottomFace = new int [nSectors];

		// System.out
		// .println("SimpleMeshFactory.createCylinder () expected sizes: points: "
		// + points.length + " faces: " + faces.length + "...");

		double sectorAngle = (2 * Math.PI) / nSectors;
		double sliceSize = length / nSlices;
		int p = 0; // an index in points
		int f = 0; // an index in faces

		// Starting point
		for (p = 0; p <= nSlices; p++) {
			float x = (float) (x0 + radius);
			float y = (float) y0;
			float z = (float) (z0 + p * sliceSize);

			// System.out.println("SimpleMeshFactory.createCylinder () Starting point p: "+p);

			points[p] = new Point3f (x, y, z);
			
			if(p == 0) bottomFace [0] = p;
			if(p == nSlices) topFace [0] = p;
		}

		// Triangulated slices
		for (int i = 1; i < nSectors; i++) {
			double u = i * sectorAngle;

			float x = (float) (x0 + Math.cos (u) * radius);
			float y = (float) (y0 + Math.sin (u) * radius);

			// System.out.println("SimpleMeshFactory.createCylinder () Sector: "+i);
			
			
			for (int j = 0; j <= nSlices; j++) {
				double shift = j * sliceSize;

				// System.out.println("SimpleMeshFactory.createCylinder ()    Slice: "+j);

				float z = (float) (z0 + shift);

				points[p] = new Point3f (x, y, z);

				if (j != 0) {
					int m0 = p - (nSlices + 2);
					int m1 = m0 + 1;
					int m2 = p - 1;
					faces[f++] = new int[] {m0, m2, p};
					faces[f++] = new int[] {p, m1, m0};
				}
				
				if(j == 0) bottomFace [i] = p;
				if(j == nSlices) topFace [i] = p;

				p++;
			}

		}

		// Faces for the last sector (no more points added)
		for (int j = 0; j < nSlices; j++) {
			// double shift = j * sliceSize;

			int m0 = (p - 1) - nSlices + j;
			int m1 = m0 + 1;
			int m2 = j;
			int m3 = j + 1;

			faces[f++] = new int[] {m0, m2, m3};
			faces[f++] = new int[] {m0, m3, m1};

		}

		
		if(closed) {
			faces[f++] = topFace;
			faces[f++] = bottomFace;			
		}
		
		// Reverse bottomFaces' points to have correct normals
		int n = bottomFace.length;
		int[] aux = new int[n];
		for (int i = 0; i < n; i++) {
			aux[n - i - 1] = bottomFace[i];
		}
		bottomFace = aux;
		
		// System.out
		// .println("SimpleMeshFactory.createCylinder () real sizes: points: "
		// + p + " faces: " + f);

		SimpleMesh mesh = new SimpleMesh (points, faces);
//		mesh.computeNormals (); seems buggy on my mac... fc-12.3.2013
		
		return mesh;
	}

	/**
	 * Creates a mesh to represent the given ellipsoid. The center of the ellipsoid is (x0, y0, z0),
	 * the 3 radius are a (on the x axis), b (on the y axis) and c (on the z axis). We build a mesh
	 * with 4 * nSectors sectors and 2* nSlices slices. All the faces are ordered in the
	 * trigonometric order (convenient for further rendering).
	 * 
	 * @author F. de Coligny
	 */
	static public SimpleMesh createEllipsoid (double x0, double y0, double z0, double a, double b, double c,
			int nSectors, int nSlices) throws Exception {

		if (a == 0) throw new Exception ("SimpleMeshFactory ().createEllipsoid () Error, a must be different than 0");
		if (b == 0) throw new Exception ("SimpleMeshFactory ().createEllipsoid () Error, b must be different than 0");
		if (c == 0) throw new Exception ("SimpleMeshFactory ().createEllipsoid () Error, c must be different than 0");

		a = Math.abs (a);
		b = Math.abs (b);
		c = Math.abs (c);

		List<SimpleMesh> meshList = new ArrayList<SimpleMesh> ();

		// Top
		meshList.add (createEllipsoidHalf (x0, y0, z0, a, b, c, true, nSectors, nSlices));
		// Bottom
		meshList.add (createEllipsoidHalf (x0, y0, z0, a, b, c, false, nSectors, nSlices));

		// Merge the 2 meshes
		return SimpleMesh.concatenate (meshList); // and purify () ?

	}

	/**
	 * Creates a mesh to represent the given ellipsoid half (top or bottom). The center of the
	 * ellipsoid is (x0, y0, z0), the 3 radius are a (on the x axis), b (on the y axis) and c (on
	 * the z axis). If topPart is true, this is a top half, else a bottom half. We build a mesh with
	 * 4 * nSectors sectors and nSlices slices. All the faces are ordered in the trigonometric order
	 * (convenient for further rendering).
	 * 
	 * @author F. de Coligny
	 */
	static public SimpleMesh createEllipsoidHalf (double x0, double y0, double z0, double a, double b, double c,
			boolean topPart, int nSectors, int nSlices) throws Exception {

		if (a == 0)
			throw new Exception ("SimpleMeshFactory ().createEllipsoidHalf () Error, a must be different than 0");
		if (b == 0)
			throw new Exception ("SimpleMeshFactory ().createEllipsoidHalf () Error, b must be different than 0");
		if (c == 0)
			throw new Exception ("SimpleMeshFactory ().createEllipsoidHalf () Error, c must be different than 0");

		a = Math.abs (a);
		b = Math.abs (b);
		c = Math.abs (c);

		if (!topPart) c = -c;

		// The final ellipsoid half will have at least 4 * nSectors sectors

		List<SimpleMesh> meshList = new ArrayList<SimpleMesh> ();

		meshList.add (createEllipsoid8th (x0, y0, z0, a, b, c, nSectors, nSlices));
		meshList.add (createEllipsoid8th (x0, y0, z0, -a, b, c, nSectors, nSlices));
		meshList.add (createEllipsoid8th (x0, y0, z0, -a, -b, c, nSectors, nSlices));
		meshList.add (createEllipsoid8th (x0, y0, z0, a, -b, c, nSectors, nSlices));

		// Merge all meshes
		return SimpleMesh.concatenate (meshList); // and purify () ?

	}

	/**
	 * Creates a mesh to represent the given ellipsoid 8th. The center of the ellipsoid is (x0, y0,
	 * z0), the 3 radius are a (on the x axis), b (on the y axis) and c (on the z axis). We can know
	 * what part of the ellipsoid is under consideration by testing the signs of a, b and c. We
	 * build a mesh with nSectors sectors and nSlices slices. All the faces are ordered in the
	 * trigonometric order (convenient for further rendering).
	 * 
	 * @author F. de Coligny
	 */
	static public SimpleMesh createEllipsoid8th (double x0, double y0, double z0, double a, double b, double c,
			int nSectors, int nSlices) throws Exception {
		if (nSectors < 1)
			throw new Exception ("SimpleMeshFactory.createEllipsoid8th() Cannot create an ellipsoid 8th, nSectors: "
					+ nSectors + " must be >= 1.");
		if (a == 0 || b == 0 || c == 0)
			throw new Exception (
					"SimpleMeshFactory.createEllipsoid8th() Cannot create an ellipsoid 8th, a, b and c must be different than 0, a: "
							+ a + " b: " + b + " c: " + c);
		// System.out.println("SimpleMeshFactory.createEllipsoid8th () nSectors: "
		// + nSectors + " nSlices: " + nSlices);

		double sector0 = 0;
		if (a < 0 && b > 0) sector0 = Math.PI / 2;
		if (a < 0 && b < 0) sector0 = Math.PI;
		if (a > 0 && b < 0) sector0 = 3 * Math.PI / 2;

		Point3f[] points = new Point3f[(nSectors + 1) * nSlices + 1];
		int[][] faces = new int[nSectors * (nSlices - 1) * 2 + nSectors][3]; // i.e.
																				// triangles

		// System.out
		// .println("SimpleMeshFactory.createEllipsoid8th () expected sizes: points: "
		// + points.length + " faces: " + faces.length + "...");

		double sectorAngle = (Math.PI / 2) / nSectors;
		double sliceAngle = (Math.PI / 2) / nSlices;
		int p = 0; // an index in points
		int f = 0; // an index in faces

		// Starting point
		for (p = 0; p <= nSlices; p++) {
			// u: sector, v: slice
			double u = sector0;
			double v = p * sliceAngle;
			if (c < 0) v = -v;
			points[p] = getPointInEllipsoid (x0, y0, z0, a, b, c, u, v);
		}
		int extreme = nSlices;

		// System.out
		// .println("SimpleMeshFactory.createEllipsoid8th () end-of-Starting point: extreme: "
		// + extreme);

		// Triangulated slices
		for (int i = 1; i <= nSectors; i++) {
			double u = sector0 + i * sectorAngle;

			// System.out
			// .println("SimpleMeshFactory.createEllipsoid8th () Sector: " + i);

			for (int j = 0; j < nSlices; j++) {
				double v = j * sliceAngle;
				if (c < 0) v = -v;

				// System.out
				// .println("SimpleMeshFactory.createEllipsoid8th ()    Slice: "
				// + j);

				Point3f p0 = getPointInEllipsoid (x0, y0, z0, a, b, c, u, v);

				int i0 = p;
				points[i0] = p0;

				int m0 = p - (nSlices + 1);

				if (i > 1) m0++; // extreme is only once in points
				int m1 = m0 + 1;

				if (j == 0) { // 1 single triangle
					if (c >= 0) // always stay CCW
						faces[f++] = new int[] {m0, i0, m1};
					else
						faces[f++] = new int[] {m1, i0, m0};

				} else { // 2 triangles
					int m2 = p - 1;

					if (j == (nSlices - 1)) {
						m1 = extreme; // one vertex is extreme
					}
					if (c >= 0) { // always stay CCW
						faces[f++] = new int[] {m0, m2, i0};
						faces[f++] = new int[] {m0, i0, m1};
					} else {
						faces[f++] = new int[] {i0, m2, m0};
						faces[f++] = new int[] {m1, i0, m0};
					}
				}

				p++;
			}

		}

		// System.out
		// .println("SimpleMeshFactory.createEllipsoid8th () accurate sizes: points: "
		// + p + " faces: " + f);

		SimpleMesh mesh = new SimpleMesh (points, faces);

		return mesh;

	}

	/**
	 * In an ellipsoid (x0, y0, z0, a, b, c), calculates the point at the given u (sector angle) and
	 * v (slice angle).
	 * 
	 * @author F. de Coligny
	 */
	static private Point3f getPointInEllipsoid (double x0, double y0, double z0, double a, double b, double c,
			double u, double v) {
		double x = x0 + Math.abs (a) * Math.cos (v) * Math.cos (u);
		double y = y0 + Math.abs (b) * Math.cos (v) * Math.sin (u);
		double z = z0 + Math.abs (c) * Math.sin (v);
		return new Point3f ((float) x, (float) y, (float) z);
	}

	/**
	 * Creates a polygon defined by the X and Y coordinates.
	 * 
	 * @param p array of Point2f coordinates
	 * @return a simple mesh
	 * @author J. Dauzat, moved in SimpleMeshFactory by F. de Coligny and S. Griffon, feb 2013
	 */
	static public SimpleMesh polygon (Point2f[] p) {
		SimpleMesh mesh = new SimpleMesh ();
		int k = p.length;
		mesh.paths = new int[1][k];
		mesh.points = new Point3f[k];
		mesh.normals = new Vector3f[k];

		for (int i = 0; i < k; i++) {
			mesh.paths[0][i] = i;
			mesh.points[i] = new Point3f (p[i].x, p[i].y, 0);
			mesh.normals[i] = new Vector3f (0, 0, 1);
		}
		return mesh;
	}

	/**
	 * Creates a k-sided regular polygon approximation to a circular disk.
	 * 
	 * @param k number of sides of the regular polygon
	 * @return current shape geometry
	 * @author S. Griffon, moved in SimpleMeshFactory by F. de coligny and S. Griffon, feb 2013
	 */
	static public SimpleMesh disk (int k) {
		SimpleMesh mesh = new SimpleMesh ();
		mesh.paths = new int[k][3];
		mesh.points = new Point3f[k + 1];
		mesh.normals = new Vector3f[k + 1];
		for (int i = 0; i < k; i++) {
			mesh.paths[i][0] = i;
			mesh.paths[i][1] = (i + 1) % k;
			mesh.paths[i][2] = k;
			double theta = 2 * Math.PI * i / k;
			mesh.points[i] = new Point3f ((float) Math.cos (theta), (float) Math.sin (theta), 0.0f);
			mesh.normals[i] = new Vector3f (0, 0, 1);
		}
		mesh.points[k] = new Point3f (0.0f, 0.0f, 0.0f);
		mesh.normals[k] = new Vector3f (0, 0, 1);
		return mesh;
	}

	/**
	 * Creates a parallelepiped. The bottom face is centered on the origin.
	 * 
	 * @author F. de coligny, April 2013
	 */
	static public SimpleMesh makeParallelepiped (double xSize, double ySize, double zSize) {
		SimpleMesh mesh = new SimpleMesh ();
		mesh.points = new Point3f[8];		
		mesh.paths = new int[6][4];
		
		float x0 = (float)-xSize / 2f;
		float x1 = (float)xSize / 2f;
		float y0 = (float)-ySize / 2f;
		float y1 = (float)ySize / 2f;
		float z0 = 0f;
		float z1 = (float)zSize;

		// 4 bottom face points
		mesh.points[0] = new Point3f (x0, y0, z0);
		mesh.points[1] = new Point3f (x1, y0, z0);
		mesh.points[2] = new Point3f (x1, y1, z0);
		mesh.points[3] = new Point3f (x0, y1, z0);
		
		// 4 top face points
		mesh.points[4] = new Point3f (x0, y0, z1);
		mesh.points[5] = new Point3f (x1, y0, z1);
		mesh.points[6] = new Point3f (x1, y1, z1);
		mesh.points[7] = new Point3f (x0, y1, z1);
		
		// 6 faces
		mesh.paths[0] = new int[] {3, 2, 1, 0}; // bottom
		mesh.paths[1] = new int[] {4, 5, 6, 7}; // top
		mesh.paths[2] = new int[] {1, 2, 6, 5}; //right
		mesh.paths[3] = new int[] {2, 3, 7, 6};
		mesh.paths[4] = new int[] {0, 4, 7, 3};
		mesh.paths[5] = new int[] {0, 1, 5, 4};
		
		mesh.triangulate ();
		mesh.computeNormals ();
		
		return mesh;
	}
	
	/**
	 * Creates a parallelepiped. The bottom face is centered on the origin.
	 * 
	 * @author F. de coligny, April 2013
	 */
	static public SimpleMesh makeRichParallelepiped (double xSize, double ySize, double zSize) {
		SimpleMesh mesh = new SimpleMesh ();
		mesh.points = new Point3f[24];	
		mesh.normals = new Vector3f [24];
		mesh.paths = new int[6][4];
		
		float x0 = (float)-xSize / 2f;
		float x1 = (float)xSize / 2f;
		float y0 = (float)-ySize / 2f;
		float y1 = (float)ySize / 2f;
		float z0 = 0f;
		float z1 = (float)zSize;

		//Series 1 Top & Bottom
		// 4 bottom face points
		mesh.points[0] = new Point3f (x0, y0, z0);
		mesh.normals[0] = new Vector3f (0,0,-1);
		mesh.points[1] = new Point3f (x1, y0, z0);
		mesh.normals[1] = new Vector3f (0,0,-1);
		mesh.points[2] = new Point3f (x1, y1, z0);
		mesh.normals[2] = new Vector3f (0,0,-1);
		mesh.points[3] = new Point3f (x0, y1, z0);
		mesh.normals[3] = new Vector3f (0,0,-1);
		
		// 4 top face points
		mesh.points[4] = new Point3f (x0, y0, z1);
		mesh.normals[4] = new Vector3f (0,0,1);
		mesh.points[5] = new Point3f (x1, y0, z1);
		mesh.normals[5] = new Vector3f (0,0,1);
		mesh.points[6] = new Point3f (x1, y1, z1);
		mesh.normals[6] = new Vector3f (0,0,1);
		mesh.points[7] = new Point3f (x0, y1, z1);
		mesh.normals[7] = new Vector3f (0,0,1);
		
		
		//Series 2 Right & Left
		
		mesh.points[8] = new Point3f (x0, y0, z0);
		mesh.normals[8] = new Vector3f (-1,0,0);
		mesh.points[9] = new Point3f (x1, y0, z0);
		mesh.normals[9] = new Vector3f (1,0,0);
		mesh.points[10] = new Point3f (x1, y1, z0);
		mesh.normals[10] = new Vector3f (1,0,0);
		mesh.points[11] = new Point3f (x0, y1, z0);
		mesh.normals[11] = new Vector3f (-1,0,0);
		
		mesh.points[12] = new Point3f (x0, y0, z1);
		mesh.normals[12] = new Vector3f (-1,0,0);
		mesh.points[13] = new Point3f (x1, y0, z1);
		mesh.normals[13] = new Vector3f (1,0,0);
		mesh.points[14] = new Point3f (x1, y1, z1);
		mesh.normals[14] = new Vector3f (1,0,0);
		mesh.points[15] = new Point3f (x0, y1, z1);
		mesh.normals[15] = new Vector3f (-1,0,0);
		
		
		
		//Series 3 Front & back
		
		mesh.points[16] = new Point3f (x0, y0, z0);
		mesh.normals[16] = new Vector3f (0,-1,0);
		mesh.points[17] = new Point3f (x1, y0, z0);
		mesh.normals[17] = new Vector3f (0,-1,0);
		mesh.points[18] = new Point3f (x1, y1, z0);
		mesh.normals[18] = new Vector3f (0,1,0);
		mesh.points[19] = new Point3f (x0, y1, z0);
		mesh.normals[19] = new Vector3f (0,1,0);
		
		mesh.points[20] = new Point3f (x0, y0, z1);
		mesh.normals[20] = new Vector3f (0,-1,0);
		mesh.points[21] = new Point3f (x1, y0, z1);
		mesh.normals[21] = new Vector3f (0,-1,0);
		mesh.points[22] = new Point3f (x1, y1, z1);
		mesh.normals[22] = new Vector3f (0,1,0);
		mesh.points[23] = new Point3f (x0, y1, z1);
		mesh.normals[23] = new Vector3f (0,1,0);
		
		// 6 faces
		mesh.paths[0] = new int[] {3, 2, 1, 0}; // bottom
		mesh.paths[1] = new int[] {4, 5, 6, 7}; // top
		mesh.paths[2] = new int[] {1+8, 2+8, 6+8, 5+8}; //right
		mesh.paths[3] = new int[] {2+16, 3+16, 7+16, 6+16}; // back
		mesh.paths[4] = new int[] {0+8, 4+8, 7+8, 3+8}; //left
		mesh.paths[5] = new int[] {0+16, 1+16, 5+16, 4+16}; // front
		
		mesh.triangulate ();
		
		
		return mesh;
	}
	
	/**
	 * Build a simple tetrahedron. 
	 * 
	 * @author S. Griffon, moved in SimpleMeshFactory by F. de coligny and S. Griffon, feb 2013
	 */
	public static SimpleMesh makeTetrahedron () {
		double rt3 = Math.sqrt (3);
		final Point3f[] points = new Point3f[] {new Point3f (0, 0, 0), new Point3f (1, 0, 0),
				new Point3f ((float) .5, 0, (float) rt3 / 2),
				new Point3f ((float) .5, InlineMath.sqrt ((float) 2D / 3), (float) rt3 / 6)};

		final int[][] faces = new int[][] { {0, 1, 2}, {0, 3, 1}, {1, 2, 3}, {2, 3, 0}};
		return new SimpleMesh (points, faces);
	}

	protected static final Point3f[] CUBE_POINTS = new Point3f[] {new Point3f (0, 0, 0), new Point3f (1, 0, 0),
			new Point3f (1, 0, 1), new Point3f (0, 0, 1), new Point3f (0, 1, 0), new Point3f (1, 1, 0),
			new Point3f (1, 1, 1), new Point3f (0, 1, 1),};

	protected static final int[][] CUBE_FACES = new int[][] { {0, 1, 2, 3}, {0, 1, 5, 4}, {0, 3, 7, 4}, {1, 2, 6, 5},
			{2, 3, 7, 6}, {4, 5, 6, 7}};

	/**
	 * Builds a simple cube.
	 * 
	 * @author S. Griffon, moved in SimpleMeshFactory by F. de coligny and S. Griffon, feb 2013
	 */
	public static SimpleMesh makeCube () {

		return new SimpleMesh (CUBE_POINTS, CUBE_FACES);
	}

	/**
	 * Builds a simple 2D grid.
	 * 
	 * @author S. Griffon, moved in SimpleMeshFactory by F. de coligny and S. Griffon, feb 2013
	 */
	public static SimpleMesh makeGrid (int horiz, int vert, float hspacing, float vspacing) {
		Point3f[] points = new Point3f[horiz * vert];
		Vector3f[] normals = new Vector3f [horiz * vert];
		
		for (int j = 0; j < vert; j++)
			for (int i = 0; i < horiz; i++) {
				points[j * horiz + i] = new Point3f (i * hspacing, j * vspacing , 0);
				normals[j * horiz + i] = new Vector3f (0,0,1);
			}

		int nSquares = (horiz - 1) * (vert - 1);
		int[][] faces = new int[nSquares][4];
		
		
		int index = 0;
		int sqIndex = 0;
		for (int j = 0; j < vert - 1; j++) {
			for (int i = 0; i < horiz - 1; i++) {
				faces[sqIndex] = new int[] {index, index + 1, index + 1 + horiz, index + horiz};
				sqIndex = sqIndex + 1;
				index = index + 1;
			}
			index = index + 1;
		}

		return new SimpleMesh (points, faces,normals);
	}
	
	public static SimpleMesh makePlane (float xSize, float ySize) {
		return makeGrid (1, 1, xSize, ySize);
	}
	
	public static void main (String[] args) {
		try {
			
			SimpleMesh mesh1 = makeParallelepiped (5, 2, 100); // cms
			
			SimpleMesh mesh2 = createCylinder (0, 0, 100, 25, 2, 8, 2, true); // cms
			
//			// testing the gwa factory and writer
//			HashMap<String,SimpleMesh> map = new HashMap<String,SimpleMesh> ();
//			map.put ("Parallelepiped", mesh1);
//			map.put ("Disk", mesh2);
//			
//			ArchiTree gwa = GeometryWithAttribute.createGWA (map);
//
//			GWAWriter writer = new GWAWriter ("./test.gwa");
//			try {
//				writer.save (gwa);
//			} catch (Exception e) {
//				e.printStackTrace ();
//			}
			
			
			
//			Mesh mesh9 = new Mesh (mesh1);
//			ObjMeshWriter writer = new ObjMeshWriter ("./testMesh.obj");
//			writer.save (mesh9);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}


