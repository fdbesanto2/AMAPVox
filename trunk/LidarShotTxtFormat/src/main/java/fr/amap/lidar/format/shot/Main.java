/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.format.shot;

import java.io.File;

/**
 *
 * @author Julien Heurtebize
 */
public class Main {
    
    public static void main(String[] args) throws Exception {
        
        ShotFileContext context = new ShotFileContext(
                new Column("reflectance", Column.Type.FLOAT),
                new Column("amplitude", Column.Type.FLOAT),
                new Column("deviation", Column.Type.FLOAT),
                new Column("time", Column.Type.DOUBLE));
        
        ShotWriter writer = new ShotWriter(context, new File("/home/julien/Bureau/tmp/shots_written.txt"));
        
        writer.write(new Shot(0, 0, 0, 0, 1, 0, 0));
        writer.write(new Shot(1, 0, 0, 0, 0, 1, 0, new Echo(50, 0.1, 0.2, 20, 521010)
                                                 , new Echo(60, 0.2, 0.2, 30, 521010)));
        writer.write(new Shot(2, 0.1, -0.1, 0.05, 0.2, 0.3, 0.4));
        writer.write(new Shot(3, 0, 0, 0, 0, 1, 0, new Echo(50, 0.3, 0.2, 50, 521011)
                                                 , new Echo(60, 0.4, 0.2, 20, 521011)
                                                 , new Echo(70, 0.5, 0.2, 40, 521011)));
        
        writer.close();
        
        ShotReader shotReader = new ShotReader(new File("/home/julien/Bureau/tmp/shots.txt"));
        
        IteratorWithException<Shot> iterator = shotReader.iterator();
        
        Shot shot;
        
        while((shot = iterator.next()) != null){
            System.out.println(shot.getXOrigin() + " "+shot.getYOrigin()+" "+shot.getZOrigin() +" " +
                    shot.getXDirection() + " "+shot.getYDirection()+" "+shot.getZDirection() +" " );
            
            for (int i = 0; i < shot.getNbEchoes(); i++) {
                Echo echo = shot.getEchoes()[i];
                System.out.println("Echo "+i+
                        " , range : "+echo.getRange()+
                        " , reflectance : "+echo.getAttribute("reflectance", shotReader.getContext()));
            }
        }
    }
}
