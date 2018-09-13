package fr.amap.lidar.amapvox.commons;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.lang.reflect.Field;
import javax.vecmath.Point3d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Voxel {

    /**
     * indice du voxel, position en x
     */
    public int i;

    /**
     * indice du voxel, position en y
     */
    public int j;

    /**
     * indice du voxel, position en z
     */
    public int k;

    /**
     * Nombre de fois où un rayon à échantillonné le voxel
     */
    public int nbSampling;

    /**
     * Nombre d'échos dans le voxel
     */
    public int nbEchos;

    /**
     * Longueurs cumulées des trajets optiques dans le voxel Lorqu'il y a
     * interception dans le voxel vaut: distance du point d'entrée au point
     * d'interception Lorsqu'il y n'y a pas d'interception dans le voxel vaut:
     * distance du point d'entré au point de sortie
     */
    public float lgTotal;

    /**
     * Distance du voxel par rapport au sol
     */
    public float ground_distance;

    /**
     * Position du voxel dans l'espace Note: un attribut de voxel commençant par
     */
    public Point3d position;

    /**
     * Longueur moyenne du trajet optique dans le voxel En ALS est égal à:
     * pathLength / (nbSampling) En TLS est égal à: lgTraversant / (nbSampling -
     * nbEchos)
     */
    public double lMeanTotal;

    public double transmittance;
    public double transmittance_tmp;
    public double angleMean;
    public double bvEntering;
    public double bsEntering;
    public double bvIntercepted;
    public double bsIntercepted;
    public double bvPotential;
    public double PadBVTotal;
    public double cumulatedBeamVolume;
    public double cumulatedBeamVolumIn;
    public float passNumber;
    public float neighboursNumber;

    /**
     *
     * @param i
     * @param j
     * @param k
     */
    public Voxel(int i, int j, int k) {

        this.i = i;
        this.j = j;
        this.k = k;
    }

    public void setFieldValue(Class< ? extends Voxel> c, String fieldName, Object object, Object value)
            throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

        Field f = c.getField(fieldName);
        Class<?> type = f.getType();
        float v;

        switch (type.getName()) {
            case "double":
                v = (float) value;
                f.setDouble(object, (double) v);
                break;
            case "float":
                f.setFloat(object, (float) value);
                break;
            case "int":
                v = (float) value;
                f.setInt(object, (int) v);
                break;
        }
    }

    public double getFieldValue(Class< ? extends Voxel> c, String fieldName, Object o)
            throws SecurityException, NoSuchFieldException, IllegalAccessException {

        return c.getField(fieldName).getDouble(o);
    }
}
