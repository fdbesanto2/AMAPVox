package fr.ird.jeeb.workspace.archimedes.raytracing.voxel;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.workspace.archimedes.geometry.shapes.ConvexMesh;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Plane;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Shape;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.ShapeUtils;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Sphere;
import fr.ird.jeeb.workspace.archimedes.raytracing.ArtNode;
import fr.ird.jeeb.workspace.archimedes.raytracing.RadiativeModel;
import fr.ird.jeeb.workspace.archimedes.raytracing.interceptionmodel.InterceptionModel;
import fr.ird.jeeb.workspace.archimedes.raytracing.interceptionmodel.PorousModel;
import fr.ird.jeeb.workspace.archimedes.raytracing.interceptionmodel.TurbidMediumModel;
import fr.ird.jeeb.workspace.archimedes.raytracing.ray.Ray;
import fr.ird.jeeb.workspace.archimedes.raytracing.ray.RayShot;
import fr.ird.jeeb.workspace.archimedes.raytracing.ray.RaySurroundingContext;
import fr.ird.jeeb.workspace.archimedes.raytracing.scatteringmodel.Lambertian;
import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.VoxelManager.ArtNodeVoxelID;
import fr.ird.jeeb.workspace.archimedes.scene.PlotBox;
import fr.ird.jeeb.lib.structure.geometry.util.BoundingBox3f;

/**
 * Scene class
 * Contains artNodes
 * Can be used to store light values in the nodes
 * 
 * @author Cresson, Nov 2012
 *
 */
public class Scene {
	
	private ArrayList<ArtNode>			artNodes;
	private PlotBox						plotBox;
	//TODO added Dauzat Feb 2013
	private BoundingBox3f bbox = null;
	
	public Scene(){
		this.artNodes = new ArrayList<ArtNode>();
	}
	
	public Scene(ArrayList<ArtNode> artNodes) {
			this.artNodes = artNodes;
	}
	
	public ArrayList<ArtNode> getArtNodes() {
		return artNodes;
	}
	
	public void setPlotBox(PlotBox plotBox) {
		this.plotBox = plotBox;
	}
	
	public PlotBox getPlotBox() {
		return plotBox;
	}
	
	/**
	 * Computes the global bounding box of artNodes
	 */
	public BoundingBox3f getBoundingBox() {
		// added Dauzat Feb. 2013
		if (bbox != null)
			return bbox;
		
		BoundingBox3f bbox = new BoundingBox3f();
		for (int i = 0 ; i < artNodes.size () ; i++) {
			Shape[] shapes = artNodes.get (i).getShape ();
			for (int j = 0 ; j < shapes.length ; j++) {
				BoundingBox3f currentBBox = ShapeUtils.computeBoundingBox(shapes[j]);
				if (currentBBox==null)
					continue;
				bbox.update (currentBBox.getMax ());
				bbox.update (currentBBox.getMin ());
			}
		}
		
		return bbox;
	}
	
	// added Dauzat, Feb. 2013
	public void setBoundingBox(BoundingBox3f boundingBox) {
		bbox = new BoundingBox3f(boundingBox.getMin(), boundingBox.getMax());
	}
		
	/**
	 * Add a floor
	 */
	public void addFloor() {
		
		ArtNode floor = new ArtNode(-999,-999);
		Plane[] pl = new Plane[1];
		pl[0] = new Plane(new Point3f(0,0,0),new Vector3f(0,0,1));
		floor.setShape (pl);
//		Specular model = new Specular();
		Lambertian model = new Lambertian(0.65f,0.0f);
		InterceptionModel interceptionModel = null;//new TranslucentModel(pl[0], 0.5f);
		floor.setRadiativeModel (new RadiativeModel(model,interceptionModel));
		artNodes.add (floor);
		
	}
	
	/**
	 * Add a sphere
	 */
	public void addSphere(Sphere[] s) {

		ArtNode sp = new ArtNode(-999,-999);
		sp.setShape(s);
		Lambertian model = new Lambertian(0.5f, 0.2f);
//		Specular model = new Specular(1.0f, 0);
		InterceptionModel iModel = new TurbidMediumModel(0.69f/200f);
		RadiativeModel radiativeModel = new RadiativeModel(model,iModel);
		sp.setRadiativeModel (radiativeModel);
		
		artNodes.add(sp);
	}
	
	public void addBox() {
		ArtNode b = new ArtNode(-999,-999);
		ConvexMesh[] box = new ConvexMesh[1];
		BoundingBox3f bbox = new BoundingBox3f();
		float r = 99f;
		bbox.update(new Point3f(r,r,r));
		bbox.update(new Point3f(-r,-r,-r));
		box[0] = ShapeUtils.createRectangleBox(bbox, 0);
		b.setShape(box);
		Lambertian model = new Lambertian();
		RadiativeModel radiativeModel = new RadiativeModel(model,null);
		model.setTransmittance(0);
		model.setReflectance(1.0f);
		b.setRadiativeModel (radiativeModel);
		
		artNodes.add(b);
		
	}
	
	public void applyLambertian() {
		for (ArtNode artNode: artNodes) {
			float randomReflectance = 0.7f*(float) Math.random();
			float randomTransmittance = 0.5f*(float) Math.random();
			Lambertian model = new Lambertian(randomReflectance,randomTransmittance);
			RadiativeModel radiativeModel = new RadiativeModel(model,null);
			artNode.setRadiativeModel (radiativeModel);
		}
	}

	public void applyPorous() {
		for (ArtNode artNode: artNodes) {
			InterceptionModel interMod = new PorousModel(0.3f);
			RadiativeModel radiativeModel = new RadiativeModel(null,interMod);
			artNode.setRadiativeModel (radiativeModel);
		}
	}
	
	public void addAShot(Ray ray, RayShot shot) {
		
		RaySurroundingContext context = ray.getSurroundingContext();
		ArtNodeVoxelID id = context.rayInter.artNodeVoxelID;
		float value = shot.getIntensity();
		artNodes.get(id.nodeID).addRadiativeEvent(id.shapeID,value);
	}

}