package fr.ird.jeeb.workspace.archimedes.raytracing.voxel;

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

import fr.ird.jeeb.lib.structure.geometry.util.BoundingBox3f;
import fr.ird.jeeb.workspace.archimedes.geometry.Intersection;
import fr.ird.jeeb.workspace.archimedes.geometry.LineElement;
import fr.ird.jeeb.workspace.archimedes.geometry.Transformations;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.BoundingShapeComputing;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Plane;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Shape;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.ShapeUtils;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.VolumicShape;
import fr.ird.jeeb.workspace.archimedes.raytracing.ArtNode;
import fr.ird.jeeb.workspace.archimedes.scene.PlotBox;
import fr.ird.jeeb.workspace.archimedes.util.ArtLog;

/**
 * Voxel Manager class
 * 	-Create the voxel space (Assuming user plot box)
 * 	-Manage the line segment crossing, and next encountered voxel
 * 	-Manage the first entry of the ray in the voxel space
 * 
 * @author Cresson, Aug. 2012
 *
 */
public class VoxelManager {

	private static final float 				BBOX_SCENE_MARGIN	= 0.01f;

	private VoxelSpace						voxelSpace;
	private ArrayList<ArtNodeVoxelID>[][][]	artNodeIndexList;
	private VolumicShape					sceneCanvas;

	/**
	 * Used for returning voxel crossing context
	 * 	-Estimation of the line segment length to set, during the current voxel crossing ("length")
	 * 	-Prediction of the next voxel encountered (after the current voxel) ("indices")
	 * 	-Calculation of the translation to perform on the line segment (when exit the scene bounding box & toric voxel space) ("translation")
	 * @author cresson
	 *
	 */
	public class VoxelCrossingContext {
		public Point3i indices;
		public Vector3f translation;
		public float length;
		public VoxelCrossingContext(Point3i indices, float length, Vector3f t) {
			this.indices		= indices;
			this.length			= length;
			this.translation	= t;
		}
	}
		
	/**
	 * Used to return the ID of a shape in a node
	 * @author cresson
	 *
	 */
	public class ArtNodeVoxelID implements Cloneable {
		public int nodeID;
		public int shapeID;
		public ArtNodeVoxelID(int nodeID, int shapeID) {
			this.nodeID		= nodeID;
			this.shapeID	= shapeID;
		}
		
		public Object clone() {
			ArtNodeVoxelID artNodeVoxelId = null;
			try {
				artNodeVoxelId = (ArtNodeVoxelID) super.clone();
			} catch (CloneNotSupportedException cnse) {
				System.err.println(System.err+"/"+cnse.getMessage ());
			}
			artNodeVoxelId.nodeID = nodeID;
			artNodeVoxelId.shapeID = shapeID;
			return artNodeVoxelId;
			
		}
			
	}
	
	/**
	 * Used for returning test results, between two bounding boxes (i.e. inclusion/exclusion/location context)
	 * 	-Total inclusion (when "null" is returned)
	 * 	-Total exclusion (when "outside" = true)
	 * 	-Exceed location (list of indices. e.g. a box can exceed another to the left & to the top: two indices are returned) 
	 * @author cresson
	 *
	 */
	public static class BoundingBoxInclusion {
		public boolean outside;
		ArrayList<Point3i> locationList;
		public BoundingBoxInclusion (boolean outside, ArrayList<Point3i> locationList) {
			this.outside 		= outside;
			this.locationList	= locationList;
		}
		
	}
	
	/**
	 * Used to enclose the scene.
	 * Infinite box in (X,Y) dimensions.
	 * @author cresson
	 *
	 */
	private class InfiniteBox extends Shape implements VolumicShape{
		/*
		 * (X,Y) Infinite box class. Used for building the scene canvas when toric = true 
		 */
		
		private Plane top;
		private Plane bottom;
		private float zMax;
		private float zMin;

