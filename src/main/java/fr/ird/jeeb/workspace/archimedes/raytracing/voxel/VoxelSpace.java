/**
 * 
 */
package fr.ird.jeeb.workspace.archimedes.raytracing.voxel;

import java.util.ArrayList;

import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

import fr.ird.jeeb.lib.structure.geometry.util.BoundingBox3f;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.BoundingShapeComputing;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.ConvexMesh;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Ellipsoid;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Plane;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.Shape;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.ShapeUtils;
import fr.ird.jeeb.workspace.archimedes.geometry.shapes.TriangulatedMesh;


/**
 * 
 * @author DAUZAT/Cresson sept.2012
 *
 */
public class VoxelSpace {
	
	private BoundingBox3f	boundingBox;
	private Point3f			boundingBoxSize;
	private Point3f			voxelSize;
	private Point3i			splitting;
	private boolean			toric	= false;
	private boolean			finite	= false;
	private int				topology;
	
	public VoxelSpace (BoundingBox3f bb, Point3i splitting, int topology) {
		
		this.boundingBox		= bb;
		this.boundingBoxSize	= new Point3f (bb.max); this.boundingBoxSize.sub (bb.min);
		this.splitting 			= splitting;
		this.voxelSize 			= new Point3f (
				boundingBoxSize.x/splitting.x,
				boundingBoxSize.y/splitting.y,
				boundingBoxSize.z/splitting.z);
		setTopology(topology);
	}

	public VoxelSpace (BoundingBox3f bb, int numberOfVoxels, int topology) {
		
		this.boundingBox		= bb;
		this.boundingBoxSize	= new Point3f (bb.max);	this.boundingBoxSize.sub (bb.min);
		setDefaultSpliting (numberOfVoxels);
		this.voxelSize			= new Point3f (
				boundingBoxSize.x/splitting.x,
				boundingBoxSize.y/splitting.y,
				boundingBoxSize.z/splitting.z);
		setTopology(topology);
	}

	private void setTopology(int topology) {
		this.topology = topology;
		switch (topology) {
			case VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY	: toric = false; finite = true ; break;
			case VoxelManagerSettings.TORIC_FINITE_BOX_TOPOLOGY		: toric = true ; finite = false; break;
			case VoxelManagerSettings.TORIC_INFINITE_BOX_TOPOLOGY	: toric = true ; finite = false; break;
		}
	}
	/**
	 * Calculates the splitting of the voxel space in order to get more or less cubic voxels.
	 * Note that the final number of voxels is close but generally different from the input number
	 */
	public void setDefaultSpliting (int numberOfVoxels) {

		double volumeVoxel = (boundingBoxSize.x*boundingBoxSize.y*boundingBoxSize.z) / numberOfVoxels;
		double edge = Math.cbrt (volumeVoxel);		// average voxel edge

		int nVx = (int) ((boundingBoxSize.x/edge) + 0.5);
		int nVy = (int) ((boundingBoxSize.y/edge) + 0.5);
		int nVz = (int) ((boundingBoxSize.z/edge) + 0.5);

		splitting = new Point3i (nVx, nVy, nVz);
	}
	
	/**
	 * Return scene indices of a given point
	 * @param point
	 * @return scene indices
	 */
	public Point3i getSceneIndices (Point3f point) {
		Point3f pt =new Point3f (point);
		pt.sub (boundingBox.getMin ());

		double x = pt.x/boundingBoxSize.x;
		double y = pt.y/boundingBoxSize.y;

		return new Point3i((int) Math.floor (x), (int) Math.floor (y), 0);
	}
	
