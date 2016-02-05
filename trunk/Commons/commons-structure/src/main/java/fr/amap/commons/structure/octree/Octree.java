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

package fr.amap.commons.structure.octree;

import fr.amap.commons.util.Statistic;
import fr.amap.commons.math.geometry.BoundingBox3D;
import fr.amap.commons.math.geometry.BoundingBox3F;
import fr.amap.commons.math.geometry.Intersection;
import fr.amap.commons.math.point.Point3D;
import fr.amap.commons.math.point.Point3F;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class Octree {
    
    
    private Point3D[] points;
    private Point3D minPoint;
    private Point3D maxPoint;
    private int depth;
    private Node root;
    private List<Node> leafs;
    
    private final int maximumPoints;
    
    public final static short BINARY_SEARCH = 0;
    public final static short INCREMENTAL_SEARCH = 1;
    
    
    public Octree(int maximumPoints){
        this.maximumPoints = maximumPoints;
        depth = 0;
    }
    
    public void build() throws Exception{
        
        if(points != null){
            
            root = new Node(minPoint, maxPoint);
            
            for(int i=0;i<points.length;i++){
                
                root.insertPoint(this, i);
            }
            
        }else{
            throw new Exception("Attempt to build octree but points array is null");
        }
    }
    
    public List<Node> getLeafs(){
        
        leafs = new ArrayList<>();
        
        if(root != null){
            getChilds(root);
        }
        
        return leafs;
    }
    
    private void getChilds(Node node){
        
        if(node.hasChilds()){
            
            for(short i=0;i<8;i++){
                Node child = node.getChild(i);
                if(child.isLeaf()){
                    leafs.add(child);
                }else{
                    getChilds(child);
                }
            }

        }
    }
    
    public Node traverse(Point3D point){
        
        Node node = null;
        
        if(root != null){
            
            node = root;
            
            while(node.hasChilds()){
                short indice = node.get1DIndiceFromPoint(point);
                
                //le point est à l'extérieur de la bounding-box
                if(indice == -1){
                    return null;
                }
                
                Node child = node.getChild(indice);
                node = child;
                
            }
        }
        
        return node;
    }
    
    public Node traverse(Node node, Point3D source, Point3D end){
                
        BoundingBox3D boundingBox3D = new BoundingBox3D(node.getMinPoint(), node.getMaxPoint());
        Point3D intersection = Intersection.getIntersectionLineBoundingBox(source, end, boundingBox3D);

        while(node.hasChilds() && intersection != null){

            if(node.getPoints().length != 0){

            }else{
                for(int i=0;i<8;i++){
                    Node child = node.getChild((short) i);
                    node = traverse(child, source, end);
                }
            }
        }
        
        return node;
    }
    
    public Point3D rayIntersectNode(Node node, Point3D source, Point3D end){
                
        //on vérifie l'intersection avec le noeud root
        BoundingBox3D boundingBox3D = new BoundingBox3D(node.getMinPoint(), node.getMaxPoint());
        Point3D intersection = Intersection.getIntersectionLineBoundingBox(source, end, boundingBox3D);
        
        return intersection;
        //si la droite intersecte le noeud, on vérifie l'intersection avec tous les noeuds du root
        //dès qu'il y a intersection dans un ou plusieurs des enfants du noeud, on arrête le processus
        //pour chaque noeud qui intersecte la droite, 
    }
    
    //retourne le noeud intersecté le plus proche
    public Node rayTraversal(Node node, Point3D source, Point3D end){
                
        //on vérifie l'intersection avec le noeud root
        
        Node nearestIntersectedNode = null;
        
        Point3D intersection = rayIntersectNode(node, source, end);
            
        if(intersection != null){

            if(node.hasChilds()){ //le noeud a des enfants, on continue la recherche
                for(int i=0;i<8;i++){
                    Node child = node.getChild((short) i);
                    nearestIntersectedNode = rayTraversal(child, source, end);
                }
            }else if(node.getPoints().length > 0){ //le noeud n'a pas d'enfants et il contient des points
                nearestIntersectedNode = node;
            }else{ //le noeud n'a pas d'enfants et ne contient pas de points
                return null;
            }

        }
        
        return nearestIntersectedNode;
        
        //si la droite intersecte le noeud, on vérifie l'intersection avec tous les noeuds du root
        //dès qu'il y a intersection dans un ou plusieurs des enfants du noeud, on arrête le processus
        //pour chaque noeud qui intersecte la droite, 
    }
    
    public Point3D searchNearestPoint(Point3D point, short type, float errorMargin){
        
        switch(type){
            case BINARY_SEARCH:
                throw new UnsupportedOperationException("The binary search has not been implemented yet, use the incremental search instead");
            case INCREMENTAL_SEARCH:
                return incrementalSearchNearestPoint(point, errorMargin);
        }
        
        return null;
    }
    
    public boolean isPointBelongsToPointcloud(Point3D point, float errorMargin, short type){
        
        Point3D incrementalSearchNearestPoint = searchNearestPoint(point, type, errorMargin);
                            
                            
        boolean test = false;
        if(incrementalSearchNearestPoint != null){
            double distance = point.distanceTo(incrementalSearchNearestPoint);
            

            if(distance < errorMargin){
                test = true;
            }
        }
        
        return test;
    }
    
    private Point3D binarySearchNearestPoint(Point3D point, float errorMargin){
        
        return null;
    }
    
    private Point3D incrementalSearchNearestPoint(Point3D point, float errorMargin){
        
        Point3D nearestPoint = null;
        
        if(root != null){
            
            int[] nearestPoints;
            double distance = 99999999;
                
            List<Node> nodesIntersectingSphere = new ArrayList<>();

            Sphere searchArea = new Sphere(point, errorMargin);

            incrementalSphereIntersectionSearch(nodesIntersectingSphere, root, searchArea);

            for(Node node : nodesIntersectingSphere){

                nearestPoints = node.getPoints();

                if(nearestPoints != null){

                    for (int pointToTest : nearestPoints) {

                        double dist = point.distanceTo(points[pointToTest]);

                        if(dist < distance){
                            distance = dist;
                            nearestPoint = points[pointToTest];
                        }
                    }
                }
            }
        }
        
        return nearestPoint;
    }
    
    private void incrementalSphereIntersectionSearch(List<Node> nodesIntersectingSphere, Node node, Sphere sphere){
        
        if(sphereIntersection(node, sphere)){
                    
            if(node.hasChilds()){

                for(short i=0;i<8;i++){
                    Node child = node.getChild(i);
                    boolean intersect = sphereIntersection(child, sphere);

                    if(child.isLeaf() && intersect){
                        nodesIntersectingSphere.add(child);
                    }else{
                        incrementalSphereIntersectionSearch(nodesIntersectingSphere, child, sphere);
                    }
                }

            }else{
                nodesIntersectingSphere.add(node);
            }
        }
    }
    
    private boolean sphereIntersection(Node node, Sphere sphere){
        
        float dist_squared = sphere.getRadius()*sphere.getRadius();
        
        Point3D sphereCenter = sphere.getCenter();
        
        Point3D nodeBottomCorner = node.getMinPoint();
        Point3D nodeTopCorner = node.getMaxPoint();
        
        if (sphereCenter.x < nodeBottomCorner.x){
            dist_squared -= Math.pow(sphereCenter.x - nodeBottomCorner.x, 2);
        }else if (sphereCenter.x > nodeTopCorner.x) {
            dist_squared -= Math.pow(sphereCenter.x - nodeTopCorner.x, 2);
        }
        
        if (sphereCenter.y < nodeBottomCorner.y){
            dist_squared -= Math.pow(sphereCenter.y - nodeBottomCorner.y, 2);
        }else if (sphereCenter.y > nodeTopCorner.y) {
            dist_squared -= Math.pow(sphereCenter.y - nodeTopCorner.y, 2);
        }
        
        if (sphereCenter.z < nodeBottomCorner.z){
            dist_squared -= Math.pow(sphereCenter.z - nodeBottomCorner.z, 2);
        }else if (sphereCenter.z > nodeTopCorner.z) {
            dist_squared -= Math.pow(sphereCenter.z - nodeTopCorner.z, 2);
        }
        
        return dist_squared > 0;
    }

    public int getMaximumPoints() {
        return maximumPoints;
    }

    public Point3D[] getPoints() {
        return points;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public void setPoints(Point3D[] points) {
        
        double minPointX = 0, minPointY = 0, minPointZ = 0;
        double maxPointX = 0, maxPointY = 0, maxPointZ = 0;
                
        boolean init = false;
        
        for(Point3D point : points){
            
            if(!init){
                minPointX = point.x;
                minPointY = point.y;
                minPointZ = point.z;
                
                maxPointX = point.x;
                maxPointY = point.y;
                maxPointZ = point.z;
                
                init = true;
                
            }else{
                
                if(point.x > maxPointX){
                    maxPointX = point.x;
                }else if(point.x < minPointX){
                    minPointX = point.x;
                }
                
                if(point.y > maxPointY){
                    maxPointY = point.y;
                }else if(point.y < minPointY){
                    minPointY = point.y;
                }
                
                if(point.z > maxPointZ){
                    maxPointZ = point.z;
                }else if(point.z < minPointZ){
                    minPointZ = point.z;
                }
            }
        }
        
        this.points = points;
        
        setMinPoint(new Point3D(minPointX, minPointY, minPointZ));
        setMaxPoint(new Point3D(maxPointX, maxPointY, maxPointZ));
        
    }
    
    public void setPoints(float[] points) {
        
        Point3D minPoint = null;
        Point3D maxPoint = null;
        
        this.points = new Point3D[points.length/3];
        
        for(int i=0, j=0;i<points.length;i++,j+=3){
            
            this.points[i] = new Point3D(points[i], points[i+1], points[i+2]);
            
            if(i == 0){
                minPoint = this.points[i];
                maxPoint = this.points[i];
            }else{
                int minPointComparison = minPoint.compareTo(this.points[i]);
                int maxPointComparison = maxPoint.compareTo(this.points[i]);
                
                if(minPointComparison < 0){
                    minPoint = this.points[i];
                }
                
                if(maxPointComparison > 0){
                    maxPoint = this.points[i];
                }
                
            }
        }
        
        this.setMinPoint(minPoint);
        this.setMaxPoint(maxPoint);
    }

    public void setMinPoint(Point3D minPoint) {
        this.minPoint = minPoint;
    }

    public void setMaxPoint(Point3D maxPoint) {
        this.maxPoint = maxPoint;
    }
    
}