		public InfiniteBox(float zmax, float zmin) {
			zMax	= zmax;															// Zmax (Z ceiling)
			zMin	= zmin;															// Zmin (Z floor)
			top		= new Plane(new Point3f(0,0,zmax),	new Vector3f(0,0,+1));		// Upper plane (ceiling)
			bottom	= new Plane(new Point3f(0,0,zmin),	new Vector3f(0,0,-1));		// Lower plane (floor)
		}

		@Override
		public void transform (Transformations transform) {
		}

		@Override
		public boolean isIntersectedBy (LineElement linel) {
			return false;
		}

		@Override
		public ArrayList<Intersection> getIntersections (LineElement linel) {
			return null;
		}

		@Override
		public Intersection getNearestIntersection (LineElement linel) {
			
			Intersection iTop = top.getNearestIntersection (linel); 
			Intersection iBottom = bottom.getNearestIntersection (linel);

			if (iTop!=null & iBottom!=null) {
				float distanceTop = iTop.getDistance ();
				float distanceBottom = iBottom.getDistance ();
				if (distanceTop<distanceBottom)
					return new Intersection(distanceTop,new Vector3f(0,0,1));
				else
					return new Intersection(distanceBottom,new Vector3f(0,0,-1));
			}
			else if (iTop!=null)
				return new Intersection(iTop.getDistance (),new Vector3f(0,0,1));
			else if (iBottom!=null)
				return new Intersection(iBottom.getDistance (),new Vector3f(0,0,-1));
			else
				return null;
		}

		public boolean contains(Point3f point) {
			if (point.z<=zMin || point.z>=zMax)
				return false;
			return true;
		}

	}

	/*
	 * Build mesh index list, for each voxel
	 * @param meshes	array list of meshes
	 */
	@SuppressWarnings ("unchecked")
	private void buildArtNodesIndexList(List<ArtNode> artNodes) {

		ArtLog.println ("Building voxel indices list..");
		
		// Allocate voxel indices list
		Point3i subdiv = voxelSpace.getSplitting ();
		artNodeIndexList = new ArrayList[subdiv.x][subdiv.y][subdiv.z];
		for (int i = 0 ; i < subdiv.x ; i++) {
			artNodeIndexList[i] = new ArrayList[subdiv.y][subdiv.z];
			for ( int j = 0 ; j < subdiv.y ; j++) {
				artNodeIndexList[i][j] = new ArrayList[subdiv.z];
				for ( int k = 0 ; k < subdiv.z ; k++)
					artNodeIndexList[i][j][k] = new ArrayList<ArtNodeVoxelID>();
			}
		}
		
		// Writing voxel indices list
		for (int i = 0 ; i < artNodes.size () ; i++) {
			Shape[] artNodeShapes = artNodes.get (i).getShape ();
			for (int k = 0 ; k < artNodeShapes.length ; k++) {
				Point3i[] indexList = voxelSpace.getShapeOptimumVoxelIndices(artNodeShapes[k]);
				for (int j = 0 ; j < indexList.length ; j++)
					artNodeIndexList[indexList[j].x][indexList[j].y][indexList[j].z].add (new ArtNodeVoxelID(i,k));
			}
		}
		
		ArtLog.println ("ok");

		ArtLog.println ("Computing bounding shapes..");
		
		// Computing bounding shapes
		BoundingShapeComputing bsc = new BoundingShapeComputing ();
		for (int i = 0 ; i < artNodes.size () ; i++) {
			ArtNode artNode = artNodes.get (i);
			Shape[]	artNodeShapes = artNode.getShape ();
			VolumicShape[]	boundingShapes = new VolumicShape[artNodeShapes.length];
			for (int k = 0 ; k < artNodeShapes.length ; k++) {
				VolumicShape shp = null;
				if (!ShapeUtils.isInfiniteShape(artNodeShapes[k]))
					shp = bsc.boundingShape (artNodeShapes[k]);
				boundingShapes[k] = shp;
			}
			artNode.setBoundingShape (boundingShapes);
		}
		
		ArtLog.println ("ok");

	}

