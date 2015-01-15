package fr.ird.jeeb.lib.structure.geometry.mesh;

import java.io.Serializable;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.lib.structure.geometry.spline.SplineFactory;

/**
 * 
 * @author griffon
 * 
 * 
 */
public class Mesh extends SimpleMesh implements Serializable, Cloneable {

	static private final long serialVersionUID = 1L;


	public static enum DrawMode {
		TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN, LINES, LINE_STRIP, LINE_LOOP, POLYGON, POINTS
	};

	private String filename;

	private boolean enableScaling = true;

	/**
	 * Stores the children geometries of the current geometry.
	 */

	public Mesh child[] = null;

	private Mesh parent = null;

	protected Point2f[] uvtexture;

	protected DrawMode drawMode;
	protected MeshMaterial material;

	public Mesh () {

		uvtexture = null;
		material = null;
		filename = null;
		drawMode = DrawMode.TRIANGLES;
	} // Mesh constructor

	/**
	 * Creates a new <code>Mesh</code> instance to represent a simple path in 3D space.
	 * 
	 * @param pts The points along the simple path (in order of the path)
	 */
	public Mesh (Point3f[] pts) {

		super (pts);
		uvtexture = null;
		filename = null;
		drawMode = DrawMode.TRIANGLES;

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
	public Mesh (Point3f[] pts, int[][] connections) {

		super (pts, connections);

		material = null;
		uvtexture = null;
		filename = null;
		drawMode = DrawMode.TRIANGLES;
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
	public Mesh (Point3f[] pts, int[][] connections, Vector3f[] norm) {

		super (pts, connections, norm);

		material = null;
		uvtexture = null;
		filename = null;
		drawMode = DrawMode.TRIANGLES;
	}

	public Mesh (Point3f[] pts, int[][] connections, Vector3f[] norm, Point2f[] uv) {

		super (pts, connections, norm);

		material = null;
		filename = null;
		uvtexture = uv;
		drawMode = DrawMode.TRIANGLES;
	}

	/**
	 * Creates a new <code>Mesh</code> instance from a existing Mesh
	 * 
	 * @param mesh The mesh to copy into this
	 */
	public Mesh (Mesh mesh) {

		super (mesh);

		child = mesh.child;
		parent = mesh.parent;
		uvtexture = mesh.uvtexture;

		material = mesh.getMaterial ();
		drawMode = mesh.getDrawMode ();
		enableScaling = mesh.enableScaling;
		filename = mesh.filename;

		if (mesh.getUVTexture () != null) uvtexture = mesh.getUVTexture ().clone ();

	}

	/**
	 * Creates a new <code>Mesh</code> instance from a existing Mesh
	 * 
	 * @param mesh The mesh to copy into this
	 */
	public Mesh (SimpleMesh mesh) {

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
				paths = new int[currentPath.length][];
				for (int i = 0; i < mesh.getPaths ().length; i++) {
					paths[i] = new int [currentPath[i].length];
					for (int j = 0; j < currentPath[i].length; j++) {
						paths[i][j] = currentPath[i][j];
					}
				}
			}
		}

		volumic = mesh.volumic;
		triangulated = mesh.triangulated;
		material = null;
		uvtexture = null;
		filename = null;
		drawMode = DrawMode.TRIANGLES;
		

	}

	public void copyFrom (Mesh mesh) {
		super.copyFrom (mesh);
		material = mesh.getMaterial ();
		drawMode = mesh.getDrawMode ();
		filename = mesh.filename;
		enableScaling = mesh.enableScaling;

		if (mesh.getUVTexture () != null) uvtexture = mesh.getUVTexture ().clone ();
		if (mesh.getUVTexture () != null) {
			uvtexture = new Point2f[mesh.getUVTexture ().length];
			int i = 0;
			for (Point2f v : mesh.getUVTexture ()) {
				uvtexture[i] = new Point2f (v);
				i++;
			}
		}
	}

	public void setEnableScaling (boolean enableScaling) {
		this.enableScaling = enableScaling;
	}

	public boolean getEnableScaling () {
		return this.enableScaling;
	}

	public void setDrawMode (DrawMode drawMode) {
		this.drawMode = drawMode;
	}

	public DrawMode getDrawMode () {
		return drawMode;
	}

	/**
	 * Returns the parent geometry object.
	 * 
	 * @return the parent geometry object
	 */
	public Mesh getParent () {
		return parent;
	}

	/**
	 * Returns the nth child of the geometry .
	 * 
	 * @param n the index of the child requested
	 * @return the shape geometry of the child.
	 */
	public Mesh child (int n) {
		if (n < child.length) return child[n];
		return null;
	}

	/**
	 * Adds a new child shape to this shape.
	 * No triangulation needed here
	 */
	public Mesh add () {
		Mesh s = add (new Mesh ());
		s.parent = this;
		s.material = material;
		return s;
	}

