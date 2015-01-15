package fr.ird.jeeb.lib.structure;

import java.io.Serializable;

import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import fr.ird.jeeb.lib.math.InlineMath;
import fr.ird.jeeb.lib.structure.geometry.mesh.Mesh;
import fr.ird.jeeb.lib.structure.geometry.util.BoundingBox3d;

;

public abstract class SimpleGeometry implements Serializable{

	protected Matrix4f transformation = new Matrix4f();
	
	protected double length = Double.NaN; // The scale in the main direction (Z axis)

	


	public SimpleGeometry() {
		transformation.setIdentity();
	}

	public SimpleGeometry(SimpleGeometry m) {

		this.length = m.length;
		
		this.transformation = (Matrix4f) m.transformation.clone();


	}
	
	public abstract Object clone() ;
	
	

	
	

	/* (non-Javadoc)
	 * @see jeeb.lib.structure.ArchiNodeGeometry#getNormal()
	 */
	
	public Vector4f getNormal() {
		if(transformation==null) return null;
		Vector4f normal = new Vector4f();
		transformation.getColumn(2, normal);
		normal.normalize();		
		return normal;
	}
	
	public Vector4f  getDirection(boolean scaled) {
		if(transformation==null) return null;
		Vector4f direction = new Vector4f();
		transformation.getColumn(0, direction );
		direction.normalize();
		if(scaled && !((Double)length).equals(Double.NaN))
			direction.scale((float) length);
		return direction;
	}

	public Vector4f  getSecondaryDirection() {
		if(transformation==null) return null;
		Vector4f direction = new Vector4f();		
		transformation.getColumn(1, direction );
		direction.normalize();
		return direction;
	}

	public abstract Vector4f getPositionAt(float distanceFromTheBasePosition);

	public abstract Vector4f getTopPosition();
	
	public abstract void setAverageWidth(double width);
	
	public abstract double getAverageWidth();
	
	public abstract void setAverageHeight(double width);
	
	public abstract double getAverageHeight();

	//public abstract Vector4f getDirection();

	public double getLength() {
		return length;
	}

	

	public abstract Matrix4f getLocalTransformationAt(float distanceFromTheBasePosition);
	
	public abstract Matrix4f getTransformationAt(float distanceFromTheBasePosition);

	public void setLength(double length) {
		this.length = length;

	}
	
	
	
	public void setTransformation(Matrix4f transformation) {
		this.transformation = transformation;
	}
	
	public void transform (Matrix4f transform) {
		Matrix4f t = (Matrix4f) transform.clone();
		t.mul(transformation);
		transformation = t;
	}
		
	public Matrix4f getTransformation() {
		return transformation;
	}

	public Matrix4f getNonUniformTransformation() {
		Matrix4f nonUniformTransform = (Matrix4f) transformation.clone();
//		nonUniformTransform.m01 *= (float) length;
//		nonUniformTransform.m11 *= (float) length;
//		nonUniformTransform.m21 *= (float) length;
		if (!((Double)length).equals(Double.NaN))
			InlineMath.setNonUniformScale(nonUniformTransform, new Vector3f((float) length,1.f,1.f));
		return nonUniformTransform;
	}
	
	public void setPosition(Vector4f position) {
		this.transformation.setTranslation(new Vector3f(position.x,position.y,position.z));
	}
	
	
	public Vector4f getPosition() {
		if(transformation==null) return null;
		Vector4f position = new Vector4f();
		transformation.getColumn(3, position );
		return position;
		
	}

	public abstract Mesh getTransformedMesh();
	
	public abstract Mesh getTransformedMesh(Mesh mesh);

	
	
	public String toString() {
		return "Position = " + getPosition().toString();
	}

	public BoundingBox3d computeBBox() {
		
		Vector4f basePosition = getPosition();
		Vector4f topPosition = getTopPosition();
		Vector4f offsetWidth = getNormal ();
		
		Vector4f offsetHeight = getSecondaryDirection ();
		
		
		offsetWidth.scale ((float) getAverageWidth()/2.0f);
		offsetHeight.scale ((float) getAverageHeight()/2.0f);
		
	
		
		BoundingBox3d bbox = new BoundingBox3d();
		bbox.update(new Point3d(basePosition.x, basePosition.y, basePosition.z));
		bbox.update(new Point3d(topPosition.x, topPosition.y, topPosition.z));
		
		topPosition.add (offsetWidth);
		topPosition.add (offsetHeight);
		offsetWidth.negate ();
		offsetHeight.negate ();
		basePosition.add (offsetWidth);
		basePosition.add (offsetHeight);
		bbox.update(new Point3d(basePosition.x, basePosition.y, basePosition.z));
		bbox.update(new Point3d(topPosition.x, topPosition.y, topPosition.z));
		
		return bbox;
		//return new BoundingBox3d(new Point3d(basePosition.x, basePosition.y, basePosition.z), new Point3d(topPosition.x, topPosition.y, topPosition.z));
	}

	public void reset () {
		transformation.setIdentity ();		
		length = Double.NaN;		
	}

	
}