	/* 
	 * crop the scene assuming the given plotBox
	 * @param meshes	array list of meshes
	 * @param plotBox	user plot box
	 * @param clipping	user clipping option
	 * @return the cropped array list of meshes. if clipped=false, meshes are replicated on the plot box opposite side, when they exceed the plot box size.
	 */
	private ArrayList<ArtNode> crop (ArrayList<ArtNode> artNodes, PlotBox plotBox, boolean clipping) {

		ArtLog.println ("Cropping scene (clipping = " + clipping + ") ..");
		ArtLog.println ("Cropping Inf. corner: " + plotBox.getInfCorner());
		ArtLog.println ("Cropping Sup. corner: " + plotBox.getSupCorner ());
		Point3f plotBoxInf = new Point3f(plotBox.getBox().getMin ());
		Point3f plotBoxSup = new Point3f(plotBox.getBox().getMax ());
		ArrayList<ArtNode> newArtNodes = new ArrayList<ArtNode>();
		for (int i = 0 ; i < artNodes.size () ; i++) {
			ArtNode currentArtNode = artNodes.get (i);
			Shape firstArtNodeShape = currentArtNode.getShape ()[0];
			ArrayList<Shape> newArtNodeShapes = new ArrayList<Shape>();
			
			// If the shape is an infinite shape, just add it and do nothing more
			VoxelManager.BoundingBoxInclusion test = null;
			if (!ShapeUtils.isInfiniteShape(firstArtNodeShape)) {
				// Get current artNode position relative to the plotbox (Completely outide/inside? North,South,East,West,N-E...?)
				BoundingBox3f artNodeBBox = ShapeUtils.computeBoundingBox (firstArtNodeShape);
				test = VoxelManager.getBoundingBoxIntersection (artNodeBBox,plotBox.getBox());
			}
			if (test!=null) { // When the object is not completely inside the plotBox
				if (!clipping) {
					/* Get the "locationList" (North,South,East,West,N-E...)
					 * then duplicate the shape as many times as the number of different locations.
					 * Note that the Z-Coordinates of the location is always 0, because
					 * the plot box is not taking account of Z-coordinates (XY toricity).
					 */
					for (Point3i dir : test.locationList) {
						Vector3f pos = new Vector3f(
								dir.x*(plotBoxInf.x-plotBoxSup.x),
								dir.y*(plotBoxInf.y-plotBoxSup.y),
								0f);
						Shape newShape = null;
						try {
							newShape = (Shape) firstArtNodeShape.clone ();
						} catch (CloneNotSupportedException e) {
							ArtLog.println (e.getMessage ());
							return null;
						}
						Transformations t = new Transformations();
						t.setTranslation (pos);
						newShape.transform (t);
						newArtNodeShapes.add (newShape);
					}
				}
				if (!test.outside)
					newArtNodeShapes.add (firstArtNodeShape);
			}
			else
				newArtNodeShapes.add (firstArtNodeShape);
			ArtNode newArtNode = new ArtNode(	currentArtNode.getPlantID (),
												currentArtNode.getNodeID ());
			newArtNode.setRadiativeModel(currentArtNode.getRadiativeModel());
			newArtNode.setShape (newArtNodeShapes.toArray (new Shape[newArtNodeShapes.size ()]));
			newArtNodes.add (newArtNode);
		}
		ArtLog.println ("ok");
		return newArtNodes;
		
	}
	
