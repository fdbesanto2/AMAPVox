package fr.ird.jeeb.workspace.archimedes.geometry.shapes;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.lib.structure.geometry.mesh.Mesh;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.shapemodeler.HullMesh;
import fr.ird.jeeb.workspace.archimedes.util.ArtLog;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.stat.correlation.Covariance;
import org.ejml.simple.SimpleMatrix;

/**
 * Computes bounding objects (Sphere, Ellipsoid, Convex Hull...) of a given set of points.
 * @author Cresson, Sept. 2012
 *
 */
public class BoundingShapeComputing {
	private	static final float			eps 		= 0.01f;
	private	static final boolean		debugExport	= false;
	private	static final boolean		debugInfo	= true;
	private	static final String			debugPath	= "/home/cresson/Octave/debug_bsc/";
	/*
	 * Class used for minimum enclosing sphere problem solving
	 */
	private class Ball {
		
		public float radius;
		public Point3f center;

		public Ball(){
			radius = -1.0f;
		}
		public Ball(Point3f O) {
			radius	= 0f + eps;
			center	= new Point3f(O);
		}
		public Ball(Point3f O, float r) {
			radius	= r + eps;
			center	= new Point3f(O);
		}
		public Ball(Point3f O, Point3f A) {
			Vector3f a = new Vector3f(A); a.sub (O);		// a = A - O
			Vector3f o = new Vector3f(a); o.scale (0.5f);	// o = 0.5*a
			radius	= o.length () + eps;
			center	= new Point3f(O); center.add (o);		// center = O + o
		}
		public Ball(Point3f O, Point3f A, Point3f B) {
			Vector3f a = new Vector3f(A); a.sub (O);		// a = A - O
			Vector3f b = new Vector3f(B); b.sub (O);		// b = B - O

			Vector3f aCROSSb = new Vector3f(); aCROSSb.cross (a, b);	// aCROSSb = a^b

			float denominator = 2.0f * (aCROSSb.dot (aCROSSb));

			//o = ((b²) * ((a ^ b) ^ a) + (a²) * (b ^ (a ^ b))) / Denominator;
			Vector3f aCROSSbCROSSa = new Vector3f(); aCROSSbCROSSa.cross (aCROSSb, a);
			Vector3f bCROSSaCROSSb = new Vector3f(); bCROSSaCROSSb.cross (b, aCROSSb);
			aCROSSbCROSSa.scale (b.lengthSquared ()); // b²*((a^b)^a)
			bCROSSaCROSSb.scale (a.lengthSquared ()); // a²*(b^(a^b))
			Vector3f o = new Vector3f(bCROSSaCROSSb); o.add (aCROSSbCROSSa); o.scale (1.0f/denominator);
			radius = o.length () + eps;
			center = new Point3f(O); center.add (o);

		}
		public Ball(Point3f O, Point3f A, Point3f B, Point3f C) {

			Vector3f a = new Vector3f(A); a.sub (O);		// a = A - O
			Vector3f b = new Vector3f(B); b.sub (O);		// b = B - O
			Vector3f c = new Vector3f(C); c.sub (O);		// c = C - O

			// Denominator = 2.0f * Matrix::det(a.x, a.y, a.z, b.x, b.y, b.z, c.x, c.y, c.z);
			Matrix3d sMatrix = new Matrix3d();
			sMatrix.m00 = a.x;
			sMatrix.m01 = a.y;
			sMatrix.m02 = a.z;
			sMatrix.m10 = b.x;
			sMatrix.m11 = b.y;
			sMatrix.m12 = b.z;
			sMatrix.m20 = c.x;
			sMatrix.m21 = c.y;
			sMatrix.m22 = c.z;

			float denominator = (float) (2.0 * sMatrix.determinant());

			Vector3f aCROSSb = new Vector3f(); aCROSSb.cross (a, b);
			Vector3f cCROSSa = new Vector3f(); cCROSSa.cross (c, a);
			Vector3f bCROSSc = new Vector3f(); bCROSSc.cross (b, c);

			aCROSSb.scale (c.lengthSquared ());
			cCROSSa.scale (b.lengthSquared ());
			bCROSSc.scale (a.lengthSquared ());

			//o = ((c²) * (a ^ b) +  (b²) * (c ^ a) + (a²) * (b ^ c)) / Denominator;
			Vector3f o = new Vector3f ();
			o.add (bCROSSc);
			o.add (cCROSSa);
			o.add (aCROSSb);
			o.scale (1.0f/denominator);

			radius = o.length () + eps;
			center = new Point3f(O); center.add (o); // center = O + o

		}

