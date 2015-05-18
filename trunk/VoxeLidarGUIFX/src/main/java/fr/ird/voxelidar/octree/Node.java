
/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package fr.ird.voxelidar.octree;

import fr.ird.voxelidar.engine3d.math.point.Point3F;
import fr.ird.voxelidar.engine3d.math.point.Point3I;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class Node {
    
    private final static Logger logger = Logger.getLogger(Node.class);
    
    private boolean leaf;
    private Node[] childs;
    private int[] points;
    private int pointNumber;
    private Point3F minPoint;
    private Point3F maxPoint;
    
    public Node(Point3F minPoint, Point3F maxPoint){
        
        this.minPoint = minPoint;
        this.maxPoint = maxPoint;
        
        init();
    }
    
    public Node(Node parent, short indice){
        
        if(parent != null && indice >= 0 && indice <= 7){
            
            init();
            
            short[] decToBin = decToBin(indice);
            
            short indiceX = decToBin[0];
            short indiceY = decToBin[1];
            short indiceZ = decToBin[2];
            
            float minPointX, minPointY, minPointZ;
            float maxPointX, maxPointY, maxPointZ;
            
            if(indiceX == 0){
                minPointX = parent.minPoint.x;
                maxPointX = (parent.minPoint.x+parent.maxPoint.x)/2.0f;
            }else{
                minPointX = (parent.minPoint.x+parent.maxPoint.x)/2.0f;
                maxPointX = parent.maxPoint.x;
            }
            
            if(indiceY == 0){
                minPointY = parent.minPoint.y;
                maxPointY = (parent.minPoint.y+parent.maxPoint.y)/2.0f;
            }else{
                minPointY = (parent.minPoint.y+parent.maxPoint.y)/2.0f;
                maxPointY = parent.maxPoint.y;
            }
            
            if(indiceZ == 0){
                minPointZ = parent.minPoint.z;
                maxPointZ = (parent.minPoint.z+parent.maxPoint.z)/2.0f;
            }else{
                minPointZ = (parent.minPoint.z+parent.maxPoint.z)/2.0f;
                maxPointZ = parent.maxPoint.z;
            }
            
            minPoint = new Point3F(minPointX, minPointY, minPointZ);
            maxPoint = new Point3F(maxPointX, maxPointY, maxPointZ);
            
        }else{
            logger.error("Cannot instantiate node cause parent is null or indice is not range between 0 to 7");
        }
    }
    
    private void init(){
        leaf = true;
        childs = null;
    }

    public boolean isLeaf() {
        return leaf;
    }
    
    public void insertPoint(Octree octree, int indice){
        
        //la condition est vraie quand aucun point n'a déjà été ajouté au noeud
        if(childs == null && points == null){
            
            points = new int[octree.getMaximumPoints()];
            pointNumber = 0;
        }
        
        
        if(childs == null && points !=null){
            
            if(pointNumber < points.length){ 
            
                points[pointNumber] = indice;
                pointNumber++;
            }else{ //la condition est vraie quand le nombre maximum de points dans le noeud a été atteint
                
                // on crée les enfants
                subdivide();
                
                //on replace tous les points du noeud dans ses enfants
                for(int i = 0 ; i< pointNumber;i++){
                    insertPoint(octree, points[i]);
                }
                
                points = null;
            }
            
            
        }
        
        if(childs != null){
            
            //on détermine dans quel enfant se trouve le point
            int childContainingPointID;
            Point3F point = octree.getPoints()[indice];

            Point3I indices = getIndicesFromPoint(point);

            childContainingPointID = get1DIndiceFrom3DIndices(indices.x, indices.y, indices.z);

            if(childContainingPointID >= 0 && childContainingPointID <=7){
                //on ajoute le point à l'enfant
                childs[childContainingPointID].insertPoint(octree, indice);
            }else{
                logger.error("");
            }
            
        }
    }
    
    public short getIndiceFromPoint(Point3F point){
        
        Point3I indices = getIndicesFromPoint(point);
        return get1DIndiceFrom3DIndices(indices.x, indices.y, indices.z);
    }
    
    public Point3I getIndicesFromPoint(Point3F point){
        
        int indiceX = (int)((point.x-minPoint.x)/((maxPoint.x-minPoint.x)/2.0f));
        int indiceY = (int)((point.y-minPoint.y)/((maxPoint.y-minPoint.y)/2.0f));
        int indiceZ = (int)((point.z-minPoint.z)/((maxPoint.z-minPoint.z)/2.0f));

        if(indiceX == 2.0f){indiceX = 1;}
        if(indiceY == 2.0f){indiceY = 1;}
        if(indiceZ == 2.0f){indiceZ = 1;}
        
        return new Point3I(indiceX, indiceY, indiceZ);
    }
    
    public boolean hasChilds(){
        
        return childs != null;
    }

    public int[] getPoints() {
        return points;
    }
    
    private void subdivide(){
        
        childs = new Node[8];
        
        for(short i=0;i<8;i++){
            childs[i] = new Node(this, i);
        }
        
        leaf = false;
    }
    
    private short get1DIndiceFrom3DIndices(int bit1, int bit2, int bit3){
        
        short dec = 0;
        
        dec += bit1 * Math.pow(2, 2);
        dec += bit2 * Math.pow(2, 1);
        dec += bit3 * Math.pow(2, 0);
        
        return dec;
    }
    
    private short[] decToBin(short decimal){
        
        short[] result = new short[3];
        short tmp = decimal;
        
        for(short i=2 ; i >= 0 ; i--){
            result[i] = (short) (tmp%2);
            tmp = (short) (tmp/2);
        }
        
        return result;
    }
    
    public Node getChild(short indice){
        
        if(childs != null){
            return childs[indice];
        }
        
        return null;
    }
}