	/**
	 * Voxel Manager Constructor
	 * @param scene					scene
	 * @param voxelManagerSettings	voxel manager settings
	 */
	public VoxelManager(Scene scene, VoxelManagerSettings voxelManagerSettings) {
		
		// Get the artNode list
		ArrayList<ArtNode> artNodes = scene.getArtNodes ();

		if (scene.getPlotBox()!=null) {	// IF A PLOTBOX IS SPECIFIED
			// Get the boundingBox
			PlotBox plotBox = scene.getPlotBox();
			
			// Computes the new scene (assuming the boxPlot)
			ArrayList<ArtNode> newArtNodes = crop(artNodes, plotBox, plotBox.getClipping());
	
			// Find the bounding box of the ArrayList of mesh
			BoundingBox3f bbox = scene.getBoundingBox();
	
			// Add margins to the bounding box
			bbox = ShapeUtils.getPaddedBoundingBox(bbox, BBOX_SCENE_MARGIN);
	
			// Create the new scene bounding box
			BoundingBox3f newBoundingBox = plotBox.getBox();
			
			Point3f pMax = new Point3f(newBoundingBox.getMax ());
			Point3f pMin = new Point3f(newBoundingBox.getMin ());
			pMax.z= (bbox.getMax ().z);
			pMin.z= (bbox.getMin ().z);
			newBoundingBox.setMax (pMax);
			newBoundingBox.setMin (pMin);
	
			// Build voxelspace (memory allocation)
			voxelSpace = new VoxelSpace(newBoundingBox, voxelManagerSettings.getSplitting(), voxelManagerSettings.getTopology());
	
			// Build mesh index lists (from meshes bounding boxes)
			buildArtNodesIndexList(newArtNodes);
	
	
			artNodes.clear ();
			for (ArtNode artNode: newArtNodes){
				artNodes.add (artNode);
			}
		}
		else {						// IF NO PLOTBOX IS SPECIFIED
			// Find the bounding box of the ArrayList of mesh
			BoundingBox3f bbox = scene.getBoundingBox();
			
			// Add margins to the bounding box
			bbox = ShapeUtils.getPaddedBoundingBox(bbox, BBOX_SCENE_MARGIN);

			// Build voxelSpace
			voxelSpace = new VoxelSpace(bbox, voxelManagerSettings.getSplitting(), voxelManagerSettings.getTopology());

			// Build mesh index lists (from meshes bounding boxes)
			buildArtNodesIndexList(artNodes);
		}
		// Build scene canvas
		buildSceneCanvas();
	}
	
	/*
	 * Return the "relative spatial context" between two bounding boxes:
	 * 		null:				the test bounding box is completely inside the canvas bounding box
	 * 		outside:			true if the test bounding box is completely outside the canvas bounding box, else false
	 * 		location:			list of positions of test bounding box corners, relative to canvas bounding box 
	 * 							(East, North, South, West, N-W, ... indicated with +/-1 values in Point3i[] locationList) 
	 * 
	 * @param bboxCanvas		Canvas bounding box
	 * @param bboxTest			Test bounding box
	 * @return The bounding boxes inclusion
	 */
	private static BoundingBoxInclusion getBoundingBoxIntersection(BoundingBox3f bboxCanvas, BoundingBox3f bboxTest) {
		
		if (bboxCanvas==null)
			bboxCanvas = new BoundingBox3f();

		// bboxCanvas corners
		float[]x1 = {bboxCanvas.getMax().x, bboxCanvas.getMin().x};
		float[]y1 = {bboxCanvas.getMax().y, bboxCanvas.getMin().y};
		float[]z1 = {bboxCanvas.getMax().z, bboxCanvas.getMin().z};

		// Get 8 points of the bbox1
		boolean exceed = false;
		for (int i = 0; i < 8 ; i++) {
			Vector3d p1 = new Vector3d(x1[i%2],y1[((i-i%2)/2)%2],z1[((i-i%4)/4)%2]);
			if (bboxTest.out (p1))
				exceed = true;
		}
		
		// return null if every points of bboxTest are inside bboxCanvas 
		if (!exceed)
			return null;
		
		// Computes the position of the bboxTest, relative to bboxCanvas
		boolean outside = false;
		int n = 0;
		
		ArrayList<Point3i> outsideList = new ArrayList<Point3i>();
		for (int i = 0; i < 8 ; i++) {
			Vector3d p1 = new Vector3d(x1[i%2],y1[((i-i%2)/2)%2],z1[((i-i%4)/4)%2]);
			if (bboxTest.out (p1)) {
				n++;
				Point3i pos = new Point3i(0,0,0);
				if (p1.x<bboxTest.getMin ().x)
					pos.x= (-1);
				if (p1.x>bboxTest.getMax ().x)
					pos.x= (+1);
				if (p1.y<bboxTest.getMin ().y)
					pos.y= (-1);
				if (p1.y>bboxTest.getMax ().y)
					pos.y= (+1);
				boolean exists = false;
				for (Point3i outsideIndice : outsideList)
					if (outsideIndice.x==pos.x & outsideIndice.y==pos.y & outsideIndice.z==pos.z)
						exists = true;
				if (!exists)
					outsideList.add (pos);
			}
		}
				
		// Check if bboxTest is completely outside bboxCanvas
		if (n==8)
			outside = true;
		
		BoundingBoxInclusion inter = new BoundingBoxInclusion (outside, outsideList); 
		return inter;
		
	}

