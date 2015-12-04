/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.test;

import fr.amap.amapvox.voxcommons.Voxel;
import fr.amap.amapvox.voxreader.VoxelFileReader;
import java.io.File;
import java.nio.file.Files;
import java.util.Iterator;

/**
 *
 * @author calcul
 */
public class Main {
    
    //static final String ALS_REFERENCE_FILE_PATH = "./resources/als_spherical.vox";
    
    static final int totalNBsamplingRef = 2240704;
    static final int totalNbEchosRef = 111647;
    
    static final int totalSumPADToNaNsRef = 139554;
    static final double totalSumPADRef = 60692.45387013734;
    
    static final int totalSumTransmittanceToNaNsRef = 139554;
    static final double totalSumtransmittanceRef = 212333.90992997927;
    
    static final int sumAngleMeanToNaNRef = 139554;
    static final double totalSumAngleMeanRef = 3.899137238298035E7;
    
    static final int sumLMeanTotalToNaNRef = 139554;
    static final double sumLMeanTotalRef = 184475.18677807;
    
    static final double sumLgTotalRef = 1824399.6306530256;
    static final double sumBVEnteringRef = 71023.3380650186;
    static final double sumBVInterceptedRef = 1816.1145419991474;
    
    private VoxelFileReader reader;
    private final File referenceFile;
    private final File fileToCompare;
    private boolean verbose = false;
    
    public Main(File referenceFile, File fileToCompare, boolean verbose){
        
        this.referenceFile = referenceFile;
        this.fileToCompare = fileToCompare;
        this.verbose = verbose;
        
        try {
            //lecture du fichier de référence (garde en mémoire)
            reader = new VoxelFileReader(referenceFile, true);
            
            Iterator<Voxel> iterator = reader.iterator();
            
            while(iterator.hasNext()){
                iterator.next();
            }
            
        } catch (Exception ex) {
            System.err.println("Cannot read reference file");
        }
    }
    
    //comparaison des sommes de chaque attribut entre le fichier d'origine et le fichier de référence
    public void test1(){
        
        boolean isDifferent = false;
            
        //vérification du nombre d'échos total et du nombre d'échantillonnages total

        int totalNBsampling = 0;
        int totalNbEchos = 0;
        double sumBVEntering = 0;
        double sumBVIntercepted = 0;
        int totalSumPADToNaNs = 0;
        double totalSumPAD = 0;
        int totalSumTransmittanceToNaNs = 0;
        double totalSumTransmittance = 0;
        int totalSumAngleMeanToNaN = 0;
        double totalSumAngleMean = 0;
        int sumLMeanTotalToNaN = 0;
        double sumLMeanTotal = 0;
        double sumLgTotal = 0;

        Iterator<Voxel> iterator = reader.iterator();

        while(iterator.hasNext()){

            Voxel voxel = iterator.next();
            totalNBsampling += voxel.nbSampling;
            totalNbEchos += voxel.nbEchos;

            if(Float.isNaN(voxel.PadBVTotal)){
                totalSumPADToNaNs++;
            }else{
                totalSumPAD += voxel.PadBVTotal;
            }

            if(Float.isNaN(voxel.transmittance)){
                totalSumTransmittanceToNaNs++;
            }else{
                totalSumTransmittance += voxel.transmittance;
            }
            
            if(Float.isNaN(voxel.angleMean)){
                totalSumAngleMeanToNaN++;
            }else{
                totalSumAngleMean += voxel.angleMean;
            }
            
            if(Double.isNaN(voxel.lMeanTotal)){
                sumLMeanTotalToNaN++;
            }else{
                sumLMeanTotal += voxel.lMeanTotal;
            }
            
            
            sumLgTotal += voxel.lgTotal;
            sumBVEntering += voxel.bvEntering;
            sumBVIntercepted += voxel.bvIntercepted;
        }

        System.out.println("Sum NbSampling : "+totalNBsampling);


        if(totalNBsampling != totalNBsamplingRef){
            System.out.println("NbSampling total != NbSampling ref");
            isDifferent = true;
        }

        System.out.println("Sum NbEchos : "+totalNBsampling);

        if(totalNbEchos != totalNbEchosRef){
            System.out.println("NbEchos total != NbEchos ref");
            isDifferent = true;
        }

        System.out.println("PAD to NaN : "+totalSumPADToNaNs);

        if(totalSumPADToNaNs != totalSumPADToNaNsRef){
            System.out.println("Sum PAD to NaN != Sum PAD to NaN ref");
            isDifferent = true;
        }

        System.out.println("PAD total : "+totalSumPAD);

        if(totalSumPAD != totalSumPADRef){
            System.out.println("Sum PAD != Sum PAD ref");
            isDifferent = true;
        }

        System.out.println("AngleMean total : "+totalSumAngleMean);

        if(totalSumAngleMean != totalSumAngleMeanRef){
            System.out.println("Sum AngleMean != Sum AngleMean ref");
            isDifferent = true;
        }
        
        System.out.println("AngleMean to NaN : "+totalSumAngleMeanToNaN);

        if(totalSumAngleMeanToNaN != sumAngleMeanToNaNRef){
            System.out.println("Sum AngleMean to NaN != Sum AngleMean to NaN ref");
            isDifferent = true;
        }

        System.out.println("Sum LgTotal : "+sumLgTotal);

        if(sumLgTotal != sumLgTotalRef){
            System.out.println("Sum LgTotal != Sum LgTotal ref");
            isDifferent = true;
        }

        System.out.println("Sum LMeanTotal : "+sumLMeanTotal);

        if(sumLMeanTotal != sumLMeanTotalRef){
            System.out.println("Sum LMeanTotal != Sum LMeanTotal ref");
            isDifferent = true;
        }
        
        System.out.println("Sum LMeanTotal to NaN : "+sumLMeanTotalToNaN);

        if(sumLMeanTotalToNaN != sumLMeanTotalToNaNRef){
            System.out.println("Sum LMeanTotal to NaN != Sum LMeanTotal to NaN ref");
            isDifferent = true;
        }

        System.out.println("Sum BVEntering : "+sumBVEntering);

        if(sumBVEntering != sumBVEnteringRef){
            System.out.println("Sum BVEntering != Sum BVEntering ref");
            isDifferent = true;
        }

        System.out.println("Sum BVIntercepted : "+sumBVIntercepted);

        if(sumBVIntercepted != sumBVInterceptedRef){
            System.out.println("Sum BVIntercepted != Sum BVIntercepted ref");
            isDifferent = true;
        }

        System.out.println("Transmittance to NaN : "+totalSumTransmittanceToNaNs);

        if(totalSumTransmittanceToNaNs != totalSumTransmittanceToNaNsRef){
            System.out.println("Sum transmittance to NaN != Sum transmittance to NaN ref");
            isDifferent = true;
        }

        System.out.println("Sum transmittance : "+totalSumTransmittance);

        if(totalSumTransmittance != totalSumtransmittanceRef){
            System.out.println("Sum transmittance != Sum transmittance ref");
            isDifferent = true;
        }
        
        if(isDifferent){
            System.out.println("Input file is different from the reference!");
        }else{
            System.out.println("Files seems identical!");
        }
    }
    
