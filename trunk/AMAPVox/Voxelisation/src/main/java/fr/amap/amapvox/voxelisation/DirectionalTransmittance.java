/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxelisation;

import static fr.amap.amapvox.voxelisation.LeafAngleDistribution.Type.SPHERIC;
import static fr.amap.amapvox.voxelisation.LeafAngleDistribution.Type.TWO_PARAMETER_BETA;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.TrapezoidIntegrator;

/**
 * References:
 *      -Wang W. M., Li Z.-L. and Su H.-B., 2007,
 *          Comparison of leaf angle distribution functions: 
 *          effects on extinction coefficient and sunlit foliage, 
 *          Agricultural and Forest Meteorology, 2007, Vol. 143, NO. 1-2, pp. 106-122.
 * 
 * @author calcul
 */
public class DirectionalTransmittance {
    
    private LeafAngleDistribution distribution;
    private double[] pdfArray; //probability density function array
    private int nbIntervals;
    private double[] serie_angulaire;
    private double SOM;
    
    private double[] transmittanceFunctions;
    private double res;
    private boolean isBuildingTable;
    
    public static int MIN_STEP_NUMBER = 91;
    public static int DEFAULT_STEP_NUMBER = 181;
    
    private class CustomFunction1 implements UnivariateFunction{

        private final double thetaRad;
        
        public CustomFunction1(double thetaRad){
            this.thetaRad = thetaRad;
        }
        
        @Override
        public double value(double x) {
            return Math.cos(thetaRad)*Math.cos(x);
        }
        
    }
    
    private class CustomFunction2 implements UnivariateFunction{

        private final double thetaRad;
        
        public CustomFunction2(double thetaRad){
            this.thetaRad = thetaRad;
        }
        
        @Override
        public double value(double x) {
            
            double tmp = (Math.tan(thetaRad)*Math.tan(x));
            
            //patch to avoid acos(1.000000000001), causing psi result equals to NaN
            if(Math.abs(1-tmp) < 0.00000001){
                tmp = 1;
            }
            double psi = Math.acos(1/tmp);

            double result = Math.cos(thetaRad) * Math.cos(x) * (1 + (2 / Math.PI) * (Math.tan(psi) - psi));

            return result;
        }
        
    }
    
    
    
    public DirectionalTransmittance(LeafAngleDistribution leafAngleDistribution){
        
        this.distribution = leafAngleDistribution;
        
    }
    
    private void setupDensityProbabilityArray(int stepNumber){
                
        //calcul des bornes des classes d'angles foliaires
        
        //contient des angles foliaires de 0 à 90°
        serie_angulaire = new double[stepNumber];
        
        double step = (Math.round((90/(float)(stepNumber-1))*100))/100.0;
        double totalStep = 0;
        
        for(int i = 0; i < stepNumber ; i++){
            
            if(i == 0){
                serie_angulaire[i] = Math.toRadians((Math.round(0.001*1000))/1000.0);
            }else{
                serie_angulaire[i] = Math.toRadians((Math.round(totalStep*1000))/1000.0);
            }
            
            totalStep += step;
            //System.out.println(serie_angulaire[i]);
        }
        
        nbIntervals = serie_angulaire.length-1;
        
        //calcul du tableau de densités ou probabilités
        pdfArray = new double[serie_angulaire.length];
        for(int i = 0 ; i < pdfArray.length ; i++){
            
            pdfArray[i] = distribution.getDensityProbability(serie_angulaire[i]);
            SOM += pdfArray[i];
        }
    }
    
    /**
     *
     * @param stepNumber must be greater or equals than 91
     */
    public void buildTable(int stepNumber){
        
        isBuildingTable = true;
        
        setupDensityProbabilityArray(stepNumber);
        
        res = 90.0 / stepNumber;
        
        double step = (Math.round((90/(float)(stepNumber-1))*100))/100.0;
        double totalStep = 0;
        
        transmittanceFunctions = new double[stepNumber];
        
        for(int i = 0 ; i < stepNumber ; i++){
            transmittanceFunctions[i] = getTransmittanceFromAngle(totalStep, true);
            totalStep += step;
        }
        
        isBuildingTable = false;
    }
    
