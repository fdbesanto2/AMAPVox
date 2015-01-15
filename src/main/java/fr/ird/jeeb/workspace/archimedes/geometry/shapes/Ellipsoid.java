package fr.ird.jeeb.workspace.archimedes.geometry.shapes;

import java.util.ArrayList;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.geometry.HalfLine;
import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;
import fr.ird.jeeb.workspace.archimedes.geometry.Line;
import fr.ird.jeeb.workspace.archimedes.geometry.LineElement;
import fr.ird.jeeb.workspace.archimedes.geometry.LineSegment;
import fr.ird.jeeb.workspace.archimedes.geometry.Transformations;

import org.apache.commons.math.linear.MatrixUtils;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.linear.EigenDecomposition;
import org.apache.commons.math.linear.EigenDecompositionImpl;
import org.apache.commons.math.util.MathUtils;

/**
 * Ellipsoid class
 * @author Cresson, Sept.2012
 *
 */
public class Ellipsoid extends Shape implements VolumicShape{

	private final static float[] 	DEFAULT_AXES	= {1,0,0,0,1,0,0,0,1};
	private final static Point3f 	DEFAULT_CENTER	= new Point3f(0,0,0);
	
	private Sphere		S;	// Sphere in canonical space
	private Matrix3f	P;	// Canonical matrix
	
	public Ellipsoid(Sphere s, Matrix3f a) {
		
		S = s;
		P = a;
		
	}
	
	public Ellipsoid(float[] semiLengths) {
		
		// default center & axes
		this(semiLengths,DEFAULT_CENTER);
		
	}
	
	public Ellipsoid(float[] semiLengths, Point3f O) {
		
		// default axes
		this(semiLengths, O, DEFAULT_AXES);
		
	}
	
	public Ellipsoid(float[] semiLengths, Point3f center, float[] axes) {
		
		// user specified axes, semi-lengths & centre
		float[] lengths 	= {1/semiLengths[0],0,0,0,1/semiLengths[1],0,0,0,1/semiLengths[2]};

		// Canonical matrix
		Matrix3f M = new Matrix3f(axes);
		P = new Matrix3f(lengths);
		P.mul (M);
		
		// Sphere in canonical space
		Point3f O = new Point3f(center);
		P.transform (O);
		S = new Sphere(O,1.0f);
		
	}
	
	/**
	 * Constructor with axis, axis length, and center.
	 */
	public Ellipsoid(float[] semiLengths, Point3f O, Vector3f[] axes) {

		this(semiLengths, O,new float[] {axes[0].x,axes[0].y,axes[0].z,axes[1].x,axes[1].y,axes[1].z,axes[2].x,axes[2].y,axes[2].z});
		
	}
	
	@Override
	public Object clone() {
	    Ellipsoid e = null;
	    try {
	    	e = (Ellipsoid) super.clone();
	    } catch(CloneNotSupportedException cnse) {
	      	System.err.println (System.err+"/"+cnse.getMessage ());
	    }
	    e.S = (Sphere) S.clone();
	    e.P = (Matrix3f)P.clone ();
	    return e;
	}
	
	@Override
	public void transform (Transformations transform) {
		
		// Rotation & Scaling matrix in XYZ (a)
		Matrix3f a = new Matrix3f();
		transform.getMatrix ().getRotationScale (a);
		
		// Translation matrix in XYZ (b)
		Vector3f b = new Vector3f(new float[] {
				transform.getMatrix ().m03,
				transform.getMatrix ().m13,
				transform.getMatrix ().m23});
		
		// Transformations in UVW (as, bs) 
		Matrix3f Pa = new Matrix3f(P);
		Matrix3f iP = new Matrix3f(P);
		iP.invert ();
		
		Pa.mul (a);
		Matrix3f as = new Matrix3f(Pa);
		as.mul (iP);
		Vector3f bs = new Vector3f(b);
		Pa.transform (bs);
		
		// Computing transformation in UVW
		Matrix4f m = new Matrix4f();
		m.setRotationScale (as);
		m.setElement(3, 3, 1.0f);
		m.setElement (0,3,bs.x);
		m.setElement (1,3,bs.y);
		m.setElement (2,3,bs.z);
		Transformations ts = new Transformations ();
		ts.setMatrix (m);
		
		// Apply transformation  to the center of the sphere, in UVW space
		ts.apply (S.center);

		// Change sphere radius
		Matrix4f transformMatrix = transform.getMatrix ();
		float[] norm = new float[3];
		float tol = 0.001f;
		norm[0] = (float)Math.sqrt (transformMatrix.m00*transformMatrix.m00 + transformMatrix.m10*transformMatrix.m10 + transformMatrix.m20*transformMatrix.m20);
		norm[1] = (float)Math.sqrt (transformMatrix.m01*transformMatrix.m01 + transformMatrix.m11*transformMatrix.m11 + transformMatrix.m21*transformMatrix.m21);
		norm[2] = (float)Math.sqrt (transformMatrix.m02*transformMatrix.m02 + transformMatrix.m12*transformMatrix.m12 + transformMatrix.m22*transformMatrix.m22);
		float mNorm = 0f;
		for (float n: norm)
			mNorm += n;
		mNorm /= 3;
		float err = 0f;
		for (float n: norm)
			err += (n-mNorm);
		if (err>tol)
			System.err.println ("Non isotropic scaling !!!");
		S.scale (mNorm*mNorm);
		
		// Update transformation matrix
		P.mul (a);
		
	}