    private boolean areFloatsEquals(float value1, float value2){
        
        if(Float.isNaN(value1) && Float.isNaN(value2)){
            return true;
        }
        
        return value1 == value2;
    }
    
    private boolean areDoubleEquals(double value1, double value2){
        
        if(Double.isNaN(value1) && Double.isNaN(value2)){
            return true;
        }
        
        return value1 == value2;
    }
    
    public void test2(){
        
        VoxelFileReader reader2;
        try {
            reader2 = new VoxelFileReader(fileToCompare, true);
        } catch (Exception ex) {
            System.err.println("Cannot read file to compare");
            return;
        }
        
        Iterator<Voxel> iteratorRef = reader.iterator();
        Iterator<Voxel> iteratorFileToCompare = reader2.iterator();
        
        int count = 0;
        boolean isDifferent = false;
        
        while(iteratorRef.hasNext()){
            
            Voxel voxelRef = iteratorRef.next();
            
            if(iteratorFileToCompare.hasNext()){
                Voxel voxelToCompare = iteratorFileToCompare.next();
                                
                if(voxelRef.$i != voxelToCompare.$i){
                    if(verbose){System.err.println("i values are differents at line "+count);}
                    isDifferent = true;
                }
                if(voxelRef.$j != voxelToCompare.$j){
                    if(verbose){System.err.println("j values are differents at line "+count);}
                    isDifferent = true;
                }
                if(voxelRef.$k != voxelToCompare.$k){
                    if(verbose){System.err.println("k values are differents at line "+count);}
                    isDifferent = true;
                }
                
                if(!areFloatsEquals(voxelRef.PadBVTotal, voxelToCompare.PadBVTotal)){
                    if(verbose){System.err.println("PadBVTotal values are differents at line "+count);}
                    isDifferent = true;
                }
                
                if(!areFloatsEquals(voxelRef.angleMean, voxelToCompare.angleMean)){
                    if(verbose){System.err.println("angleMean values are differents at line "+count);}
                    isDifferent = true;
                }
                
                if(!areFloatsEquals(voxelRef.bvEntering, voxelToCompare.bvEntering)){
                    if(verbose){System.err.println("bvEntering values are differents at line "+count);}
                    isDifferent = true;
                }
                
                if(!areFloatsEquals(voxelRef.bvIntercepted, voxelToCompare.bvIntercepted)){
                    if(verbose){System.err.println("bvIntercepted values are differents at line "+count);}
                    isDifferent = true;
                }
                
                if(!areFloatsEquals(voxelRef.ground_distance, voxelToCompare.ground_distance)){
                    if(verbose){System.err.println("ground_distance values are differents at line "+count);}
                    isDifferent = true;
                }
                
                if(!areDoubleEquals(voxelRef.lMeanTotal, voxelToCompare.lMeanTotal)){
                    if(verbose){System.err.println("lMeanTotal values are differents at line "+count);}
                    isDifferent = true;
                }
                
                if(!areFloatsEquals(voxelRef.lgTotal, voxelToCompare.lgTotal)){
                    if(verbose){System.err.println("lgTotal values are differents at line "+count);}
                    isDifferent = true;
                }
                
                if(voxelRef.nbEchos != voxelToCompare.nbEchos){
                    if(verbose){System.err.println("nbEchos values are differents at line "+count);}
                    isDifferent = true;
                }
                if(voxelRef.nbSampling != voxelToCompare.nbSampling){
                    if(verbose){System.err.println("nbSampling values are differents at line "+count);}
                    isDifferent = true;
                }
                
                if(!areFloatsEquals(voxelRef.transmittance, voxelToCompare.transmittance)){
                    if(verbose){System.err.println("transmittance values are differents at line "+count);}
                    isDifferent = true;
                }
                
                
            }else{
                System.err.println("Files are differents, it doesn't have the same line number");
                return;
            }
            
            count++;
        }
        
        if(isDifferent){
            System.out.println("Input file is different from his reference!");
        }else{
            System.out.println("Files seems identical!");
        }
        
    }
    
