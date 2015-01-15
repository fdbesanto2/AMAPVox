package fr.ird.jeeb.lib.math;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import fr.ird.jeeb.lib.util.Log;

/**
 * Math functions<BR>
 * 
 * @author Sebastien GRIFFON
 * 
 */
public final class InlineMath {
	private InlineMath() {
	}

	public final static float PI = (float) Math.PI;

	public final static float toRadians(float degrees) {
		return degrees * (PI / 180.0f);
	}

	public final static float toDegrees(float radians) {
		return radians * (180.0f / PI);
	}

	public final static int abs(int a) {
		return (a < 0) ? -a : a;
	}

	public final static float abs(float a) {
		return (a < 0) ? -a : a;
	}

	public final static double abs(double a) {
		return (a < 0) ? -a : a;
	}

	public final static int min(int a, int b) {
		return (a <= b) ? a : b;
	}

	public final static float min(float a, float b) {
		return (a <= b) ? a : b;
	}

	public final static double min(double a, double b) {
		return (a <= b) ? a : b;
	}
	
	public final static int min(int[] t) {
	    int minimum = t[0];   // start with the first value
	    for (int i=1; i<t.length; i++) {
	        if (t[i] < minimum) {
	        	minimum = t[i];   // new minimum
	        }
	    }
	    return minimum;
	}//end method max

	public final static float min(float[] t) {
		float minimum = t[0];   // start with the first value
	    for (int i=1; i<t.length; i++) {
	        if (t[i] < minimum) {
	        	minimum = t[i];   // new minimum
	        }
	    }
	    return minimum;
	}//end method max

	public final static double min(double[] t) {
		double minimum = t[0];   // start with the first value
	    for (int i=1; i<t.length; i++) {
	        if (t[i] < minimum) {
	        	minimum = t[i];   // new minimum
	        }
	    }
	    return minimum;
	}//end method max

	public final static int max(int a, int b) {
		return (a >= b) ? a : b;
	}

	public final static float max(float a, float b) {
		return (a >= b) ? a : b;
	}

	public final static double max(double a, double b) {
		return (a >= b) ? a : b;
	}
	
	public final static int max(int[] t) {
	    int maximum = t[0];   // start with the first value
	    for (int i=1; i<t.length; i++) {
	        if (t[i] > maximum) {
	            maximum = t[i];   // new maximum
	        }
	    }
	    return maximum;
	}//end method max

	public final static float max(float[] t) {
		float maximum = t[0];   // start with the first value
	    for (int i=1; i<t.length; i++) {
	        if (t[i] > maximum) {
	            maximum = t[i];   // new maximum
	        }
	    }
	    return maximum;
	}//end method max

	public final static double max(double[] t) {
		double maximum = t[0];   // start with the first value
	    for (int i=1; i<t.length; i++) {
	        if (t[i] > maximum) {
	            maximum = t[i];   // new maximum
	        }
	    }
	    return maximum;
	}//end method max


	public final static int signum(int a) {
		return (a < 0) ? -1 : 1;
	}

	public final static float signum(float a) {
		return (a < 0) ? -1 : 1;
	}

	public final static double signum(double a) {
		return (a < 0) ? -1 : 1;
	}

	public final static float sqrt(float a) {
		return (float) Math.sqrt(a);
	}

	public final static double sqrt(double a) {
		return (double) Math.sqrt(a);
	}

	public final static boolean floatEquals(float a, float b) {
		return abs(a - b) < 0.0001f;
	}

	public final static boolean doubleEquals(double a, double b) {
		return abs(a - b) < 0.0001d;
	}

	public final static float interpolate(float cur, float next, float t) {
		// if(t == 0) return cur; //In most case, different to 0 and 1, so the
		// if will be a lose of time
		// if(t == 1) return next;
		return cur + (next - cur) * t;
	}

	public final static double cos(double a) {
		return (double) Math.cos(a);
	}

	public final static double acos(double a) {
		return (double) Math.acos(a);
	}

	public final static double sin(double a) {
		return (double) Math.sin(a);
	}

	public final static double asin(double a) {
		return (double) Math.asin(a);
	}

	public final static double tan(double a) {
		return (double) Math.tan(a);
	}

	public final static double atan(double a) {
		return (double) Math.atan(a);
	}

	public final static double atan2(double a, double b) {
		return (double) Math.atan2(a, b);
	}

	public final static float cos(float a) {
		return (float) Math.cos(a);
	}

