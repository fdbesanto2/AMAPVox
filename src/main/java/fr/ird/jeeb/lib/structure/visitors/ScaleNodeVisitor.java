package fr.ird.jeeb.lib.structure.visitors;



import java.util.HashSet;

import fr.ird.jeeb.lib.structure.ArchiNode;
import fr.ird.jeeb.lib.structure.MeshGeometry;
import fr.ird.jeeb.lib.structure.geometry.mesh.Mesh;


public class ScaleNodeVisitor implements NodeVisitor {

	private double scale;
	private HashSet<Mesh> meshSet = new HashSet<Mesh>();
	
	public ScaleNodeVisitor (double scale) {
		this.scale = scale;
	}
	
	@Override
	public void visit(ArchiNode archiNode) {
		if (archiNode != null) {
			MeshGeometry geom = (MeshGeometry) archiNode.getGeometry();
			if(geom != null) {	
				
				geom.getTransformation().m03 *= scale;
				geom.getTransformation().m13 *= scale;
				geom.getTransformation().m33 *= scale;
				
				if(geom.getMesh() != null && !meshSet.contains(geom.getMesh())) {					
					geom.getMesh().scale((float)scale);
					meshSet.add(geom.getMesh());
				}
			}
		}		

	}

}