    public static void showHelp(){
        
        String helpMsg = "Parameters : \n"+
                        "-i\tPath to the file to compare to\n"+
                        "-ref\tPath to the reference file\n"+
                        "-v\tVerbose (Optional)\n";
                        
                
        helpMsg += "\nExample:\n"
                + "-v -i /home/Documents/input.vox -ref /home/Documents/reference.vox\n";
        
        System.out.println(helpMsg);
    }
        
    public static void main(String[] args) {
        
        File inputFile = null;
        File referenceFile = null;
        boolean verbose = false;
        
        /*args = new String[]{"-i",
            "/home/calcul/Documents/Julien/AMAPVox/trunk/AMAPVox/VoxChecking/resources/als_spherical.vox",
            "-ref",
            "/home/calcul/Documents/Julien/test_lad/als_spherical_new.vox",
            "-v"};*/
        
        for(int i = 0;i<args.length;i++){
            switch(args[i]){
                case "-i":
                    if((i+1) < args.length){
                        inputFile = new File(args[i+1]);
                        if(!Files.exists(inputFile.toPath())){
                            System.err.println("Input file not found");
                            showHelp();
                        }
                        
                    }else{
                        System.err.println("Input file is missing");
                        showHelp();
                    }
                    
                    break;
                    
                case "-ref":
                    if((i+1) < args.length){
                        referenceFile = new File(args[i+1]);
                        if(!Files.exists(referenceFile.toPath())){
                            System.err.println("Reference file not found");
                            showHelp();
                        }
                        
                    }else{
                        System.err.println("Reference file is missing");
                        showHelp();
                    }
                    
                    break;
                    
                case "-v":
                    verbose = true;
                    break;
                    
                case "-h":
                    showHelp();
                    break;
            }
        }
        
        if(inputFile == null){
            
            System.err.println("Input file is missing");
            showHelp();
            
        }else if(referenceFile == null){
            System.err.println("Reference file is missing");
            showHelp();
        }else{
            
            System.out.println("Input file : "+inputFile.getAbsolutePath()+"\n");
            System.out.println("Reference file : "+referenceFile.getAbsolutePath()+"\n");
            System.out.println("Verbose : "+Boolean.toString(verbose)+"\n");
            
            System.out.println("Test is running");
            
            Main mainClass = new Main(referenceFile, inputFile, verbose);
            mainClass.test2();
        }
        
        
        
        /*switch(testName){
            case "test1":
                mainClass.test1();
                break;
            case "test2":
                
                break;
        }*/
        
    }
}
