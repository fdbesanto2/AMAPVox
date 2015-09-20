/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.als.las;

import fr.amap.lidar.als.LasHeader;
import fr.amap.lidar.als.LasHeader11;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.math.vector.Vec4D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import javax.swing.SwingWorker;
import javax.swing.event.EventListenerList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class LasToTxt {
    
    final static Logger logger = Logger.getLogger(LasToTxt.class);
    
    private EventListenerList listeners;
    private int progress;
    private boolean finished;
    
    public LasToTxt(){
        listeners = new EventListenerList();
        progress = 0;
        finished = false;
    }
    public void setWriteFileProgress(int progress) {
        this.progress = progress;
        fireWriteFileProgress(progress);
    }
    
    public void fireWriteFileProgress(int progress){
        
        for(LasToTxtListener listener :listeners.getListeners(LasToTxtListener.class)){
            
            listener.LasToTxtProgress(progress);
        }
    }
    
    public void setWriteFileFinished(boolean finished) {
        this.finished = finished;
        
        if(finished){
            fireWriteFileFinished();
        }
    }
    
    public void fireWriteFileFinished(){
        
        for(LasToTxtListener listener :listeners.getListeners(LasToTxtListener.class)){
            
            listener.LasToTxtFinished();
        }
    }
    
    public void addLasToTxtListener(LasToTxtListener listener){
        listeners.add(LasToTxtListener.class, listener);
    }
    
    private String writePoint(PointDataRecordFormat point){
        StringBuilder sb = new StringBuilder();
        
        return sb.toString();
    }
    
    private String scaleFormat(double number){
        
        String numberS = String.valueOf(number);
        int indexOfPoint = numberS.indexOf(".")+1;
        int scaleFormat = numberS.length()-indexOfPoint;
        
        String scaleFormatS = ".";
        for(int i=0;i<scaleFormat;i++){
            scaleFormatS += "0";
        }
        
        return scaleFormatS;
    }
    
    public void writeTxt(final Mat4D mat, final Las las, final String path, final String parameters, final boolean writeHeader){
        
        SwingWorker sw = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path)))) {
            
                    char[] params = parameters.toCharArray();
                    
                    LasHeader header = (LasHeader)las.getHeader();

                    int version = las.getHeader().getVersionMinor();
                    
                    DecimalFormat dfX = new DecimalFormat(scaleFormat(header.getxScaleFactor()));
                    DecimalFormat dfY = new DecimalFormat(scaleFormat(header.getyScaleFactor()));
                    DecimalFormat dfZ = new DecimalFormat(scaleFormat(header.getzScaleFactor()));

                    DecimalFormatSymbols dfs = new DecimalFormatSymbols();
                    dfs.setDecimalSeparator('.');

                    dfX.setDecimalFormatSymbols(dfs);
                    dfY.setDecimalFormatSymbols(dfs);
                    dfZ.setDecimalFormatSymbols(dfs);
                    
                    if(writeHeader){
                        
                        writer.write("# file signature:            '"+header.getFileSignature()+"'\n");
                        writer.write("# file source ID:            "+header.getFileSourceId()+"\n");

                        if(version == 0){
                            writer.write("# reserved (global encoding):"+header.getReserved()+"\n");
                        }else{
                            writer.write("# reserved (global encoding):"+((LasHeader11)header).getGlobalEncoding()+"\n");
                        }

                        writer.write("# project ID GUID data 1-4:  "+header.getProjectIdGuidData1()+" "+header.getProjectIdGuidData2()+" "+header.getProjectIdGuidData3()+" "+header.getProjectIdGuidData4()+"\n");  
                        writer.write("# version major.minor:       "+header.getVersionMajor()+"."+header.getVersionMinor()+"\n");
                        writer.write("# system_identifier:         "+"'"+header.getSystemIdentifier().trim()+"'\n");
                        writer.write("# generating_software:       "+"'"+header.getGeneratingSoftware().trim()+"'\n");
                        writer.write("# file creation day/year:    "+header.getFileCreationDayOfYear()+"/"+header.getFileCreationYear()+"\n");
                        writer.write("# header size                "+header.getHeaderSize()+"\n");
                        writer.write("# offset to point data       "+header.getOffsetToPointData()+"\n");
                        writer.write("# number var. length records "+header.getNumberOfVariableLengthRecords()+"\n");
                        writer.write("# point data format          "+header.getPointDataFormatID()+"\n");
                        writer.write("# point data record length   "+header.getPointDataRecordLength()+"\n");
                        writer.write("# number of point records    "+header.getNumberOfPointrecords()+"\n");
                        writer.write("# number of points by return "+header.getNumberOfPointsByReturn()[0]+" "+header.getNumberOfPointsByReturn()[1]+" "+header.getNumberOfPointsByReturn()[2]+" "+header.getNumberOfPointsByReturn()[3]+" "+header.getNumberOfPointsByReturn()[4]+"\n");
                        writer.write("# scale factor x y z         "+header.getxScaleFactor()+" "+header.getyScaleFactor()+" "+header.getzScaleFactor()+"\n");
                        writer.write("# offset x y z               "+header.getxOffset()+" "+header.getyOffset()+" "+header.getzOffset()+"\n");
                        writer.write("# min x y z                  "+dfX.format(header.getMinX())+" "+dfY.format(header.getMinY())+" "+dfZ.format(header.getMinZ())+"\n");
                        writer.write("# max x y z                  "+dfX.format(header.getMaxX())+" "+dfY.format(header.getMaxY())+" "+dfZ.format(header.getMaxZ())+"\n");

                    }
                    
                    ArrayList<? extends PointDataRecordFormat> pointDataRecords = las.getPointDataRecords();
                    
                    int compteur =0;
                    for(PointDataRecordFormat point:pointDataRecords){
                        
                        short isEdgeOfFlightLine = (short)(point.isEdgeOfFlightLine()? 1 : 0);
                        short isScanDirectionFlag = (short)(point.isScanDirectionFlag()? 1 : 0);

                        StringBuilder sb = new StringBuilder();
                        
                        if(ArrayUtils.contains(params, 'x')){
                            sb.append(dfX.format((point.getX()*header.getxScaleFactor())+header.getxOffset())).append(" ");
                        }
                        if(ArrayUtils.contains(params, 'y')){
                            sb.append(dfY.format((point.getY()*header.getyScaleFactor())+header.getyOffset())).append(" ");
                        }
                        if(ArrayUtils.contains(params, 'z')){
                            sb.append(dfZ.format((point.getZ()*header.getzScaleFactor())+header.getzOffset())).append(" ");
                        }
                        if(ArrayUtils.contains(params, 'i')){
                            sb.append(point.getIntensity()).append(" ");
                        }
                        if(ArrayUtils.contains(params, 'r')){
                            sb.append(point.getReturnNumber()).append(" ");
                        }
                        if(ArrayUtils.contains(params, 'n')){
                            sb.append(point.getNumberOfReturns()).append(" ");
                        }
                        if(ArrayUtils.contains(params, 'd')){
                            sb.append(isScanDirectionFlag).append(" ");
                        }
                        if(ArrayUtils.contains(params, 'e')){
                            sb.append(isEdgeOfFlightLine).append(" ");
                        }
                        if(ArrayUtils.contains(params, 'c')){
                            sb.append(point.getClassification()).append(" ");
                        }
                        if(ArrayUtils.contains(params, 'a')){
                            sb.append(point.getScanAngleRank()).append(" ");
                        }
                        if(ArrayUtils.contains(params, 'p')){
                            sb.append(point.getPointSourceID()).append(" ");
                        }
                        if(ArrayUtils.contains(params, 't') && header.getPointDataFormatID() >= 1){
                            sb.append(((PointDataRecordFormat1)point).getGpsTime()).append(" ");
                        }

                        if(ArrayUtils.contains(params, 'R') && header.getPointDataFormatID() >= 2){
                            int posR = ArrayUtils.indexOf(params, 'R');
                            if(params[posR+1] == 'G' && params[posR+2] == 'B'){

                                if(header.getPointDataFormatID() == 2){

                                    sb.append(((PointDataRecordFormat2)point).getRed()).append(" ");
                                    sb.append(((PointDataRecordFormat2)point).getGreen()).append(" ");
                                    sb.append(((PointDataRecordFormat2)point).getBlue()).append(" ");

                                }else if(header.getPointDataFormatID() == 3){

                                    sb.append(((PointDataRecordFormat3)point).getRed()).append(" ");
                                    sb.append(((PointDataRecordFormat3)point).getGreen()).append(" ");
                                    sb.append(((PointDataRecordFormat3)point).getBlue()).append(" ");
                                }

                            }
                        }
                        if(ArrayUtils.contains(params, 'W')){

                        }
                        if(ArrayUtils.contains(params, 'V')){

                        }

                        writer.write(sb.toString()+"\n");
                        
                        compteur ++;
                        setWriteFileProgress((compteur*100)/pointDataRecords.size());
                    }

                    writer.close();

                } catch (IOException ex) {
                    logger.error("cannot write las file", ex);
                }
                setWriteFileFinished(true);
                return null;
            }
        };
        sw.execute();
                
        
    }
}
