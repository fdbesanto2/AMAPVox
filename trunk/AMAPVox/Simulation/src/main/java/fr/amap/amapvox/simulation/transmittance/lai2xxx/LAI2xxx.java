/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package fr.amap.amapvox.simulation.transmittance.lai2xxx;

import fr.amap.amapvox.commons.util.SphericalCoordinates;
import fr.amap.amapvox.jeeb.workspace.sunrapp.light.IncidentRadiation;
import fr.amap.amapvox.jeeb.workspace.sunrapp.light.SolarRadiation;
import fr.amap.amapvox.jeeb.workspace.sunrapp.util.Time;
import fr.amap.amapvox.simulation.transmittance.SimulationPeriod;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Level;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import org.apache.log4j.Logger;

/**
 * <p>This class compute lai2000-2200 values based on the lai-2200 manual</p>
 * @see <a href=http://www.licor.co.za/manuals/LAI-2200_Manual.pdf>http://www.licor.co.za/manuals/LAI-2200_Manual.pdf</a>
 * @author Julien Heurtebize
 */


public abstract class LAI2xxx {
    
    protected final static Logger logger = Logger.getLogger(LAI2xxx.class);
    
    /**
     *
     */
    protected int directionNumber;
    protected int positionNumber;

    /**
     *
     */
    protected final Ring[] rings;

    /**
     *
     */
    protected Vector3f[] directions;
    protected int[] ringOffsetID;
    
    private int[] shotNumberByRing;
    protected float[] avgTransByRing;
    protected float[] meanContactNumber;
    protected float[] gapsByRing;
    protected float[][] gapsByRingAndPosition;
    protected float[] contactNumberByRing;
    
    /**
     * standard deviation of the contact numbers for each ring
     */
    protected float[] stdevByRing;
    
    /**
     * apparent clumping factor for each ring
     */
    protected float[] acfsByRing;
    
    public float[][] transmittances;
    
    //test
    public float[][] normalizedTransmittances;
    public float[][] pathLengths;
    protected int[][] countByPositionAndRing2;
    
    protected float[] byPosition_LAI;
    protected float global_LAI; //lai for all positions
    protected float acf;
    
    protected ViewCap viewCap;
    
    protected int[][] countByPositionAndRing;
    
    /**
     * Create a LAI2000-2200 device
     * @param shotNumber
     * @param viewCap
     * @param rings
     */
    protected LAI2xxx(int shotNumber, ViewCap viewCap, Ring... rings){
        
        this.directionNumber = shotNumber;
        this.viewCap = viewCap;
        this.avgTransByRing = new float[rings.length];
        this.gapsByRing = new float[rings.length];
        this.meanContactNumber = new float[rings.length];
        
        this.rings = rings;
        
        /****normalize weighting factor to sum to 1****/
        float residualWeigtingFactor = 0;
        int nbOfMaskedRings = 0;
        
        for(Ring ring : rings){
            if(ring.isMasked()){
                residualWeigtingFactor += ring.getWeightingFactor();
                nbOfMaskedRings++;
            }
        }
        
        int nbRingsLeft = rings.length-nbOfMaskedRings;
                
        for(Ring ring : rings){
            if(!ring.isMasked()){
                ring.setWeightingFactor(ring.getWeightingFactor()+(residualWeigtingFactor / nbRingsLeft));
            }
        }
    }
    
    public enum ViewCap{
        
        CAP_360(360),
        CAP_270(270),
        CAP_180(180),
        CAP_90(90),
        CAP_45(45);
        
        private final float viewCap;
        
        private ViewCap(float angle){
            this.viewCap = angle;
        }

        public float getViewCap() {
            return viewCap;
        }
    }
    
    public void initPositions(int positionNumber){
        
        this.positionNumber = positionNumber;
        
        transmittances = new float[rings.length][positionNumber];
        countByPositionAndRing = new int[positionNumber][rings.length];
        
        //test
//        normalizedTransmittances = new float[rings.length][positionNumber];
//        pathLengths = new float[rings.length][positionNumber];
//        countByPositionAndRing2 = new int[positionNumber][rings.length];
    }
    
    public void addNormalizedTransmittance(int ringID, int position, float transmittance, float pathLength){
        
        if(ringID < rings.length && position < positionNumber){
            normalizedTransmittances[ringID][position] += transmittance;
            countByPositionAndRing2[position][ringID] ++;
            pathLengths[ringID][position] += pathLength;
        }
    }
    
