/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.als;

/**
 *
 * @author Julien Heurtebize
 * @param <T> the type of elements returned by this iterator
 */
public interface IteratorWithException<T>{

    public boolean hasNext();

    public T next() throws Exception;
    
}
