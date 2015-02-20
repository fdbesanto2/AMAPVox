/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util.image;

import fr.ird.voxelidar.engine3d.object.scene.Voxel;
import fr.ird.voxelidar.engine3d.math.vector.Vec3F;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.vecmath.Vector3f;

/**
 *
 * @author Julien
 */
public class ImageEqualisation {
    
    public static ArrayList<Voxel> scaleHistogramm(ArrayList<Voxel> voxelList){
        
        //étirement d'histogramme
        float minX = 0, maxX = 0;
        float minY = 0, maxY = 0;
        float minZ = 0, maxZ = 0;
        
        for(int i=0;i<voxelList.size();i++){
            
            if(i == 0){
                minX = voxelList.get(i).color.x;
                maxX = voxelList.get(i).color.x;
                
                minY = voxelList.get(i).color.y;
                maxY = voxelList.get(i).color.y;
                
                minZ = voxelList.get(i).color.z;
                maxZ = voxelList.get(i).color.z;
            }
            
            if(voxelList.get(i).color.x>maxX){
                maxX = voxelList.get(i).color.x;
            }
            if(voxelList.get(i).color.x<minX){
                minX = voxelList.get(i).color.x;
            }
            
            if(voxelList.get(i).color.x>maxY){
                maxY = voxelList.get(i).color.x;
            }
            if(voxelList.get(i).color.x<minY){
                minY = voxelList.get(i).color.x;
            }
            
            if(voxelList.get(i).color.x>maxZ){
                maxZ = voxelList.get(i).color.x;
            }
            if(voxelList.get(i).color.x<minZ){
                minZ = voxelList.get(i).color.x;
            }
        }
        
        for(int i=0;i<voxelList.size();i++){
            
            voxelList.get(i).color.x = ((voxelList.get(i).color.x - minX))/(maxX-minX);
            voxelList.get(i).color.y = ((voxelList.get(i).color.y - minY))/(maxY-minY);
            voxelList.get(i).color.z = ((voxelList.get(i).color.z - minZ))/(maxZ-minZ);
        }
        
        return voxelList;
    }
    
    public static float[] getProportionnateHistogramm(int[] histogramm, int nbValue){
        
        //calcul de l'histogramme proportionné 
        
        float[] histogramP = new float[256];
        
        for(int i=0;i<256;i++){
            
            histogramP[i] = (histogramm[i])/(float)(nbValue);
        }
        
        return histogramP;
    }
    
    public static float[] getCumulateHistogramm(float[] proportionnateHistogramm){
        
        //calcul de l'histogramme cumulé
        
        float[] histogramC = new float[256];
        
        float somme = 0;
        
        for(int i=0;i<256;i++){
            
            somme+=proportionnateHistogramm[i];
            histogramC[i] = somme;
        }
        
        return histogramC;
    }
    
    public static ArrayList<Voxel> equalizedValues(ArrayList<Voxel> voxelList){
        
        int[] histogramRed = new int[256];
        int[] histogramGreen = new int[256];
        int[] histogramBlue = new int[256];
        
        for (Voxel voxel : voxelList) {
            Vector3f colorVector = voxel.color;
            Color color = new Color(colorVector.x, colorVector.y, colorVector.z);
            int red = color.getRed();
            int green = color.getGreen();
            int blue = color.getBlue();
            
            histogramRed[red]++;
            histogramGreen[green]++;
            histogramBlue[blue]++;
        }
        
        float[] proportionnateHistogrammRed = getProportionnateHistogramm(histogramRed, voxelList.size());
        float[] proportionnateHistogrammGreen = getProportionnateHistogramm(histogramGreen, voxelList.size());
        float[] proportionnateHistogrammBlue = getProportionnateHistogramm(histogramBlue, voxelList.size());
        
        float[] cumulateHistogrammRed = getCumulateHistogramm(proportionnateHistogrammRed);
        float[] cumulateHistogrammGreen = getCumulateHistogramm(proportionnateHistogrammGreen);
        float[] cumulateHistogrammBlue = getCumulateHistogramm(proportionnateHistogrammBlue);
        
        //égalisation d'histogramme
        
        for (Voxel voxel : voxelList) {
            Color initialColor = new Color(voxel.color.x, voxel.color.y, voxel.color.z);
            int red = initialColor.getRed();
            int green = initialColor.getGreen();
            int blue = initialColor.getBlue();
            float factorRed = (cumulateHistogrammRed[red]);
            float factorGreen = (cumulateHistogrammGreen[green]);
            float factorBlue = (cumulateHistogrammBlue[blue]);
            
            voxel.color = new Vector3f(factorRed, factorGreen, factorBlue);
            //System.out.println("test");
        }
        
        return voxelList;
    }
    
    
    
