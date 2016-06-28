/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.simulation.transmittance.lai2xxx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
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
public class LAI2xxxTest {
    
    private static LAI2xxx instance;
    
    private class Measure{
        
        public String type;
        public int id;
        public Calendar date;
        public String sensor;
        
        public float[] gaps;

        public Measure() {
        }

        public Measure(String type, int id, Calendar date, String sensor, float gap1, float gap2, float gap3, float gap4, float gap5) {
            this.type = type;
            this.id = id;
            this.date = date;
            this.sensor = sensor;
            
            this.gaps = new float[]{gap1, gap2, gap3, gap4, gap5};
        }        
    }
    
    private class ComputedMeasure{
        
        public Measure aboveMeasure;
        public Measure belowMeasure;

        public ComputedMeasure(Measure aboveMeasure, Measure belowMeasure) {
            this.aboveMeasure = aboveMeasure;
            this.belowMeasure = belowMeasure;
        }
        
        public float computeRatio(int gapID){
            return belowMeasure.gaps[gapID] / aboveMeasure.gaps[gapID];
        }
        
    }
    
    public LAI2xxxTest() {
        
        
    }
    
    @BeforeClass
    public static void setUpClass() {
        instance = new LAI2200(500, LAI2xxx.ViewCap.CAP_360, new boolean[]{false, false, false, false, false});
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
    
    @Test
    public void testComputeMeasuredValues() throws URISyntaxException, FileNotFoundException, IOException, ParseException{
     
        
        File aboveValuesFile = new File(LAI2xxxTest.class.getResource("/lai2200/1609-PM-ABO.TXT").toURI());
        File belowValuesFile = new File(LAI2xxxTest.class.getResource("/lai2200/1609-PM-BEL.TXT").toURI());
        
        List<Measure> aboveMeasures = getMeasuresFromFile(aboveValuesFile, "A");
        List<Measure> belowMeasures = getMeasuresFromFile(belowValuesFile, "B");
        
        /****we get the nearest (time speaking) above measure of each below measure****/
        
        Comparator c = new Comparator<Measure>() {
            @Override
            public int compare(Measure o1, Measure o2) {
                return o1.date.getTime().compareTo(o2.date.getTime());
            }
        };
        
        Collections.sort(aboveMeasures, c);
        Collections.sort(belowMeasures, c);
        
        List<ComputedMeasure> computedMeasures = new ArrayList<>();
        
        
        float[][] aboveMeasurePerRingAndPos = new float[5][aboveMeasures.size()];
        
        for(Measure belowMeasure : belowMeasures){
            
            int count = 0;
            Measure closestMeasure = new Measure();
            
            for(Measure aboveMeasure : aboveMeasures){

                aboveMeasurePerRingAndPos[0][count] = aboveMeasure.gaps[0];
                aboveMeasurePerRingAndPos[1][count] = aboveMeasure.gaps[1];
                aboveMeasurePerRingAndPos[2][count] = aboveMeasure.gaps[2];
                aboveMeasurePerRingAndPos[3][count] = aboveMeasure.gaps[3];
                aboveMeasurePerRingAndPos[4][count] = aboveMeasure.gaps[4];
                        
                if(count == 0){
                    closestMeasure = aboveMeasure;
                }else{
                    
                    long oldDiff = Math.abs(closestMeasure.date.getTimeInMillis() - belowMeasure.date.getTimeInMillis());
                    long newDiff = Math.abs(aboveMeasure.date.getTimeInMillis() - belowMeasure.date.getTimeInMillis());
                    
                    if(newDiff < oldDiff){
                        closestMeasure = aboveMeasure;
                    }
                }
                
                count++;
            }
            
            computedMeasures.add(new ComputedMeasure(closestMeasure, belowMeasure));
        }
        
        /****Merging values****/
        float[][] transmittances = new float[5][computedMeasures.size()];
        
        for(int j=0;j<computedMeasures.size();j++){
            
            ComputedMeasure measure = computedMeasures.get(j);
            
            for(int i=0;i<5;i++){
                transmittances[i][j] = measure.computeRatio(i);
            }
        }
        
        /****LAI computation****/
        
        //contact number
        float[][] contactNumberByRingAndPos = new float[5][computedMeasures.size()];
        
        for(int i=0;i<5;i++){
            
            for(int j=0;j<transmittances[i].length;j++){
                float contactNumber = (float) (-Math.log(transmittances[i][j]) / instance.rings[i].getDist());
                contactNumberByRingAndPos[i][j] = contactNumber;
            }
        }
        
        //compute LAI
        
        ;
        
        float[] laiArray = new float[computedMeasures.size()];
        float laiSum = 0;
        
        for(int j=0;j<computedMeasures.size();j++){
            
            /*float lai = 0.0f;
            
            for(int i=0;i<5;i++){
                lai += contactNumberByRingAndPos[i][j] * instance.rings[i].getWeightingFactor();
            }
            
            lai *= 2;
            laiArray[j] = lai;
            laiSum += lai;*/
            
            laiArray[j] = instance.computeLAIForOneMeasure(
                    new double[]{instance.computeContactNumber(transmittances[0][j], 0),
                                instance.computeContactNumber(transmittances[1][j], 1),
                                instance.computeContactNumber(transmittances[2][j], 2),
                                instance.computeContactNumber(transmittances[3][j], 3),
                                instance.computeContactNumber(transmittances[4][j], 4)});
        }
        
        laiSum /= computedMeasures.size();
        
        instance.initPositions(computedMeasures.size());
        
        File computedValuesFromFV2200 = new File(LAI2xxxTest.class.getResource("/lai2200/1609-PM.txt").toURI());
        BufferedReader reader = new BufferedReader(new FileReader(computedValuesFromFV2200));
        
        reader.readLine();
        
        String line;
        
        
        int count = 0;
        while((line = reader.readLine()) != null){
            
            String[] values = line.split("\t");
            
            assertEquals((long)Long.valueOf(values[0]), (long)computedMeasures.get(count).belowMeasure.id);
            
            //test LAI
            assertEquals(Double.valueOf(values[2]), laiArray[count], 0.01);
            
            //test gap 1
            assertEquals(Double.valueOf(values[3]), computedMeasures.get(count).computeRatio(0), 0.000001);
            instance.addTransmittance(0, count, computedMeasures.get(count).computeRatio(0));
            
            //test gap 2
            assertEquals(Double.valueOf(values[4]), computedMeasures.get(count).computeRatio(1), 0.000001);
            instance.addTransmittance(1, count, computedMeasures.get(count).computeRatio(1));
            
            //test gap 3
            assertEquals(Double.valueOf(values[5]), computedMeasures.get(count).computeRatio(2), 0.000001);
            instance.addTransmittance(2, count, computedMeasures.get(count).computeRatio(2));
            
            //test gap 4
            assertEquals(Double.valueOf(values[6]), computedMeasures.get(count).computeRatio(3), 0.000001);
            instance.addTransmittance(3, count, computedMeasures.get(count).computeRatio(3));
            
            //test gap 5
            assertEquals(Double.valueOf(values[7]), computedMeasures.get(count).computeRatio(4), 0.000001);
            instance.addTransmittance(4, count, computedMeasures.get(count).computeRatio(4));
            
            
            
            count++;
            
        }
        
        reader.close();
        
        instance.computeValues();
        
        float skyDIFN = instance.computeSkyDIFN(aboveMeasurePerRingAndPos, instance.gapsByRing);
        assertEquals(0.013, skyDIFN, 0.001);
        
        assertEquals(0.011, instance.DIFN, 0.001);
        //assertEquals(0.975, instance.acf, 0.1);
        assertEquals(0.08, instance.sel, 0.01);
        assertEquals(5.92, instance.LAI, 0.001);
        
        assertArrayEquals(new float[]{0.022f,0.023f,0.015f,7.5e-003f,2.5e-003f}, instance.avgTransByRing, 0.01f);
        assertArrayEquals(new float[]{0.924f,0.955f,0.981f,0.985f,0.980f}, instance.acfsByRing, 0.01f);
        assertArrayEquals(new float[]{4.077f,3.635f,3.371f,2.990f,2.286f}, instance.contactNumberByRing, 0.01f);
        assertArrayEquals(new float[]{0.750f,0.530f,0.319f,0.235f,0.189f}, instance.stdevByRing, 0.1f);
        assertArrayEquals(new float[]{0.016f,0.019f,0.014f,6.9e-003f,2.2e-003f}, instance.gapsByRing, 0.01f);
        
        System.out.println("test");
    }
    
    private List<Measure> getMeasuresFromFile(File file, String requiredType) throws FileNotFoundException, IOException, ParseException{
        
        List<Measure> measures = new ArrayList<>();
        
        SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
        
        BufferedReader reader = new BufferedReader(new FileReader(file));
        
        String line;
        
        while((line = reader.readLine()) != null){
            
            if(line.equals("### Observations")){
                
                while((line = reader.readLine()) != null){
                    
                    String[] measure = line.split("\t");
                    
                    if(measure.length < 9){
                        break;
                    }
                    
                    Date date = simpleDateFormat.parse(measure[2]);

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);
                    
                    if(measure[0].equals(requiredType)){
                        measures.add(new Measure(measure[0],
                            Integer.valueOf(measure[1]),
                            calendar,
                            measure[3],
                            Float.valueOf(measure[4]),
                            Float.valueOf(measure[5]),
                            Float.valueOf(measure[6]),
                            Float.valueOf(measure[7]),
                            Float.valueOf(measure[8])));
                    }
                }
            }
        }
        
        reader.close();
        
        return  measures;
    }
    
}
