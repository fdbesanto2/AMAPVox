/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.matrix;

import fr.amap.commons.math.vector.Vec3D;
import fr.amap.commons.math.vector.Vec4D;
import static java.lang.Double.NaN;
import javax.vecmath.Matrix4d;

/**
 * A double precision 4x4 matrix
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Mat4D {
    
    /**
     * The matrix array
     */
    public double[] mat;
    
    /**
     * Constructs and initialize a new double precision 4x4 matrix filled with zero
     */
    public Mat4D(){
        
        mat = new double[16];
        
    }
    
    /**
     * Convert the 4x4 matrix to an undefine sized matrix
     * @return a new Mat object with 4x4 size
     */
    public Mat toMat(){
        Mat result = new Mat(4,4);
        
        result.setData(mat);
        
        return result;
    }
    
    /**
     * Constructs and initialize a new 4x4 double precision matrix with an existing matrix
     * @param source the matrix to copy
     */
    public Mat4D(Mat4D source){
        
        mat = new double[16];
        
        mat[0] = source.mat[0];
        mat[1] = source.mat[1];
        mat[2] = source.mat[2];
        mat[3] = source.mat[3];
        mat[4] = source.mat[4];
        mat[5] = source.mat[5];
        mat[6] = source.mat[6];
        mat[7] = source.mat[7];
        mat[8] = source.mat[8];
        mat[9] = source.mat[9];
        mat[10] = source.mat[10];
        mat[11] = source.mat[11];
        mat[12] = source.mat[12];
        mat[13] = source.mat[13];
        mat[14] = source.mat[14];
        mat[15] = source.mat[15];
    }
    
    /**
     * Constructs and initialize a new 4x4 double precision matrix with an existing matrix
     * @param source the matrix to copy
     */
    public Mat4D(Matrix4d source){
        
        mat = new double[16];
        int index = 0;
        for (int ligne = 0; ligne < 4; ligne++) {
            for (int colonne = 0; colonne < 4; colonne++) {
                mat[index] = source.getElement(ligne, colonne);
                index++;
            }
        }
    }
    
    /**
     *
     * @return double array of 16
     */
    public static double[] create(){
        
        double[] dest = new double[16];
                
        return dest;
    }
    
    /**
     *
     * @return 4x4 identity matrix
     */
    public static Mat4D identity(){
        
        
        Mat4D dest = new Mat4D();
        
        dest.mat = new double[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        };
        
        return dest;
    }
    
    /**
     * Get inverse of the given matrix
     * @param mat4D 4x4 matrix to inverse
     * @return inverse of the matrix
     */
    public static Mat4D inverse(Mat4D mat4D){
        
        Mat4D dest = new Mat4D();
        double[] mat = mat4D.mat;
        
        double a00 = mat[0], a01 = mat[1], a02 = mat[2], a03 = mat[3];
        double a10 = mat[4], a11 = mat[5], a12 = mat[6], a13 = mat[7];
        double a20 = mat[8], a21 = mat[9], a22 = mat[10], a23 = mat[11];
        double a30 = mat[12], a31 = mat[13], a32 = mat[14], a33 = mat[15];
        
        double b00 = a00*a11 - a01*a10;
        double b01 = a00*a12 - a02*a10;
        double b02 = a00*a13 - a03*a10;
        double b03 = a01*a12 - a02*a11;
        double b04 = a01*a13 - a03*a11;
        double b05 = a02*a13 - a03*a12;
        double b06 = a20*a31 - a21*a30;
        double b07 = a20*a32 - a22*a30;
        double b08 = a20*a33 - a23*a30;
        double b09 = a21*a32 - a22*a31;
        double b10 = a21*a33 - a23*a31;
        double b11 = a22*a33 - a23*a32;
        
        double invDet = 1/(b00*b11 - b01*b10 + b02*b09 + b03*b08 - b04*b07 + b05*b06);
        
        dest.mat = new double[]{
            (a11*b11 - a12*b10 + a13*b09)*invDet, (-a01*b11 + a02*b10 - a03*b09)*invDet, (a31*b05 - a32*b04 + a33*b03)*invDet, (-a21*b05 + a22*b04 - a23*b03)*invDet,
            (-a10*b11 + a12*b08 - a13*b07)*invDet, (a00*b11 - a02*b08 + a03*b07)*invDet, (-a30*b05 + a32*b02 - a33*b01)*invDet, (a20*b05 - a22*b02 + a23*b01)*invDet,
            (a10*b10 - a11*b08 + a13*b06)*invDet, (-a00*b10 + a01*b08 - a03*b06)*invDet, (a30*b04 - a31*b02 + a33*b00)*invDet, (-a20*b04 + a21*b02 - a23*b00)*invDet,
            (-a10*b09 + a11*b07 - a12*b06)*invDet, (a00*b09 - a01*b07 + a02*b06)*invDet, (-a30*b03 + a31*b01 - a32*b00)*invDet, (a20*b03 - a21*b01 + a22*b00)*invDet
        };
        
        return dest;
    }
    
    /**
     * Multiply a 4x4 double precision matrix by another in this order
     * @param mat4D1 The first 4x4 matrix
     * @param mat4D2 The second 4x4 matrix
     * @return The new matrix, result of the multiplication
     */
    public static Mat4D multiply(Mat4D mat4D1, Mat4D mat4D2){
        
        Mat4D dest = new Mat4D();
        double[] mat = mat4D1.mat;
        double[] mat2 = mat4D2.mat;
        
        double a00 = mat[0], a01 = mat[1], a02 = mat[2], a03 = mat[3];
        double a10 = mat[4], a11 = mat[5], a12 = mat[6], a13 = mat[7];
        double a20 = mat[8], a21 = mat[9], a22 = mat[10], a23 = mat[11];
        double a30 = mat[12], a31 = mat[13], a32 = mat[14], a33 = mat[15];
        
        double b00 = mat2[0], b01 = mat2[1], b02 = mat2[2], b03 = mat2[3];
        double b10 = mat2[4], b11 = mat2[5], b12 = mat2[6], b13 = mat2[7];
        double b20 = mat2[8], b21 = mat2[9], b22 = mat2[10], b23 = mat2[11];
        double b30 = mat2[12], b31 = mat2[13], b32 = mat2[14], b33 = mat2[15];
        
        dest.mat = new double[]{
            b00*a00 + b01*a10 + b02*a20 + b03*a30, b00*a01 + b01*a11 + b02*a21 + b03*a31, b00*a02 + b01*a12 + b02*a22 + b03*a32, b00*a03 + b01*a13 + b02*a23 + b03*a33,
            b10*a00 + b11*a10 + b12*a20 + b13*a30, b10*a01 + b11*a11 + b12*a21 + b13*a31, b10*a02 + b11*a12 + b12*a22 + b13*a32, b10*a03 + b11*a13 + b12*a23 + b13*a33,
            b20*a00 + b21*a10 + b22*a20 + b23*a30, b20*a01 + b21*a11 + b22*a21 + b23*a31, b20*a02 + b21*a12 + b22*a22 + b23*a32, b20*a03 + b21*a13 + b22*a23 + b23*a33,
            b30*a00 + b31*a10 + b32*a20 + b33*a30, b30*a01 + b31*a11 + b32*a21 + b33*a31, b30*a02 + b31*a12 + b32*a22 + b33*a32, b30*a03 + b31*a13 + b32*a23 + b33*a33
        };
        
        return dest;
    }
    
    /**
     * Multiply a 4x4 double precision matrix by a 4d vector in this order
     * @param mat4D The 4x4 matrix
     * @param vec4D The 4d vector
     * @return A new 4d vector, result of the multiplication
     */
    public static Vec4D multiply(Mat4D mat4D, Vec4D vec4D){
        
        Vec4D dest = new Vec4D();
        double[] mat = mat4D.mat;
        
        double a00 = mat[0], a01 = mat[1], a02 = mat[2], a03 = mat[3];
        double a10 = mat[4], a11 = mat[5], a12 = mat[6], a13 = mat[7];
        double a20 = mat[8], a21 = mat[9], a22 = mat[10], a23 = mat[11];
        double a30 = mat[12], a31 = mat[13], a32 = mat[14], a33 = mat[15];
        
        double v00 = vec4D.x;
        double v10 = vec4D.y;
        double v20 = vec4D.z;
        double v30 = vec4D.w;
        
        dest.x = a00 * v00 + a01 * v10 + a02 * v20 + a03 * v30;
        dest.y = a10 * v00 + a11 * v10 + a12 * v20 + a13 * v30;
        dest.z = a20 * v00 + a21 * v10 + a22 * v20 + a23 * v30;
        dest.w = a30 * v00 + a31 * v10 + a32 * v20 + a33 * v30;
        
        return dest;
    }
    
    /**
     * Perform a translation of a 4x4 double precision transformation matrix by a 3d translation vector
     * @param mat4D The 4x4 matrix
     * @param vec3D The 3d vector
     * @return The given matrix, translated by the vector
     */
    public static Mat4D translate(Mat4D mat4D, Vec3D vec3D){
        
        Mat4D dest = new Mat4D();
        double[] mat = mat4D.mat;
        
        double x = vec3D.x, y = vec3D.y, z = vec3D.z;
        
        double a00 = mat[0], a01 = mat[1], a02 = mat[2], a03 = mat[3];
        double a10 = mat[4], a11 = mat[5], a12 = mat[6], a13 = mat[7];
        double a20 = mat[8], a21 = mat[9], a22 = mat[10], a23 = mat[11];
        double a30 = mat[12], a31 = mat[13], a32 = mat[14], a33 = mat[15];
        
        dest.mat = new double[]{
            a00, a01, a02, a03,
            a10, a11, a12, a13,
            a20, a21, a22, a23,
            a00*x + a10*y + a20*z + a30, a01*x + a11*y + a21*z + a31, a02*x + a12*y + a22*z + a32, a03*x + a13*y + a23*z + a33
        };
        
        return dest;
    }
    
    /**
     * Perform a scale of a 4x4 double precision transformation matrix by a 3d scaling vector
     * @param mat4D The 4x4 matrix
     * @param vec3D The 3d vector
     * @return The given matrix, scaled by the vector
     */
    public static Mat4D scale(Mat4D mat4D, Vec3D vec3D){
        
        Mat4D dest = new Mat4D();
        double[] mat = mat4D.mat;
        
        double x = vec3D.x, y = vec3D.y, z = vec3D.z;
        
        dest.mat = new double[]{
            mat[0]*x, mat[1]*x, mat[2]*x, mat[3]*x,
            mat[4]*y, mat[5]*y, mat[6]*y, mat[7]*y,
            mat[8]*z, mat[9]*z, mat[10]*z, mat[11]*z,
            mat[12], mat[13], mat[14], mat[15]
        };
        
        return dest;
    }

    /**
     * Perform a rotation of a 4x4 double precision transformation matrix by the given axis and angle
     * @param mat4D The 4x4 matrix
     * @param angle The angle, in radians
     * @param axis The 3d vector acting representing the axis
     * @return The given matrix, with the rotation applied
     */
    public static Mat4D rotate(Mat4D mat4D, double angle, Vec3D axis){
        
        double[] dest = Mat4D.create();
        double[] mat = mat4D.mat;

        
        double x = axis.x, y = axis.y, z = axis.z;
        double len = (double)Math.sqrt(x*x + y*y + z*z);
        if (len == NaN) { return null; }
        if (len != 1) {
                len = 1 / len;
                x *= len; 
                y *= len; 
                z *= len;
        }
        
        double s = (double)Math.sin(angle);
        double c = (double)Math.cos(angle);
        double t = 1-c;
        
        // Cache the matrix values (makes for huge speed increases!)
        double a00 = mat[0], a01 = mat[1], a02 = mat[2], a03 = mat[3];
        double a10 = mat[4], a11 = mat[5], a12 = mat[6], a13 = mat[7];
        double a20 = mat[8], a21 = mat[9], a22 = mat[10], a23 = mat[11];
        
        // Construct the elements of the rotation matrix
        double b00 = x*x*t + c, b01 = y*x*t + z*s, b02 = z*x*t - y*s;
        double b10 = x*y*t - z*s, b11 = y*y*t + c, b12 = z*y*t + x*s;
        double b20 = x*z*t + y*s, b21 = y*z*t - x*s, b22 = z*z*t + c;
        
        
        
        // Perform rotation-specific matrix multiplication
        dest[0] = a00*b00 + a10*b01 + a20*b02;
        dest[1] = a01*b00 + a11*b01 + a21*b02;
        dest[2] = a02*b00 + a12*b01 + a22*b02;
        dest[3] = a03*b00 + a13*b01 + a23*b02;
        
        dest[4] = a00*b10 + a10*b11 + a20*b12;
        dest[5] = a01*b10 + a11*b11 + a21*b12;
        dest[6] = a02*b10 + a12*b11 + a22*b12;
        dest[7] = a03*b10 + a13*b11 + a23*b12;
        
        dest[8] = a00*b20 + a10*b21 + a20*b22;
        dest[9] = a01*b20 + a11*b21 + a21*b22;
        dest[10] = a02*b20 + a12*b21 + a22*b22;
        dest[11] = a03*b20 + a13*b21 + a23*b22;
        
        dest[12] = mat[12];
        dest[13] = mat[13];
        dest[14] = mat[14];
        dest[15] = mat[15];
        
        Mat4D result = new Mat4D();
        result.mat = dest;
        
        return result;
    }
    
    /**
     * Constructs a square frustum by the given parameters, represents this frustum like a 4x4 matrix
     * @param left The left limit
     * @param right The right limit
     * @param bottom The bottom limit
     * @param top The top limit
     * @param near The near limit
     * @param far The far limit
     * @return The frustum 4x4 matrix
     */
    public static Mat4D frustum(double left, double right,double bottom, double top, double near, double far){
        
        Mat4D dest = new Mat4D();
        
        double rl = (right - left);
        double tb = (top - bottom);
        double fn = (far - near);
        
        dest.mat = new double[]{
            (near*2) / rl, 0, 0, 0,
            0, (near*2) / tb, 0, 0,
            (right + left) / rl, (top + bottom) / tb, -(far + near) / fn, -1,
            0, 0, -(far*near*2) / fn, 0
        };
        
        return dest;
    }

    /**
     * Constructs and initialize a 4x4 double precision perspective matrix
     * @param fovy The field of view
     * @param aspect The aspect ratio
     * @param near The near limit of the frustum
     * @param far The far limit of the frustum
     * @return A perspective double precision 4x4 matrix
     */
    public static Mat4D perspective(double fovy, double aspect, double near, double far){
        
        double top = (double)(near*Math.tan(fovy*Math.PI / 360.0));
        double right = top*aspect;
        return Mat4D.frustum(-right, right, -top, top, near, far);
    }
    
    /**
     * <p>Constructs and initialize a 4x4 double precision orthographic projection matrix.</p>
     * An orthographic matrix is represented by a rectangular volume.
     * @param left The left limit
     * @param right The right limit
     * @param bottom The bottom limit
     * @param top The top limit
     * @param near The near limit
     * @param far The far limit
     * @return An orthographic projection 4x4 matrix
     */
    public static Mat4D ortho(double left, double right, double bottom, double top, double near, double far){
        
        Mat4D dest = new Mat4D();
        
        double rl = (right - left);
        double tb = (top - bottom);
        double fn = (far - near);
        
        dest.mat = new double[]{
            2 / rl, 0, 0, 0,
            0, 2 / tb, 0, 0,
            0, 0, -2 / fn, 0,
            -(left + right) / rl, -(top + bottom) / tb, -(far + near) / fn, 1
        };
        
        return dest;
    }
    
    /**
     * Compute a view matrix from the world position of the camera (eye), 
     * a global up vector and a target point (the point we want to look at)
     * @param eye The eye position as a 3d vector
     * @param center The target position as a 3d vector
     * @param up The up direction as a 3d vector
     * @return a 4x4 double precision lookat matrix
     */
    public static Mat4D lookAt(Vec3D eye, Vec3D center, Vec3D up){
        
        double  eyex = eye.x,
                eyey = eye.y,
                eyez = eye.z,
                upx = up.x,
                upy = up.y,
                upz = up.z,
                centerx = center.x,
                centery = center.y,
                centerz = center.z;

        if (eyex == centerx && eyey == centery && eyez == centerz) {
                return Mat4D.identity();
        }
        
        double z0,z1,z2,x0,x1,x2,y0,y1,y2,len;
        
        //vec3.direction(eye, center, z);
        z0 = eyex - center.x;
        z1 = eyey - center.y;
        z2 = eyez - center.z;
        
        // normalize (no check needed for 0 because of early return)
        len = (double)(1/Math.sqrt(z0*z0 + z1*z1 + z2*z2));
        z0 *= len;
        z1 *= len;
        z2 *= len;
        
        //vec3.normalize(vec3.cross(up, z, x));
        x0 = upy*z2 - upz*z1;
        x1 = upz*z0 - upx*z2;
        x2 = upx*z1 - upy*z0;
        len = (double)(Math.sqrt(x0*x0 + x1*x1 + x2*x2));
        if (len == NaN) {
                x0 = 0;
                x1 = 0;
                x2 = 0;
        } else {
                len = 1/len;
                x0 *= len;
                x1 *= len;
                x2 *= len;
        }
        
        //vec3.normalize(vec3.cross(z, x, y));
        y0 = z1*x2 - z2*x1;
        y1 = z2*x0 - z0*x2;
        y2 = z0*x1 - z1*x0;
        
        len = (double)(Math.sqrt(y0*y0 + y1*y1 + y2*y2));
        if (len == NaN) {
                y0 = 0;
                y1 = 0;
                y2 = 0;
        } else {
                len = 1/len;
                y0 *= len;
                y1 *= len;
                y2 *= len;
        }
        
        Mat4D result = new Mat4D();
        
        result.mat = new double[]{
            x0, y0, z0, 0,
            x1, y1, z1, 0,
            x2, y2, z2, 0,
            -(x0*eyex + x1*eyey + x2*eyez), -(y0*eyex + y1*eyey + y2*eyez), -(z0*eyex + z1*eyey + z2*eyez), 1
        };
        
        return result;
    }

    /**
     * Transpose a given 4x4 double precision matrix and return the result
     * @param mat4D The 4x4 matrix to transpose
     * @return The transposed matrix
     */
    public static Mat4D transpose(Mat4D mat4D){
        
        Mat4D dest = new Mat4D();
        double[] mat = mat4D.mat;
        
        dest.mat = new double[]{
            mat[0], mat[4], mat[8], mat[12],
            mat[1], mat[5], mat[9], mat[13],
            mat[2], mat[6], mat[10], mat[14],
            mat[3], mat[7], mat[11], mat[15]
        };
        
        return dest;
    }
    
    public static Mat4D removeTranslationPart(Mat4D source){
        
        Mat4D dest = new Mat4D();
        double[] mat = source.mat;
        
        dest.mat = new double[]{
            mat[0], mat[1], mat[2], 0,
            mat[4], mat[5], mat[6], 0,
            mat[8], mat[9], mat[10], 0,
            mat[12], mat[13], mat[14], mat[15]
        };
        
        return dest;
    }
    
    @Override
    public String toString(){
        
        StringBuilder result = new StringBuilder();
        
        for(double d : mat){
            result.append(d).append(" ");
        }
        
        return result.subSequence(0, result.length()-1).toString();
    }
    
    
}
