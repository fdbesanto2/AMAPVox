/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.util;

import fr.amap.amapvox.als.LasHeader;
import fr.amap.amapvox.als.LasPoint;
import fr.amap.amapvox.als.las.LasReader;
import fr.amap.amapvox.als.las.PointDataRecordFormat;
import fr.amap.amapvox.als.laz.LazExtraction;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.commons.raster.asc.AsciiGridHelper;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.structure.octree.Octree;
import fr.amap.commons.structure.octree.OctreeFactory;
import fr.amap.commons.util.BoundingBox3d;
import fr.amap.commons.util.MatrixUtility;
import fr.amap.commons.util.io.file.CSVFile;
import fr.amap.commons.util.io.file.FileManager;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize
 */
public class Util {
    
    private final static Logger LOGGER = Logger.getLogger(Util.class);
    
    public static BoundingBox3d getALSMinAndMax(File file){
        
        LasHeader header = null;

        switch (FileManager.getExtension(file)) {
            case ".las":
                LasReader lasReader = new LasReader();
                try {
                    header = lasReader.readHeader(file);
                } catch (IOException ex) {
                    LOGGER.error(ex);
                }
                break;

            case ".laz":
                LazExtraction laz = new LazExtraction();
                try {
                    laz.openLazFile(file);
                } catch (Exception ex) {
                    LOGGER.error(ex);
                }
                header = laz.getHeader();
                laz.close();
                break;
        }

        if (header != null) {

            double minX = header.getMinX();
            double minY = header.getMinY();
            double minZ = header.getMinZ();

            double maxX = header.getMaxX();
            double maxY = header.getMaxY();
            double maxZ = header.getMaxZ();

            return new BoundingBox3d(new Point3d(minX, minY, minZ), new Point3d(maxX, maxY, maxZ));
        }
        
        return null;
    }
    
    public static Raster loadDTM(File dtmFile) throws Exception {

        Raster terrain = null;

        if (dtmFile != null) {

            try {
                terrain = AsciiGridHelper.readFromAscFile(dtmFile);
            } catch (Exception ex) {
                throw ex;
            }
        }

        return terrain;
    }
    
    public static Octree loadOctree(CSVFile pointcloudFile, Mat4D vopMatrix) {

        Octree octree = null;
        
        if (pointcloudFile != null) {

            try {
                LOGGER.info("Loading point cloud file...");
                octree = OctreeFactory.createOctreeFromPointFile(pointcloudFile, OctreeFactory.DEFAULT_MAXIMUM_POINT_NUMBER, false, vopMatrix);
                octree.build();
                LOGGER.info("Point cloud file loaded");
                
            } catch (Exception ex) {
                LOGGER.error(ex);
            }
        }

        return octree;
    }
    
    /**
     *
     * @param pointFile
     * @param resultMatrix
     * @param quick don't use classification filters
     * @param classificationsToDiscard list of point classification to skip during getting bounding box process
     * @return 
     */
    public static BoundingBox3d getBoundingBoxOfPoints(File pointFile, Matrix4d resultMatrix, boolean quick, List<Integer> classificationsToDiscard){
        
        BoundingBox3d boundingBox = new BoundingBox3d();
        
        Matrix4d identityMatrix = new Matrix4d();
        identityMatrix.setIdentity();
                
        if (resultMatrix.equals(identityMatrix) && quick) {

            boundingBox= getALSMinAndMax(pointFile);

        } else {

            int count = 0;
            double xMin = 0, yMin = 0, zMin = 0;
            double xMax = 0, yMax = 0, zMax = 0;

            Mat4D mat = MatrixUtility.convertMatrix4dToMat4D(resultMatrix);
            LasHeader lasHeader;


            switch (FileManager.getExtension(pointFile)) {
                case ".las":

                    LasReader lasReader = new LasReader();
                    try {
                        lasReader.open(pointFile);
                    } catch (IOException ex) {
                        LOGGER.error(ex);
                    } catch (Exception ex) {
                        LOGGER.error(ex);
                    }

                    lasHeader = lasReader.getHeader();
                    Iterator<PointDataRecordFormat> iterator = lasReader.iterator();

                    while (iterator.hasNext()) {

                        PointDataRecordFormat point = iterator.next();

                        if(classificationsToDiscard.contains(Integer.valueOf((int)point.getClassification()))){ //skip those

                        }else{
                            Vec4D pt = new Vec4D(((point.getX() * lasHeader.getxScaleFactor()) + lasHeader.getxOffset()),
                                (point.getY() * lasHeader.getyScaleFactor()) + lasHeader.getyOffset(),
                                (point.getZ() * lasHeader.getzScaleFactor()) + lasHeader.getzOffset(),
                                1);

                            pt = Mat4D.multiply(mat, pt);

                            if (count != 0) {

                                if (pt.x < xMin) {
                                    xMin = pt.x;
                                } else if (pt.x > xMax) {
                                    xMax = pt.x;
                                }

                                if (pt.y < yMin) {
                                    yMin = pt.y;
                                } else if (pt.y > yMax) {
                                    yMax = pt.y;
                                }

                                if (pt.z < zMin) {
                                    zMin = pt.z;
                                } else if (pt.z > zMax) {
                                    zMax = pt.z;
                                }

                            } else {

                                xMin = pt.x;
                                yMin = pt.y;
                                zMin = pt.z;

                                xMax = pt.x;
                                yMax = pt.y;
                                zMax = pt.z;

                                count++;
                            }
                        }                                                
                    }
                    
                    boundingBox.min = new Point3d(xMin, yMin, zMin);
                    boundingBox.max = new Point3d(xMax, yMax, zMax);

                    break;

                case ".laz":
                    LazExtraction lazReader = new LazExtraction();
                    try {
                        lazReader.openLazFile(pointFile);
                    } catch (Exception ex) {
                        LOGGER.error(ex);
                    }

                    lasHeader = lazReader.getHeader();
                    
                    Iterator<LasPoint> it = lazReader.iterator();

                    while (it.hasNext()) {

                        LasPoint point = it.next();

                        if(classificationsToDiscard.contains(Integer.valueOf(point.classification))){ //skip those

                        }else{
                            Vec4D pt = new Vec4D(((point.x * lasHeader.getxScaleFactor()) + lasHeader.getxOffset()),
                                (point.y * lasHeader.getyScaleFactor()) + lasHeader.getyOffset(),
                                (point.z * lasHeader.getzScaleFactor()) + lasHeader.getzOffset(),
                                1);

                            pt = Mat4D.multiply(mat, pt);

                            if (count != 0) {

                                if (pt.x < xMin) {
                                    xMin = pt.x;
                                } else if (pt.x > xMax) {
                                    xMax = pt.x;
                                }

                                if (pt.y < yMin) {
                                    yMin = pt.y;
                                } else if (pt.y > yMax) {
                                    yMax = pt.y;
                                }

                                if (pt.z < zMin) {
                                    zMin = pt.z;
                                } else if (pt.z > zMax) {
                                    zMax = pt.z;
                                }

                            } else {

                                xMin = pt.x;
                                yMin = pt.y;
                                zMin = pt.z;

                                xMax = pt.x;
                                yMax = pt.y;
                                zMax = pt.z;

                                count++;
                            }
                        }

                    }

                    boundingBox.min = new Point3d(xMin, yMin, zMin);
                    boundingBox.max = new Point3d(xMax, yMax, zMax);

                    lazReader.close();

                    break;
            }

        }
        
        return boundingBox;
    }   
}
