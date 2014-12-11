/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.tls;

import fr.ird.voxelidar.math.matrix.Mat4D;
import fr.ird.voxelidar.math.vector.Vec4D;
import fr.ird.voxelidar.util.CsvLine;
import fr.ird.voxelidar.util.Voxelisation;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */

public class VoxelPreprocessingRxp extends SwingWorker<Void, Void>{
    
    private final static Logger logger = Logger.getLogger(VoxelPreprocessingRxp.class);
            
    private final static String PROGRAM_NAME = "TLSRivLib.exe";
    
    private Rsp rsp;
    private ArrayList<String> fileList;
    private Rxp rxp;
    private short mode;
    private Mat4D popMatrix;
    private Mat4D transfMatrix = Mat4D.identity();
    private boolean filtered = false;
    
    public VoxelPreprocessingRxp(Mat4D transformationMatrix, String fileFullPath){
        
    }
    
    /**
     *
     * @param rxp Rxp file 
     * @param popMatrix Project orientation and position
     */
    public VoxelPreprocessingRxp(Rxp rxp, Mat4D popMatrix){
        
        this.rxp = rxp;
        this.popMatrix = popMatrix;
        mode = 1;
    }
    
    /**
     *
     * @param rsp Rsp project to treat all data
     * @param transfMatrix Transformation matrix
     */
    public VoxelPreprocessingRxp(Rsp rsp, Mat4D transfMatrix, boolean filtered){
        
        this.rsp = rsp;
        this.transfMatrix = transfMatrix;
        this.filtered = filtered;
        
        mode = 2;
    }
    
    public VoxelPreprocessingRxp(ArrayList<String> fileList){
        
        this.fileList = fileList;
        mode = 3;
    }
    
    private void doExec(String path, Mat4D transfMatrix){
        
        Process p;
        String command = null;
        
        try {
            command = PROGRAM_NAME + " -0 " +"\""+ path+ "\"";
            p = Runtime.getRuntime().exec(command);

            Transformation fluxSortie = new Transformation(p.getInputStream(), path+"_echos", transfMatrix);
            new Thread(fluxSortie).start();

            p.waitFor();

        } catch (IOException | InterruptedException ex) {
            logger.error("error executing command: "+command, ex);
        }
    }
    
    @Override
    protected Void doInBackground(){
        
        //doExec("C:\\Users\\Julien\\Documents\\Visual Studio 2012\\Projects\\TLSRivLib\\Debug\\testmtd.rxp", Mat4D.identity());
        
        String fullPath;
        Mat4D sopMatrix;
        
        switch(mode){
            case 1:
                
                fullPath = rxp.getName();
                sopMatrix = rxp.getSopMatrix();
                
                if(popMatrix == null){
                    popMatrix = Mat4D.identity();
                }
                
                transfMatrix = Mat4D.multiply(popMatrix, sopMatrix);
                
                doExec(fullPath, transfMatrix);
                
                break;
            case 2:
                
                ArrayList<Rxp> rxpList = rsp.getRxpList();
                
                popMatrix = rsp.getPopMatrix();
                
                for(Rxp rxpItem : rxpList){
                    
                    sopMatrix = rxpItem.getSopMatrix();
                    transfMatrix = Mat4D.multiply(transfMatrix, sopMatrix);
                    
                    
                    Map<Integer, Scan> scanList;
                    
                    if(filtered){
                        scanList = rxpItem.getScanListFiltered(true);
                    }else{
                        scanList = rxpItem.getScanListFiltered(false);
                    }
                    
                    
                    for(Entry entry:scanList.entrySet()){
                        fullPath = scanList.get((int)entry.getKey()).getAbsolutePath();
                        doExec(fullPath, transfMatrix);
                    }
                    
                }
                
                break;
        }
        
        return null;
    }
    
    
    private class Transformation implements Runnable{
    
    private final InputStream inputStream;
    private final String outputFullPath;
    private final Mat4D transfMatrix;
    
    private BufferedReader getBufferedReader(InputStream is) {
        return new BufferedReader(new InputStreamReader(is));
    }
    
    public Transformation(InputStream inputStream, String outputFullPath, Mat4D transMatrix) {
        
        this.inputStream = inputStream;
        this.outputFullPath = outputFullPath;
        this.transfMatrix = transMatrix;
    }
    
    @Override
    public void run() {
        
        BufferedReader br = getBufferedReader(inputStream);
        File file = new File(outputFullPath);
        String name = file.getName();
        String ligne;
        
        String get = (String)System.getProperties().get("user.dir");
        
        File f = new File("./fichier_echos");
        f.mkdirs();
        
        String n = f+"\\"+name;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(n));
            
            while ((ligne = br.readLine()) != null) {
                
                String[] lineSplit = ligne.split(" ");
                
                CsvLine newLine = new CsvLine(" ");
                
                String id = lineSplit[0];
                newLine.add(id);
                
                String echoCount = lineSplit[1];
                newLine.add(echoCount);
                
                double xBeam = Double.valueOf(lineSplit[2]);
                double yBeam = Double.valueOf(lineSplit[3]);
                double zBeam = Double.valueOf(lineSplit[4]);
                
                Vec4D newVector = Mat4D.multiply(transfMatrix, new Vec4D(xBeam, yBeam, zBeam, 1.0d));
                
                
                newLine.add(String.valueOf(newVector.x));
                newLine.add(String.valueOf(newVector.y));
                newLine.add(String.valueOf(newVector.z));
                
                for(int i=1,j=5;j<lineSplit.length;i++,j++){
                    switch(i){
                        //écho 1
                        case 1:
                            double echo1 = Double.valueOf(lineSplit[j]);
                            newLine.add(String.valueOf(echo1));
                            break;
                        //écho 2
                        case 2:
                            double echo2 = Double.valueOf(lineSplit[j]);
                            newLine.add(String.valueOf(echo2));
                            break;
                        //écho 3
                        case 3:
                            double echo3 = Double.valueOf(lineSplit[j]);
                            newLine.add(String.valueOf(echo3));
                            break;
                        //écho 4
                        case 4:
                            double echo4 = Double.valueOf(lineSplit[j]);
                            newLine.add(String.valueOf(echo4));
                            break;
                        //écho 5
                        case 5:
                            double echo5 = Double.valueOf(lineSplit[j]);
                            newLine.add(String.valueOf(echo5));
                            break;
                        //écho 6
                        case 6:
                            double echo6 = Double.valueOf(lineSplit[j]);
                            newLine.add(String.valueOf(echo6));
                            break;
                        //écho 7
                        case 7:
                            double echo7 = Double.valueOf(lineSplit[j]);
                            newLine.add(String.valueOf(echo7));
                            break;
                    }
                }
                
                writer.write(newLine.getLine());
                
                //System.out.println(ligne);
            }
            writer.close();
            
            //appel du jar de voxelisation
            
            
        } catch (IOException e) {
            logger.error(null, e);
        }
    }
    
}
    
}

