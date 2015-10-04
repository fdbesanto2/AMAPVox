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

package fr.ird.voxelidar.configuration;

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


public class TLSVoxCfg extends VoxCfg{
    
    private List<MatrixAndFile> matricesAndFiles;
    
    @Override
    public void readConfiguration(File inputParametersFile) {
        
        super.readConfiguration(inputParametersFile);
        
        processMode = ProcessMode.VOXELISATION_TLS;
        
        if (inputType == InputType.RSP_PROJECT) {

            Element filesElement = processElement.getChild("files");
            List<Element> childrens = filesElement.getChildren("file");
            matricesAndFiles = new ArrayList<>();

            for (Element e : childrens) {

                Matrix4d mat = getMatrixFromData(e.getChildText("matrix"));
                File f = new File(e.getAttributeValue("src"));

                matricesAndFiles.add(new MatrixAndFile(f, mat));
            }

            Element mergingElement = processElement.getChild("merging");
            if (mergingElement != null) {
                voxelParameters.setMergingAfter(Boolean.valueOf(mergingElement.getAttributeValue("enabled")));
                if (voxelParameters.isMergingAfter()) {
                    voxelParameters.setMergedFile(new File(mergingElement.getAttributeValue("src")));
                }
            }
        }
        
    }
    
    @Override
    public void writeConfiguration(File outputParametersFile) {
        
        createCommonData();
        
        super.writeConfiguration(outputParametersFile);
        
        processElement.setAttribute(new Attribute("mode","voxelisation"));
        processElement.setAttribute(new Attribute("type","TLS"));
        
        if(inputType == InputType.RSP_PROJECT){
                
            /***MERGING***/

            Element mergingElement = new Element("merging");
            mergingElement.setAttribute("enabled", String.valueOf(voxelParameters.isMergingAfter()));
            if(voxelParameters.isMergingAfter()){
                mergingElement.setAttribute("src", voxelParameters.getMergedFile().getAbsolutePath());
            }

            processElement.addContent(mergingElement);

            /***FILE LIST TO PROCESS***/

            Element filesElement = new Element("files");
            if(matricesAndFiles != null){
                for(MatrixAndFile f : matricesAndFiles){
                    Element fileElement = new Element("file");
                    fileElement.setAttribute("src", f.file.getAbsolutePath());
                    fileElement.addContent(new Element("matrix").setText(f.matrix.toString()));
                    filesElement.addContent(fileElement);
                }
            }
            processElement.addContent(filesElement);
        }
        
        writeDocument(outputParametersFile);
    }

    public List<MatrixAndFile> getMatricesAndFiles() {
        return matricesAndFiles;
    }

    public void setMatricesAndFiles(List<MatrixAndFile> matricesAndFiles) {
        this.matricesAndFiles = matricesAndFiles;
    }    
}
