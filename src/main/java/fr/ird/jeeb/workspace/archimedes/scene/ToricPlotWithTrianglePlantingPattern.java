package fr.ird.jeeb.workspace.archimedes.scene;

import java.io.IOException;
import java.util.ArrayList;

import javax.vecmath.Point2d;

public class ToricPlotWithTrianglePlantingPattern {

	private double distTrees;
	private double distRows;
	private int nbPairsInX;
	private int nbPairsInY;
	private ArrayList<Point2d> treePositions;

	/**Create a plot with a triangular planting pattern. Rows are North-South oriented by default.
	 * <p>The number of trees is necessarily an even number!
	 * <p>This constructor assumes that the pattern is a regular (equilateral) triangle.
	 * Consequently the distance between rows is equal to the distance between trees multiplied by cos(PI/6)
	 * Rows are North-South oriented by default
	 * @param distanceBetweenTrees
	 * @param nbTreePairsInX
	 * @param nbTreePairsInY
	 */
	public ToricPlotWithTrianglePlantingPattern(double distanceBetweenTrees, int nbTreePairsInX, int nbTreePairsInY) {
		this (distanceBetweenTrees, distanceBetweenTrees*Math.cos(Math.PI/6), nbTreePairsInX, nbTreePairsInY);
	}

	/**Create a plot with a triangular planting pattern. Rows are North-South oriented by default.
	 * <p>The number of trees is necessarily an even number!
	 * @param distanceBetweenTrees
	 * @param distanceBetweenRows
	 * @param nbTreePairsInX
	 * @param nbTreePairsInY
	 */
	public ToricPlotWithTrianglePlantingPattern (double distanceBetweenTrees, double distanceBetweenRows, int nbTreePairsInX, int nbTreePairsInY) {
		distTrees = distanceBetweenTrees;
		distRows = distanceBetweenRows;
		this.nbPairsInX = nbTreePairsInX;
		this.nbPairsInY = nbTreePairsInY;
		treePositions = new ArrayList<Point2d>();
		treePositions.add (new Point2d(distRows*0.5, distTrees*0.25));
		treePositions.add (new Point2d(distRows*1.5, distTrees*0.75));

		// duplicate the elementary plot
		Point2d pos0 = treePositions.get(0);
		Point2d pos1 = treePositions.get(1);
		Point2d pos;
		double translateX = distRows * 2.0;
		double translateY = distTrees;
		
		for (int x=1; x<nbPairsInX; x++) {
			pos = new Point2d(pos0);
			pos.add(new Point2d(translateX*x, 0));
			treePositions.add(new Point2d(pos));
			pos = new Point2d(pos1);
			pos.add(new Point2d(translateX*x, 0));
			treePositions.add(new Point2d(pos));
		}
		
		int nbTrees = treePositions.size();
		for (int t=0; t<nbTrees; t++) {
			pos = new Point2d(treePositions.get(t));
			for (int y=1; y<nbPairsInY; y++) {
				pos.add(new Point2d(0, translateY));
				treePositions.add(new Point2d(pos));
			}
		}
	}
	

	private double getMinX() {
		return 0;
	}
	private double getMinY() {
		return 0;
	}
	private double getMaxX() {
		return distRows*2*nbPairsInX;
	}
	private double getMaxY() {
		return distTrees*nbPairsInY;
	}

	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		double distanceBetweenTrees = 9.0;
		double distanceBetweenRows = distanceBetweenTrees * Math.cos(Math.PI/6); // to use with the second constructor
		int nbTreePairsInX = 5;
		int nbTreePairsInY = 4;

		// ToricPlots plot = new ToricPlots ("TRIANGLE", distanceBetweenTrees, distanceBetweenRows, nbTreePairsInX, nbTreePairsInY);
		ToricPlotWithTrianglePlantingPattern plot = new ToricPlotWithTrianglePlantingPattern (distanceBetweenTrees, nbTreePairsInX, nbTreePairsInY);

		System.out.println("treeIndex\tX\tY");
		for (int t=0; t<plot.treePositions.size(); t++) {
			Point2d pos = plot.treePositions.get(t);
			System.out.println(t+"\t"+pos.x+"\t"+pos.y);
		}
		System.out.println("plot vertices");
		System.out.println("c0\t"+plot.getMinX()+"\t\t"+plot.getMinY());
		System.out.println("c1\t"+plot.getMaxX()+"\t\t"+plot.getMinY());
		System.out.println("c2\t"+plot.getMaxX()+"\t\t"+plot.getMaxY());
		System.out.println("c3\t"+plot.getMinX()+"\t\t"+plot.getMaxY());
		System.out.println("c0\t"+plot.getMinX()+"\t\t"+plot.getMinY());
	}

}
