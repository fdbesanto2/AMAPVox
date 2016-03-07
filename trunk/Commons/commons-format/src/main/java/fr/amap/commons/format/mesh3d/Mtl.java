/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.format.mesh3d;

import fr.amap.commons.math.vector.Vec3F;

/**
 *
 * @author calcul
 */
public class Mtl {
    
    private String name;
    
    private final Vec3F diffuseColor;
    private final Vec3F ambientColor;
    private final Vec3F specularColor;

    public Mtl(String name, Vec3F diffuseColor, Vec3F ambientColor, Vec3F specularColor) {
        this.name = name;
        this.diffuseColor = diffuseColor;
        this.ambientColor = ambientColor;
        this.specularColor = specularColor;
    }

    public Vec3F getDiffuseColor() {
        return diffuseColor;
    }

    public Vec3F getAmbientColor() {
        return ambientColor;
    }

    public Vec3F getSpecularColor() {
        return specularColor;
    }
}
