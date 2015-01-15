package fr.ird.jeeb.lib.structure;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector4f;

import fr.ird.jeeb.lib.structure.geometry.mesh.Mesh;
import fr.ird.jeeb.lib.structure.geometry.util.BoundingBox3d;

;

public class MeshGeometry extends SimpleGeometry {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected Mesh mesh; // current 3D mesh
	protected double bottom_width = Double.NaN; // current width
	protected double top_width = Double.NaN; // current width
	protected double bottom_height = Double.NaN; // height ration : scale Z local axis
	protected double top_height = Double.NaN;

	public MeshGeometry() {
		super();
		transformation.setIdentity();
	}

	public MeshGeometry(MeshGeometry m) {
		super(m);
		this.mesh = m.mesh;
		this.bottom_width = m.bottom_width;
		this.top_width = m.top_width;
		this.bottom_height = m.bottom_height;
		this.top_height = m.top_height;
	}

	public Object clone() {
		return new MeshGeometry(this);
	}

	@Override
	public double getAverageWidth() {
		return (bottom_width + top_width) / 2.;
	}

	public double getBottomWidth() {
		return bottom_width;
	}

	public double getTopWidth() {
		return top_width;
	}

	public void setAverageHeight(double height) {
		this.bottom_height = height;
		this.top_height = height;
	}

	@Override
	public double getAverageHeight() {
		return (bottom_height + top_height) / 2.0;
	}

	public void setTopHeight(double height) {
		this.top_height = height;
	}

	public double getTopHeight() {
		return top_height;
	}

	public void setBottomHeight(double height) {
		this.bottom_height = height;
	}

	public double getBottomHeight() {
		return bottom_height;
	}

	public Vector4f getNormal() {
		if (transformation == null)
			return null;
		Vector4f normal = new Vector4f();
		transformation.getColumn(2, normal);
		return normal;
	}

	@Override
	public void setAverageWidth(double width) {
		this.bottom_width = width;
		this.top_width = width;
	}

	public void setBottomWidth(double bottom_width) {
		this.bottom_width = bottom_width;
	}

	public void setTopWidth(double top_width) {
		this.top_width = top_width;
	}

	@Override
	public Vector4f getPositionAt(float distanceFromTheBasePosition) {
		if (transformation == null)
			return null;
		Vector4f direction = new Vector4f();
		transformation.getColumn(0, direction);
		direction.normalize();
		direction.scale((float) (distanceFromTheBasePosition));
		Vector4f topPosition = getPosition();
		topPosition.add(direction);
		return topPosition;
	}

	@Override
	public Vector4f getTopPosition() {
		if (transformation == null)
			return null;
		Vector4f direction = new Vector4f();
		transformation.getColumn(0, direction);
		direction.normalize();
		direction.scale((float) length);
		Vector4f topPosition = getPosition();
		topPosition.add(direction);
		return topPosition;
	}

	@Override
	public Matrix4f getTransformationAt(float distanceFromTheBasePosition) {
		return (Matrix4f) transformation.clone();
	}

	@Override
	public Matrix4f getLocalTransformationAt(float distanceFromTheBasePosition) {
		Matrix4f local = new Matrix4f();
		local.setIdentity();
		return local;
	}

	public void setMesh(Mesh m) {
		this.mesh = m;
	}

	public Mesh getMesh() {
		return mesh;
	}

	@Override
	public Mesh getTransformedMesh() {
		return getTransformedMesh(this.mesh);
	}

	@Override
	public Mesh getTransformedMesh(Mesh mesh) {

		if (mesh != null) {
			Mesh transformedMesh = (Mesh) mesh.clone();
			if (transformation != null) {

				if (   mesh.getEnableScaling()
					&& !((Double)(top_height)).equals(Double.NaN)) {
					transformedMesh.taper((float) bottom_height, (float) top_height, (float) bottom_width, (float) top_width);
					Matrix4f nonUniformTransform = getNonUniformTransformation();
					transformedMesh.transform(nonUniformTransform);
				} else {
					transformedMesh.transform(transformation);
				}

			}
			return transformedMesh;
		}
		return null;
	}
	public void reset () {
		super.reset();
		bottom_width = Double.NaN; // current width
		top_width = Double.NaN; // current width
		bottom_height = Double.NaN; // height ration : scale Z local axis
		top_height = Double.NaN;	
		mesh = null;
		
	}
	
	@Override
	public BoundingBox3d computeBBox () {
	
		if(mesh != null) {
			return getTransformedMesh().computeBBox ();
		}
		return super.computeBBox ();
	}

}
