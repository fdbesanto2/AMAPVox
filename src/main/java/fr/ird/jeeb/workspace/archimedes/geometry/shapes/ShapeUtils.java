package fr.ird.jeeb.workspace.archimedes.geometry.shapes;

import javax.vecmath.Matrix3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.lib.structure.geometry.util.BoundingBox3f;
import fr.ird.jeeb.workspace.archimedes.geometry.LineSegment;

/**
 * Common operations on shapes
 * @author Cresson, Nov. 2012
 *
 */
public class ShapeUtils {

	/**
	 * Computes the boundingbox of the given shape
	 * @param shp	shape
	 * @return boundingbox of the shape
	 */
	public static BoundingBox3f computeBoundingBox(Shape shp) {
		BoundingBox3f bbox = null;
		if (shp instanceof Sphere) {
			bbox = new BoundingBox3f();
			Point3f p0 = new Point3f(((Sphere) shp).getCenter ());
			float r = ((Sphere) shp).getRadius ();
			p0.add (new Point3f(+1*r,+1*r,+1*r)); bbox.update (p0);
			p0.add (new Point3f(-2*r,-2*r,-2*r)); bbox.update (p0);
		}
		if (shp instanceof Ellipsoid) {
			bbox = new BoundingBox3f();
			Point3f p0 = new Point3f(((Ellipsoid) shp).getCenter ());
			Vector3f[] axis = ((Ellipsoid) shp).getAxis ();
			Vector3f v = new Vector3f(0,0,0);
			v.add (axis[0]);
			v.add (axis[1]);
			v.add (axis[2]);
			v.scale (+1f); p0.add (v); bbox.update (p0);
			v.scale (-2f); p0.add (v); bbox.update (p0);
		}
		if (shp instanceof TriangulatedMesh) {
			bbox = new BoundingBox3f();
			Point3f[] pts = ((TriangulatedMesh) shp).getPoints ();
			for (Point3f p : pts)
				bbox.update (p);
		}

		return bbox;
	}
	
