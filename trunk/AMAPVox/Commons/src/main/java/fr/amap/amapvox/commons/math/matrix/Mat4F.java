/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.commons.math.matrix;

import fr.amap.amapvox.commons.math.vector.Vec3F;
import fr.amap.amapvox.commons.math.vector.Vec4;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Mat4F {
    
    public float[] mat;
    
    public Mat4F(){
        
        //mat=Mat4.create();
        mat = new float[16];
        
    }
    
    public Mat4F(Mat4F source){
        
        mat = new float[16];
        
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
    
    public static float[] create(){
        
        float[] dest = new float[16];
                
        return dest;
    }
    
    public static Mat4F identity(){
        
        
        Mat4F dest = new Mat4F();
        
        dest.mat = new float[]{
            1, 0, 0, 0,
            0, 1, 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        };
        
        return dest;
    }
    
    public static Mat4F inverse(Mat4F mat4F){
        
        Mat4F dest = new Mat4F();
        float[] mat = mat4F.mat;
        
        float a00 = mat[0], a01 = mat[1], a02 = mat[2], a03 = mat[3];
        float a10 = mat[4], a11 = mat[5], a12 = mat[6], a13 = mat[7];
        float a20 = mat[8], a21 = mat[9], a22 = mat[10], a23 = mat[11];
        float a30 = mat[12], a31 = mat[13], a32 = mat[14], a33 = mat[15];
        
        float b00 = a00*a11 - a01*a10;
        float b01 = a00*a12 - a02*a10;
        float b02 = a00*a13 - a03*a10;
        float b03 = a01*a12 - a02*a11;
        float b04 = a01*a13 - a03*a11;
        float b05 = a02*a13 - a03*a12;
        float b06 = a20*a31 - a21*a30;
        float b07 = a20*a32 - a22*a30;
        float b08 = a20*a33 - a23*a30;
        float b09 = a21*a32 - a22*a31;
        float b10 = a21*a33 - a23*a31;
        float b11 = a22*a33 - a23*a32;
        
        float invDet = 1/(b00*b11 - b01*b10 + b02*b09 + b03*b08 - b04*b07 + b05*b06);
        
        dest.mat = new float[]{
            (a11*b11 - a12*b10 + a13*b09)*invDet, (-a01*b11 + a02*b10 - a03*b09)*invDet, (a31*b05 - a32*b04 + a33*b03)*invDet, (-a21*b05 + a22*b04 - a23*b03)*invDet,
            (-a10*b11 + a12*b08 - a13*b07)*invDet, (a00*b11 - a02*b08 + a03*b07)*invDet, (-a30*b05 + a32*b02 - a33*b01)*invDet, (a20*b05 - a22*b02 + a23*b01)*invDet,
            (a10*b10 - a11*b08 + a13*b06)*invDet, (-a00*b10 + a01*b08 - a03*b06)*invDet, (a30*b04 - a31*b02 + a33*b00)*invDet, (-a20*b04 + a21*b02 - a23*b00)*invDet,
            (-a10*b09 + a11*b07 - a12*b06)*invDet, (a00*b09 - a01*b07 + a02*b06)*invDet, (-a30*b03 + a31*b01 - a32*b00)*invDet, (a20*b03 - a21*b01 + a22*b00)*invDet
        };
        
        return dest;
    }
    
    public static Mat4F multiply(Mat4F mat4F, float[] mat2){
        
        Mat4F dest = new Mat4F();
        float[] mat = mat4F.mat;
        
        float a00 = mat[0], a01 = mat[1], a02 = mat[2], a03 = mat[3];
        float a10 = mat[4], a11 = mat[5], a12 = mat[6], a13 = mat[7];
        float a20 = mat[8], a21 = mat[9], a22 = mat[10], a23 = mat[11];
        float a30 = mat[12], a31 = mat[13], a32 = mat[14], a33 = mat[15];
        
        float b00 = mat2[0], b01 = mat2[1], b02 = mat2[2], b03 = mat2[3];
        float b10 = mat2[4], b11 = mat2[5], b12 = mat2[6], b13 = mat2[7];
        float b20 = mat2[8], b21 = mat2[9], b22 = mat2[10], b23 = mat2[11];
        float b30 = mat2[12], b31 = mat2[13], b32 = mat2[14], b33 = mat2[15];
        
        dest.mat = new float[]{
            b00*a00 + b01*a10 + b02*a20 + b03*a30, b00*a01 + b01*a11 + b02*a21 + b03*a31, b00*a02 + b01*a12 + b02*a22 + b03*a32, b00*a03 + b01*a13 + b02*a23 + b03*a33,
            b10*a00 + b11*a10 + b12*a20 + b13*a30, b10*a01 + b11*a11 + b12*a21 + b13*a31, b10*a02 + b11*a12 + b12*a22 + b13*a32, b10*a03 + b11*a13 + b12*a23 + b13*a33,
            b20*a00 + b21*a10 + b22*a20 + b23*a30, b20*a01 + b21*a11 + b22*a21 + b23*a31, b20*a02 + b21*a12 + b22*a22 + b23*a32, b20*a03 + b21*a13 + b22*a23 + b23*a33,
            b30*a00 + b31*a10 + b32*a20 + b33*a30, b30*a01 + b31*a11 + b32*a21 + b33*a31, b30*a02 + b31*a12 + b32*a22 + b33*a32, b30*a03 + b31*a13 + b32*a23 + b33*a33
        };
        
        return dest;
    }
    
    public static Vec4 multiply(Mat4F mat4F, Vec4 vec4){
        
        Vec4 dest = new Vec4();
        float[] mat = mat4F.mat;
        
        float a00 = mat[0], a01 = mat[1], a02 = mat[2], a03 = mat[3];
        float a10 = mat[4], a11 = mat[5], a12 = mat[6], a13 = mat[7];
        float a20 = mat[8], a21 = mat[9], a22 = mat[10], a23 = mat[11];
        float a30 = mat[12], a31 = mat[13], a32 = mat[14], a33 = mat[15];
        
        float v00 = vec4.x;
        float v10 = vec4.y;
        float v20 = vec4.z;
        float v30 = vec4.w;
        
        dest.x = a00 * v00 + a01 * v10 + a02 * v20 + a03 * v30;
        dest.y = a10 * v00 + a11 * v10 + a12 * v20 + a13 * v30;
        dest.z = a20 * v00 + a21 * v10 + a22 * v20 + a23 * v30;
        dest.w = a30 * v00 + a31 * v10 + a32 * v20 + a33 * v30;
        
        return dest;
    }
    
    public static Mat4F translate(Mat4F mat4F, Vec3F vec){
        
        Mat4F dest = new Mat4F();
        float[] mat = mat4F.mat;
        
        float x = vec.x, y = vec.y, z = vec.z;
        
        float a00 = mat[0], a01 = mat[1], a02 = mat[2], a03 = mat[3];
        float a10 = mat[4], a11 = mat[5], a12 = mat[6], a13 = mat[7];
        float a20 = mat[8], a21 = mat[9], a22 = mat[10], a23 = mat[11];
        float a30 = mat[12], a31 = mat[13], a32 = mat[14], a33 = mat[15];
        
        dest.mat = new float[]{
            a00, a01, a02, a03,
            a10, a11, a12, a13,
            a20, a21, a22, a23,
            a00*x + a10*y + a20*z + a30, a01*x + a11*y + a21*z + a31, a02*x + a12*y + a22*z + a32, a03*x + a13*y + a23*z + a33
        };
        
        return dest;
    }
    
    public static Mat4F scale(Mat4F mat4F, Vec3F vec){
        
        Mat4F dest = new Mat4F();
        float[] mat = mat4F.mat;
        float x = vec.x, y = vec.y, z = vec.z;
        
        dest.mat = new float[]{
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
    
    public static Mat4F setRotation(Mat4F matrix, Vec3F axis, float angle){
        
        Mat4F result = new Mat4F(matrix);
        
        if(axis.x != 0){
            result.mat[5] = (float) Math.cos(angle);
            result.mat[6] = -(float) Math.sin(angle);
            result.mat[9] = (float) Math.sin(angle);
            result.mat[10] = (float) Math.cos(angle);
        }else if(axis.y != 0){
            result.mat[0] = (float) Math.cos(angle);
            result.mat[2] = (float) Math.sin(angle);
            result.mat[8] = -(float) Math.sin(angle);
            result.mat[10] = (float) Math.cos(angle);
        }else if(axis.z != 0){
            result.mat[0] = (float) Math.cos(angle);
            result.mat[1] = -(float) Math.sin(angle);
            result.mat[4] = (float) Math.sin(angle);
            result.mat[5] = (float) Math.cos(angle);
        }
        
        return result;
        
    }
    
    public static float[] rotate(float[] mat, float angle, float[] axis){
        
        float[] dest = Mat4F.create();
        
        float x = axis[0], y = axis[1], z = axis[2];
        float len = (float)Math.sqrt(x*x + y*y + z*z);
        if (Float.isNaN(len)) { return null; }
        if (len != 1) {
                len = 1 / len;
                x *= len; 
                y *= len; 
                z *= len;
        }
        
        float s = (float)Math.sin(angle);
        float c = (float)Math.cos(angle);
        float t = 1-c;
        
        // Cache the matrix values (makes for huge speed increases!)
        float a00 = mat[0], a01 = mat[1], a02 = mat[2], a03 = mat[3];
        float a10 = mat[4], a11 = mat[5], a12 = mat[6], a13 = mat[7];
        float a20 = mat[8], a21 = mat[9], a22 = mat[10], a23 = mat[11];
        
        // Construct the elements of the rotation matrix
        float b00 = x*x*t + c, b01 = y*x*t + z*s, b02 = z*x*t - y*s;
        float b10 = x*y*t - z*s, b11 = y*y*t + c, b12 = z*y*t + x*s;
        float b20 = x*z*t + y*s, b21 = y*z*t - x*s, b22 = z*z*t + c;
        
        
        
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
    
    public static Mat4F frustum(float left, float right,float bottom, float top, float near, float far){
        
        Mat4F dest = new Mat4F();
        
        float rl = (right - left);
        float tb = (top - bottom);
        float fn = (far - near);
        
        dest.mat = new float[]{
            (near*2) / rl, 0, 0, 0,
            0, (near*2) / tb, 0, 0,
            (right + left) / rl, (top + bottom) / tb, -(far + near) / fn, -1,
            0, 0, -(far*near*2) / fn, 0
        };
        
        return dest;
    }
    public static Mat4F perspective(float fovy, float aspect, float near, float far){
        
        float top = (float)(near*Math.tan(fovy*Math.PI / 360.0));
        float right = top*aspect;
        return Mat4F.frustum(-right, right, -top, top, near, far);
    }
    
    public static Mat4F ortho(float left, float right, float bottom, float top, float near, float far){
        
        Mat4F dest = new Mat4F();
        
        float rl = (right - left);
        float tb = (top - bottom);
        float fn = (far - near);
        
        dest.mat = new float[]{
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
    public static Mat4F lookAt(Vec3F eye, Vec3F center, Vec3F up){
        

        if (eye.x == center.x && eye.y == center.y && eye.z == center.z) {
            return Mat4F.identity();
        }
        
        Vec3F forward = Vec3F.normalize(Vec3F.substract(eye, center));
        
        Vec3F right = Vec3F.normalize(Vec3F.cross(up, forward));
        
        if(Vec3F.length(right) == 0){
            right.x = 1;
        }
        
        Vec3F newUp = Vec3F.cross(forward, right);
        
        Mat4F result = new Mat4F();
        
        result.mat = new float[]{
            right.x, newUp.x, forward.x, 0,
            right.y, newUp.y, forward.y, 0,
            right.z, newUp.z, forward.z, 0,
            -Vec3F.dot(right, eye), -Vec3F.dot(newUp, eye), -Vec3F.dot(forward, eye), 1
        };
        
        
        return result;
    }
    public static Mat4F transpose(Mat4F mat4F){
        
        float[] source = mat4F.mat;
        Mat4F dest = new Mat4F();
        
        dest.mat = new float[]{
            source[0], source[4], source[8], source[12],
            source[1], source[5], source[9], source[13],
            source[2], source[6], source[10], source[14],
            source[3], source[7], source[11], source[15]
        };
        
        return dest;
    }
    
    /**
     *
     * @param mat4F the matrix who got transformed by a lookat
     * @return
     */
    public static Vec3F getEyeFromMatrix(Mat4F mat4F){
      
        Mat4F modelViewT = transpose(mat4F);

        // Get plane normals 
        Vec3F n1 = new Vec3F(modelViewT.mat[0],modelViewT.mat[1],modelViewT.mat[2]);
        Vec3F n2 = new Vec3F(modelViewT.mat[4],modelViewT.mat[5],modelViewT.mat[6]);
        Vec3F n3 = new Vec3F(modelViewT.mat[8],modelViewT.mat[9],modelViewT.mat[10]);

        // Get plane distances
        float d1 = modelViewT.mat[3];
        float d2 = modelViewT.mat[7];
        float d3 = modelViewT.mat[11];

        Vec3F n2n3 = Vec3F.cross(n2, n3);
        Vec3F n3n1 = Vec3F.cross(n3, n1);
        Vec3F n1n2 = Vec3F.cross(n1, n2);

        Vec3F top = Vec3F.add(Vec3F.add(Vec3F.multiply(n2n3, d1), Vec3F.multiply(n3n1, d2)), Vec3F.multiply(n1n2, d3));
        float denom = Vec3F.dot(n1, n2n3);

        return Vec3F.multiply(top, 1/(-denom));
        //return top / -denom;
    }
}
