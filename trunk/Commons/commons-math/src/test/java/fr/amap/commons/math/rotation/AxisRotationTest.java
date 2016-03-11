/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.rotation;

import fr.amap.commons.math.matrix.Mat3D;
import fr.amap.commons.math.matrix.Mat4D;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author calcul
 */
public class AxisRotationTest {
    
    public AxisRotationTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }


    /**
     * Test of getRotationMatrix method, of class AxisRotation.
     */
    @Test
    public void testGetRotationMatrix() {
        
        AxisRotation rotation = new AxisRotation(0, 0, 1, 54);
        Mat3D rotationMatrix1 = rotation.getRotationMatrix();
        
        AxisRotation rotation2 = new AxisRotation(0, 0, 1, 324);
        Mat3D rotationMatrix2 = rotation2.getRotationMatrix();
        
        System.out.println("test");
    }
}
