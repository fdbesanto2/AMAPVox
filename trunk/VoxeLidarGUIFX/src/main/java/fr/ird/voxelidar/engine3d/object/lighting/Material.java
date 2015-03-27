/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.lighting;

import java.awt.Color;
import java.util.Set;

/**
 *
 * @author calcul
 */
public class Material {
    
    private Shader shader;
    private Set<Texture> texture;
    private Color diffuse;
    private Color ambiant;
    private Color specular;
    
    public void setTexture(){
        
    }
}