    /**
     *
     * @param theta
     * @param degrees if passed angle is in degrees, otherwise it is radians
     * @return directional transmittance (GTheta)
     */
    public double getTransmittanceFromAngle(double theta, boolean degrees){
        
        
        if(transmittanceFunctions != null && !isBuildingTable){ //a table was built
            
            if(degrees){
                if(theta > 90){ //get an angle between 0 and 90°
                    theta = 180 - theta;
                }
            }else{
                if(theta > (Math.PI/2.0)){ //get an angle between 0 and pi/2
                    theta = Math.PI - theta;
                }
            }
            
            int indice = 0;
            if(degrees){
                indice = (int) (theta/res);
            }else{
                indice = (int) (Math.toDegrees(theta)/res);
            }
            
            return transmittanceFunctions[indice];
            
        }else{ //no table was built, get transmittance on the fly
            
            if(pdfArray == null){
                setupDensityProbabilityArray(DEFAULT_STEP_NUMBER);
            }
            if (distribution.getType() == SPHERIC) {

                return 0.5;

            } else {

                if (degrees) {
                    theta = Math.toRadians(theta);
                }

                if (theta == 0) {
                    theta = Double.MIN_VALUE;
                }

                if (theta >= Math.PI / 2.0) {
                    theta = (Math.PI / 2.0)-0.00001;
                }

                UnivariateFunction function1 = new CustomFunction1(theta);
                UnivariateFunction function2 = new CustomFunction2(theta);

                TrapezoidIntegrator integrator = new TrapezoidIntegrator();

                double sum = 0;
                for (int j = 0; j < nbIntervals - 1; j++) {

                    double thetaL = (serie_angulaire[j] + serie_angulaire[j + 1]) / 2.0d;
                    double Fi = (pdfArray[j]) / SOM;

                    double cotcot = Math.abs(1 / (Math.tan(theta) * Math.tan(thetaL)));

                    double Hi;

                    if (cotcot > 1 || Double.isInfinite(cotcot)) {
                        Hi = integrator.integrate(10000, function1, serie_angulaire[j], serie_angulaire[j + 1]);
                    } else {
                        Hi = integrator.integrate(10000, function2, serie_angulaire[j], serie_angulaire[j + 1]);
                        //System.out.println("nb evaluations: " + integrator.getEvaluations());
                    }

                    double Gi = Fi * Hi / ((Math.PI / 2) / (double) serie_angulaire.length); //because we need the average value not the actual integral value!!!!
                    sum += Gi;
                }

                return sum;
            }
        }
        
    }
    
    public static void main(String[] args) {
     
        //LeafAngleDistribution distribution = new LeafAngleDistribution(TWO_PARAMETER_BETA, Math.toRadians(60), 0.03);
        LeafAngleDistribution distribution = new LeafAngleDistribution(TWO_PARAMETER_BETA, 60, 0.03);
        DirectionalTransmittance m = new DirectionalTransmittance(distribution);
        m.buildTable(181);
        
        //DirectionalTransmittance m = new DirectionalTransmittance(distribution);
        
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/home/calcul/DART/user_data/database/LADFile.txt")));
            writer.write("Thetaf g_sinThetaf\n");
            for(int i=0;i<181;i++){
                writer.write((Math.round(Math.toDegrees(m.serie_angulaire[i])*1000)/1000.0)+" "+m.pdfArray[i]+"\n");
            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(DirectionalTransmittance.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/home/calcul/Documents/Julien/GTheta_extremophile.txt")));
            for(int i = 0 ; i < 180 ; i++){
                double GTheta = m.getTransmittanceFromAngle(i/2.0, true);
                writer.write(GTheta+" "+i/2.0+"\n");
                System.out.println(i+" : "+GTheta);
            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(DirectionalTransmittance.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
}