	/*
	 * Build scene canvas.
	 *		1.sceneCanvas = a box (Topology NON_TORIC_FINITE_BOX_TOPOLOGY or TORIC_FINITE_BOX_TOPOLOGY)
	 *	or
	 *		2.sceneCanvas = an infinite box (Topology TORIC_INFINITE_BOX_TOPOLOGY)
	 */
	private void buildSceneCanvas () {

		// Scene bounding box
		BoundingBox3f sceneBoundingBox = voxelSpace.getBoundingBox ();

		if (voxelSpace.getTopology()==VoxelManagerSettings.TORIC_INFINITE_BOX_TOPOLOGY)
		{
			// Scene boundaries are an infinite box (ceiling+floor)
			sceneCanvas = new InfiniteBox(sceneBoundingBox.getMax().z-BBOX_SCENE_MARGIN, sceneBoundingBox.getMin().z+BBOX_SCENE_MARGIN);
		}
		else {
			// Scene boundaries are a box (the bounding box of the scene, plus a margin)
			sceneCanvas = ShapeUtils.createRectangleBox (sceneBoundingBox, BBOX_SCENE_MARGIN, true);
		}

	}

	/*
	 * Returns hypothenuse (Thales theorem)
	 * @param zVectorOrigin		origin of vector
	 * @param zBoxOrigin		origin of the z-coordinate system
	 * @param zVectorDir		norm of the vector
	 * @return the hypothenuse, i.e. norm of z-axis
	 */
	private float hypothenuse (float zVectorOrigin, float zBoxOrigin, float zVectorDir) {
		if (zVectorDir == 0)
			return Float.MAX_VALUE;
		return (zVectorOrigin - zBoxOrigin)/zVectorDir;
	}
	
	/**
	 * @param lineElement		Line element in the current voxel
	 * @param currentVoxel		Current voxel
	 * @return Voxel crossing context of the current voxel (Indices, Length, Translation).
	 * @see VoxelCrossingContext CrossVoxel(Point3f, Vector3f, Point3i)
	 */
	public VoxelCrossingContext CrossVoxel(LineElement lineElement, Point3i currentVoxel) {
		return CrossVoxel(lineElement.getOrigin (), lineElement.getDirection (), currentVoxel);
	}
		
