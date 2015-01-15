package fr.ird.jeeb.lib.structure.geometry.mesh;


import javax.vecmath.Point3f;


public class TaperTransform {

	  //
	private static final double DEFAULT_BASE_RADIUS = 1.;

	private static final double DEFAULT_TOP_RADIUS = 1.;  
	  
	private double baseWidth;	
	private double topWidth;
	
	private double baseHeight;	
	private double topHeight;
	
	public TaperTransform() {
		
		baseWidth = DEFAULT_BASE_RADIUS;
		topWidth = DEFAULT_TOP_RADIUS;
		
		baseHeight = DEFAULT_BASE_RADIUS;
		topHeight = DEFAULT_TOP_RADIUS;
	}
	
	public TaperTransform(double baseWidth, double topWidth, double baseHeight, double topHeight) {
		
		this.baseWidth = baseWidth;
		this.topWidth = topWidth;
		
		this.baseHeight = baseHeight;
		this.topHeight = topHeight;
	}
	
	
	public void transform( SimpleMesh mesh ) {  
		  Point3f[] points = mesh.getPoints();	  
		  
		  if (points.length > 0) {
			  
			  double deltaX = mesh.getBBox().getMax().x - mesh.getBBox().getMin().x;
			  if(Double.isInfinite(deltaX) || Double.isNaN(deltaX) || deltaX == 0) return;
			  
			  double deltaWidth = baseWidth - topWidth;
			  double deltaHeight = baseHeight - topHeight;

			  for(Point3f p : points) {
				  
				  float dX = (float) (p.x - mesh.getBBox().getMin().x);
				  float factorW = (float) (baseWidth - deltaWidth * (dX / deltaX));
				  float factorH = (float) (baseHeight - deltaHeight * (dX / deltaX));
			      float [] newPoint = new float [3];
			      newPoint[0]=p.x ;
			      newPoint[1]=p.y * factorH;
			      newPoint[2]=p.z * factorW;
			      p.set(newPoint);
			  }
		  }
		}

	
}
