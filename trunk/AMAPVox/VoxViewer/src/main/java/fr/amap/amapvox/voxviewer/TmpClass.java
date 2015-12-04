/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer;

import fr.amap.amapvox.io.tls.rsp.Rsp;
import fr.amap.amapvox.io.tls.rxp.RxpExtraction;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.amapvox.jeeb.workspace.sunrapp.util.Colouring;
import fr.amap.amapvox.math.matrix.Mat4D;
import fr.amap.amapvox.math.point.Point3F;
import fr.amap.amapvox.math.vector.Vec3F;
import fr.amap.amapvox.math.vector.Vec4D;
import fr.amap.amapvox.voxviewer.event.BasicEvent;
import fr.amap.amapvox.voxviewer.object.camera.TrackballCamera;
import fr.amap.amapvox.voxviewer.object.scene.PointCloudSceneObject;
import fr.amap.amapvox.voxviewer.renderer.MinimalWindowAdapter;
import java.io.File;
import java.util.Iterator;
import java.util.logging.Level;
import javax.vecmath.Point3f;

/**
 *
 * @author calcul
 */
public class TmpClass{
        
        public TmpClass(){
            
        }
        
        public static void execute(){
            
            try {
                PointCloudSceneObject pointCloud = new PointCloudSceneObject();
                Rsp rsp = new Rsp();
                rsp.read(new File("/media/calcul/IomegaHDD/BDLidar/TLS/Paracou2013/Paracou2013complet.RISCAN/project.rsp"));


                long startTime = System.currentTimeMillis();

                RxpExtraction reader = new RxpExtraction();
                reader.openRxpFile(new File("/media/calcul/IomegaHDD/BDLidar/TLS/Paracou2013/Paracou2013complet.RISCAN/SCANS/ScanPos001/SINGLESCANS/130917_153258.rxp"), RxpExtraction.REFLECTANCE);
                final Iterator<Shot> iterator = reader.iterator();

                /*List<Float> vertexDataList = new ArrayList<>(81547452);
                List<Float> colorDataList = new ArrayList<>(81547452);*/
                Mat4D sopMatrix = rsp.getRxpList().get(0).getSopMatrix();
                int count = 0;
                
                while(iterator.hasNext()){

                    try{
                        
                        Shot shot = iterator.next();

                        for(int i=0;i<shot.ranges.length;i++){

                            double range = shot.ranges[i];

                            float x = (float) (shot.origin.x + shot.direction.x * range);
                            float y = (float) (shot.origin.y + shot.direction.y * range);
                            float z = (float) (shot.origin.z + shot.direction.z * range);

                            Vec4D transformedPoint = Mat4D.multiply(sopMatrix, new Vec4D(x, y, z, 1));
                            pointCloud.addPoint((float)transformedPoint.x, (float)transformedPoint.y, (float)transformedPoint.z);

                            double reflectance = shot.reflectances[i];

                            float reflectanceColor = (float) ((reflectance+38)/69.0f);

                            //pointCloud.addColor(0, reflectanceColor, reflectanceColor, reflectanceColor);

                            float zColor = (float) (Math.abs(z)/70.0f);
                            Point3f rainbowRGB = Colouring.rainbowRGB(zColor);
                            //pointCloud.addColor(1, rainbowRGB.x/255.0f, rainbowRGB.y/255.0f, rainbowRGB.z/255.0f);

                            /*float yColor = (float) (Math.abs(y)/70.0f);
                            Point3f rainbowRGBY = Colouring.rainbowRGB(yColor);
                            pointCloud.addColor(2, rainbowRGBY.x/255.0f, rainbowRGBY.y/255.0f, rainbowRGBY.z/255.0f);

                            float xColor = (float) (Math.abs(x)/70.0f);
                            Point3f rainbowRGBX = Colouring.rainbowRGB(xColor);
                            pointCloud.addColor(3, rainbowRGBX.x/255.0f, rainbowRGBX.y/255.0f, rainbowRGBX.z/255.0f);*/
                        }

                        count++;

                    }catch(Exception e){
                        System.out.println("test");
                    }
                    

                }

                long endTime = System.currentTimeMillis();
                System.out.println("Temps de lecture du fichier : "+(endTime-startTime)+" ms");
                System.out.println("Nombre de tirs : "+count);
    //            
    //            reader.openRxpFile(new File("/media/calcul/IomegaHDD/BDLidar/TLS/Paracou2013/Paracou2013complet.RISCAN/SCANS/ScanPos002/SINGLESCANS/130917_155228.mon.rxp"), RxpExtraction.SHOT_WITH_REFLECTANCE);
    //            iterator = reader.iterator();
    //            
    //            sopMatrix = rsp.getRxpList().get(1).getSopMatrix();
    //            
    //            count = 0;
    //            
    //            while(iterator.hasNext()){
    //                
    //                Shot shot = iterator.next();
    //                
    //                for(int i=0;i<shot.ranges.length;i++){
    //                    
    //                    double range = shot.ranges[i];
    //                    
    //                    float x = (float) (shot.origin.x + shot.direction.x * range);
    //                    float y = (float) (shot.origin.y + shot.direction.y * range);
    //                    float z = (float) (shot.origin.z + shot.direction.z * range);
    //                    
    //                    Vec4D transformedPoint = Mat4D.multiply(sopMatrix, new Vec4D(x, y, z, 1));
    //                    pointCloud.addPoint((float)transformedPoint.x, (float)transformedPoint.y, (float)transformedPoint.z);
    //                    
    //                    double reflectance = shot.reflectances[i];
    //                    
    //                    float reflectanceColor = (float) ((reflectance+38)/69.0f);
    //                    
    //                    pointCloud.addColor(0, reflectanceColor, reflectanceColor, reflectanceColor);
    //                    
    //                    float zColor = (float) (Math.abs(z)/70.0f);
    //                    Point3f rainbowRGB = Colouring.rainbowRGB(zColor);
    //                    pointCloud.addColor(1, rainbowRGB.x/255.0f, rainbowRGB.y/255.0f, rainbowRGB.z/255.0f);
    //                }
    //                
    //                count++;
    //                
    //            }

                //System.out.println("Nombre de points : "+vertexDataList.size()/3);

                /*float[] vertexData = new float[vertexDataList.size()];
                float[] colorData = new float[colorDataList.size()];

                for(int i=0;i<vertexDataList.size();i++){
                    vertexData[i] = vertexDataList.get(i);
                    colorData[i] = colorDataList.get(i);
                }*/


    //            LasReader lasReader = new LasReader();
    //            lasReader.open(new File("/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/ALSbuf_xyzirncapt.las"));
    //            LasHeader header = lasReader.getHeader();
    //            int numberOfPointrecords = (int) header.getNumberOfPointrecords();
    //            
    //            float[] vertexData = new float[numberOfPointrecords*3];
    //            float[] colorData = new float[numberOfPointrecords*3];
    //            
    //            Iterator<PointDataRecordFormat> iterator = lasReader.iterator();
    //            Mat4D transfMatrix = Mat4D.identity();
    //            
    //            int i=0;
    //            while(iterator.hasNext()){
    //                
    //                PointDataRecordFormat point = iterator.next();
    //                float x = (float) ((header.getxOffset()) + point.getX() * header.getxScaleFactor());
    //                float y = (float) ((header.getyOffset()) + point.getY() * header.getyScaleFactor());
    //                float z = (float) ((header.getzOffset()) + point.getZ() * header.getzScaleFactor());
    //                
    //                Vec4D pointTransformed = Mat4D.multiply(transfMatrix, new Vec4D(x, y, z, 1));
    //                vertexData[i] = (float) pointTransformed.x;
    //                vertexData[i+1] = (float) pointTransformed.y;
    //                vertexData[i+2] = (float) pointTransformed.z;
    //                
    //                if(point.getClassification() == 2){
    //                    colorData[i] = 1;
    //                    colorData[i+2] = 0;
    //                }else{
    //                    colorData[i] = 0;
    //                    colorData[i+2] = 1;
    //                }
    //                
    //                colorData[i+1] = 0;
    //                
    //                
    //                i+=3;
    //            }

                Viewer3D viewer3D = new Viewer3D(((int) 640 / 4), (int) 480 / 4, 640, 480, "");
                viewer3D.attachEventManager(new BasicEvent(viewer3D.getAnimator(), viewer3D.getJoglContext()));
                fr.amap.amapvox.voxviewer.object.scene.Scene scene = viewer3D.getScene();

                //PointCloudGLMesh pointcloudMesh = (PointCloudGLMesh) GLMeshFactory.createPointCloud(vertexData,colorData);

                //PointCloudSceneObject pointCloud = new PointCloudSceneObject(pointcloudMesh, false);
                pointCloud.initMesh();
                pointCloud.setShader(scene.colorShader);
                scene.addSceneObject(pointCloud);

                /**
                 * *light**
                 */
                scene.setLightPosition(new Point3F(pointCloud.getPosition().x, pointCloud.getPosition().y, pointCloud.getPosition().z + 100));

                /**
                 * *camera**
                 */
                TrackballCamera trackballCamera = new TrackballCamera();
                trackballCamera.setPivot(pointCloud);
                trackballCamera.setLocation(new Vec3F(pointCloud.getPosition().x-50, pointCloud.getPosition().y, pointCloud.getPosition().z+50));
                viewer3D.getScene().setCamera(trackballCamera);




                //viewer3D.addWindowListener(new MinimalWindowAdapter(null, viewer3D.getAnimator()));
                //joglWindow.setOnTop();
                viewer3D.show();
            } catch (Exception ex) {
                java.util.logging.Logger.getLogger(Viewer3D.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        public static void main(String[] args) {
            
            TmpClass.execute();
        }
    }