	@Override
	public boolean isIntersectedBy (LineElement linel) {
		
		// Direction in canonical space
		Vector3f direction = new Vector3f(linel.getDirection ());
		P.transform (direction);
		
		// Starting point in canonical space
		Point3f startingPoint = new Point3f(linel.getOrigin ());
		P.transform (startingPoint);
		
		// New ray direction & length in canonical space
		float strech = direction.length ();
		direction.normalize ();

		// Call sphere intersection in canonical space
		if (linel instanceof LineSegment)
			return S.isIntersectedBy (new LineSegment(startingPoint,direction,linel.getLength ()*strech));
		if (linel instanceof HalfLine)
			return S.isIntersectedBy (new HalfLine(startingPoint,direction));
		if (linel instanceof Line)
			return S.isIntersectedBy (new Line(startingPoint,direction));
		return false;
	}

	@Override
	public ArrayList<Intersection> getIntersections (LineElement linel) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Intersection getNearestIntersection (LineElement linel) {
		// Direction in canonical space
		Vector3f direction = new Vector3f(linel.getDirection ());
		P.transform (direction);

		// Starting point in canonical space
		Point3f startingPoint = new Point3f(linel.getOrigin ());
		P.transform (startingPoint);

		// New ray direction & length in canonical space
		float strech = direction.length ();
		direction.normalize ();

		// Call sphere intersection in canonical space
		if (linel instanceof LineSegment) {
			Intersection inter = S.getNearestIntersection (new LineSegment(startingPoint,direction,linel.getLength ()*strech));
			if (inter!=null)
				return new Intersection (inter.distance/strech,inter.getNormal ());
		}
		if (linel instanceof HalfLine){
			Intersection inter = S.getNearestIntersection (new HalfLine(startingPoint,direction));
			if (inter!=null)
				return new Intersection (inter.distance/strech,inter.getNormal ());
		}
		if (linel instanceof Line){
			Intersection inter = S.getNearestIntersection (new Line(startingPoint,direction));
			if (inter!=null)
				return new Intersection (inter.distance/strech,inter.getNormal ());
		}

		return null;
	}

	public Point3f getCenter() {
		Point3f centerUVW = new Point3f(S.center);
		Matrix3f iP = new Matrix3f(P);
		iP.invert ();
		iP.transform (centerUVW);
		return centerUVW;
	}

	public Vector3f[] getAxis() {
		
		// Construct quadratic form matrix (M)
		double[][] p = {
				{this.P.m00,this.P.m01,this.P.m02},
				{this.P.m10,this.P.m11,this.P.m12},
				{this.P.m20,this.P.m21,this.P.m22}};
		RealMatrix P = MatrixUtils.createRealMatrix(p);
		RealMatrix M = P.transpose ().multiply (P);
		
		// Eigendecomposition of M
		EigenDecomposition eig = new EigenDecompositionImpl(M, MathUtils.SAFE_MIN);
		float R = S.getRadius ();
		Vector3f[] v = new Vector3f[3];
		for (int i = 0 ; i < 3 ; i++) {
			double[] eigenVector = eig.getEigenvector (i).getData (); 
			v[i]=new Vector3f(
					(float)eigenVector[0],
					(float)eigenVector[1],
					(float)eigenVector[2]);
			v[i].scale (R/(float) ( Math.sqrt(eig.getRealEigenvalue (i)))); 
		}

		return v;
	}
	
	@Override
	public boolean contains (Point3f inputPoint) {
		Point3f point = new Point3f(inputPoint);
		P.transform (point);
		return S.contains (point);
	}
	
	public Matrix3f getP() {
		return P;
	}
	
	public Sphere getS() {
		return S;
	}
}