	/**
	 * Add an existing shape, as a child to this shape.
	 * No triangulation needed here
	 * @param s shape Geometry to be added
	 * @return s the added shape
	 */
	public Mesh add (Mesh s) {
		s.parent = this;
		if (child == null)
			child = new Mesh[16];
		else if (child[child.length - 1] != null) {
			Mesh c[] = child;
			child = new Mesh[2 * c.length];
			for (int i = 0; i < c.length; i++)
				child[i] = c[i];
		}

		for (int i = 0; i < child.length; i++)
			if (child[i] == null) {
				child[i] = s;
				if (s.material == null) s.material = material;
				break;
			}
		return s;
	}

	/**
	 * Delete a child of a shape.
	 * No triangulation needed here
	 * @param s shape to be deleted from list of children
	 * @return the current shape geometry
	 */
	public Mesh delete (Mesh s) {
		if (child != null) for (int i = 0; i < child.length; i++)
			if (child[i] == s) {
				delete (i);
				break;
			}
		return this;
	}

	/**
	 * Delete the nth child of a shape.
	 * No triangulation needed here
	 * @param n the index of the child to be deleted
	 * @return the current shape geometry
	 */
	public Mesh delete (int n) {
		if (child != null && n >= 0 && n < child.length) {
			for (; n < child.length - 1 && child[n + 1] != null; n++)
				child[n] = child[n + 1];
			child[n] = null;
		}
		return this;
	}

	/**
	 * Find out whether the argument is a sub-geometry of this geometry object.
	 * 
	 * @param s Geometry to be tested
	 * @return true or false
	 */
	public boolean contains (Mesh s) {
		for (; s != null; s = s.getParent ())
			if (s == this) return true;
		return false;
	}

	public MeshMaterial getMaterial () {
		return material;
	}

	public void setFilename (String fileRef) {
		this.filename = fileRef;
	}

	public String getFilename () {
		return filename;
	}

	public void setMaterial (MeshMaterial material) {
		this.material = material;

		if (child != null) for (int i = 0; i < child.length; i++)
			if (child[i] != null) child[i].setMaterial (material);
	}

	public Point2f[] getUVTexture () {
		return uvtexture;
	}

	public void setUVTexture (Point2f[] uv) {
		uvtexture = uv;
	}

	

	/** translates the vertices in this polygon by the position */
	public Object clone () {
		Mesh mesh = new Mesh ((SimpleMesh) super.clone ());

		mesh.setMaterial (this.getMaterial ());
		mesh.setDrawMode (drawMode);

		mesh.filename = this.filename;

		if (uvtexture != null) {

			Point2f[] uvtexture2 = new Point2f[uvtexture.length];
			for (int i = 0; i < uvtexture.length; i++) {
				uvtexture2[i] = (Point2f) uvtexture[i].clone ();
			}
			mesh.setUVTexture (uvtexture2);
		}

		return mesh;
	}

	
// SOME METHODS BELOW MAY BE MOVED IN A MeshFactory and turned into static methods (like SimpleMeshFactory) fc, sg, 13.2.2013
	
	/**
	 * Creates a new m by n rectangular mesh ( rows by columns ).
	 * 
	 * @param m number of rows
	 * @param n number of columns
	 * @return the current shape geometry
	 */
	public Mesh rectangularMesh (int m, int n) {
		newRectangularMesh (m, n);
		Point3f vertex = new Point3f (0, 0, 0);
		Vector3f normal = new Vector3f (0, 0, 1);
		Point2f textureUV = new Point2f (0, 0);

		for (int k = 0; k <= n; k++)
			for (int j = 0; j <= m; j++) {
				vertex.x = (float) (2. * j / m - 1);
				vertex.y = (float) (2. * k / n - 1);

				textureUV.x = (float) (1. * j / m); // u goes from 0 to 1,

				// not convince in this
				// system yet
				// textureUV.y = (float) (1. * k / n); // v goes from 0 to 1
				textureUV.y = (float) (1.0); // v goes from 0 to 1

				this.points[k * (m + 1) + j] = (Point3f) vertex.clone ();
				this.normals[k * (m + 1) + j] = (Vector3f) normal.clone ();
				this.uvtexture[k * (m + 1) + j] = (Point2f) textureUV.clone ();

			}
		return this;
	}

	private void newRectangularMesh (int m, int n) {
		paths = new int[m * n][4];

		for (int k = 0; k < n; k++)
			for (int j = 0; j < m; j++) {
				int f = k * m + j;
				int v = k * (m + 1) + j;
				paths[f][0] = v;
				paths[f][1] = v + 1;
				paths[f][2] = v + m + 1 + 1;
				paths[f][3] = v + m + 1;
			}

		points = new Point3f[(m + 1) * (n + 1)];
		normals = new Vector3f[(m + 1) * (n + 1)];
		uvtexture = new Point2f[(m + 1) * (n + 1)];

	}