	/**
	 * Test if there is an intersection between the hull of a given TriangulatedMesh and a Sphere
	 * @param triangulatedMesh	triangulated mesh
	 * @param sphere			sphere
	 * @return true if there is an intersection, false if not
	 */
	public static boolean collision(TriangulatedMesh triangulatedMesh, Sphere sphere) {
		
		// Get points & paths from the mesh
		int[][]		paths	= triangulatedMesh.getPaths ();
		Point3f[]	points	= triangulatedMesh.getPoints ();
		
		for (int i = 0 ; i < paths.length ; i++){
			// Compute triangle points, vertex, normal.
			Point3f point0 = points[paths[i][0]];
			Point3f point1 = points[paths[i][1]];
			Point3f point2 = points[paths[i][2]];
			Vector3f edge0 = new Vector3f(point1.x - point0.x,point1.y - point0.y,point1.z - point0.z);
			Vector3f edge1 = new Vector3f(point2.x - point1.x,point2.y - point1.y,point2.z - point1.z);
			Vector3f edge2 = new Vector3f(point0.x - point2.x,point0.y - point2.y,point0.z - point2.z);
			Vector3f normal  = new Vector3f();
			normal.cross (edge0, edge1);
			normal.normalize ();
			
			// Sphere params
			Point3f C = new Point3f(sphere.getCenter ());
			float r = sphere.getRadius ();
			
			// 1. Check distance between sphere center and triangle plane
			Vector3f SC = new Vector3f(C); SC.sub (point0);
			float dist = SC.dot (normal);
			if (dist>r | dist<-r)
				continue;
			
			// 2. Check if sphere center is inside the 3 planes generated from triangle vertex and normal
			Vector3f v0 = SC;
			Vector3f v1 = new Vector3f(C); v1.sub (point1);
			Vector3f v2 = new Vector3f(C); v2.sub (point2);
			Vector3f n0 = new Vector3f(); n0.cross (normal,edge0);
			Vector3f n1 = new Vector3f(); n1.cross (normal,edge1);
			Vector3f n2 = new Vector3f(); n2.cross (normal,edge2);
			if (n0.dot (v0)>=0 & n1.dot (v1)>=0 & n2.dot (v2)>=0)
				return true;
			
			// 3. Check if sphere contains one of the triangle point
			if (sphere.contains (point0))
				return true;
			if (sphere.contains (point1))
				return true;
			if (sphere.contains (point2))
				return true;
			
			// 4. Check if each edge intersects the sphere
			float da = v0.dot (edge0)/edge0.lengthSquared ();
			if (0<=da & da<=1) {
				Vector3f vd = new Vector3f(); vd.cross (v0, edge0);
				if (vd.length ()/edge0.length ()<=r)
					return true;
			}
			da = v1.dot (edge1)/edge1.lengthSquared ();
			if (0<=da & da<=1) {
				Vector3f vd = new Vector3f(); vd.cross (v1, edge1);
				if (vd.length ()/edge1.length ()<=r)
					return true;
			}
			da = v2.dot (edge2)/edge2.lengthSquared ();
			if (0<=da & da<=1) {
				Vector3f vd = new Vector3f(); vd.cross (v2, edge2);
				if (vd.length ()/edge2.length ()<=r)
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Test if there is a collision between a convexMesh and a sphere
	 * @param convexMesh	convex triangulated mesh
	 * @param sphere		sphere
	 * @return true if there is an intersection, false if not (including total volumic inclusion)
	 */
	public static boolean collision(ConvexMesh convexMesh, Sphere sphere) {
				
		// Test if center of the sphere is inside the box
		if (convexMesh.contains (sphere.getCenter ()))
			return true;
		
		// Test if sphere intersects the volumic rectangle
		return collision((TriangulatedMesh) convexMesh,sphere);
	}
	
	/**
	 * Test if there is a collision between a convexMesh and an ellipsoid
	 * @param convexMesh	convex triangulated mesh
	 * @param ellipsoid		ellipsoid
	 * @return true if there is an intersection, false if not (including total volumic inclusion)
	 */
	public static boolean collision(ConvexMesh convexMesh, Ellipsoid ellipsoid) {
		
		// Get sphere in ellipsoid canonical space
		Sphere s = ellipsoid.getS ();
		
		// Get canonical space transform matrix
		Matrix3f P = ellipsoid.getP ();
		
		// Pass the convexMesh into the canonical space
		Point3f[]	points	= convexMesh.getPoints ();
		int[][]		paths	= convexMesh.getPaths ();
		Point3f[] pointsInCanonical = new Point3f[points.length];
		for (int i = 0 ; i < points.length ; i++){
			Point3f newPoint = new Point3f(points[i]);
			P.transform (newPoint);
			pointsInCanonical[i] = newPoint;
		}
		ConvexMesh convexMeshInCanonical = new ConvexMesh(pointsInCanonical,paths);
		
		// Test collision in canonical space
		return collision(convexMeshInCanonical,s);
	}
	
	/**
	 * Test if there is a collision between a Plane and a TriangulatedMesh
	 * @param plane				plane
	 * @param triangulatedMesh	triangulated mesh
	 * @return true if there is an intersection, false if not
	 */
	public static boolean collision(Plane plane, TriangulatedMesh triangulatedMesh) {
		// Create a non-oriented plane
		Plane nonOrientedPlane = new Plane(plane.getPoint(),plane.getNormal(),false);
		
		// Get edges of the mesh
		int[][]		paths	= triangulatedMesh.getPaths();
		Point3f[]	points	= triangulatedMesh.getPoints();
		
		// Detect edges-plane intersection
		for (int i = 0 ; i < paths.length ; i++) {
			for (int j = 0 ; j < paths[i].length ; j++) {
				Point3f A = new Point3f(points[paths[i][j]]);
				Point3f B = null;
				if (j==0)
					B = new Point3f(points[paths[i][paths[i].length-1]]);
				else
					B = new Point3f(points[paths[i][j-1]]);
				LineSegment lineSegment = new LineSegment(A,B);
				if (nonOrientedPlane.isIntersectedBy(lineSegment))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * Create a rectangle box, aligned in XYZ axis (like a boundingbox)
	 * @param boundingBox	bounding box
	 * @param margin		margin
	 * @return rectangular volumic shape
	 */
	public static ConvexMesh createRectangleBox(BoundingBox3f boundingBox, float margin) {
		
		return ShapeUtils.createRectangleBox(boundingBox, margin, false);
		
	}
		
	/**
	 * Create a rectangle box, aligned in XYZ axis (like a boundingbox)
	 * @param boundingBox	bounding box
	 * @param margin		margin
	 * @param culling		culling
	 * @return rectangular volumic shape
	 */
	public static ConvexMesh createRectangleBox(BoundingBox3f boundingBox, float margin, boolean culling) {
		
		// Walls
		float[]x = {boundingBox.getMax().x-margin, boundingBox.getMin().x+margin};
		float[]y = {boundingBox.getMax().y-margin, boundingBox.getMin().y+margin};
		float[]z = {boundingBox.getMax().z-margin, boundingBox.getMin().z+margin};

		// Get 8 corners of the bbox
		Point3f[] bboxPoint = new Point3f[8];
		for (int i = 0; i < 8 ; i++)
			bboxPoint[i] = new Point3f(x[i%2],y[((i-i%2)/2)%2],z[((i-i%4)/4)%2]);

		// Create paths
		int[][] paths = new int[12][];
		paths[0] = new int[] {1, 7, 3};
		paths[1] = new int[] {1, 5, 7};
		paths[2] = new int[] {0, 5, 1};
		paths[3] = new int[] {0, 4, 5};
		paths[4] = new int[] {2, 4, 0};
		paths[5] = new int[] {2, 6, 4};
		paths[6] = new int[] {3, 6, 2};
		paths[7] = new int[] {3, 7, 6};
		paths[8] = new int[] {0, 3, 2};
		paths[9] = new int[] {0, 1, 3};
		paths[10]= new int[] {5, 6, 7};
		paths[11]= new int[] {5, 4, 6};

		// Compute Box
		return new ConvexMesh(bboxPoint, paths, culling);
	}
	
	
	/**
	 * Add margins to the given bounding box
	 * @param bbox		bounding box
	 * @param margin	margin
	 * @return the new bounding box, updated with the given margin
	 */
	public static BoundingBox3f getPaddedBoundingBox(BoundingBox3f bbox, float margin) {
		
		Point3f ptSup 		= bbox.getMax();
		Point3f ptInf 		= bbox.getMin();
		Point3f epsVector 	= new Point3f(	margin,
											margin,
											margin);
		ptSup.add (epsVector);
		ptInf.sub (epsVector);
		BoundingBox3f newBbox = new BoundingBox3f ();
		newBbox.setMin (ptInf);
		newBbox.setMax (ptSup);
		return newBbox;
	}
	
	/**
	 * Returns true if the shape is infinite
	 * @param shape
	 * @return true if the shape is infinite
	 */
	public static boolean isInfiniteShape(Shape shape) {
		if (shape instanceof Plane)
			return true;
//		if (shape instanceof infiniteNurbs)
//			return true;
		return false;
		
	}
	
	/**
	 * Create a bounding box from the specified XY coordinates (Z are set to {0,1})
	 */
	public static BoundingBox3f createXYBoundingBox(float x1,float y1,float x2,float y2) {
		BoundingBox3f box = new BoundingBox3f();
		box.update(new Point3f(x1,y1,0));
		box.update(new Point3f(x2,y2,1));
		return box;
	}
	
	/**
	 * Returns if the given point is inside the specified cone
	 * @param P		point to test
	 * @param dir	axis of the cone (oriented)
	 * @param C		center of the base of the cone
	 * @param tanC	tangent of the cone angle
	 * @param r0	cone base radius
	 * @return true if P is inside the cone
	 */
	public static boolean isInsideCone(Point3f P, Vector3f dir, Point3f C, float tanC, float r0) {
		
		// Point H: point P projected on the axis
		Vector3f CP = new Vector3f(P); CP.sub(C);
		float k = dir.dot(CP);
		if (k<0)
			// If the point is behind the direction, return false;
			return false;
		Point3f H = new Point3f(dir); H.scale(k);
		
		// Distance H-axis
		CP.cross(CP, dir);
		return (CP.length()<k*tanC+r0);

	}
	
	/**
	 * Returns if the given point is inside the specified cylinder
	 * @param P		point to test
	 * @param dir	axis of the cylinder
	 * @param C		center of the base of the cylinder
	 * @param r		cylinder base radius
	 * @return true if P is inside the cylinder
	 */
	public static boolean isInsideCylinder(Point3f P, Vector3f dir, Point3f C, float r) {
		
		return isInsideCone(P, dir, C, 0, r);

	}

	/**
	 * Computes the area of a shape
	 * @param shape	shape
	 * @return the area of the shape
	 */
	public static float computeShapeArea(Shape shape) {
		float area = Float.NaN;
		if (shape instanceof TriangulatedMesh){
			area = 0f;
			int[][] paths = ((TriangulatedMesh) shape).getPaths();
			Point3f[] points = ((TriangulatedMesh) shape).getPoints();
			for (int i = 0 ; i<paths.length ; i++) {
				Point3f vertex0 = (points[paths[i][0]]);
				Point3f vertex1 = (points[paths[i][1]]);
				Point3f vertex2 = (points[paths[i][2]]);
				Vector3f edge1 = new Vector3f(vertex1); edge1.sub(vertex2);
				Vector3f edge2 = new Vector3f(vertex0); edge1.sub(vertex2);
				Vector3f prod = new Vector3f();
				prod.cross(edge1, edge2);
				area += prod.length()*0.5f;
			}
		}
		return area;
			
	}
	
}