	public final static float acos(float a) {
		return (float) Math.acos(a);
	}

	public final static float sin(float a) {
		return (float) Math.sin(a);
	}

	public final static float asin(float a) {
		return (float) Math.asin(a);
	}

	public final static float tan(float a) {
		return (float) Math.tan(a);
	}

	public final static float atan(float a) {
		return (float) Math.atan(a);
	}

	public final static float atan2(float a, float b) {
		return (float) Math.atan2(a, b);
	}
	
	public final static double sum (double [] a) {
		double sum = 0;
		for (int i = 0; i < a.length; i++)
			sum += a[i];
		
		return sum;
	}

	public final static boolean isFloatValid(float f) {
		boolean isValid = !(new Float(f).equals(Float.NaN));
		// boolean isValid = (f != f);
		if (isValid) {
			Log.println(Log.WARNING, "InlineMath.isFloatValid :",
					"Not a valid float.");
		}
		return isValid;
	}

	public final static Vector3f interpolateNoCheck(Vector3f current,
			Vector3f next, float t) {
		Vector3f interpolation = new Vector3f();
		interpolation.x=(current.x + (next.x - current.x) * t);
		interpolation.y=(current.y + (next.y - current.y) * t);
		interpolation.z=(current.z + (next.z - current.z) * t);
		return interpolation;
	}

	/**
	 * Distance between two vectors
	 */
	public final static float distance(Vector3f vector1, Vector3f vector2) {
		float dx = vector1.x - vector2.x;
		float dy = vector1.y - vector2.y;
		float dz = vector1.z - vector2.z;

		float f = dx * dx + dy * dy + dz * dz;
		if (f == 0 || f == 1) {
			return f;
		} else {
			return sqrt(f);
		}
	}

	public final static void setNonUniformScale(Matrix4f matrix, Vector3f scale) {
		float[] scales = new float[3];
		
	    
		float[] mat = getArray(matrix);
		
		
		scales[0] = (float) Math.sqrt(mat[0]*mat[0] + mat[4]*mat[4] + mat[8]*mat[8]);
		scales[1] = (float) Math.sqrt(mat[1]*mat[1] + mat[5]*mat[5] + mat[9]*mat[9]);
		scales[2] = (float) Math.sqrt(mat[2]*mat[2] + mat[6]*mat[6] + mat[10]*mat[10]);

		if(scales[0] <= 0 || scales[1] <= 0|| scales[2]<= 0)
			return;
		
		float[] rot = new float [9];
		float s = 1/scales[0];
		rot[0] = mat[0]*s;
		rot[3] = mat[4]*s;
		rot[6] = mat[8]*s;
		s = 1/scales[1];
		rot[1] = mat[1]*s;
		rot[4] = mat[5]*s;
		rot[7] = mat[9]*s;
		s = 1/scales[2];
		rot[2] = mat[2]*s;
		rot[5] = mat[6]*s;
		rot[8] = mat[10]*s;

		mat[0] = rot[0]*scale.x;
		mat[1] = rot[1]*scale.y;
		mat[2] = rot[2]*scale.z;
		mat[4] = rot[3]*scale.x;
		mat[5] = rot[4]*scale.y;
		mat[6] = rot[5]*scale.z;
		mat[8] = rot[6]*scale.x;
		mat[9] = rot[7]*scale.y;
		mat[10] = rot[8]*scale.z;

		
		
		matrix.set(mat.clone());
	}
	
	public final static void normalize(Matrix4f matrix) {
		float[] scales = new float[3];
		
		    
		float[] mat = getArray(matrix);
		scales[0] = (float) Math.sqrt(mat[0]*mat[0] + mat[4]*mat[4] + mat[8]*mat[8]);
		scales[1] = (float) Math.sqrt(mat[1]*mat[1] + mat[5]*mat[5] + mat[9]*mat[9]);
		scales[2] = (float) Math.sqrt(mat[2]*mat[2] + mat[6]*mat[6] + mat[10]*mat[10]);

		float[] rot = new float [9];
		float s = 1/scales[0];
		rot[0] = mat[0]*s;
		rot[3] = mat[4]*s;
		rot[6] = mat[8]*s;
		s = 1/scales[1];
		rot[1] = mat[1]*s;
		rot[4] = mat[5]*s;
		rot[7] = mat[9]*s;
		s = 1/scales[2];
		rot[2] = mat[2]*s;
		rot[5] = mat[6]*s;
		rot[8] = mat[10]*s;

		matrix.set(mat.clone());
	}

