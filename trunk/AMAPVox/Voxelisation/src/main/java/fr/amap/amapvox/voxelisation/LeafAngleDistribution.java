/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxelisation;

import org.apache.commons.math3.distribution.BetaDistribution;

/**
 *
 * @author calcul
 */
public class LeafAngleDistribution {

    public enum Type {

        UNIFORM(0),
        SPHERIC(1),
        ERECTOPHILE(2),
        PLANOPHILE(3),
        EXTREMOPHILE(4),
        PLAGIOPHILE(5),
        HORIZONTAL(6),
        VERTICAL(7),
        ELLIPSOIDAL(8),
        ELLIPTICAL(9),
        TWO_PARAMETER_BETA(10);

        private final int type;

        private Type(int type) {
            this.type = type;
        }
        
        @Override
        public String toString(){
            
            switch(type){
                case 0:
                    return "Uniform";
                case 1:
                    return "Spherical";
                case 2:
                    return "Erectophile";
                case 3:
                    return "Planophile";
                case 4:
                    return "Extremophile";
                case 5:
                    return "Plagiophile";
                case 6:
                    return "Horizontal";
                case 7:
                    return "Vertical";
                case 8:
                    return "Ellipsoidal";
                case 9:
                    return "Elliptical";
                case 10:
                    return "Two-parameter beta distribution";
                default:
                    return "Unknown";
            }
        }
        
        public static Type fromString(String value){
            
            switch(value){
                case "Uniform":
                    return Type.UNIFORM;
                case "Spherical":
                    return Type.SPHERIC;
                case "Erectophile":
                    return Type.ERECTOPHILE;
                case "Planophile":
                    return Type.PLANOPHILE;
                case "Extremophile":
                    return Type.EXTREMOPHILE;
                case "Plagiophile":
                    return Type.PLAGIOPHILE;
                case "Horizontal":
                    return Type.HORIZONTAL;
                case "Vertical":
                    return Type.VERTICAL;
                case "Ellipsoidal":
                    return Type.ELLIPSOIDAL;
                case "Elliptical":
                    return Type.ELLIPTICAL;
                case "Two-parameter beta distribution":
                    return Type.TWO_PARAMETER_BETA;
                default:
                    return Type.SPHERIC;
            }
        }
    }
    
    
    private Type type;
    private BetaDistribution distribution;

    public LeafAngleDistribution(Type type, double... params) {

        this.type = type;
        
        switch (type) {

            case TWO_PARAMETER_BETA:

                if (params.length > 1) {
                    setupBetaDistribution(params[0], params[1]);
                }

                break;
        }

    }

    private void setupBetaDistribution(double param1, double param2) {

        double alphamean = Math.toRadians(param1);
        double tvar = param2;
        double tmean = 2 * (alphamean / Math.PI);
        double sigma0 = tmean * (1 - tmean);

        double v = tmean * ((sigma0 / tvar) - 1);
        double m = (1 - tmean) * ((sigma0 / tvar) - 1);

        distribution = new BetaDistribution(null, m, v);
    }

    /**
     *
     * @param angle must be in radians from in [0,2pi]
     * @return pdf function
     */
    public double getDensityProbability(double angle) {
        
        double density = 0;
        
        double tmp = Math.PI/2.0;
        if(angle == tmp){
            angle = tmp-0.000001;
        }
        
        switch(type){
            
            case PLANOPHILE:
                density = (2.0/Math.PI) * (1-Math.cos(2 * angle));
                break;
            case ERECTOPHILE:
                density = (2.0/Math.PI) * (1+Math.cos(2 * angle));
                break;
            case PLAGIOPHILE:
                density = (2.0/Math.PI) * (1-Math.cos(4 * angle));
                break;
            case EXTREMOPHILE:
                density = (2.0/Math.PI) * (1+Math.cos(4 * angle));
                break;
            case SPHERIC:
                density = Math.sin(angle);
                break;
            case UNIFORM:
                density = 2.0/Math.PI;
                break;
            case HORIZONTAL:
                break;
            case VERTICAL:
                break;
            case ELLIPTICAL:
                break;
            case ELLIPSOIDAL:
                break;
            case TWO_PARAMETER_BETA:
                
                double te = angle / (Math.PI / 2.0);
                density = distribution.density(te);
        
                break;
        }
        

        return density;
    }

    public Type getType() {
        return type;
    }
}
