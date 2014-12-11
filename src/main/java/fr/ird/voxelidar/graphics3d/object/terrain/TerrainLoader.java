/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.object.terrain;

import fr.ird.voxelidar.graphics3d.mesh.Face;
import fr.ird.voxelidar.io.file.FileAdapter;
import fr.ird.voxelidar.io.file.FileManager;
import fr.ird.voxelidar.math.vector.Vec3F;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class TerrainLoader {

    final static Logger logger = Logger.getLogger(TerrainLoader.class);
    
    public static Terrain readFromFile(String path){
        
        final String pathFile = path;
        
        FileManager m = new FileManager();
        
        m.addFileListener(new FileAdapter() {
            @Override
            public void fileRead(){
                logger.debug("terrain file "+pathFile+" read");
            }
        });
        

        ArrayList<String> lines = m.readAllLines(pathFile);

        //create points list
        ArrayList<Vec3F> points = new ArrayList<>();

        for (int i=0;i<lines.size();i++) {

            if(lines.get(i) != null){

                String[] split = lines.get(i).split(" ");
                Float x = Float.valueOf(split[0]);
                Float y = Float.valueOf(split[2]);
                Float z = Float.valueOf(split[1]);

                points.add(new Vec3F(x, y, z));
            }


        }

        //create faces list
        ArrayList<Face> faces = new ArrayList<>();
        
        //on regarde sur quel axe se fait l'ordre de parcours des points
        Vec3F point1 = points.get(0);
        Vec3F point2 = points.get(1);
        
        int largeur = 0;
        
        if(point1.x != point2.x){
            
            //on cherche la largeur d'une bande de terrain
            for(int i=0;i<points.size();i++){
                
                if((points.get(i).x == points.get(0).x) && (i != 0)){
                    largeur = i;
                    
                     i = points.size()-1;
                }
            }
            
            //on crée les faces
            int line = 1;
            for(int i=0;i<points.size();i++){
            
                if((i % (largeur)) == 0 && i>0){
                    line++;
                }
                
                int pt1 = i;
                int pt2 = i+1;
                if(pt2 < (largeur*line)){
                    
                    int pt3 = pt1+largeur;
                    int pt4 = pt2+largeur;
                    
                    Face triangle1 = new Face(pt1, pt2, pt3);
                    faces.add(triangle1);
                    
                    if(pt4 <= points.size() -1){
                        
                        Face triangle2 = new Face(pt2, pt3, pt4);
                        faces.add(triangle2);
                    } 
                }
                
            }
            
        }else if(point1.y != point2.y){
            
        }else{
            logger.error("cannot reconstruct terrrin from points, must be rectangular");
        }
        /*
        ArrayList<Short> faces = new ArrayList<>();
        int largeur =42+1;

        for(int i = 0;i<points.size();i++){

            faces.add((short)i);
            faces.add((short)(i+1));
            faces.add((short)(i+largeur));
        }
        */
        Terrain terrain = new Terrain(path, points, faces);
        
        //terrain.exportObj();

        return terrain;
    }
}