	public final static double[] getArray(Matrix4d m1) {
		double[] mat = new double[16];
		mat[0] = m1.m00;
		mat[1] = m1.m01;
		mat[2] = m1.m02;
		mat[3] = m1.m03;
		mat[4] = m1.m10;
		mat[5] = m1.m11;
		mat[6] = m1.m12;
		mat[7] = m1.m13;
		mat[8] = m1.m20;
		mat[9] = m1.m21;
		mat[10] = m1.m22;
		mat[11] = m1.m23;
		mat[12] = m1.m30;
		mat[13] = m1.m31;
		mat[14] = m1.m32;
		mat[15] = m1.m33;

		return mat;
	}

	/**
	 * @return
	 */
	public final static float[] getArray(Matrix4f m1) {

		float[] mat = new float[16];
		mat[0] = m1.m00;
		mat[1] = m1.m01;
		mat[2] = m1.m02;
		mat[3] = m1.m03;
		mat[4] = m1.m10;
		mat[5] = m1.m11;
		mat[6] = m1.m12;
		mat[7] = m1.m13;
		mat[8] = m1.m20;
		mat[9] = m1.m21;
		mat[10] = m1.m22;
		mat[11] = m1.m23;
		mat[12] = m1.m30;
		mat[13] = m1.m31;
		mat[14] = m1.m32;
		mat[15] = m1.m33;

		return mat;
	}
	
	/**
	 * @return
	 */
	public final static float[] getArray(Vector4f v1) {

		float[] vec = new float[4];
		vec[0] = v1.x;
		vec[1] = v1.y;
		vec[2] = v1.z;
		vec[3] = v1.w;
		

		return vec;
	}
	
	/**
     * Sets the rotational component (upper 3x3) of this transform to the
     * rotation matrix converted from the Euler angles provided; the other
     * non-rotational elements are set as if this were an identity matrix.
     * The euler parameter is a Vector3d consisting of three rotation angles
     * applied first about the X, then Y then Z axis.
     * These rotations are applied using a static frame of reference. In
     * other words, the orientation of the Y rotation axis is not affected
     * by the X rotation and the orientation of the Z rotation axis is not
     * affected by the X or Y rotation.
     * @param euler  the Vector3d consisting of three rotation angles about X,Y,Z
     *
     */
    public final static Matrix4f setEuler(Vector3f euler) {
		float sina, sinb, sinc;
		float cosa, cosb, cosc;
	
		sina = (float) Math.sin(euler.x);
		sinb = (float) Math.sin(euler.y);
		sinc = (float) Math.sin(euler.z);
		cosa = (float) Math.cos(euler.x);
		cosb = (float) Math.cos(euler.y);
		cosc = (float) Math.cos(euler.z);
	
		float [] mat = new float[16];
		mat[0] = cosb * cosc;
		mat[1] = -(cosa * sinc) + (sina * sinb * cosc);
		mat[2] = (sina * sinc) + (cosa * sinb *cosc);
		mat[3] = (float) 0.0;
	
		mat[4] = cosb * sinc;
		mat[5] = (cosa * cosc) + (sina * sinb * sinc);
		mat[6] = -(sina * cosc) + (cosa * sinb *sinc);
		mat[7] = (float) 0.0;
	
		mat[8] = -sinb;
		mat[9] = sina * cosb;
		mat[10] = cosa * cosb;
		mat[11] = (float) 0.0;
	
		mat[12] = (float) 0.0;
		mat[13] = (float) 0.0;
		mat[14] = (float) 0.0;
		mat[15] = (float) 1.0;
		
		
		return new Matrix4f(mat);

        
    }
    

    
    public static void toLocalMatrix(Matrix4f referential, Matrix4f globalMatrix) {
		Matrix4f rotation = (Matrix4f) referential.clone();		
		rotation.invert();				
				
		globalMatrix.mul(rotation);
		
    	
    }
    
    public static Vector3f getEuler(Matrix3f rotation) {
        float heading;
        float attitude;
        float bank;
		// Assuming the angles are in radians.
    	if (rotation.m10 > 0.998) { // singularity at north pole
    		heading = (float) Math.atan2(rotation.m02,rotation.m22);
    		attitude = (float) (Math.PI/2);
    		bank = 0;
    		return new Vector3f(heading, attitude, bank);
    	}
    	if (rotation.m10 < -0.998) { // singularity at south pole
    		heading = (float) Math.atan2(rotation.m02,rotation.m22);
    		attitude = (float) (-Math.PI/2);
    		bank = 0;
    		return new Vector3f(heading, attitude, bank);
    	}
    	heading = (float) Math.atan2(-rotation.m20,rotation.m00);
    	bank = (float) Math.atan2(-rotation.m12,rotation.m11);
    	attitude = (float) Math.asin(rotation.m10);
    	return new Vector3f(heading, attitude, bank);
    }
    
