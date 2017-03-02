/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.shot.filters;

import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.lidar.amapvox.shot.converters.TxtShotIteratorConverter;
import fr.amap.lidar.format.shot.ShotReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Iterator;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author claudia
 */
public class ShotFilter2D_TXTTest {
    
    public ShotFilter2D_TXTTest() {
       
    }
    
    @Test
    public void testShotFilter2D() throws Exception{

        
        File inputFile = new File(ShotFilter2D_TXTTest.class.getResource("/shots_files/Pochette70cm_UnitTest.txt").getFile());
        File referenceFile = new File(ShotFilter2D_TXTTest.class.getResource("/shots_files/Pochette70cm_UnitTest_Filter2DAMAPVoxTXT.txt").getFile());
        
        ShotReader shotReader = new ShotReader(inputFile);

        TxtShotIteratorConverter converter = new TxtShotIteratorConverter(shotReader.iterator());
        
        ShotFilter2D_TXT filter = new ShotFilter2D_TXT(converter.iterator());
        
        try (BufferedReader reader = new BufferedReader(new FileReader(referenceFile))) {
            Iterator<fr.amap.lidar.amapvox.shot.Shot> filterIterator = filter.iterator();
            
            double delta = 0.0000000001;
            
            //skip headers
            reader.readLine();
            
            while(filterIterator.hasNext()){
                
                fr.amap.lidar.amapvox.shot.Shot filteredShot = filterIterator.next();
                String line = reader.readLine();
                String[] split = line.split(" ");
                
                assertEquals(Double.valueOf(split[0]), filteredShot.direction.x, delta);
                assertEquals(Double.valueOf(split[1]), filteredShot.direction.y, delta);
                assertEquals(Double.valueOf(split[2]), filteredShot.direction.z, delta);
                assertEquals(Double.valueOf(split[3]), filteredShot.getFirstRange(), delta);
                assertEquals(Integer.valueOf(split[4]), (filteredShot.isEmpty() ? 1  : 0), 0);
            }
        }
    }
    
}
