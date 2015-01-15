package fr.ird.jeeb.lib.structure.visitors;

import javax.vecmath.Matrix4f;

import fr.ird.jeeb.lib.structure.ArchiNode;
import fr.ird.jeeb.lib.structure.SimpleGeometry;

public class TransformNodeVisitor implements NodeVisitor{

	
	private Matrix4f transform;

	public TransformNodeVisitor (Matrix4f transform) {
		this.transform = transform; 
	}
	
	@Override
	public void visit(ArchiNode archiNode) {
		if (archiNode != null) {
			SimpleGeometry geom = archiNode.getGeometry();
			if(geom != null) {
				
				geom.transform(transform);
				
			}
		}		
	}

}