    public void addTransmittance(int ringID, int position, float transmittance){
        
        if(ringID < rings.length && position < positionNumber){
            transmittances[ringID][position] += transmittance;
            countByPositionAndRing[position][ringID] ++;
        }
        
        /*if(!Float.isNaN(transmittance)){
            avgTransByRing[ringID] += transmittance;
        }*/
    }
    
    /**
     *
     */
    public void computeDirections(){
        
        //somme des angles solides
        float solidAngleSum = 0;
        
        for(Ring ring : rings){
            solidAngleSum += ring.getSolidAngle();
        }
        
        //nombre de tirs par ring
        shotNumberByRing = new int[rings.length];
        
        for(int i=0;i<rings.length;i++){
            
            //pourcentage d'angle solide
            float solidAnglePercentage = (rings[i].getSolidAngle()/solidAngleSum);
            
            shotNumberByRing[i] = (int) Math.ceil(solidAnglePercentage * directionNumber);
        }
        
        int nbDirectionForOneRing = (directionNumber/5);
        
        for(int i=0;i<rings.length;i++){
            
           shotNumberByRing[i] = (shotNumberByRing[i] + nbDirectionForOneRing)/2;
           rings[i].setNbDirections(shotNumberByRing[i]);
           logger.info("Nb shots ring "+(i+1)+" = "+shotNumberByRing[i]);
        }
        
        int nbSubRings=3;
        switch(directionNumber){
            case 500:
                nbSubRings = 4;
                break;
            case 4000:
                nbSubRings = 8;
                break;
            case 10000:
                nbSubRings = 12;
                break;
            default:
                if(directionNumber > 4000){
                    nbSubRings = 6;
                }else if(directionNumber < 500){
                    nbSubRings = 3;
                }
                
        }
        
        
        //paramètres définissant le balayage incrémentielle d'un angle donné
        RingInformation[] ringInformations = new RingInformation[rings.length];
        
        for (int i=0;i<rings.length;i++) {
            
            Ring ring = rings[i];
            
            ringInformations[i] = new RingInformation(
                    ring.getLowerZenithalAngle(),
                    ring.getUpperZenithalAngle(),
                    360/(float)shotNumberByRing[i], shotNumberByRing[i], rings[i].getSolidAngle());
                    
            /*ringInformations[i] = new RingInformation(
                    ring.getUpperZenithalAngle() + (ring.getLowerZenithalAngle() - ring.getUpperZenithalAngle()) * 0.25f,
                    ring.getUpperZenithalAngle() + (ring.getLowerZenithalAngle() - ring.getUpperZenithalAngle()) * 0.75f,
                    360/(float)shotNumberByRing[i]);*/
            
        }
        
        List<Vector3f> directionList = new ArrayList<>();
        List<Float> azimuthAnglesList = new ArrayList<>();
        List<Float> elevationAnglesList = new ArrayList<>();
        
        //view cap: calcul des bornes azimutales
        float viewCapAngle = viewCap.getViewCap();
        float minAzimuthAngle = 180 + ((360 - viewCapAngle)/2.0f);
        float maxAzimuthAngle = 180 - ((360 - viewCapAngle)/2.0f);
        
        ringOffsetID = new int[rings.length];
        
        //pour chaque plage angulaire
        for (int i=0;i<rings.length;i++) {
            
            ringOffsetID[i] = directionList.size();
            
            RingInformation ringInformation = ringInformations[i];
                    
            float azimuthalAngle = 0;
            float elevationAngle;
            float azimuthalOffset = 0;
            
            for (int j=0;j<ringInformation.nbSubRings;j++){
                
                if(ringInformation.subRingsSamplingRate[j] != 0){
                    
                    float azimuthalStep = 360/(float)ringInformation.subRingsSamplingRate[j];
                    
                    azimuthalAngle = azimuthalOffset;
                
                    for (int s=0;s<ringInformation.subRingsSamplingRate[j];s++){

                        elevationAngle = ringInformation.subRingsAngles[j];
                        azimuthAnglesList.add(azimuthalAngle);
                        elevationAnglesList.add(elevationAngle);

                        SphericalCoordinates sphericalCoordinates = new SphericalCoordinates(
                                (float)Math.toRadians(azimuthalAngle), (float)Math.toRadians(elevationAngle));

                        Point3d toCartesian = sphericalCoordinates.toCartesian();
                        directionList.add(new Vector3f(toCartesian));

                        azimuthalAngle += azimuthalStep;
                    }

                    azimuthalOffset += (azimuthalStep/2.0f);
                }
                
            }
            
//            boolean lowAngle = false;
//            
//            
//            
//            //pour tous les tirs d'une plage angulaire
//            for (int s=0;s<shotNumberByRing[i];s++){
//                
//                //view cap filtrage
//                if(azimuthalAngle < minAzimuthAngle && azimuthalAngle > maxAzimuthAngle){
//                    //on filtre
//                }else{
//                    
//                    if(lowAngle){ //alternate lower angle and upper angle
//                        elevationAngle = ringInformation.getLowerShotAngle();
//                        lowAngle = false;
//                    }else{
//                        elevationAngle = ringInformation.getUpperShotAngle();
//                        lowAngle = true;
//                    }
//                    
//                    azimuthAnglesList.add(azimuthalAngle);
//                    elevationAnglesList.add(elevationAngle);
//
//                    SphericalCoordinates sphericalCoordinates = new SphericalCoordinates(
//                            (float)Math.toRadians(azimuthalAngle), (float)Math.toRadians(elevationAngle));
//
//                    directionList.add(sphericalCoordinates.toCartesian());
//                }
//                
//                azimuthalAngle += ringInformations[i].getAzimuthalStepAngle();
//                
//            }
        }
        
        directions = new Vector3f[directionList.size()];
        
        for(int i=0;i<directionList.size();i++){
            
            directions[i] = directionList.get(i);
        }
        
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/home/calcul/Documents/Julien/Test_transmittance_marilyne/directions.obj")));
//            
//            float length = 100;
//            for(Vector3f direction : directions){
//                
//                float x1 = 0, y1 = 0, z1 = 0;
//                
//                float x2 = x1+direction.x*length;
//                float y2 = y1+direction.y*length;
//                float z2 = z1+direction.z*length;
//                
//                writer.write("v "+ x1+" "+y1+" "+z1+"\n");
//                writer.write("v "+ x2+" "+y2+" "+z2+"\n");
//            }
//            
//            writer.close();
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(LAI2xxx.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }

    public Vector3f[] getDirections() {
        return directions;
    }
    
    public int getRingNumber(){
        return rings.length;
    }
    
    public void computeValues(){
        
        
        
        for(int j=0;j<positionNumber;j++){
            for(int i=0;i<rings.length;i++){
                
                transmittances[i][j] /= (float)countByPositionAndRing[j][i];
                
//                //test
//                normalizedTransmittances[i][j] /= (float)countByPositionAndRing2[j][i];
//                pathLengths[i][j] /= (float)countByPositionAndRing2[j][i];
            }
        }
        
        //compute avgtrans
        logger.info("Computation of AVGTRANS...");
        int[] countByRing = new int[rings.length];
        
        //int[][] countByPositionAndRing = new int[positionNumber][rings.length];
        avgTransByRing = new float[rings.length];
        
        float[][] avgTransByPosAndRing = new float[positionNumber][rings.length];
            
        for(int j=0;j<positionNumber;j++){
            for(int i=0;i<rings.length;i++){
                //for(int d=0;d<directionNumber;d++){

                //on détermine ici quel est le ring concerné
                //int i = getRingIDFromDirectionID(d);
                
                if(!Float.isNaN(transmittances[i][j])){
                    avgTransByRing[i] += transmittances[i][j];
                    avgTransByPosAndRing[j][i] += transmittances[i][j];
                    countByRing[i]++;
                    countByPositionAndRing[j][i]++;
                }
            //}
            }
            
        }
        
        //moyenne
        for(int i=0;i<rings.length;i++){
            avgTransByRing[i] /= countByRing[i];
        }
        
        for(int j=0;j<positionNumber;j++){
            for(int i=0;i<rings.length;i++){
                avgTransByPosAndRing[j][i] /= countByPositionAndRing[j][i];
            }
        }
        
        //compute contact numbers (CNTC#)
        logger.info("Computation of CNTC#...");
        contactNumberByRing = new float[rings.length]; //Ki
        
        float[][] meanContactNumberByPositionAndRing = new float[positionNumber][rings.length]; //Ki
        
        //contact value for the pair for each ring (Kij)
        float[][] contactValueForPairByRing = new float[rings.length][positionNumber];

        countByRing = new int[rings.length];
        countByPositionAndRing = new int[positionNumber][rings.length];

        //calcul des indices K
        for(int j=0;j<positionNumber;j++){
            
            for(int i=0;i<5;i++){

                float pathLength = rings[i].getDist();
                
                if(transmittances[i][j] != 0){
                    
                    contactValueForPairByRing[i][j] = (float) (-Math.log(transmittances[i][j]) / pathLength);
                    
                    float contactNumber = (float) (-Math.log(transmittances[i][j]) / pathLength);
                    contactNumberByRing[i] += contactNumber;
                    meanContactNumberByPositionAndRing[j][i] += contactNumber;
                    
                    countByRing[i]++;
                    countByPositionAndRing[j][i]++;
                }else{
                    contactValueForPairByRing[i][j] = Float.NaN;
                }
            }
        }
        
        for(int i=0;i<rings.length;i++){
            contactNumberByRing[i] /= countByRing[i];
        }
        
        //mean contact number for each position
        for(int j=0;j<positionNumber;j++){
            for(int i=0;i<rings.length;i++){
                meanContactNumberByPositionAndRing[j][i] /= countByPositionAndRing[j][i];
            }
        }
        
        //compute global_LAI
        logger.info("Computation of LAI...");
        global_LAI = 0.0f;

        for(int i=0;i<rings.length;i++){
            if(!rings[i].isMasked()){
                global_LAI += contactNumberByRing[i] * rings[i].getWeightingFactor();
            }
        }

        global_LAI *= 2;
        
        //compute one LAI by position
        byPosition_LAI = new float[positionNumber];
        
        for(int j=0;j<positionNumber;j++){
            
            float position_LAI = 0;
            
            for(int i=0;i<rings.length;i++){
                if(!rings[i].isMasked()){
                    position_LAI += meanContactNumberByPositionAndRing[j][i] * rings[i].getWeightingFactor();
                }
            }
            
            position_LAI *= 2;
            byPosition_LAI[j] = position_LAI;
        }
        
        
        //compute Gaps
        logger.info("Computation of GAPS...");
        gapsByRing = new float[rings.length]; //Ki
        gapsByRingAndPosition = new float[rings.length][positionNumber];

        countByRing = new int[rings.length];
        countByPositionAndRing = new int[positionNumber][rings.length];

        //calcul des indices K
        for(int j=0;j<positionNumber;j++){
            for(int i=0;i<5;i++){
                
                if(transmittances[i][j] != 0){
                    
                    double gap = Math.log(transmittances[i][j]);
                    
                    gapsByRing[i] += gap;
                    gapsByRingAndPosition[i][j] += gap;
                    
                    countByRing[i]++;
                    countByPositionAndRing[j][i]++;
                }
            }
        }
        
        //moyenne des ln des transmittances pour chaque ring
        float[] meanLnTransByring = new float[rings.length];
        
        for(int i=0;i<rings.length;i++){
            gapsByRing[i] /= countByRing[i];
            meanLnTransByring[i] = gapsByRing[i];
            gapsByRing[i] = (float) Math.exp(gapsByRing[i]);
        }
        
        //moyenne des ln des transmittances pour chaque ring et position
        for(int j=0;j<positionNumber;j++){
            for(int i=0;i<rings.length;i++){
                
                gapsByRingAndPosition[i][j] /= countByPositionAndRing[j][i];
                gapsByRingAndPosition[i][j] = (float) Math.exp(gapsByRingAndPosition[i][j]);
            }
        }
        
        //compute acfs
        logger.info("Computation of ACFS...");
        acfsByRing = new float[rings.length];
        for(int i=0;i<rings.length;i++){
            acfsByRing[i] = (float) (Math.log(avgTransByRing[i]) / meanLnTransByring[i]);
        }
        
        //compute acf
        logger.info("Computation of ACF...");
        
        float numerateur = 0;
        float denominateur = 0;
        
        for(int i=0;i<rings.length;i++){
            numerateur += -(Math.log(avgTransByRing[i])/rings[i].getDist()) * rings[i].getWeightingFactor();
            denominateur += contactNumberByRing[i] * rings[i].getWeightingFactor();
        }
        
        numerateur *= 2;
        denominateur *= 2;
        
        acf = numerateur/denominateur;
        
        //compute STDEV
        logger.info("Computation of STDEV...");
        
        stdevByRing = new float[rings.length];
        countByRing = new int[rings.length];
        
        for(int i=0;i<rings.length;i++){
            for(int j=0;j<positionNumber;j++){
                if(!Float.isNaN(contactValueForPairByRing[i][j])){
                    stdevByRing[i] += Math.pow(contactValueForPairByRing[i][j] - contactNumberByRing[i], 2);
                    countByRing[i]++;
                }
            }
        }
        
        for(int i=0;i<rings.length;i++){
            stdevByRing[i] = (float) Math.sqrt((1/(float)(countByRing[i]-1))*stdevByRing[i]);
        }
        
    }
    
    public int getRingIDFromDirectionID(int directionID){
        
        for(int i=0;i<ringOffsetID.length;i++){
            
            if(i+1 < ringOffsetID.length){
                if(directionID >= ringOffsetID[i] && directionID < ringOffsetID[i+1]){
                    return i;
                }
            }else{
                return i;
            }
        }
        
        return -1;
    }
    
    private class RingInformation{
        
        /**
        * angle tirs bas
        */
        private final float lowerShotAngle;
        
        /**
        * angle tirs haut
        */
        private final float upperShotAngle;
        
        /**
        * pas angulaire azimuthal
        */
        private final float azimuthalStepAngle;
        
        public final float[] subRingsAngles;
        public final float[] subRingsSolidAngles;
        public final int[] subRingsSamplingRate;
        public int nbSubRings = 0;

        public RingInformation(float lowerShotAngle, float upperShotAngle, float azimuthalStepAngle, int nbShots, float solidAngle) {
            
            
            float zenitalStep = (float) (Math.toRadians(lowerShotAngle) - Math.toRadians(upperShotAngle));
            float petitOmega = solidAngle/(float)nbShots;
            float twoAlpha = (float) Math.sqrt(petitOmega);
            //float alpha = (float) Math.acos(1-(petitOmega/(2*Math.PI)));
            nbSubRings = (int)(((zenitalStep) / (twoAlpha))+0.5);
            
            //float alpha = (float) Math.acos(1- ((solidAngle/(float)nbShots)/(2*Math.PI)));
            //nbSubRings = (int) (((Math.toRadians(lowerShotAngle) - Math.toRadians(upperShotAngle)) / (2 * alpha))+0.5);
                    
            this.lowerShotAngle = lowerShotAngle;
            this.upperShotAngle = upperShotAngle;
            this.azimuthalStepAngle = azimuthalStepAngle;
            this.subRingsAngles = new float[nbSubRings];
            this.subRingsSolidAngles = new float[nbSubRings];
            this.subRingsSamplingRate = new int[nbSubRings];
            
            float oldUpperSubRingAngle = upperShotAngle;
            float step = (lowerShotAngle - upperShotAngle)/nbSubRings;
            
            for(int i=0;i<subRingsAngles.length;i++){
                
                float upperSubRingAngle = oldUpperSubRingAngle;
                
                float lowerSubRingAngle = upperSubRingAngle + step;
                oldUpperSubRingAngle = lowerSubRingAngle;
                
                subRingsAngles[i] = (lowerSubRingAngle + upperSubRingAngle)/2;
                subRingsSolidAngles[i] = (float) (2* Math.PI * (Math.cos(Math.toRadians(upperSubRingAngle)) - Math.cos(Math.toRadians(lowerSubRingAngle))));
                subRingsSamplingRate[i] = (int) (((subRingsSolidAngles[i] / solidAngle)*nbShots)+0.5);
            }
        }
        public float getLowerShotAngle() {
            return lowerShotAngle;
        }

        public float getUpperShotAngle() {
            return upperShotAngle;
        }

        public float getAzimuthalStepAngle() {
            return azimuthalStepAngle;
        }
    }
    
    public Ring getRing(int ringID){
        return rings[ringID];
    }

    public float[] getByPosition_LAI() {
        return byPosition_LAI;
    }    

    public float[][] getGapsByRingAndPosition() {
        return gapsByRingAndPosition;
    }    

//    //test
//    public float[][] getNormalizedTransmittances() {
//        return normalizedTransmittances;
//    }
//    //test
//    public float[][] getPathLengths() {
//        return pathLengths;
//    }
    
    public abstract void writeOutput(File outputFile);
}
