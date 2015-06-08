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
package fr.ird.voxelidar.lidar.format.dart;

import com.sun.xml.internal.txw2.output.IndentingXMLStreamWriter;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceHeader;
import fr.ird.voxelidar.io.file.FileManager;
import fr.ird.voxelidar.util.Cancellable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import javax.vecmath.Point2f;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class DartPlotsXMLWriter implements Cancellable{

    private final static Logger logger = Logger.getLogger(DartPlotsXMLWriter.class);
    
    private boolean cancelled = false;
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public void writeFromVoxelFile(File voxelFile, File plotFile, boolean globalCoordinates) {

        VoxelSpaceHeader header = VoxelSpaceHeader.readVoxelFileHeader(voxelFile);

        int indiceIIndex = header.attributsNames.indexOf("i");
        int indiceJIndex = header.attributsNames.indexOf("j");
        int indiceKIndex = header.attributsNames.indexOf("k");

        int padIndex = header.attributsNames.indexOf("PadBVTotal");
            
        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        try {
            
            XMLStreamWriter writer = new IndentingXMLStreamWriter(factory.createXMLStreamWriter(new FileWriter(plotFile)));
            writer.writeStartDocument("UTF-8", "1.0");
            
            writer.writeStartElement("DartFile");
            
                writer.writeAttribute("version", "5.5.3");
                
                writer.writeStartElement("Plots");
                writer.writeAttribute("isVegetation", "0");
                
                try (BufferedReader reader = new BufferedReader(new FileReader(voxelFile))) {

                    FileManager.skipLines(reader, 6);

                    String currentFileLine;
                    while ((currentFileLine = reader.readLine()) != null) {
                        
                        if(cancelled){
                            writer.close();
                            return;
                        }

                        String[] lineSplittedFile = currentFileLine.split(" ");

                        float lad = Float.valueOf(lineSplittedFile[padIndex]);

                        if (!Float.isNaN(lad)) {

                            int i = Integer.valueOf(lineSplittedFile[indiceIIndex]);
                            int j = Integer.valueOf(lineSplittedFile[indiceJIndex]);
                            int k = Integer.valueOf(lineSplittedFile[indiceKIndex]);

                            writer.writeStartElement("Plot");
                            
                                writer.writeAttribute("form", "0");
                                writer.writeAttribute("isDisplayed", "1");
                                writer.writeAttribute("type", "1");

                                /**
                                 * *CORNERS POSITIONS**
                                 */
                                float baseHeight = (k * header.res);

                                Point2f bottomLeft = new Point2f(i * (header.res), j * (header.res));

                                if (globalCoordinates) {
                                    bottomLeft.x += (float) header.bottomCorner.x;
                                    bottomLeft.y += (float) header.bottomCorner.y;
                                    baseHeight += (float) header.bottomCorner.z;
                                }

                                Point2f topRight = new Point2f(bottomLeft.x + header.res, bottomLeft.y + header.res);
                                Point2f topLeft = new Point2f(bottomLeft.x, bottomLeft.y + header.res);
                                Point2f bottomRight = new Point2f(bottomLeft.x + header.res, bottomLeft.y);

                                writer.writeStartElement("Polygon2D");

                                    writer.writeStartElement("Point2D");

                                        writer.writeAttribute("x", String.valueOf(topLeft.x));
                                        writer.writeAttribute("y", String.valueOf(topLeft.y));

                                    writer.writeEndElement();

                                    writer.writeStartElement("Point2D");

                                        writer.writeAttribute("x", String.valueOf(topRight.x));
                                        writer.writeAttribute("y", String.valueOf(topRight.y));

                                    writer.writeEndElement();

                                    writer.writeStartElement("Point2D");

                                        writer.writeAttribute("x", String.valueOf(bottomRight.x));
                                        writer.writeAttribute("y", String.valueOf(bottomRight.y));

                                    writer.writeEndElement();

                                    writer.writeStartElement("Point2D");

                                        writer.writeAttribute("x", String.valueOf(bottomLeft.x));
                                        writer.writeAttribute("y", String.valueOf(bottomLeft.y));

                                    writer.writeEndElement();

                                writer.writeEndElement();

                                /**
                                 * *VEGETATION PROPERTIES**
                                 */
                                writer.writeStartElement("PlotVegetationProperties");

                                    writer.writeAttribute("baseheight", String.valueOf(baseHeight));
                                    writer.writeAttribute("height", String.valueOf(header.res));

                                    //le signe moins indique que l'attribut lai est en fait un lad??
                                    writer.writeAttribute("lai", String.valueOf(-lad));
                                    writer.writeAttribute("stDev", "0.0");

                                writer.writeEndElement();


                                /**
                                 * *OPTICAL PROPERTY**
                                 */
                                writer.writeStartElement("VegetationOpticalPropertyLink");

                                    writer.writeAttribute("ident", "Turbid_Voxel_AMAPVox");
                                    writer.writeAttribute("indexFctPhase", "0");

                                writer.writeEndElement();
                                
                            writer.writeEndElement();
                        }

                    }

                    
                    
                } catch (XMLStreamException | IOException ex) {
                    logger.error("Cannot write xml file", ex);
                }
                
            writer.writeEndElement();
                
            writer.writeEndElement();


            writer.writeEndDocument();
            writer.flush();
            writer.close();

        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        } catch (XMLStreamException ex) {
            logger.error("Error during xml streaming process", ex);
        }

    }
}