	/**
	 * @return the x, y and z indices of the voxel including the point under the Point3i format
	 * or null if the point is outside the voxel space except if the voxel space is toric.
	 * In such case the returned index is modulo the number of splitting along each coordinate 
	 */
	public Point3i getVoxelIndices (Point3f point) {

		// shift to scene Min
		Point3f pt =new Point3f (point);
		pt.sub (boundingBox.getMin ());
		
		if ((pt.z < 0) || (pt.z >= boundingBoxSize.z))
			return null;

		if (toric==false) {
			if ((pt.x < 0) || (pt.x >= boundingBoxSize.x))	return null;
			if ((pt.y < 0) || (pt.y >= boundingBoxSize.y))	return null;
		}
		pt.x /= voxelSize.x;
		pt.y /= voxelSize.y;
		pt.z /= voxelSize.z;
		
		Point3i indices = new Point3i();
		
		// voxel indexes
		indices.x = (int) Math.floor ((double) (pt.x % splitting.x)); if (indices.x<0) indices.x += splitting.x;
		indices.y = (int) Math.floor ((double) (pt.y % splitting.y)); if (indices.y<0) indices.y += splitting.y;
		indices.z = (int) Math.min (pt.z, splitting.z-1);
		return indices;

	}
	
	public Point3i getVoxelIndices (Point3f point, boolean recover) {
		if (recover)
			return getVoxelIndices (recoverPoint (point));
		else
			return getVoxelIndices (point);
	}

	private Point3f recoverPoint(Point3f point) {
		Point3f recovered = new Point3f(point);
		
		recovered.sub (boundingBox.getMin ());
		if (recovered.x<=0)
			recovered.x= (+voxelSize.x/2);
		else if (recovered.x>=boundingBoxSize.x)
			recovered.x= (boundingBoxSize.x-voxelSize.x/2);
		if (recovered.y<=0)
			recovered.y= (+voxelSize.y/2);
		else if (recovered.y>=boundingBoxSize.y)
			recovered.y= (boundingBoxSize.y-voxelSize.y/2);

		recovered.add (boundingBox.getMin ());

		return recovered;
	}
	
	/**
	 * @param bbox 			bounding box
	 * @return the x, y and z indices of voxels occupied by the given bounding box
	 */
	public Point3i[] getVoxelIndices (BoundingBox3f bbox) {
		
		ArrayList<Point3i> indexList = new ArrayList<Point3i>();
		
		// Find inf. & sup. corners
		Point3i min = getVoxelIndices(recoverPoint(bbox.getMin ()));
		Point3i max = getVoxelIndices(recoverPoint(bbox.getMax ()));
		
		// Computes occupied indices
		for (int X = min.x ; X <= max.x ; X++)
			for (int Y = min.y ; Y <= max.y ; Y++)
				for (int Z = min.z ; Z <= max.z ; Z++)
					indexList.add (new Point3i(X,Y,Z));
		
		return indexList.toArray (new Point3i[indexList.size ()]);
	}

	/**
	 * @param voxel			specified voxel
	 * @return the inf corner of the specified voxel
	 */
	public Point3f getVoxelInfCorner (Point3i voxel) {
		Point3f corner = new Point3f(voxel.x*voxelSize.x,voxel.y*voxelSize.y,voxel.z*voxelSize.z);
		corner.add (boundingBox.getMin ());
		return corner;
	}

	/**
	 * @param voxel			specified voxel
	 * @return the sup corner of the specified voxel
	 */
	public Point3f getVoxelSupCorner (Point3i voxel) {
		Point3f corner = new Point3f((voxel.x+1)*voxelSize.x,(voxel.y+1)*voxelSize.y,(voxel.z+1)*voxelSize.z);
		corner.add (boundingBox.getMin ());
		return corner;
	}

	public Point3f getRelativePoint (Point3f point) {
		Point3f relativePoint = new Point3f(point);
		relativePoint.sub(boundingBox.getMin());
		relativePoint.x = (int) Math.floor ((double) (relativePoint.x % boundingBoxSize.x)); if (relativePoint.x<0) relativePoint.x += boundingBoxSize.x;
		relativePoint.y = (int) Math.floor ((double) (relativePoint.y % boundingBoxSize.y)); if (relativePoint.y<0) relativePoint.y += boundingBoxSize.y;
		relativePoint.add(boundingBox.getMin());
		return relativePoint;
	}
	/**
	 * Returns shape voxel indices 
	 * @param shape	shape
	 * @return voxel indices
	 */
	public Point3i[] getShapeVoxelIndices(Shape shape){
		
		// Infinite shapes
		if (ShapeUtils.isInfiniteShape(shape))
			return getInfiniteShapeVoxelIndices(shape);
		// Finite shapes
		else
			return getFiniteShapeVoxelIndices(shape);
	}

