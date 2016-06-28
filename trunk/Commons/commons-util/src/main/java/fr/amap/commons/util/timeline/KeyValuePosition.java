/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util.timeline;

/**
 *
 * @author Julien Heurtebize
 */
public class KeyValuePosition implements Comparable<KeyValuePosition>{
    
    public Integer timelineIndex;
    public Integer keyframeIndex;

    public KeyValuePosition(int timelineIndex, int keyframeIndex) {
        this.timelineIndex = timelineIndex;
        this.keyframeIndex = keyframeIndex;
    }

    @Override
    public int compareTo(KeyValuePosition o) {
        return this.timelineIndex.compareTo(o.timelineIndex);
    }
}
