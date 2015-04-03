/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.raytracing.voxel;

import java.lang.reflect.Field;
import java.util.Set;

/**
 *
 * @author calcul
 */
public class ALSVoxel extends Voxel{
    
    //public double bvOutgoing = 0;
    public double bvEntering = 0;
    public double bvIntercepted = 0;
    public double sumSurfaceMultiplyLength = 0;
    public double PadBVTotal = 0;
    public double PadBVTotal_V2 = 0;
    
    private static final Set<Field> _fields = Voxel.getFields(ALSVoxel.class);
        
    public ALSVoxel(int i, int j, int k) {
        super(i, j, k);
    }

    
    public static String getHeader(){
            
            String header = "";
            
            for (String fieldName : fieldsNames) {
                
                if(!fieldName.startsWith("_")){
                    
                    if(fieldName.startsWith("$")){
                        header += fieldName.substring(1)+" ";
                    }else{
                        header += fieldName+" ";
                    }
                    
                }
            }
            
            header = header.trim();
            
            return header;
        }
        

    @Override
    public String toString() {

        String voxelString = "";

        // compare values now
        for (Field _field : _fields) {

            String fieldName = _field.getName();

            if (!fieldName.startsWith("_")) {

                try {
                    Object newObj = _field.get(this);
                    voxelString += newObj + " ";
                }catch (IllegalArgumentException | IllegalAccessException ex) {
                    _logger.error(ex);
                }
            }
        }

        voxelString = voxelString.trim();
        return voxelString;
        /*
        return sb.append(i).append(" ").
                append(j).append(" ").
                append(k).append(" ").
                append(bfEntering).append(" ").
                append(bfIntercepted).append(" ").
                append(bsEntering).append(" ").
                append(bsIntercepted).append(" ").
                append(Lg_Exiting).append(" ").
                append(Lg_NoInterception).append(" ").
                append(PadBF).append(" ").
                append(PadBS).append(" ").
                append(ground_distance).append(" ").
                append(nbSampling).append(" ").
                append(nbEchos).append(" ").
                append(nbOutgoing).append(" ").
                append(LMean_Exiting).append(" ").
                append(angleMean).append(" ").toString();
                */
    }
}
