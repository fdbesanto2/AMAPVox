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
    protected final int shotNumber;

    /**
     *
     */
    protected Ring[] rings;

    /**
     *
     */
    protected Vector3f[] directions;
    protected float[] azimuthAngles;
    protected float[] elevationAngles;
    protected int[] ringOffsetID;
    
    //protected float[] transmittances;
    private int[] shotNumberByRing;
    
    protected final ViewCap viewCap;
    
    /**
     *
     * @param shotNumber
     * @param viewCap
     */
    public LAI2xxx(int shotNumber, ViewCap viewCap){
        this.shotNumber = shotNumber;
        this.viewCap = viewCap;
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
            
            shotNumberByRing[i] = (int) Math.ceil(solidAnglePercentage * shotNumber);
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
            
            if(directionID >= ringOffsetID[i] && directionID <= ringOffsetID[i]){
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
    
    
    /**
     *
     */
    protected abstract void initRings();
    protected abstract void writeOutput(File outputFile);
}
