/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxreader;

import fr.amap.amapvox.voxcommons.Voxel;
import java.io.File;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class VoxelFileReaderTest {
    
    public VoxelFileReaderTest() {
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
     * Test of iterator method, of class VoxelFileReader.
     */
    @Test
    public void testIterator() {
        
        System.out.println("iterator");
        VoxelFileReader instance;
        try {
            instance = new VoxelFileReader(new File(getClass().getClassLoader().getResource("sample.vox").toURI()));
            
            Iterator<Voxel> iterator = instance.iterator();
            assertNotNull(iterator);
            
            double delta = 0;
            
            iterator.hasNext();
            Voxel voxel = iterator.next();
            
            assertEquals(0, voxel.$i);
            assertEquals(0, voxel.$j);
            assertEquals(0, voxel.$k);
            assertEquals(6.212515f, voxel.PadBVTotal, delta);
            assertEquals(2.0f, voxel.bvEntering, delta);
            assertEquals(1.2f, voxel.bvIntercepted, delta);
            assertEquals(1.0f, voxel.transmittance, delta);
            
            iterator.hasNext();
            voxel = (Voxel) iterator.next();
            
            assertEquals(voxel.$i, 0);
            assertEquals(voxel.$j, 0);
            assertEquals(voxel.$k, 1);
            assertEquals(3.94545f, voxel.PadBVTotal, delta);
            assertEquals(5.4f, voxel.bvEntering, delta);
            assertEquals(6.4f, voxel.bvIntercepted, delta);
            assertEquals(0.5f, voxel.transmittance, delta);
            
            iterator.hasNext();
            voxel = (Voxel) iterator.next();
            
            assertEquals(voxel.$i, 0);
            assertEquals(voxel.$j, 1);
            assertEquals(voxel.$k, 0);
            assertEquals(787485.1f, voxel.PadBVTotal, delta);
            assertEquals(5.3f, voxel.bvEntering, delta);
            assertEquals(2.1f, voxel.bvIntercepted, delta);
            assertEquals(0.9f, voxel.transmittance, delta);
            
            iterator.hasNext();
            voxel = (Voxel) iterator.next();
            
            assertEquals(voxel.$i, 0);
            assertEquals(voxel.$j, 1);
            assertEquals(voxel.$k, 1);
            assertEquals(5561.2f, voxel.PadBVTotal, delta);
            assertEquals(6.12f, voxel.bvEntering, delta);
            assertEquals(1.5f, voxel.bvIntercepted, delta);
            assertEquals(0.7f, voxel.transmittance, delta);
            
            iterator.hasNext();
            voxel = (Voxel) iterator.next();
            
            assertEquals(voxel.$i, 1);
            assertEquals(voxel.$j, 0);
            assertEquals(voxel.$k, 0);
            assertEquals(Float.NaN, voxel.PadBVTotal, delta);
            assertEquals(45.0f, voxel.bvEntering, delta);
            assertEquals(10.4f, voxel.bvIntercepted, delta);
            assertEquals(0.8f, voxel.transmittance, delta);
            
            iterator.hasNext();
            voxel = (Voxel) iterator.next();
            
            assertEquals(voxel.$i, 1);
            assertEquals(voxel.$j, 0);
            assertEquals(voxel.$k, 1);
            assertEquals(6.36f, voxel.PadBVTotal, delta);
            assertEquals(3.0f, voxel.bvEntering, delta);
            assertEquals(9.0f, voxel.bvIntercepted, delta);
            assertEquals(0.0f, voxel.transmittance, delta);
            
            
        } catch (URISyntaxException ex) {
            Logger.getLogger(VoxelFileReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(VoxelFileReaderTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
