/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.matrix;

import fr.amap.commons.math.vector.Vec3F;

/**
 * A double precision matrix with undefined row and column number
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Mat {
   
    
    /**
     *
     */
    public double mat[][];

    /**
     *
     */
    public int lineNumber;

    /**
     *
     */
    public int columnNumber;
    
    /**
     *
     * @param lineNumber
     * @param columnNumber
     */
    public Mat(int lineNumber, int columnNumber){
        
        this.lineNumber = lineNumber;
        this.columnNumber = columnNumber;
        mat = new double[lineNumber][columnNumber];
    }
    
    /**
     *
     * @param index matrix index line
     * @param data array to set the line
     */
    public void setLine(int index, double[] data){
        
        System.arraycopy(data, 0, mat[index], 0, columnNumber);
        
    }
    
    /**
     *
     * @param index matrix index column
     * @param data array to set the column
     */
    public void setColumn(int index, double[] data){
        
        for(int i=0;i<lineNumber;i++){
            mat[i][index] = data[i];
        }
        
    }
    
    /**
     *
     * @param index matrix index column
     * @param value value to set to the column
     */
    public void setColumn(int index, double value){
        
        for(int i=0;i<lineNumber;i++){
            mat[i][index] = value;
        }
        
    }
    
    /**
     *
     * @param data array to add to tthe matrix
     */
    public void setData(double[] data){
        
        int index = 0;
        
        for(int i=0;i<lineNumber;i++){
            for(int j=0;j<columnNumber;j++){
                
                mat[i][j] = data[index];
                index++;
            }
        }
    }
    
    /**
     *
     * @param matSource the matrix to transpose
     * @return the transposed matrix
     */
    public static Mat transpose(Mat matSource){
        
        Mat matDest = new Mat(matSource.columnNumber, matSource.lineNumber);
        
        for(int i=0;i<matSource.lineNumber;i++){
            
            for(int j=0;j<matSource.columnNumber;j++){

                matDest.mat[j][i] = matSource.mat[i][j];
            }
        }
        
        return matDest;
    }
    
    /**
     * Add a 3d vector to the matrix
     * @param vec
     */
    public void addVec3(Vec3F vec){
        
        if(lineNumber == 3){
            
            for(int i=0;i<columnNumber;i++){

                mat[0][i] = mat[0][i]+vec.x;
                mat[1][i] = mat[1][i]+vec.y;
                mat[2][i] = mat[2][i]+vec.z;
            }
        }
    }
    
    /**
     *
     * @param mat1 first matrix to multiply
     * @param mat2 second matrix to multiply
     * @return the resulting matrix
     */
    public static Mat multiply(Mat mat1, Mat mat2){
        
        if(mat1.lineNumber == mat2.lineNumber){
            
            int maxColumn = 0;
            if(mat2.columnNumber>mat1.columnNumber){
                maxColumn = mat2.columnNumber;
            }else{
                maxColumn = mat1.columnNumber;
            }

            Mat result = new Mat(mat1.lineNumber, maxColumn);
            
            
            
            double sum = 0;
            for ( int i = 0 ; i < result.lineNumber ; i++ )
            {
               for ( int j = 0 ; j < result.columnNumber ; j++ )
               {   
                  for ( int k = 0 ; k < mat2.lineNumber ; k++ )
                  {
                     sum = sum + mat1.mat[i][k]*mat2.mat[k][j];
                  }

                  result.mat[i][j] = sum;
                  sum = 0;
               }
            }
            
            return result;
        }
        
        return null;
    }
}