	/**
	 * Return the voxel crossing context of the current voxel.
	 * A voxel crossing context is:
	 * 		1. Incides of the next encounter voxel (Point3i)
	 * 		2. Length of the line segment in the current voxel (float)
	 * 		3. Translation to apply to the line segment in the current voxel (Vector3f)
	 * 
	 * @param startPoint		Start point of the line element in the current voxel
	 * @param direction			Direction of the line element in the current voxel
	 * @param currentVoxel		Current voxel
	 * @return Voxel crossing context of the current voxel (Indices, Length, Translation).
	 */
	public VoxelCrossingContext CrossVoxel(Point3f startPoint, Vector3f direction, Point3i currentVoxel) {
		
		// Distances to current voxel walls
		Point3f infCorner = voxelSpace.getVoxelInfCorner (currentVoxel);
		float x = 0f; float y = 0f; float z = 0f;

		if (direction.x < 0)
			x = hypothenuse (startPoint.x, infCorner.x, direction.x);
		else
			x = hypothenuse (infCorner.x + voxelSpace.getVoxelSize ().x, startPoint.x, direction.x);
		if (direction.y < 0)
			y = hypothenuse (startPoint.y, infCorner.y, direction.y);
		else
			y = hypothenuse (infCorner.y + voxelSpace.getVoxelSize ().y, startPoint.y, direction.y);
		if (direction.z < 0)
			z = hypothenuse (startPoint.z, infCorner.z, direction.z);
		else
			z = hypothenuse (infCorner.z + voxelSpace.getVoxelSize ().z, startPoint.z, direction.z);

		int vx = currentVoxel.x;
		int vy = currentVoxel.y;
		int vz = currentVoxel.z;

		float a = Math.abs (x);
		float b = Math.abs (y);
		float c = Math.abs (z);

		// Find minimums (even multiple occurrences)
		float sc = 0f;	// sc: min distance to current voxel walls (in x,y or z)
		if (a <= b)
			if (a <= c) {
				vx += (int) Math.signum (direction.x);
				sc = a;
			}
		if (b <= a)
			if (b <= c) {
				vy += (int) Math.signum (direction.y);
				sc = b;
			}
		if (c <= a)
			if (c <= b ) {
				vz += (int) Math.signum (direction.z);
				sc = c;
			}
		
		if (vz >= voxelSpace.getSplitting ().z | vz<0)
			// Voxel space is never toric in Z-Coordinates ! return a null voxel indices and null translation
			return new VoxelCrossingContext(null,sc,null);
		
		Point3i sp = voxelSpace.getSplitting ();		// Voxel space splittings
		Point3f sz = voxelSpace.getBoundingBoxSize ();	// Voxel space bounding box size
		
		Vector3f t = new Vector3f (0,0,0);
		if (!voxelSpace.isFinite()) {
			// When the voxel space is infinite, computes the translation and the new voxel indices
			if (vx>=sp.x){
				vx = 0;
				t.add (new Vector3f(sz.x,0,0));
			}
			else if (vx<0){
				vx = sp.x-1;
				t.add (new Vector3f(-sz.x,0,0));
			}
			if (vy>=sp.y){
				vy = 0;
				t.add (new Vector3f(0,sz.y,0));
			}
			else if (vy<0){
				vy = sp.y-1;
				t.add (new Vector3f(0,-sz.y,0));
			}
		}
		else
			// When the voxel space is finite, return a null voxel indices and null translation
			if (vx>=sp.x | vx<0 | vy>=sp.y | vy<0)
				return new VoxelCrossingContext(null,sc,null);
			
		return new VoxelCrossingContext(new Point3i(vx,vy,vz),sc,t);
		
	}

