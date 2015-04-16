/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.math.matrix;

import fr.ird.voxelidar.engine3d.math.vector.Vec3D;
import fr.ird.voxelidar.engine3d.math.vector.Vec4D;
import static java.lang.Double.NaN;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Mat4D {
    
    /**
     *
     */
    public double[] mat;
    
    /**
     *
     */
    public Mat4D(){
        
        mat = new double[16];
        
    }
    
    /**
     *
     * @return
     */
    public Mat toMat(){
        Mat result = new Mat(4,4);
        
        result.setData(mat);
        
        return result;
    }
    
    /**
     *
     * @param source the matrix to copy
     */
    public Mat4D(Mat4D source){
        
        mat = new double[9];
        
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
     *
     * @return double array of 16
     */
    public static double[] create(){
        
        double[] dest = new double[16];
                
        return dest;
    }
    
    /**
     *
     * @return identity matrix 4x4
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
     *
     * @param mat4D
     * @return
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
     *
     * @param mat4D1
     * @param mat4D2
     * @return
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
     *
     * @param mat4D
     * @param vec4D
     * @return
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
     *
     * @param mat4D
     * @param vec
     * @return
     */
    public static Mat4D translate(Mat4D mat4D, Vec3D vec){
        
        Mat4D dest = new Mat4D();
        double[] mat = mat4D.mat;
        
        double x = vec.x, y = vec.y, z = vec.z;
        
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
     *
     * @param mat4D
     * @param vec
     * @return
     */
    public static Mat4D scale(Mat4D mat4D, Vec3D vec){
        
        Mat4D dest = new Mat4D();
        double[] mat = mat4D.mat;
        
        double x = vec.x, y = vec.y, z = vec.z;
        
        dest.mat = new double[]{
            mat[0]*x, mat[1]*x, mat[2]*x, mat[3]*x,
            mat[4]*y, mat[5]*y, mat[6]*y, mat[7]*y,
            mat[8]*z, mat[9]*z, mat[10]*z, mat[11]*z,
            mat[12], mat[13], mat[14], mat[15]
        };
        
        return dest;
    }
    
    /*
    parameters:
    mat: mat to rotate
    angle: in radians
    axis: vec3 representing the axis to rotate around
    
    
    */

    /**
     *
     * @param mat4D
     * @param angle
     * @param axis
     * @return
     */
    
    public static double[] rotate(Mat4D mat4D, double angle, double[] axis){
        
        double[] dest = Mat4D.create();
        double[] mat = mat4D.mat;

        
        double x = axis[0], y = axis[1], z = axis[2];
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
        
        return dest;
    }
    
    /**
     *
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @param near
     * @param far
     * @return
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
     *
     * @param fovy
     * @param aspect
     * @param near
     * @param far
     * @return
     */
    public static Mat4D perspective(double fovy, double aspect, double near, double far){
        
        double top = (double)(near*Math.tan(fovy*Math.PI / 360.0));
        double right = top*aspect;
        return Mat4D.frustum(-right, right, -top, top, near, far);
    }
    
    /**
     *
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @param near
     * @param far
     * @return
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
     * @param eye
     * @param center
     * @param up
     * @return
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
     *
     * @param mat
     * @return
     */
    public static Mat4D transpose(double[] mat){
        
        Mat4D dest = new Mat4D();
        
        dest.mat = new double[]{
            mat[0], mat[4], mat[8], mat[12],
            mat[1], mat[5], mat[9], mat[13],
            mat[2], mat[6], mat[10], mat[14],
            mat[3], mat[7], mat[11], mat[15]
        };
        
        return dest;
    }
    
    /**
     *
     * @param mat4D the matrix who got transformed by a lookat
     * @return
     */
    public static Vec3D getEyeFromMatrix(Mat4D mat4D){
      
        Mat4D modelViewT = transpose(mat4D.mat);

        // Get plane normals 
        Vec3D n1 = new Vec3D(modelViewT.mat[0],modelViewT.mat[1],modelViewT.mat[2]);
        Vec3D n2 = new Vec3D(modelViewT.mat[4],modelViewT.mat[5],modelViewT.mat[6]);
        Vec3D n3 = new Vec3D(modelViewT.mat[8],modelViewT.mat[9],modelViewT.mat[10]);

        // Get plane distances
        double d1 = modelViewT.mat[3];
        double d2 = modelViewT.mat[7];
        double d3 = modelViewT.mat[11];

        Vec3D n2n3 = Vec3D.cross(n2, n3);
        Vec3D n3n1 = Vec3D.cross(n3, n1);
        Vec3D n1n2 = Vec3D.cross(n1, n2);

        Vec3D top = Vec3D.add(Vec3D.add(Vec3D.multiply(n2n3, d1), Vec3D.multiply(n3n1, d2)), Vec3D.multiply(n1n2, d3));
        double denom = Vec3D.dot(n1, n2n3);

        return Vec3D.multiply(top, 1/(-denom));
        //return top / -denom;
    }
}