	public static Mesh makeCube () {

		return new Mesh (SimpleMeshFactory.CUBE_POINTS, SimpleMeshFactory.CUBE_FACES);
	}

	// GLOBE (LONGITUDE/LATITUDE SPHERE)
	/**
	 * Creates a longitude/latitude partitioned sphere where m and n specify longitude and latitude
	 * respectively.
	 * 
	 * @param m number of longitude subdivisions
	 * @param n number of latitude subdivisions
	 * @return the shape geometry.
	 * @see #ball
	 */
	public Mesh globe (int m, int n) {
		return globe (m, n, 0, 1, 0, 1);
	}

	// PARAMETRIC SUBSECTION OF A GLOBE
	/**
	 * Creates a longitude/latitude partitioned sphere delimited by ranges in the east-west and
	 * north-south directions.
	 * 
	 * @param m number of longitude subdivisions
	 * @param n number of latitude subdivisions
	 * @param uLo low end of the east-west range [0..1]
	 * @param uHi high end of the east-west range [0..1]
	 * @param vLo low end of the north-south range [0..1]
	 * @param vHi high end of the north-south range [0..1]
	 * @return the shape geometry.
	 */
	public Mesh globe (int m, int n, double uLo, double uHi, double vLo, double vHi) {
		rectangularMesh (m, n);
		int N = 0;
		for (int j = 0; j <= n; j++)
			for (int i = 0; i <= m; i++) {
				double u = uLo + i * (uHi - uLo) / m;
				double v = vLo + j * (vHi - vLo) / n;
				double theta = 2 * u * Math.PI;
				double phi = (v - .5) * Math.PI;
				double x = Math.cos (phi) * Math.cos (theta);
				double y = Math.cos (phi) * Math.sin (theta);
				double z = Math.sin (phi);
				setVertex (N++, x, y, z, x, y, z);
				// uvtexture[i] = new Point2f((float)u,(float)v);
			}

		return this;
	}

	/**
	 * Origin of the noise space, default [0, 0, 0].
	 */
	public double noiseOrigin[] = {0, 0, 0};

	/**
	 * Displaces the shape geometry (each vertex) and its children by noise determined by frequency
	 * and amplitude.
	 * 
	 * @param freq frequency of noise
	 * @param ampl amplitude of noise
	 * @see #addImprovedNoise(double freq, double ampl)
	 */
	public void displaceByImprovedNoise (double freq, double ampl) {
		if (child != null) for (int i = 0; i < child.length && child[i] != null; i++)
			child[i].displaceByImprovedNoise (freq, ampl);

		double[][] vertices = setCoordinateToVertex (points, normals);
		if (points != null) {
			double v[][] = vertices, x, y, z, s;
			for (int k = 0; k < v.length; k++) {
				x = freq * (v[k][0] + noiseOrigin[0]);
				y = freq * (v[k][1] + noiseOrigin[1]);
				z = freq * (v[k][2] + noiseOrigin[2]);
				s = ampl * ImprovedNoise.noise (x, y, z);
				if (v[k][3] * v[k][3] + v[k][4] * v[k][4] + v[k][5] * v[k][5] < 2) {
					v[k][0] += s * v[k][3];
					v[k][1] += s * v[k][4];
					v[k][2] += s * v[k][5];
				}
				setVertex (k, v[k][0], v[k][1], v[k][2], v[k][3], v[k][4], v[k][5]);
			}

		}
		this.computeNormals ();
	}

	/**
	 * Makes a smooth curve
	 */
	public static void makeCurve (double X[], double Y[], double T[], double C[]) {
		double S[] = new double[X.length]; // SLOPE
		int n = X.length;

		for (int i = 1; i < n - 1; i++)
			S[i] = (Y[i] >= Y[i - 1]) == (Y[i] >= Y[i + 1]) ? 0 : ((Y[i + 1] - Y[i]) * (X[i] - X[i - 1]) + (Y[i] - Y[i - 1]) * (X[i + 1] - X[i]))
					/ ((X[i + 1] - X[i - 1]) * (X[i + 1] - X[i - 1]));

		S[0] = 2 * (Y[1] - Y[0]) / (X[1] - X[0]) - S[1];
		S[n - 1] = 2 * (Y[n - 1] - Y[n - 2]) / (X[n - 1] - X[n - 2]) - S[n - 2];

		int k = C.length;
		for (int j = 0; j < k; j++) {
			double t = j / (k - .99);
			double x = X[0] + t * (X[n - 1] - X[0]);
			int i = 0;
			for (; i < n - 1; i++)
				if (x >= X[i] != x >= X[i + 1]) break;
			T[j] = x;
			C[j] = hermite (X[i], X[i + 1], Y[i], Y[i + 1], S[i], S[i + 1], x);
		}
	}