		public boolean isInside(Point3f p){
			Vector3f dist = new Vector3f(center);
			dist.sub (p);
			return dist.length () < radius;
		}

	}
	
	/*
	 * Solve the minimum enclosing sphere problem, using Welzl's algorithm.
	 * 
	 * "Smallest enclosing disks (balls and ellipsoids)",
	 * "New Results and New Trends in Computer Science",
	 *  (H. Maurer, Ed.),
	 *  Lecture Notes in Computer Science 555 (1991) 359-370.
	 *  
	 *  Solves the problem in o(n)
	 */	
	private Ball enclosingBall(Point3f[] points, int n, Point3f[] support, int b) {

		// Compute the smallest enclosing ball given by 1,2,3 or 4 points
		Ball ball = null;
		if (b == 4)
			ball = new Ball(support[0],support[1],support[2],support[3]);
		else if (b == 3)
			ball = new Ball(support[0],support[1],support[2]);
		else if (b == 2)
			ball = new Ball(support[0],support[1]);
		else if (b == 1)
			ball = new Ball(support[0]);
		else if (b == 0)
			ball = new Ball(points[0],0f);

		// Check if all points are enclosed
		for (int i = 0 ; i < n ; i++) {
			if (!ball.isInside (points[i])) {
				support[b] = new Point3f(points[i]);
				ball = enclosingBall(points, i, support, b+1);
			}
		}

		return ball;

	}
	
	public BoundingShapeComputing() {
	}
	
	/**
	 * Computes the bounding sphere of the given set of points
	 * @param points	set of points
	 * @return bounding sphere
	 */
	public Sphere boundingSphere(Point3f[] points) {

		Ball MB = new Ball();
		Point3f[] empty = new Point3f[4]; 
		MB = enclosingBall(points,points.length, empty, 0);

		for (int i = 0; i<points.length; i++) {
			Vector3f vb = new Vector3f(MB.center);
			vb.sub (points[i]);
		}

		return new Sphere(MB.center,MB.radius+eps);
	}
	
	/*
	 * Computes the convex hull of the given point set
	 */
	private Point3f[] computeConvexHull(Point3f[] points) {
		try {
			return HullMesh.getMesh (points).getPoints ();
		}
		catch (Exception e) {
			ArtLog.println ("Convex hull: Given points returned instead their Convex Hull ("+points.length+" points)");
			return points;
		}

	}
	
	/**
	 * Computes the comvex hull of the given set of points
	 * @param points	set of points
	 * @return convex hull (VolumicTriangulatedMesh)
	 */
	public VolumicTriangulatedMesh convexHull(Point3f[] points) {
		
		Mesh hull = HullMesh.getMesh (points);
		return new VolumicTriangulatedMesh (hull.getPoints (), hull.getPaths ());
	}
	
