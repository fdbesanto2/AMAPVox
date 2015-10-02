/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.jraster.asc;

import java.io.File;
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
public class RegularDtmTest {
    
    public RegularDtmTest() {
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
     * Test of getSimpleHeight method, of class RegularDtm.
     */
    @Test
    public void testGetSimpleHeight() {
        System.out.println("getSimpleHeight");
        
        try {
            RegularDtm instance = DtmLoader.readFromAscFile(new File(getClass().getClassLoader().getResource("sample_res1m.asc").toURI()));

            float expResult;
            float result;
            
            result = instance.getSimpleHeight(286356.75f, 583517);
            expResult = 42.6f;
            assertEquals(expResult, result, 0.0);
            
            result = instance.getSimpleHeight(286358.59f, 583518);
            expResult = 72.08f;
            assertEquals(expResult, result, 0.0);
            
            result = instance.getSimpleHeight(286355.59f, 583516);
            expResult = Float.NaN;
            assertEquals(expResult, result, 0.0);
            
            instance = DtmLoader.readFromAscFile(new File(getClass().getClassLoader().getResource("sample_res3m.asc").toURI()));
            
            result = instance.getSimpleHeight(286359.62f, 583525);
            expResult = 42.6f;
            assertEquals(expResult, result, 0.0);
            
            result = instance.getSimpleHeight(286357.62f, 583525);
            expResult = 14.2899f;
            assertEquals(expResult, result, 0.0);
            
            result = instance.getSimpleHeight(286355.62f, 583530);
            expResult = Float.NaN;
            assertEquals(expResult, result, 0.0);

        } catch (Exception ex) {
            fail("Cannot read dtm test asc file");
        }
    }
    
}
