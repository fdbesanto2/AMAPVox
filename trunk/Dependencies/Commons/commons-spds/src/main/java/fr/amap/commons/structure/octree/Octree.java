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

import fr.amap.commons.math.geometry.BoundingBox3D;
import fr.amap.commons.math.geometry.Distance;
import fr.amap.commons.math.geometry.Intersection;
import fr.amap.commons.math.point.Point3D;
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
    private ArrayList<Node> nodes;
    
    private final int maximumPoints;
    
    public final static short BINARY_SEARCH = 0;
    public final static short INCREMENTAL_SEARCH = 1;
    
    
    public Octree(int maximumPoints){
        this.maximumPoints = maximumPoints;
        depth = 0;
    }
    
    /**
     * 
     * @throws Exception 
     */
    public void build() throws Exception{
        
        if(points != null){
            
            nodes = new ArrayList<>();
            root = new Node(minPoint, maxPoint, 0);
            nodes.add(root);
            
            for(int i=0;i<points.length;i++){
                
                if(points[i] != null){
                    root.insertElement(this, i);
                }
            }
            
            nodes.trimToSize();
            
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
    
    /**
     * 
     * @param point
     * @param type
     * @param errorMargin
     * @return 
     */
    public Point3D searchNearestPoint(Point3D point, short type, float errorMargin){
        
        switch(type){
            case BINARY_SEARCH:
                throw new UnsupportedOperationException("The binary search has not been implemented yet, use the incremental search instead");
            case INCREMENTAL_SEARCH:
                return incrementalSearchNearestPoint(point, errorMargin);
        }
        
        return null;
    }
    
    /**
     * 
     * @param point
     * @param errorMargin
     * @param type
     * @return 
     */
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
    
    /**
     * 
     * @param point
     * @param errorMargin
     * @return 
     */
    private Point3D binarySearchNearestPoint(Point3D point, float errorMargin){
        
        return null;
    }
    
    /**
     * 
     * @param point
     * @param errorMargin
     * @return 
     */
    private Point3D incrementalSearchNearestPoint(Point3D point, float errorMargin){
        
        Point3D nearestPoint = null;
        
        if(root != null){
            
            int[] nearestPoints;
            double distance = 99999999;
                
            List<Node> nodesIntersectingSphere = new ArrayList<>();

            Sphere searchArea = new Sphere(point, errorMargin);

            incrementalSphereIntersectionSearch(nodesIntersectingSphere, root, searchArea);

            for(Node node : nodesIntersectingSphere){

                nearestPoints = node.getElements();

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
    
    /**
     * 
     * @param nodesIntersectingSphere
     * @param node
     * @param sphere 
     */
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
    
    /**
     * 
     * @param node
     * @param sphere
     * @return 
     */
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
    
    /**
     * Get the closest element to a ray as a line segment.
     * @param source The line element start point
     * @param end The line element end point
     * @param limit The maximal distance between the line and the element
     * @return <p>Negative value if no element was found 
     * (-2 if octree was not built, -3 if the element if greater than the limit, -1 if no intersection was found)</p>
     * Positive value if an element was found, the returned value if the element index.
     */
    public int getClosestElement(Point3D source, Point3D end, float limit){
        
        if(root != null){
            
            List<Node> intersectedNodes = new ArrayList<>();
            intersectedNodes = getIntersectedNodesOfRay(intersectedNodes, root, source, end);
            
            float minDistance = 999999999;
            int closestElement = -1;
            
            if(intersectedNodes.isEmpty()){
                return -1;
            }
            
            for(Node node : intersectedNodes){
                
                int[] elements = node.getElements();
                
                for(int elementID : elements){
                    float currentDistance = Distance.getPointLineDistance(points[elementID], source, end);
                    if(currentDistance < minDistance){
                        minDistance = currentDistance;
                        closestElement = elementID;
                    }
                }
            }
            
            if(minDistance <= limit){
                return closestElement;
            }else{
                return -3;
            }
        }else{
            return -2;
        }
    }
    
    private List<Node> getIntersectedNodesOfRay(List<Node> intersectedNodes, Node currentNode, Point3D source, Point3D end){
        
        Point3D intersection = isRayIntersectNode(currentNode, source, end);
            
        if(intersection != null){

            if(currentNode.hasChilds()){
                for(int i=0;i<8;i++){

                    Node child = currentNode.getChild((short)i);
                    if(child.hasChilds()){
                        //on teste les enfants
                        intersectedNodes = getIntersectedNodesOfRay(intersectedNodes, child, source, end);

                    }else{
                        if(child.getElements() != null){
                            //on ajoute le noeud à la liste
                            intersectedNodes.add(child);
                        }
                        
                    }
                }
            }
        }
        
        return intersectedNodes;
    }
    
    public Point3D isRayIntersectNode(Node node, Point3D source, Point3D end){
                
        BoundingBox3D boundingBox3D = new BoundingBox3D(node.getMinPoint(), node.getMaxPoint());
        Point3D intersection = Intersection.getIntersectionLineBoundingBox(source, end, boundingBox3D);
        
        return intersection;
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
                
        double minPointX = 0, minPointY = 0, minPointZ = 0;
        double maxPointX = 0, maxPointY = 0, maxPointZ = 0;
        
        boolean init = false;
        
        this.points = new Point3D[points.length/3];
        
        for(int i=0, j=0;i<this.points.length;i++, j+=3){
            
            float x = points[j];
            float y = points[j+1];
            float z = points[j+2];
            
            if(Float.isNaN(x)||Float.isNaN(y)||Float.isNaN(z)){
                
            }else{
                this.points[i] = new Point3D(x, y, z);
            
                if(!init){
                    minPointX = this.points[i].x;
                    minPointY = this.points[i].y;
                    minPointZ = this.points[i].z;

                    maxPointX = this.points[i].x;
                    maxPointY = this.points[i].y;
                    maxPointZ = this.points[i].z;

                    init = true;

                }else{

                    if(this.points[i].x > maxPointX){
                        maxPointX = this.points[i].x;
                    }else if(this.points[i].x < minPointX){
                        minPointX = this.points[i].x;
                    }

                    if(this.points[i].y > maxPointY){
                        maxPointY = this.points[i].y;
                    }else if(this.points[i].y < minPointY){
                        minPointY = this.points[i].y;
                    }

                    if(this.points[i].z > maxPointZ){
                        maxPointZ = this.points[i].z;
                    }else if(this.points[i].z < minPointZ){
                        minPointZ = this.points[i].z;
                    }
                }
            }
        }
        
        setMinPoint(new Point3D(minPointX, minPointY, minPointZ));
        setMaxPoint(new Point3D(maxPointX, maxPointY, maxPointZ));
    }

    public void setMinPoint(Point3D minPoint) {
        this.minPoint = minPoint;
    }

    public void setMaxPoint(Point3D maxPoint) {
        this.maxPoint = maxPoint;
    }

    public List<Node> getNodes() {
        return nodes;
    }
}
