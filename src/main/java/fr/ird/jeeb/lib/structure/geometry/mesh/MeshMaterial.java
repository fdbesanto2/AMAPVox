package fr.ird.jeeb.lib.structure.geometry.mesh;

import java.awt.Color;
import java.io.Serializable;

public class MeshMaterial implements Serializable{

	static private final long serialVersionUID = 1L;
	
	float [] emission = {0,0,0,0};
	float [] ambiant = {0.1f,0.1f,0.1f,0.5f};
	float [] diffuse = {0.6f,0.6f,0.6f,0.5f};
	float [] specular= {0.6f,0.6f,0.6f,0.5f};
	float shininess= 10f;
	String textureFileName = null;
	
	public MeshMaterial () {
		
	}
	
	public MeshMaterial (MeshMaterial m) {
		this.emission = m.emission.clone();
		this.ambiant = m.ambiant.clone();
		this.diffuse = m.diffuse.clone();
		this.specular = m.specular.clone();
		this.shininess = m.shininess;
		this.textureFileName = m.textureFileName;
	}

	public float[] getAmbiant() {
		return ambiant;
	}
	public float[] getDiffuse() {
		return diffuse;
	}
	public float[] getEmission() {
		return emission;
	}
	public float getShininess() {
		return shininess;
	}
	public float[] getSpecular() {
		return specular;
	}
	public String getTextureFileName() {
		return textureFileName;
	}
	public void setAmbiant(float[] ambiant) {
		this.ambiant = ambiant;
	}
	public void setDiffuse(float[] diffuse) {
		this.diffuse = diffuse;
	}
	public void setEmission(float[] emission) {
		this.emission = emission;
	}
	public void setShininess(float shininess) {
		this.shininess = shininess;
	}
	public void setSpecular(float[] specular) {
		this.specular = specular;
	}
	public void setColor(Color color) {
		float[] colorf = new float[4];
		colorf[0] = color.getRed() / 255f;
		colorf[1] = color.getGreen() / 255f;
		colorf[2] = color.getBlue() / 255f;
		colorf[3] = 0.5f;
		this.ambiant = colorf;
		this.diffuse = colorf;
		this.specular = colorf;		
	}
	public void setTextureFileName(String textureFileName) {
		this.textureFileName = textureFileName;
	}
	
}
