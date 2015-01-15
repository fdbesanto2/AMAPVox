package fr.ird.jeeb.workspace.archimedes.raytracing;

import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Shape;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.ShapeUtils;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.VolumicShape;

/**
 * ArtNode Class. Used to link geometric structure and radiative models, and to store radiative balance results.
 * @author Cresson, Sept. 2012
 *
 */
public class ArtNode {
	
	/**
	 * Radiative Balance Class
	 * Used to computed the radiative balance.
	 */
	public class RadiativeBalance {
		public float		cumulatedShotsIntensity;
		public int			numberOfShots;
		public RadiativeBalance() {}
	}
	
	private Shape[]				shapes;
	private VolumicShape[]		boundingShape;
	private int					nodeId;
	private int					plantId;
	private RadiativeModel		radiativeModel;
	private RadiativeBalance[]	radiativeBalance;
	
	/**
	 * ArtNode Constructor with plantId and nodeId
	 * @param plantId	plant ID
	 * @param nodeId	node ID
	 */
	public ArtNode(int plantId, int nodeId) {
		this.plantId	= plantId;
		this.nodeId		= nodeId;
		this.radiativeModel = new RadiativeModel();
	}
	
	/**
	 * Set bounding shape
	 * @param boundingShapes	bounding shape to set
	 */
	public void setBoundingShape(VolumicShape[] boundingShapes){
		this.boundingShape = boundingShapes;
	}
	
	/**
	 * Set shape
	 * @param shapes			shapes to set
	 */
	public void setShape(Shape[] shapes) {
		this.shapes = shapes;
		
		// Radiative balance allocation
		radiativeBalance = new RadiativeBalance[shapes.length];
		for (int i = 0 ; i < shapes.length ; i++)
			radiativeBalance[i] = new RadiativeBalance();
	}
	
	/**
	 * Set the radiative model
	 * @param model	model
	 */
	public void setRadiativeModel(RadiativeModel radiativeModel) {
		this.radiativeModel = radiativeModel;
	}
	
	/**
	 * Get shape
	 * @return shape
	 */
	public Shape[] getShape() {
		return shapes;
	}
	
	/**
	 * Get bounding shape
	 * @return bounding shape
	 */
	public VolumicShape[] getBoundingShape() {
		return boundingShape;
	}

	/**
	 * Get plant ID
	 * @return plant ID
	 */
	public int getPlantID(){
		return plantId;
	}
	
	/**
	 * Get node ID
	 * @return node ID
	 */
	public int getNodeID(){
		return nodeId;
	}
	
	/**
	 * Get radiative model
	 * @return	radiative model
	 */
	public RadiativeModel getRadiativeModel() {
		return radiativeModel;
	}
	
	/**
	 * Add a shot to the radiative balance
	 * @param shapeId	impacted shape
	 * @param value		ray intensity value
	 */
	public void addRadiativeEvent(int shapeId, float value) {
		radiativeBalance[shapeId].cumulatedShotsIntensity+=value;
		radiativeBalance[shapeId].numberOfShots++;
	}
	
	/**
	 * Computes and return the radiative balance
	 */
	public float[] getRadiativeBalance() {
		
		// Instanciation
		float[] balance = new float[radiativeBalance.length];
		
		// For each shape of the node
		for (int i = 0 ; i < balance.length ; i++) {
			// Compute shape area
			float area = ShapeUtils.computeShapeArea(shapes[i]);
			
			// Compute mean absorbed light by area
			if (radiativeBalance[i].numberOfShots!=0 & !Float.isNaN(area))
				balance[i] = radiativeBalance[i].cumulatedShotsIntensity/(((float)radiativeBalance[i].numberOfShots)*area);
			else
				balance[i] = Float.NaN;
		}
		return balance;
	}

}