	/**
	 * Sets the coordinates and normal values of vertex i.
	 * 
	 * @param i index of the vertex to be set
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param z z coordinate
	 * @param nx x normal
	 * @param ny y normal
	 * @param nz z normal
	 */
	protected void setVertex (int i, double x, double y, double z, double nx, double ny, double nz) {
		points[i] = new Point3f ((float) x, (float) y, (float) z);
		normals[i] = new Vector3f ((float) nx, (float) ny, (float) nz);
	}

	/**
	 * Sets the coordinates and normal values to an array.
	 * 
	 */
	public static double[][] setCoordinateToVertex (Point3f[] p, Vector3f[] n) {
		double[][] array = new double[p.length][6];
		for (int i = 0; i < p.length; i++) {
			array[i][0] = p[i].x;
			array[i][1] = p[i].y;
			array[i][2] = p[i].z;
			array[i][3] = n[i].x;
			array[i][4] = n[i].y;
			array[i][5] = n[i].z;
		}

		return array;
	}

	/**
	 * Sets the vertex array to a point 1-D array
	 * 
	 */
	public static double[] setVertexToPoint (double[][] v) {
		double[] point = new double[v.length * 3];
		for (int i = 0; i < v.length; i++) {
			point[i * 3] = v[i][0];
			point[(i * 3) + 1] = v[i][1];
			point[(i * 3) + 2] = v[i][2];
		}

		return point;
	}

	/**
	 * Creates a regular n-sided polygonal approximation to a circle with given radius.
	 * 
	 * @param n number of sides in the polygon
	 * @param radius the radius of the circle
	 * @return the set of vertices defining the polygon. [point number][x, y, z coordinates]
	 */
	public static double[][] makeCircle (int n, double radius) {
		
		
		double P[][] = new double[n + 1][6];
		for (int i = 0; i <= n; i++) {
			double theta = 2 * Math.PI * i / n;
			double cos = Math.cos (theta);
			double sin = Math.sin (theta);

			P[i][0] = radius * cos; // LOCATION
			P[i][1] = radius * sin;
			P[i][2] = 0;

			P[i][3] = cos; // NORMAL DIRECTION
			P[i][4] = sin;
			P[i][5] = 0;
		}
		
		return P;
	}

	/**
	 * Creates a smooth path composed of n subsegments that passes through the desired key points
	 * (uses hermite spline).
	 * 
	 * @param n number of subdivisions in path
	 * @param key a set of points (inluding normals) defining key positions
	 * @return array of n+1 points defining the path
	 */
	public static double[][] makePath (int n, double key[][]) {

		double P[][] = new double[n + 1][6];

		for (int i = 0; i <= n; i++) {
			double t = i / (n + .001);
			double f = t * (key.length - 1);
			int k = (int) f;
			for (int j = 0; j < 3; j++)
				P[i][j] = hermite (0, 1, key[k][j], key[k + 1][j], key[k][3 + j], key[k + 1][3 + j], f % 1.0);
			for (int j = 3; j < 6; j++)
				P[i][j] = key[k][j];
		}
		return P;
	}

	public static double[][] makeSpline (int n, double key[][]) {

		double[] points = setVertexToPoint (key);
		double[] spline = SplineFactory.createCubic (points, n);
		int nbSeg = (int) (spline.length / 3.0);

		double P[][] = new double[nbSeg][6];
		for (int i = 0; i < nbSeg; i++) {

			double t = i / (nbSeg + .001);
			double f = t * (key.length - 1);
			int k = (int) f;

			for (int j = 0; j < 3; j++)
				P[i][j] = spline[(i * 3) + j];

			for (int j = 3; j < 6; j++)
				P[i][j] = key[k][j];
		}
		return P;
	}

	/**
	 * 
	 */
	public static double[] makeInterpolation (int n, double key[]) {

		double P[] = new double[n + 1];

		for (int i = 0; i <= n; i++) {
			double t = i / (n + .001);
			double f = t * (key.length - 1);
			int k = (int) f;
			P[i] = (f - (double) k) * (key[k + 1] - key[k]) + key[k];
			// P[i] = hermite(0, 1, key[k], key[k + 1], key[k], key[k + 1], f % 1.0);

		}
		return P;
	}

	private static double hermite (double x0, double x1, double y0, double y1, double s0, double s1, double x) {
		double t = (x - x0) / (x1 - x0);
		double s = 1 - t;
		return y0 * s * s * (3 - 2 * s) + s0 * (1 - s) * s * s - s1 * (1 - t) * t * t + y1 * t * t * (3 - 2 * t);
	}

} // Mesh