	/**
	 * Computes the bounding ellipsoid of the given set of points
	 * 1. Convex hull computing
	 * 2. Coordinates normalization
	 * 3. Singular value decomposition of the points covariance
	 * 4. Coordinates normalization in canonical space
	 * 5. Bounding sphere in canonical space (Welzl's algorithm)
	 * 6. Create an ellipsoid from the bounding sphere in canonical space and corresponding matrix
	 * 
	 * @param inputPoints	set of points
	 * @return bounding ellipsoid
	 */
	public Ellipsoid boundingEllipsoid(Point3f[] inputPoints) {

		if (inputPoints==null)
			return null;
		if (inputPoints.length==0)
			return null;
		
		// Keep only points on the convex hull
		Point3f[] points = computeConvexHull(inputPoints);
		
		// Points XYZ >> Matrix
		int n = points.length;
		if (n<4) {
			ArtLog.println ("Need more than 4 points to computes the bounding ellipsoid ("+inputPoints.length+" points given");
			return null;
		}
		
		RealMatrix pointsXYZ = MatrixUtils.createRealMatrix(3,n);
		for (int i = 0; i < n; i++)	{
			pointsXYZ.setEntry (0, i, points[i].x);
			pointsXYZ.setEntry (1, i, points[i].y);
			pointsXYZ.setEntry (2, i, points[i].z);
		}
		
		// Normalize coordinates (in XYZ) 
		double[] scale = new double[3];

		for (int i = 0; i < 3; i++)	{
			double[] row = pointsXYZ.getRow(i);
			double max = NumberUtils.max (row);
			double min = NumberUtils.min (row);
			double[] normRow = new double[n];
			scale[i] = max - min;
			if (scale[i]==0)
				scale[i] = 1;
			for (int k = 0 ; k < n ; k++)
				normRow[k] = row[k]/scale[i];
			pointsXYZ.setRow (i, normRow);
			
		}
		
		// Covariance
		Covariance cov = new Covariance(pointsXYZ.transpose ().getData ());
		RealMatrix covPoints = cov.getCovarianceMatrix ();
		
		// Singular value decomposition
		RealMatrix u = MatrixUtils.createRealMatrix(3,3);
		try {
			// Perform this with EJML library because Vecmath & Apache Common maths are so bad
			SimpleMatrix covPointsEJML = new SimpleMatrix(covPoints.getData ());
			SimpleMatrix uEJML = covPointsEJML.svd ().getU ();
			for (int i = 0 ; i < 3 ; i++)
				for (int j = 0 ; j < 3 ; j++)
					u.setEntry (i, j, uEJML.get (i, j));
		}
		catch (Exception e) {
			// If error, set u to identity matrix
			u.setEntry (0, 0, 1.0);
			u.setEntry (1, 1, 1.0);
			u.setEntry (2, 2, 1.0);
			
			ArtLog.println ("SVD error: "+e.getMessage ()+ " -> Use canonical XYZ base");
			if (debugInfo) {
				ArtLog.println("\n\tPoints:)");
				for (int i = 0; i < points.length; i++)
					ArtLog.println ("\t"+points[i]);
				ArtLog.println ("\n\tCovariance matrix:");
				for (int i = 0; i < covPoints.getRowDimension (); i++)
					ArtLog.println ("\t"+covPoints.getRowMatrix (i));
				ArtLog.println ("Exporting point cloud for debug..");
                                /*
				OctaveOutput o = new OctaveOutput ();
				o.octaveVariable (points, "p");

				if (debugExport)
					o.octaveExportScriptToFile (debugPath+"last_points_debug.m");
                                */
			}

		}

		// Coordinates in new base UVW
		RealMatrix pointsUVW = MatrixUtils.createRealMatrix(3,n);
		for (int i = 0; i < n; i++)	{
			RealMatrix pointUVW = MatrixUtils.createRealMatrix(3,1);
			RealMatrix pointXYZ = pointsXYZ.getColumnMatrix (i);
			pointUVW = u.transpose ().multiply (pointXYZ);
			pointsUVW.setColumnMatrix (i, pointUVW);
		}

		// Normalize coordinates (in UVW) 
		double[] scaleUVW = new double[3];

		for (int i = 0; i < 3; i++)	{
			double[] row = pointsUVW.getRow(i);
			double max = NumberUtils.max (row);
			double min = NumberUtils.min (row);
			double[] normRow = new double[n];
			scaleUVW[i] = max - min;
			if (scaleUVW[i]==0)
				scaleUVW[i] = 1;
			for (int k = 0 ; k < n ; k++)
				normRow[k] = row[k]/scaleUVW[i];
			pointsUVW.setRow (i, normRow);
			
		}

		// Compute bounding sphere in new base (Welzl's algorithm)
		Point3f[] pointsUVWnorm = new Point3f[n];
		for (int i = 0 ; i < n ; i++)
			pointsUVWnorm[i] = new Point3f((float) pointsUVW.getEntry (0, i),(float) pointsUVW.getEntry (1, i),(float) pointsUVW.getEntry (2, i));
		Sphere ball = boundingSphere (pointsUVWnorm);

		// Total transformation: P matrix
		double[][] a = {{1/scale[0],0,0},{0,1/scale[1],0},{0,0,1/scale[2]}};
		double[][] aUVW = {{1/scaleUVW[0],0,0},{0,1/scaleUVW[1],0},{0,0,1/scaleUVW[2]}};
		RealMatrix A = MatrixUtils.createRealMatrix(a);
		RealMatrix AUVW = MatrixUtils.createRealMatrix(aUVW);
		RealMatrix ut = u.transpose();
		RealMatrix P = AUVW.multiply (ut.multiply (A));
		
		// To vecmath matrix
		Matrix3f vecmathP = new Matrix3f();
		vecmathP.m00 = (float) P.getEntry (0, 0);
		vecmathP.m01 = (float) P.getEntry (0, 1);
		vecmathP.m02 = (float) P.getEntry (0, 2);
		vecmathP.m10 = (float) P.getEntry (1, 0);
		vecmathP.m11 = (float) P.getEntry (1, 1);
		vecmathP.m12 = (float) P.getEntry (1, 2);
		vecmathP.m20 = (float) P.getEntry (2, 0);
		vecmathP.m21 = (float) P.getEntry (2, 1);
		vecmathP.m22 = (float) P.getEntry (2, 2);
		Ellipsoid e = new Ellipsoid(ball,vecmathP);
		return e;

	}

