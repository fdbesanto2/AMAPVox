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

    public final static LaserSpecification LMS_Q560 = new LaserSpecification("LMS_Q560", 0.0003, 0.0005, false);
    public final static LaserSpecification LMS_Q780 = new LaserSpecification("LMS_Q780", 0.005, 0.00025, false);
    public final static LaserSpecification VZ_400 = new LaserSpecification("VZ_400", 0.007, 0.00035, false);
    public final static LaserSpecification LEICA_SCANSTATION_P30_40 = new LaserSpecification("LEICA_SCANSTATION_P30_40", 0.0035, 0.00023, true);
    public final static LaserSpecification LEICA_SCANSTATION_C10 = new LaserSpecification("LEICA_SCANSTATION_C10", 0.004, 0.0001, true);
    public final static LaserSpecification FARO_FOCUS_X330 = new LaserSpecification("FARO_FOCUS_X330", 0.0025, 0.00019, true);
    
    
    private final double beamDiameterAtExit;
    private final double beamDivergence;
    private final String name;
    private final boolean monoEcho;
    
    public LaserSpecification(String name, double beamDiameterAtExit, double beamDivergence, boolean monoEcho) {
        this.beamDiameterAtExit = beamDiameterAtExit;
        this.beamDivergence = beamDivergence;
        this.name = name;
        this.monoEcho = monoEcho;
    }

    public LaserSpecification(double beamDiameterAtExit, double beamDivergence, boolean monoEcho) {
        this("custum", beamDiameterAtExit, beamDivergence, monoEcho);
    }

    public double getBeamDiameterAtExit() {
        return beamDiameterAtExit;
    }

    public double getBeamDivergence() {
        return beamDivergence;
    }
    
    public boolean isMonoEcho() {
        return monoEcho;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Laser ").append(name).append("\n");
        str.append("  beam diameter at exit (meter) ").append((float) beamDiameterAtExit).append("\n");
        str.append("  beam divergence (radian) ").append((float) beamDivergence).append("\n");
        str.append("  mono-echo ").append(monoEcho);
        return str.toString();
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
