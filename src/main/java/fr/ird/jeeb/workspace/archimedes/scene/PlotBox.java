package fr.ird.jeeb.workspace.archimedes.scene;

import javax.vecmath.Point2f;
import javax.vecmath.Point3f;

import fr.ird.jeeb.lib.structure.geometry.util.BoundingBox3f;

/**
 * PlotBox class
 * Defines an (x,y) plotting area to reduce/expand the scene canvas
 * 	infCorner/supCorner: corners of he boxPlot
 * 	clipping: elements of the scene outside the specified plotBox
 *  are deleted instead of translated/duplicated
 *  
 * @author Cresson, Dec. 2012
 */
public class PlotBox {
	private Point2f	infCorner;
	private Point2f	supCorner;
	private boolean	clipping;
	
	public PlotBox(Point2f cornerInf, Point2f cornerSup) {
		this(cornerInf, cornerSup, false);
	}
	
	public PlotBox(Point2f cornerInf, Point2f cornerSup, boolean clipping) {
		
		// Check inf & sup corners, switch if necessary 
		Point2f sup = new Point2f(cornerSup);
		Point2f inf = new Point2f(cornerInf);
		
		if (cornerInf.x>cornerSup.x) {
			sup.x = cornerInf.x;
			inf.x = cornerSup.x;
		}
		if (cornerInf.y>cornerSup.y) {
			sup.y = cornerInf.y;
			inf.y = cornerSup.y;
		}
		
		this.infCorner	= inf;
		this.supCorner	= sup;
		this.clipping	= clipping;
	}
	
	public Point2f getInfCorner() {
		return infCorner;
	}
	
	public Point2f getSupCorner() {
		return supCorner;
	}
	
	public BoundingBox3f getBox() {
		BoundingBox3f box = new BoundingBox3f();
		box.update(new Point3f(infCorner.x,infCorner.y,-1f));
		box.update(new Point3f(supCorner.x,supCorner.y,+1f));
		return box;
	}
	
	public boolean getClipping() {
		return clipping;
	}
}