	/**
	 * Returns voxel indices of a finite shape (Sphere, Ellipsoid, TriangulatedMesh, ...) 
	 * @param shape
	 * @return voxel indices
	 */
	public Point3i[] getFiniteShapeVoxelIndices(Shape shape){
		BoundingBox3f bbox = ShapeUtils.computeBoundingBox (shape);
		return getVoxelIndices (bbox);
	}
	
	/**
	 * Returns voxel indices of an infinite shape (plane, ...)
	 * @param shape
	 * @return voxel indices
	 */
	public Point3i[] getInfiniteShapeVoxelIndices(Shape shape){
		
		ArrayList<Point3i> indexList = new ArrayList<Point3i>();
		
		if (shape instanceof Plane) {
			for (int X = 0 ; X < splitting.x ; X++)
				for (int Y = 0 ; Y < splitting.y ; Y++)
					for (int Z = 0; Z < splitting.z ; Z++){
						BoundingBox3f bbox = new BoundingBox3f();
						Point3i currentIndices = new Point3i(X,Y,Z);
						bbox.update(getVoxelInfCorner(currentIndices));
						bbox.update(getVoxelSupCorner(currentIndices));
						ConvexMesh box = ShapeUtils.createRectangleBox(bbox, 0);
						if (ShapeUtils.collision((Plane) shape, (TriangulatedMesh) box))
							indexList.add(currentIndices);
					}
			return indexList.toArray (new Point3i[indexList.size ()]);
		}
		return null;
	}
	/**
	 * Same as getShapeVoxelIndices, with less voxel indices returned, but slow indices list building.
	 * @param shape	shape
	 * @return voxel indices
	 */
	public Point3i[] getShapeOptimumVoxelIndices(Shape shape){
		
		ArrayList<Point3i> indexList = new ArrayList<Point3i>();
		
		// Get the bounding ellipsoid of the shape
		Ellipsoid boundingEllipsoid = null;
		if (!ShapeUtils.isInfiniteShape(shape)) {
			BoundingShapeComputing bsc = new BoundingShapeComputing ();
			boundingEllipsoid = bsc.boundingEllipsoid (shape);
		}

		// Get the voxel indices of the shape, using its boundingBox
		Point3i[] indices = getShapeVoxelIndices(shape);

		if (boundingEllipsoid==null)
			return indices;
					
		// Eliminate the voxel indices wich don't include the bounding ellipsoid
		for (int i = 0 ; i < indices.length ; i++){
			Point3i voxel = indices[i];
			BoundingBox3f voxelBox = new BoundingBox3f();
			voxelBox.update (getVoxelInfCorner (voxel));
			voxelBox.update (getVoxelSupCorner (voxel));
			ConvexMesh box = ShapeUtils.createRectangleBox (voxelBox, 0);
			if (ShapeUtils.collision (box, boundingEllipsoid))
				indexList.add (voxel);
		}
		
		return indexList.toArray (new Point3i[indexList.size ()]);
	}

	/**
	 * Get the bounding box of the voxel space
	 * @return bounding box of the voxel space
	 */
	public BoundingBox3f getBoundingBox () {
		return boundingBox;
	}

	/**
	 * Get the splitting of the voxel space
	 * @return splitting of the voxel space
	 */
	public Point3i getSplitting () {
		return splitting;
	}

	/**
	 * Get the toricity of the voxel space
	 * @return toricity
	 */
	public boolean isToric () {
		return toric;
	}

	/**
	 * Returns if the voxelspace is finite
	 * @return finite
	 */
	public boolean isFinite() {
		return finite;
	}
	
	/**
	 * Get bounding box size
	 * @return bounding box size
	 */
	public Point3f getBoundingBoxSize () {
		return this.boundingBoxSize;
	}

	/**
	 * Get voxel space 
	 * @return voxel size
	 */
	public Point3f getVoxelSize() {
		return voxelSize;
	}
	
	/**
	 * Get the topology
	 * @return
	 */
	public int getTopology() {
		return topology;
	}
}
