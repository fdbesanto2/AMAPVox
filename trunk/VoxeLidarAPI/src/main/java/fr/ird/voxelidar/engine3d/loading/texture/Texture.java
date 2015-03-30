/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.loading.texture;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.util.texture.TextureData;
import com.jogamp.opengl.util.texture.awt.AWTTextureIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.text.DecimalFormat;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class Texture {
    
    final static Logger logger = Logger.getLogger(Texture.class);
    
    private int id;
    
    private int width, height;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    
    

    public int getId() {
        return id;
    }
    
    
    public static BufferedImage createColorScaleImage(BufferedImage image, float min , float max){
        
        DecimalFormat format = new DecimalFormat();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        
        BufferedImage imageWithTextcaption = new BufferedImage(image.getWidth()+64, image.getHeight(), image.getType());
        Graphics2D graphics = (Graphics2D)imageWithTextcaption.createGraphics();
        
        graphics.drawImage(image, 0, 0, null);
        graphics.setPaint(Color.red);
        graphics.setFont(new Font("Serif",Font.PLAIN,20));
        
        
        String maxText = format.format(max);
        FontMetrics fm = graphics.getFontMetrics();
        int x = imageWithTextcaption.getWidth() - fm.stringWidth(maxText);
        int y = fm.getHeight()/2;        
        graphics.drawString(maxText, x, y);
        
        String minText = format.format(min);
        fm = graphics.getFontMetrics();
        x = imageWithTextcaption.getWidth() - fm.stringWidth(minText);
        y = imageWithTextcaption.getHeight()-5;        
        graphics.drawString(minText, x, y);
        
        return imageWithTextcaption;
    }
    /*
    public static Texture createColorScaleTexture(GL3 gl, BufferedImage image, float min , float max){
        
        DecimalFormat format = new DecimalFormat();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        
        Texture texture = new Texture(gl, image);
        
        BufferedImage imageWithTextcaption = new BufferedImage(image.getWidth()+64, image.getHeight(), image.getType());
        Graphics2D graphics = (Graphics2D)imageWithTextcaption.createGraphics();
        
        graphics.drawImage(image, 0, 0, null);
        graphics.setPaint(Color.red);
        graphics.setFont(new Font("Serif",Font.PLAIN,20));
        
        
        String maxText = format.format(max);
        FontMetrics fm = graphics.getFontMetrics();
        int x = imageWithTextcaption.getWidth() - fm.stringWidth(maxText);
        int y = fm.getHeight()/2;        
        graphics.drawString(maxText, x, y);
        
        String minText = format.format(min);
        fm = graphics.getFontMetrics();
        x = imageWithTextcaption.getWidth() - fm.stringWidth(minText);
        y = imageWithTextcaption.getHeight()-5;        
        graphics.drawString(minText, x, y);
        
        IntBuffer tmp = IntBuffer.allocate(1);
        gl.glGenTextures(1, tmp);
        texture.id = tmp.get(0);
        TextureData textureData = AWTTextureIO.newTextureData(gl.getGLProfile(), imageWithTextcaption, false);
        gl.glEnable(GL3.GL_TEXTURE_2D);
        
        gl.glBindTexture(GL3.GL_TEXTURE_2D, texture.id);
        
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_BORDER);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_BORDER);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);

        int internalFormat = textureData.getInternalFormat();
        int width = textureData.getWidth();
        int height = textureData.getHeight();
        int border = textureData.getBorder();
        int pixelFormat = textureData.getPixelFormat();
        int pixelType = textureData.getPixelType();
        Buffer buffer = textureData.getBuffer();
        

        try{
            gl.glTexImage2D(GL3.GL_TEXTURE_2D,         // target
            0,// level, 0 = base, no minimap,
            internalFormat, // internalformat
            width,// width
            height,    // height
            border,// border, always 0 in OpenGL ES
            pixelFormat,// format
            pixelType,// type
            buffer);
        }catch(Exception e){
            logger.error("cannot create texture from image", e);
        }
            
        gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
        
        texture.setWidth(width);
        texture.setHeight(height);
        
        return texture;
    }*/
    
    public static Texture createColorScaleTexture(GL3 gl, BufferedImage image, float min , float max){
        
        DecimalFormat format = new DecimalFormat();
        format.setMinimumFractionDigits(2);
        format.setMaximumFractionDigits(2);
        
        Texture texture = new Texture();
        
        BufferedImage imageWithTextcaption = new BufferedImage(image.getWidth()+40, image.getHeight()+30, image.getType());
        Graphics2D graphics = (Graphics2D)imageWithTextcaption.createGraphics();
        
        graphics.drawImage(image, 20, 0, null);
        graphics.setPaint(Color.red);
        graphics.setFont(new Font("Comic Sans MS",Font.PLAIN,30));
        
        
        String maxText = format.format(max);
        FontMetrics fm = graphics.getFontMetrics();
        int x = imageWithTextcaption.getWidth()-fm.stringWidth(maxText);
        int y = imageWithTextcaption.getHeight();        
        graphics.drawString(maxText, x, y);
        
        String minText = format.format(min);
        fm = graphics.getFontMetrics();
        x = 0;
        y = imageWithTextcaption.getHeight();        
        graphics.drawString(minText, x, y);
        
        IntBuffer tmp = IntBuffer.allocate(1);
        gl.glGenTextures(1, tmp);
        texture.id = tmp.get(0);
        TextureData textureData = AWTTextureIO.newTextureData(gl.getGLProfile(), imageWithTextcaption, false);
        gl.glEnable(GL3.GL_TEXTURE_2D);
        
        gl.glBindTexture(GL3.GL_TEXTURE_2D, texture.id);
        
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_BORDER);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_BORDER);
        gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);

        int internalFormat = textureData.getInternalFormat();
        int width = textureData.getWidth();
        int height = textureData.getHeight();
        int border = textureData.getBorder();
        int pixelFormat = textureData.getPixelFormat();
        int pixelType = textureData.getPixelType();
        Buffer buffer = textureData.getBuffer();
        

        try{
            gl.glTexImage2D(GL3.GL_TEXTURE_2D,         // target
            0,// level, 0 = base, no minimap,
            internalFormat, // internalformat
            width,// width
            height,    // height
            border,// border, always 0 in OpenGL ES
            pixelFormat,// format
            pixelType,// type
            buffer);
        }catch(Exception e){
            logger.error("cannot create texture from image", e);
        }
            
        gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
        
        texture.setWidth(width);
        texture.setHeight(height);
        
        return texture;
    }
    
    public static Texture createFromFile(GL3 gl, File file){
        
        try {
            BufferedImage image = ImageIO.read(file);
            
            Texture texture = new Texture();
            
            IntBuffer tmp = IntBuffer.allocate(1);
            gl.glGenTextures(1, tmp);
            texture.id = tmp.get(0);
            TextureData textureData = AWTTextureIO.newTextureData(gl.getGLProfile(), image, false);
            gl.glEnable(GL3.GL_TEXTURE_2D);

            gl.glBindTexture(GL3.GL_TEXTURE_2D, texture.id);

            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_S, GL3.GL_CLAMP_TO_BORDER);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_WRAP_T, GL3.GL_CLAMP_TO_BORDER);
            gl.glTexParameteri(GL3.GL_TEXTURE_2D, GL3.GL_TEXTURE_MIN_FILTER, GL3.GL_LINEAR);

            int internalFormat = textureData.getInternalFormat();
            int width = textureData.getWidth();
            int height = textureData.getHeight();
            int border = textureData.getBorder();
            int pixelFormat = textureData.getPixelFormat();
            int pixelType = textureData.getPixelType();
            Buffer buffer = textureData.getBuffer();


            try{
                gl.glTexImage2D(GL3.GL_TEXTURE_2D,         // target
                0,// level, 0 = base, no minimap,
                internalFormat, // internalformat
                width,// width
                height,    // height
                border,// border, always 0 in OpenGL ES
                pixelFormat,// format
                pixelType,// type
                buffer);
            }catch(Exception e){
                logger.error("cannot create texture from image", e);
            }

            gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);

            texture.setWidth(width);
            texture.setHeight(height);

            return texture;
            
        } catch (IOException ex) {
            logger.error("cannot create texture from image", ex);
        }
        
        return null;
    }
    
    public Texture(){
        
        
    }
}
