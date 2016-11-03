/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar;

import fr.amap.commons.math.matrix.Mat4D;
import java.io.File;

/**
 *
 * @author calcul
 */
public class SimpleScan {

    public final File file;
    public Mat4D popMatrix;
    public Mat4D sopMatrix;

    public SimpleScan(File file) {
        this.file = file;
        popMatrix = Mat4D.identity();
        sopMatrix = Mat4D.identity();
    }

    public SimpleScan(File file, Mat4D popMatrix, Mat4D sopMatrix) {
        this.file = file;
        this.popMatrix = popMatrix;
        this.sopMatrix = sopMatrix;
    }

    @Override
    public String toString() {
        return file.getAbsolutePath();
    }
}