	/*
	 * Get the points of the given shape
	 */
	private Point3f[] getPoints(Shape shape) {
		
		return ((TriangulatedMesh) shape).getPoints ();

	}
	
	/**
	 * Computes the bounding ellipsoid of the given shape
	 * 
	 * @param shape	shape
	 * @return bounding ellipsoid of the given shape
	 */
	public Ellipsoid boundingEllipsoid(Shape shape) {

		if (shape instanceof TriangulatedMesh) {
			Point3f[] points = getPoints(shape);
			Ellipsoid e = boundingEllipsoid(points);
			if (check(points,e))
				return e;
		}
		if (shape instanceof Sphere) {
			Matrix3f a = new Matrix3f();
			a.setIdentity();
			return new Ellipsoid((Sphere) shape, a);
		}
		if (shape instanceof Ellipsoid)
			return (Ellipsoid) shape;
		ArtLog.println ("Unable to compute the bounding ellipsoid (of "+shape.getClass()+")");
		return null;
	}
	
	/**
	 * Computes the bounding sphere of the given shape
	 * 
	 * @param shape	shape
	 * @return bounding sphere of the given shape
	 */
	public Sphere boundingSphere(Shape shape) {
		
		if (shape instanceof TriangulatedMesh) {
			Point3f[] points = getPoints(shape);
			Sphere s = boundingSphere(points);
			if (check(points,s))
				return s;
		}
		if (shape instanceof Ellipsoid) {
			Vector3f[] ax = ((Ellipsoid) shape).getAxis();
			float biggerAxe = 0;
			for (int i = 0 ; i < 3 ; i++)
				if (biggerAxe < ax[i].length())
					biggerAxe = ax[i].length();
			Point3f p = ((Ellipsoid) shape).getCenter();
			return new Sphere(p, biggerAxe+eps);
		}
		if (shape instanceof Sphere)
			return (Sphere) shape;
		ArtLog.println ("Unable to compute the bounding sphere (of "+shape.getClass()+")");
		return null;
		
	}
	
	/**
	 * Check if points are inside computed bounding shape
	 * 
	 * @param points	points
	 * @param shape		bounding shape of points
	 */
	public boolean check(Point3f[] points, VolumicShape shape) {

		if (shape==null)
			return false;

		int total = 0;
		for (int i = 0 ; i < points.length ; i++)
			if (!shape.contains (points[i]))
				total++;
		if (total==0)
			return true;
		ArtLog.println ("Enclosing point error (" + total + " points outside the computed bounding shape). Maybe try to increase eps value!");
		return false;

	}
	
	/**
	 * Returns the optimal boundingShape (sphere or ellipsoid)
	 * @param shape	shape to bound
	 * @return bounding shape
	 */
	public VolumicShape boundingShape(Shape shape) {
		
		// If the shape is already a sphere or an ellipsoid, return null
		if (shape instanceof Sphere)
			return null;
		if (shape instanceof Ellipsoid)
			return null;
		
		// This should use ellipsoid when it is profitable
		Sphere		s = boundingSphere (shape);
		Ellipsoid	e = boundingEllipsoid (shape);
		
		// If one of the 2 volumic shapes is null, return the other
		if (e==null)
			if (s!=null)
				return s;
			else
				return null;
		else
			if (s==null)
				return e;
		
		// Sphere volume
		float rs = s.getRadius ();
		float vs = rs*rs*rs;
		
		// Ellipsoid volume
		Vector3f[] ax = e.getAxis ();
		float ve = (ax[0].length ()*ax[1].length ()*ax[2].length ());
		
		/*
		 * Empiric formula to chose Ellipsoid or Sphere.
		 * Future work coul be:
		 * Optimisation based on probability to intersect the bounding shape,
		 * assuming the computation cost of the shape to be enclosed. (e.g. for
		 * triangulated meshes, it is proportional to the number of facets)
		 */
		if (ve<0.125f*vs)
			return e;
		else return s;
		
	}
}
