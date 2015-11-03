/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.math.geometry;

import fr.amap.amapvox.math.point.Point2D;

/**
 * Defines a rectangle with 4 corners
 * @author Julien Heurtebize
 */
public class Rectangle2D {
    
    public Point2D bottomLeftCorner;
    public Point2D topLeftCorner;
    public Point2D bottomRightCorner;
    public Point2D topRightCorner;
    
    public Rectangle2D(Point2D bottomLeftCorner, Point2D topRightCorner) {
        
        this.bottomLeftCorner = bottomLeftCorner;
        this.topLeftCorner = new Point2D(bottomLeftCorner.x, topRightCorner.y);
        this.bottomRightCorner = new Point2D(bottomLeftCorner.y, topRightCorner.x);
        this.topRightCorner = topRightCorner;
    }

    public Rectangle2D(Point2D bottomLeftCorner, Point2D topLeftCorner, Point2D bottomRightCorner, Point2D topRightCorner) {
        this.bottomLeftCorner = bottomLeftCorner;
        this.topLeftCorner = topLeftCorner;
        this.bottomRightCorner = bottomRightCorner;
        this.topRightCorner = topRightCorner;
    }
    
    public boolean isIntersectsRectangle(Rectangle2D otherRectangle){
        
        return (bottomLeftCorner.x <= otherRectangle.topRightCorner.x && topRightCorner.x >= otherRectangle.bottomLeftCorner.x &&
        bottomLeftCorner.y <= otherRectangle.topRightCorner.y && topRightCorner.y >= otherRectangle.bottomLeftCorner.y);
    }
}
