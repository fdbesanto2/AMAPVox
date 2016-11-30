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

package fr.amap.lidar.amapvox.voxelisation.configuration;

import fr.amap.lidar.amapvox.commons.LidarScan;
import fr.amap.lidar.format.jleica.ptx.PTXHeader;
import fr.amap.lidar.format.jleica.ptx.PTXScan;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Matrix4d;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 *
 * @author calcul
 */


public class TLSVoxCfg extends VoxelAnalysisCfg{
    
    private List<LidarScan> lidarScans;
    private boolean enableEmptyShotsFiltering;
    
    @Override
    public void readConfiguration(File inputParametersFile) throws Exception {
        
        super.readConfiguration(inputParametersFile);
        
        processMode = ProcessMode.VOXELISATION_TLS;
        
        if (inputType == InputType.RSP_PROJECT || inputType == InputType.PTX_PROJECT
                || inputType == InputType.PTG_PROJECT
                || inputType == InputType.PTX_PROJECT) {

            Element filesElement = processElement.getChild("files");
            List<Element> childrens = filesElement.getChildren("file");
            lidarScans = new ArrayList<>();

            int count = 0;
            
            for (Element e : childrens) {

                Matrix4d mat = getMatrixFromData(e.getChildText("matrix"));
                File f = new File(e.getAttributeValue("src"));

                if(inputType == InputType.PTX_PROJECT){
                    
                    long offset = Long.valueOf(e.getAttributeValue("offset"));
                    int numRows = Integer.valueOf(e.getAttributeValue("numRows"));
                    int numCols = Integer.valueOf(e.getAttributeValue("numCols"));
                    PTXHeader header = new PTXHeader();
                    header.setNumRows(numRows);
                    header.setNumCols(numCols);
                    header.setPointInDoubleFormat(true);
                    
                    PTXScan scan = new PTXScan(f, header, offset);
                    
                    lidarScans.add(new PTXLidarScan(f, mat, scan, count));
                    
                }else{
                    lidarScans.add(new LidarScan(f, mat, f.getAbsolutePath()));
                }
                
                count++;
            }

            Element mergingElement = processElement.getChild("merging");
            if (mergingElement != null) {
                voxelParameters.setMergingAfter(Boolean.valueOf(mergingElement.getAttributeValue("enabled")));
                if (voxelParameters.isMergingAfter()) {
                    voxelParameters.setMergedFile(new File(mergingElement.getAttributeValue("src")));
                }
            }
        }
        
        Element emptyShotFiltering = processElement.getChild("filter-empty-shots");
        if(emptyShotFiltering != null){
            this.enableEmptyShotsFiltering = Boolean.valueOf(emptyShotFiltering.getAttributeValue("enable"));
        }
        
    }
    
    @Override
    public void writeConfiguration(File outputParametersFile) throws Exception {
        
        createCommonData();
        
        super.writeConfiguration(outputParametersFile);
        
        processElement.setAttribute(new Attribute("mode","voxelisation"));
        processElement.setAttribute(new Attribute("type","TLS"));
        
        if(inputType == InputType.RSP_PROJECT || inputType == InputType.PTX_PROJECT 
                || inputType == InputType.PTG_PROJECT
                || inputType == InputType.PTX_PROJECT){
                
            /***MERGING***/

            Element mergingElement = new Element("merging");
            mergingElement.setAttribute("enabled", String.valueOf(voxelParameters.isMergingAfter()));
            if(voxelParameters.isMergingAfter()){
                mergingElement.setAttribute("src", voxelParameters.getMergedFile().getAbsolutePath());
            }

            processElement.addContent(mergingElement);

            /***FILE LIST TO PROCESS***/

            Element filesElement = new Element("files");
            if(lidarScans != null){
                for(LidarScan scan : lidarScans){
                    Element fileElement = new Element("file");
                    fileElement.setAttribute("src", scan.file.getAbsolutePath());
                    
                    if(inputType == InputType.PTX_PROJECT){
                        
                        fileElement.setAttribute("offset", String.valueOf(((PTXLidarScan)scan).getScan().offset));
                        fileElement.setAttribute("numRows", String.valueOf(((PTXLidarScan)scan).getScan().getHeader().getNumRows()));
                        fileElement.setAttribute("numCols", String.valueOf(((PTXLidarScan)scan).getScan().getHeader().getNumCols()));
                    }
                    
                    fileElement.addContent(new Element("matrix").setText(scan.matrix.toString()));
                    filesElement.addContent(fileElement);
                }
            }
            processElement.addContent(filesElement);
            
        }
        
        /***EMPTY shots filtering***/
        Element emptyShotFiltering = new Element("filter-empty-shots");
        emptyShotFiltering.setAttribute("enable", String.valueOf(enableEmptyShotsFiltering));
        processElement.addContent(emptyShotFiltering);
        
        writeDocument(outputParametersFile);
    }

    public List<LidarScan> getLidarScans() {
        return lidarScans;
    }

    public void setLidarScans(List<LidarScan> matricesAndFiles) {
        this.lidarScans = matricesAndFiles;
    }

    public boolean isEnableEmptyShotsFiltering() {
        return enableEmptyShotsFiltering;
    }

    public void setEnableEmptyShotsFiltering(boolean enableEmptyShotsFiltering) {
        this.enableEmptyShotsFiltering = enableEmptyShotsFiltering;
    }
}