	/**
	 *  Returns the voxel context of the first entry in the scene canvas
	 *  
	 *  @param lineElement		line element
	 *  @return VoxelCrossingContext of the first encountered voxel
	 */
	public VoxelCrossingContext getFirstVoxel(LineElement lineElement) {

		boolean intersectionForDebug = false;
		
		// Computes intersection with the scene canvas
		Intersection intersection = sceneCanvas.getNearestIntersection (lineElement);
		Point3f intersectionPoint = new Point3f(0f,0f,0f);
		if (intersection!=null) {
			// If the intersection exists, get the intersection point
			intersectionPoint.add (lineElement.getDirection ());
			intersectionPoint.scale (intersection.distance);
			intersectionPoint.add (lineElement.getOrigin ());
			intersectionForDebug = true;
		}
		else if (sceneCanvas.contains (lineElement.getOrigin ())) {
			// If the line origin is already in the scene canvas, just get its origin point
			intersectionPoint.add (lineElement.getOrigin ());
			intersectionForDebug = false;
		}
		else {
			// Else (no intersection & no inside), bye bye.
			return null;
		}

		Point3i intersectionPointVoxelIndices = voxelSpace.getVoxelIndices (intersectionPoint);
		if (intersectionPointVoxelIndices==null) {
			if (intersectionForDebug)
				ArtLog.println ("The given line element does intersect the scene canvas "+intersectionPoint+", but unable to get its voxel indices");
			else
				ArtLog.println ("The given line element belongs the scene canvas "+intersectionPoint+", but unable to get its voxel indices");
			ArtLog.println ("Voxel space informations are:");
			ArtLog.println ("\tCorner Inf\t:\t" + voxelSpace.getBoundingBox ().getMin ());
			ArtLog.println ("\tCorner Sup\t:\t" + voxelSpace.getBoundingBox ().getMax ());
			return null;
		}

		// Computes the 1st translation (from line origin, to intersection point with the scene canvas)
		Vector3f translation = new Vector3f (intersectionPoint);
		translation.sub (lineElement.getOrigin ());
		
		// Computes the 2nd translation (from the intersection point with the scene canvas, to the point of the "real" scene)
		Point3i intersectionPointSceneIndices = voxelSpace.getSceneIndices (intersectionPoint);
		Vector3f secondTranslation = new Vector3f (	intersectionPointSceneIndices.x*voxelSpace.getBoundingBoxSize ().x,
													intersectionPointSceneIndices.y*voxelSpace.getBoundingBoxSize ().y,
													intersectionPointSceneIndices.z*voxelSpace.getBoundingBoxSize ().z);
		
		// Careful: Length is the length of the 1st translation !
		float l = translation.length ();
		
		// Computes the total translation (1st translation + 2nd translation)
		translation.sub (secondTranslation);
		
		// Return the voxel indices, the line length, and the translation
		return new VoxelCrossingContext(intersectionPointVoxelIndices,l,translation);
 
	}

	/**
	 * Return artNode index list of the specified voxel.
	 * @param indices	indices of the voxel
	 * @return Index list of artNodes located in the voxel
	 */
	public ArrayList<ArtNodeVoxelID> getArtNodeIndexList(Point3i indices) {
		return artNodeIndexList[indices.x][indices.y][indices.z];
	}
	
	/**
	 * Display voxel space properties
	 */
	public void showInformations() {
		System.out.println ("Voxel space informations:");
		System.out.println ("\tCorner Inf\t:\t" + voxelSpace.getBoundingBox ().getMin ());
		System.out.println ("\tCorner Sup\t:\t" + voxelSpace.getBoundingBox ().getMax ());
		System.out.println ("\tDimensions\t:\t" + voxelSpace.getSplitting ());
		System.out.println ("\tToricity  \t:\t" + voxelSpace.isToric ());
	}

	/**
	 * Return the inf. corner coordinates of the specified voxel
	 * @param voxel	voxel
	 * @return corner coordinates
	 */
	public Point3f getInfCorner(Point3i voxel) {
		return voxelSpace.getVoxelInfCorner (voxel);
	}
	
	/**
	 * Return the sup. corner coordinates of the specified voxel
	 * @param voxel	voxel
	 * @return corner coordinates
	 */
	public Point3f getSupCorner(Point3i voxel) {
		Point3i i = new Point3i(voxel);
		i.add (new Point3i(1,1,1));
		return voxelSpace.getVoxelInfCorner (i);
	}
	
	/**
	 * Return the relative coordinates of a point
	 * @param point	point
	 * @return relative coordinates of the point
	 */
	public Point3f getRelativePoint(Point3f point) {
		return voxelSpace.getRelativePoint(point);
	}
}