    public static Vector3f getEuler(Matrix4f m) {
        Matrix3f rotation = new Matrix3f();
		m.get(rotation);
		return getEuler(rotation);
    }

    public static Vector3f projectOnPlane(Vector3f planeNormal, Vector3f vectorToProject) {
    	
    	planeNormal.normalize();
    	
    	Vector3f projection = new Vector3f(vectorToProject);
    	
    	float scalar = vectorToProject.dot(planeNormal);
    	
    	
    	if(Math.abs(scalar) < 0.0001) {    		
    		return projection;
    	} else if(Math.abs(scalar) > 0.9999) {    		
    		return null;
    	}   	
    	
    	planeNormal.scale (scalar);    	
    	
    	projection.sub(planeNormal);
    	
    	return projection;
    }
    
// public static Vector4f projectOnPlane(Vector4f planeNormal, Vector4f vectorToProject) {
//    	
//    	planeNormal.normalize();
//    	Vector4f projection = new Vector4f(vectorToProject);
//    	float scalar = vectorToProject.dot(planeNormal);
//    	planeNormal.scale(scalar);    	
//    	projection.sub(planeNormal);
//    	return projection;
//    }
    
    /**
     * Helping function that specifies the position and orientation of a
     * matrix. 
     * @param eye the location of the eye
     * @param center a point in the virtual world where the eye is looking
     * @param up an up vector specifying the frustum's up direction
     */
    public static Matrix4f lookAt(Point3f eye, Point3f center, Vector3f up) {
        double forwardx,forwardy,forwardz,invMag;
        double upx,upy,upz;
        double sidex,sidey,sidez;

//        forwardx =  eye.x -center.x ;
//        forwardy =  eye.y - center.y;
//        forwardz =  eye.z - center.z;
      forwardx =  center.x - eye.x;
      forwardy =  center.y - eye.y;
      forwardz =  center.z - eye.z;
        

        invMag = 1.0/Math.sqrt( forwardx*forwardx + forwardy*forwardy + forwardz*forwardz);
        forwardx = forwardx*invMag;
        forwardy = forwardy*invMag;
        forwardz = forwardz*invMag;


        invMag = 1.0/Math.sqrt( up.x*up.x + up.y*up.y + up.z*up.z);
        upx = up.x*invMag;
        upy = up.y*invMag;
        upz = up.z*invMag;

	// side = Up cross forward
	sidex = upy*forwardz-forwardy*upz;
	sidey = upz*forwardx-upx*forwardz;
	sidez = upx*forwardy-upy*forwardx;

	invMag = 1.0/Math.sqrt( sidex*sidex + sidey*sidey + sidez*sidez);
	sidex *= invMag;
	sidey *= invMag;
	sidez *= invMag;

	// recompute up = forward cross side

	upx = forwardy*sidez-sidey*forwardz;
	upy = forwardz*sidex-forwardx*sidez;
	upz = forwardx*sidey-forwardy*sidex;

	
	float[] mat = new float[16];
		// transpose because we calculated the inverse of what we want
	mat[0] =  (float) forwardx;
	mat[4] =  (float) forwardy;
	mat[8] = (float) forwardz;   
	
	mat[1] = (float) sidex;
    mat[5] = (float) sidey;
    mat[9] = (float) sidez;

	mat[2] = (float) upx;
	mat[6] = (float) upy;
	mat[10] = (float) upz;

	

//        mat[3] = -eye.x*mat[0] + -eye.y*mat[1] + -eye.z*mat[2];
//        mat[7] = -eye.x*mat[4] + -eye.y*mat[5] + -eye.z*mat[6];
//        mat[11] = -eye.x*mat[8] + -eye.y*mat[9] + -eye.z*mat[10];
	mat[3] = eye.x;
	mat[7] = eye.y;
	mat[11] = eye.z;
	mat[12] = mat[13] = mat[14] = 0;
	mat[15] = 1;

	 return new Matrix4f(mat);
    }

}
