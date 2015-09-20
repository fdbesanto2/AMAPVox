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

package fr.ird.voxelidar.transmittance.lai2xxx;

import fr.ird.voxelidar.transmittance.util.SphericalCoordinates;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Vector3f;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
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
    protected float[] azimuthAngles;
    protected float[] elevationAngles;
    protected int[] ringOffsetID;
    
    private int[] shotNumberByRing;
    
    protected float[] avgTransByRing;
    protected float[] meanContactNumber;
    protected float[] gapsByRing;
    
    protected ViewCap viewCap;
    
    /**
     *
     * @param shotNumber
     * @param viewCap
     * @param rings
     */
    protected LAI2xxx(int shotNumber, ViewCap viewCap, Ring... rings){
        
        this.directionNumber = shotNumber;
        this.viewCap = viewCap;
        this.rings = rings;
        this.avgTransByRing = new float[rings.length];
        this.gapsByRing = new float[rings.length];
        this.meanContactNumber = new float[rings.length];
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
    }
    
    public void addTransmittance(int ringID, float transmittance){
        
        if(!Float.isNaN(transmittance)){
            avgTransByRing[ringID] += transmittance;
            
            float logTransmittance = (float) Math.log(transmittance);
            gapsByRing[ringID] += logTransmittance;
            /*
            float pathLength = 0; // à déterminer
            meanContactNumber[ringID] += logTransmittance/pathLength;*/
        }
    }
    
    public float getAVGTransByRing(int ringID){
        
        int count = positionNumber*directionNumber;
        
        return avgTransByRing[ringID] /= count;
    }
    
    public float getGapsByRing(int ringID){
        
        int count = positionNumber*directionNumber;
        
        return (float) Math.exp(gapsByRing[ringID] /= count);
    }
    
    public float getLAI(){
        
        float sum = 0;
        /*
        for(int i=0;i<rings.length;i++){
            
            float ki = 
            float wi = rings[i].getWeightingFactor();
            
            sum += ki * wi;
        }*/
        
        return 2*sum;
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
            float solidAnglePercentage = rings[i].getSolidAngle()/solidAngleSum;
            
            shotNumberByRing[i] = (int) Math.ceil(solidAnglePercentage * directionNumber);
            rings[i].setNbDirections(shotNumberByRing[i]);
        }
        
        //paramètres définissant le balayage incrémentielle d'un angle donné
        RingInformation[] ringInformations = new RingInformation[rings.length];
        
        for (int i=0;i<rings.length;i++) {
            
            Ring ring = rings[i];
            
            ringInformations[i] = new RingInformation(
                    ring.getUpperZenithalAngle() + (ring.getLowerZenithalAngle() - ring.getUpperZenithalAngle()) * 0.25f,
                    ring.getUpperZenithalAngle() + (ring.getLowerZenithalAngle() - ring.getUpperZenithalAngle()) * 0.75f,
                    360/(float)shotNumberByRing[i]);
            
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
            
            boolean lowAngle = false;
            
            //pour tous les tirs d'une plage angulaire
            for (int s=0;s<shotNumberByRing[i];s++){
                
                //view cap filtrage
                if(azimuthalAngle < minAzimuthAngle && azimuthalAngle > maxAzimuthAngle){
                    //on filtre
                }else{
                    
                    if(lowAngle){
                        elevationAngle = ringInformation.getLowerShotAngle();
                    }else{
                        elevationAngle = ringInformation.getUpperShotAngle();
                    }
                    
                    azimuthAnglesList.add(azimuthalAngle);
                    elevationAnglesList.add(elevationAngle);

                    SphericalCoordinates sphericalCoordinates = new SphericalCoordinates(
                            (float)Math.toRadians(azimuthalAngle), (float)Math.toRadians(elevationAngle));

                    directionList.add(sphericalCoordinates.toCartesian());
                }
                
                azimuthalAngle += ringInformations[i].getAzimuthalStepAngle();
                
            }
        }
        
        directions = new Vector3f[directionList.size()];
        azimuthAngles = new float[azimuthAnglesList.size()];
        elevationAngles = new float[elevationAnglesList.size()];
        
        for(int i=0;i<directionList.size();i++){
            
            directions[i] = directionList.get(i);
            azimuthAngles[i] = azimuthAnglesList.get(i);
            elevationAngles[i] = elevationAnglesList.get(i);
        }
        
    }

    public Vector3f[] getDirections() {
        return directions;
    }

    public float[] getAzimuthAngles() {
        return azimuthAngles;
    }

    public float[] getElevationAngles() {
        return elevationAngles;
    }  
    
    public int getRingNumber(){
        return rings.length;
    }
    
    public int getRingIDFromDirectionID(int directionID){
        
        for(int i=0;i<ringOffsetID.length;i++){
            
            if(i+1 < ringOffsetID.length){
                if(directionID >= ringOffsetID[i] && directionID <= ringOffsetID[i+1]){
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

        public RingInformation(float lowerShotAngle, float upperShotAngle, float azimuthalStepAngle) {
            this.lowerShotAngle = lowerShotAngle;
            this.upperShotAngle = upperShotAngle;
            this.azimuthalStepAngle = azimuthalStepAngle;
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
    
    public abstract void writeOutput(File outputFile);
}
