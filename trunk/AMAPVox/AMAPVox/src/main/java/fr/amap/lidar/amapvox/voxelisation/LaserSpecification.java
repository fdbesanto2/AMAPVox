/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Julien Heurtebize
 */
public class LaserSpecification {
    
    public final static LaserSpecification LMS_Q560 = new LaserSpecification(0.0003, 0.0005, "LMS_Q560");
    public final static LaserSpecification VZ_400 = new LaserSpecification(0.007, 0.00035, "VZ_400");
    public final static LaserSpecification LEICA_SCANSTATION_P30_40 = new LaserSpecification(0.0035, 0.00023, "LEICA_SCANSTATION_P30_40");
    public final static LaserSpecification LEICA_SCANSTATION_C10 = new LaserSpecification(0.004, 0.0001, "LEICA_SCANSTATION_C10");
    
    private final double beamDiameterAtExit;
    private final double beamDivergence;
    private final String name;

    public LaserSpecification(double beamDiameterAtExit, double beamDivergence) {
        this.beamDiameterAtExit = beamDiameterAtExit;
        this.beamDivergence = beamDivergence;
        this.name = "custom";
    }
    
    public LaserSpecification(double beamDiameterAtExit, double beamDivergence, String name) {
        this.beamDiameterAtExit = beamDiameterAtExit;
        this.beamDivergence = beamDivergence;
        this.name = name;
    }

    public double getBeamDiameterAtExit() {
        return beamDiameterAtExit;
    }

    public double getBeamDivergence() {
        return beamDivergence;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
    
    public static List<LaserSpecification> getPresets() {

        List<LaserSpecification> presets = new ArrayList<>();

        Field[] declaredFields = LaserSpecification.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == LaserSpecification.class) {
                try {
                    presets.add((LaserSpecification) field.get(null));
                } catch (IllegalArgumentException | IllegalAccessException ex) {}
            }
        }

        return presets;

    }
    
}