    public static ArrayList<Voxel> voxelSpaceEqualisation(ArrayList<Voxel> voxelList){
        
        
        int[] histogramRed = new int[256];
        int[] histogramGreen = new int[256];
        int[] histogramBlue = new int[256];
        
        for (Voxel voxel : voxelList) {
            Vector3f colorVector = voxel.color;
            Color color = new Color(colorVector.x, colorVector.y, colorVector.z);
            int red = color.getRed();
            int green = color.getGreen();
            int blue = color.getBlue();
            
            histogramRed[red]++;
            histogramGreen[green]++;
            histogramBlue[blue]++;
        }
        
        //calcul de l'histogramme proportionné 
        
        double[] histogramPRed = new double[256];
        double[] histogramPGreen = new double[256];
        double[] histogramPBlue = new double[256];
        
        for(int i=0;i<256;i++){
            
            histogramPRed[i] = (histogramRed[i]/(double)voxelList.size());
            histogramPGreen[i] = (histogramGreen[i]/(double)voxelList.size());
            histogramPBlue[i] = (histogramBlue[i]/(double)voxelList.size());
        }
        
        //calcul de l'histogramme cumulé
        
        
        int[] histogramCRed = new int[256];
        int[] histogramCGreen = new int[256];
        int[] histogramCBlue = new int[256];
        
        int sommeR = 0;
        int sommeG = 0;
        int sommeB = 0;
        
        for(int i=0;i<256;i++){
            
            sommeR+=histogramPRed[i]*255;
            sommeG+=histogramPGreen[i]*255;
            sommeB+=histogramPBlue[i]*255;
            
            histogramCRed[i] = sommeR;
            histogramCGreen[i] = sommeG;
            histogramCBlue[i] = sommeB;
        }
        
        
        
        
        //égalisation d'histogramme
        
        for (Voxel voxel : voxelList) {
            Color initialColor = new Color(voxel.color.x, voxel.color.y, voxel.color.z);
            int red = initialColor.getRed();
            int green = initialColor.getGreen();
            int blue = initialColor.getBlue();
            double factorRed = (histogramCRed[red])/255.0f;
            double factorGreen = (histogramCGreen[green])/255.0f;
            double factorBlue = (histogramCBlue[blue])/255.0f;
            
            voxel.color = new Vector3f((float)factorRed, (float)factorGreen, (float)factorBlue);
            //System.out.println("test");
        }
        /*
        createImageFromHistogramm(histogramCRed, "histogramme cumulé canal rouge.png");
        createImageFromHistogramm(histogramCGreen, "histogramme cumulé canal vert.png");
        createImageFromHistogramm(histogramCBlue, "histogramme cumulé canal bleu.png");
        
        createImageFromHistogramm(histogramPRed, "histogramme proportionné canal rouge.png");
        createImageFromHistogramm(histogramPGreen, "histogramme proportionné canal vert.png");
        createImageFromHistogramm(histogramPBlue, "histogramme proportionné canal bleu.png");
        
        createImageFromHistogramm(histogramRed, "histogramme canal rouge before.png");
        createImageFromHistogramm(histogramGreen, "histogramme canal vert before.png");
        createImageFromHistogramm(histogramBlue, "histogramme canal bleu before.png");
        
        histogramRed = new int[256];
        histogramGreen = new int[256];
        histogramBlue = new int[256];
        
        
        for (Voxel voxel : voxelList) {
            
            Vec3 colorVector = voxel.color;
            
            if(colorVector.x>1.0f ||colorVector.y>1.0f ||colorVector.z>1.0f){
                System.err.println("test");
                
            }
            
            Color color = new Color(colorVector.x, colorVector.y, colorVector.z);
            int red = color.getRed();
            int green = color.getGreen();
            int blue = color.getBlue();

            histogramRed[red]++;
            histogramGreen[green]++;
            histogramBlue[blue]++;
            
        }
        
        createImageFromHistogramm(histogramRed, "histogramme canal rouge after.png");
        createImageFromHistogramm(histogramGreen, "histogramme canal vert after.png");
        createImageFromHistogramm(histogramBlue, "histogramme canal bleu after.png");
        */
        return voxelList;
    }
    
    public static void createImageFromHistogramm(double[] histogramm, String imageName){
        
        BufferedImage bi = new BufferedImage(histogramm.length, 100, BufferedImage.TYPE_INT_RGB);
        
        for(int i=0;i<histogramm.length;i++){
            
            for(int j=0;j<100;j++){
                
                bi.setRGB(i, j, Color.BLACK.getRGB());
            }
        }
        
        for(int i=0;i<histogramm.length;i++){
            
            int limit = (int) (histogramm[i]*100);
            
            for(int j=100-limit;j<100;j++){
                
                bi.setRGB(i, j, Color.white.getRGB());
            }
        }
        
        try {
            ImageIO.write(bi, "png", new File(imageName));
        } catch (IOException ex) {
            Logger.getLogger(ImageEqualisation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void createImageFromHistogramm(int[] histogramm, String imageName){
        
        BufferedImage bi = new BufferedImage(histogramm.length, 100, BufferedImage.TYPE_INT_RGB);
        
        float valMax = 0, valMin = 0;
        for(int i=0;i<histogramm.length;i++){
            
            if(i == 0){
                valMin = histogramm[i];
                valMax = histogramm[i];
            }
            
            if(histogramm[i]>valMax){
                valMax = histogramm[i];
            }
            if(histogramm[i]<valMin){
                valMin = histogramm[i];
            }
            
            for(int j=0;j<100;j++){
                
                bi.setRGB(i, j, Color.BLACK.getRGB());
            }
        }
        
        for(int i=0;i<histogramm.length;i++){
            
            int limit = (int) ((histogramm[i]/(valMax-valMin))*100);
            
            for(int j=100-limit;j<100;j++){
                
                bi.setRGB(i, j, Color.white.getRGB());
            }
        }
        
        try {
            ImageIO.write(bi, "png", new File(imageName));
        } catch (IOException ex) {
            Logger.getLogger(ImageEqualisation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
