package fr.amap.lidar.amapvox.jeeb.raytracing.voxel;

import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.Intersection;
import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.LineElement;
import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.LineSegment;
import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.shapes.ShapeUtils;
import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.shapes.VolumicShape;
import fr.amap.lidar.amapvox.jeeb.raytracing.util.BoundingBox3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;

import javax.vecmath.Point3d;

/**
 * Voxel Manager class -Create the voxel space (Assuming user plot box) -Manage
 * the line segment crossing, and next encountered voxel -Manage the first entry
 * of the ray in the voxel space
 *
 * @author Cresson, Aug. 2012
 *
 */
public class VoxelManager {

    //private static final double 				BBOX_SCENE_MARGIN	= 0.01f;
    private static final double BBOX_SCENE_MARGIN = 0.0f;
    private static final double MARGIN = 0.0000000001;

    private final VoxelSpace voxelSpace;
    public VolumicShape sceneCanvas;
    int count = 0;

    /**
     * Used for returning voxel crossing context Estimation of the line segment
     * length to set, during the current voxel crossing (length) Prediction of
     * the next voxel encountered (after the current voxel) (indices)
     * Calculation of the translation to perform on the line segment (when exit
     * the scene bounding box and toric voxel space) (translation)
     *
     * @author cresson
     *
     */
    public class VoxelCrossingContext {

        public Point3i indices;
        public Vector3d translation;
        public double length;

        public VoxelCrossingContext(Point3i indices, double length, Vector3d t) {
            this.indices = indices;
            this.length = length;
            this.translation = t;
        }
    }

    /**
     * Voxel Manager Constructor
     *
     * @param scene scene
     * @param voxelManagerSettings voxel manager settings
     */
    public VoxelManager(Scene scene, VoxelManagerSettings voxelManagerSettings) {

        // Find the bounding box of the ArrayList of mesh
        BoundingBox3d bbox = scene.getBoundingBox();

        // Add margins to the bounding box
        bbox = ShapeUtils.getPaddedBoundingBox(bbox, BBOX_SCENE_MARGIN);

        // Build voxelSpace
        voxelSpace = new VoxelSpace(bbox, voxelManagerSettings.getSplitting(), voxelManagerSettings.getTopology());

        // Build scene canvas
        buildSceneCanvas();
    }


    /*
	 * Build scene canvas.
	 *		1.sceneCanvas = a box (Topology NON_TORIC_FINITE_BOX_TOPOLOGY or TORIC_FINITE_BOX_TOPOLOGY)
	 *	or
	 *		2.sceneCanvas = an infinite box (Topology TORIC_INFINITE_BOX_TOPOLOGY)
     */
    private void buildSceneCanvas() {

        // Scene bounding box
        BoundingBox3d sceneBoundingBox = voxelSpace.getBoundingBox();
        // Scene boundaries are a box (the bounding box of the scene, plus a margin)
        sceneCanvas = ShapeUtils.createRectangleBox(sceneBoundingBox, BBOX_SCENE_MARGIN, true);
    }

    /*
	 * Returns hypothenuse (Thales theorem)
	 * @param zVectorOrigin		origin of vector
	 * @param zBoxOrigin		origin of the z-coordinate system
	 * @param zVectorDir		norm of the vector
	 * @return the hypothenuse, i.e. norm of z-axis
     */
    private double hypothenuse(double zVectorOrigin, double zBoxOrigin, double zVectorDir) {
        if (zVectorDir == 0) {
            return Float.MAX_VALUE;
        }
        return (zVectorOrigin - zBoxOrigin) / zVectorDir;
    }

    /**
     * @param lineElement Line element in the current voxel
     * @param currentVoxel Current voxel
     * @return Voxel crossing context of the current voxel (Indices, Length,
     * Translation).
     * @see VoxelCrossingContext CrossVoxel(Point3d, Vector3d, Point3i)
     */
    public VoxelCrossingContext CrossVoxel(LineElement lineElement, Point3i currentVoxel) {
        return CrossVoxel(lineElement.getOrigin(), lineElement.getDirection(), currentVoxel);
    }

    /**
     * Return the voxel crossing context of the current voxel. A voxel crossing
     * context is: 1. Incides of the next encounter voxel (Point3i) 2. Length of
     * the line segment in the current voxel (double) 3. Translation to apply to
     * the line segment in the current voxel (Vector3d)
     *
     * @param startPoint Start point of the line element in the current voxel
     * @param direction	Direction of the line element in the current voxel
     * @param currentVoxel Current voxel
     * @return Voxel crossing context of the current voxel (Indices, Length,
     * Translation).
     */
    public VoxelCrossingContext CrossVoxel(Point3d startPoint, Vector3d direction, Point3i currentVoxel) {

        Point3d infCorner = voxelSpace.getVoxelInfCorner(currentVoxel);
        double x, y, z;

        if (direction.x < 0) {
            x = hypothenuse(startPoint.x, infCorner.x, direction.x);
        } else {
            x = hypothenuse(infCorner.x + voxelSpace.getVoxelSize().x, startPoint.x, direction.x);
        }
        if (direction.y < 0) {
            y = hypothenuse(startPoint.y, infCorner.y, direction.y);
        } else {
            y = hypothenuse(infCorner.y + voxelSpace.getVoxelSize().y, startPoint.y, direction.y);
        }
        if (direction.z < 0) {
            z = hypothenuse(startPoint.z, infCorner.z, direction.z);
        } else {
            z = hypothenuse(infCorner.z + voxelSpace.getVoxelSize().z, startPoint.z, direction.z);
        }

        int vx = currentVoxel.x;
        int vy = currentVoxel.y;
        int vz = currentVoxel.z;

        double a = Math.abs(x);
        double b = Math.abs(y);
        double c = Math.abs(z);

        // Find minimums (even multiple occurrences)
        double sc = 0;	// sc: min distance to current voxel walls (in x,y or z)
        if (a <= b) {
            if (a <= c) {
                vx += (int) Math.signum(direction.x);
                sc = a;
            }
        }
        if (b <= a) {
            if (b <= c) {
                vy += (int) Math.signum(direction.y);
                sc = b;
            }
        }
        if (c <= a) {
            if (c <= b) {
                vz += (int) Math.signum(direction.z);
                sc = c;
            }
        }

        if (vz >= voxelSpace.getSplitting().z | vz < 0) // Voxel space is never toric in Z-Coordinates ! return a null voxel indices and null translation
        {
            return new VoxelCrossingContext(null, sc, null);
        }

        Point3i sp = voxelSpace.getSplitting();		// Voxel space splittings
        Point3d sz = voxelSpace.getBoundingBoxSize();	// Voxel space bounding box size

        Vector3d t = new Vector3d(0, 0, 0);
        if (!voxelSpace.isFinite()) {
            // When the voxel space is infinite, computes the translation and the new voxel indices
            if (vx >= sp.x) {
                vx = 0;
                t.add(new Vector3d(sz.x, 0, 0));
            } else if (vx < 0) {
                vx = sp.x - 1;
                t.add(new Vector3d(-sz.x, 0, 0));
            }
            if (vy >= sp.y) {
                vy = 0;
                t.add(new Vector3d(0, sz.y, 0));
            } else if (vy < 0) {
                vy = sp.y - 1;
                t.add(new Vector3d(0, -sz.y, 0));
            }
        } else // When the voxel space is finite, return a null voxel indices and null translation
        {
            if (vx >= sp.x | vx < 0 | vy >= sp.y | vy < 0) {
                return new VoxelCrossingContext(null, sc, null);
            }
        }

        return new VoxelCrossingContext(new Point3i(vx, vy, vz), sc, t);

    }

    private Point3d recalculateIntersection(LineElement lineElement, Intersection intersection) {

        Point3d end = lineElement.getEnd();
        Vector3d normal = intersection.getNormal();
        BoundingBox3d boundingBox = voxelSpace.getBoundingBox();

        //avoid -0.0
        normal.x += 0.0;
        normal.y += 0.0;
        normal.z += 0.0;

        if (normal.x == -1.0) {
            end.x = boundingBox.min.x + MARGIN;
        } else if (normal.x == 1.0) {
            end.x = boundingBox.max.x - MARGIN;
        }

        if (normal.y == -1.0) {
            end.y = boundingBox.min.y + MARGIN;
        } else if (normal.y == 1.0) {
            end.y = boundingBox.max.y - MARGIN;
        }

        if (normal.z == -1.0) {
            end.z = boundingBox.min.z + MARGIN;
        } else if (normal.z == 1.0) {
            end.z = boundingBox.max.z - MARGIN;
        }

        return end;
    }

    //alternative à sceneCanvas.contains() pour une bounding-box de type rectangle (@author: Julien Heurtebize)
    public boolean isPointInsideBoundingBox(Point3d point) {

        Point3d min = voxelSpace.getBoundingBox().min;
        Point3d max = voxelSpace.getBoundingBox().max;

        return (point.x > min.x && point.y > min.y && point.z > min.z
                && point.x < max.x && point.y < max.y && point.z < max.z);
    }

    /**
     * Returns the voxel context of the first entry in the scene canvas
     *
     * @param lineElement line element
     * @return VoxelCrossingContext of the first encountered voxel
     */
    public VoxelCrossingContext getFirstVoxel(LineElement lineElement) {

        boolean intersectionForDebug;

        Point3d intersectionPoint = new Point3d();

        if (sceneCanvas.contains(lineElement.getOrigin())) {
            // If the line origin is already in the scene canvas, just get its origin point
            intersectionPoint.add(lineElement.getOrigin());
            intersectionForDebug = false;
            //System.out.println(count);
            //count++;
        } else {
            // Computes intersection with the scene canvas
            Intersection intersection = sceneCanvas.getNearestIntersection(lineElement);

            if (intersection != null) {

                // If the intersection exists, get the intersection point
                LineElement e = new LineSegment(lineElement.getOrigin(), lineElement.getDirection(), intersection.distance);
                //intersectionPoint = e.getEnd();
                intersectionPoint = recalculateIntersection(e, intersection);

                //Point3d offset = new Point3d(lineElement.getDirection ());
                //offset.scale(0.0000053d);
                //System.out.println("intersectionPoint: "+intersectionPoint);
                //intersectionPoint.add(offset);
                intersectionForDebug = true;
            } else {
                // Else (no intersection & no inside), bye bye.
                return null;
            }
        }

        Point3i intersectionPointVoxelIndices = voxelSpace.getVoxelIndices(intersectionPoint);
        if (intersectionPointVoxelIndices == null) {
            if (intersectionForDebug) {
                //logger.error("The given line element does intersect the scene canvas "+intersectionPoint+", but unable to get its voxel indices");

            } else {/*
                        logger.info("The given line element belongs the scene canvas "+intersectionPoint+", but unable to get its voxel indices");
                        
                        logger.info("Voxel space informations are:");
                        logger.info("\tCorner Inf\t:\t" + voxelSpace.getBoundingBox ().getMin ());
                        logger.info("\tCorner Sup\t:\t" + voxelSpace.getBoundingBox ().getMax ());*/
            }

            return null;
        }

        // Computes the 1st translation (from line origin, to intersection point with the scene canvas)
        Vector3d translation = new Vector3d(intersectionPoint);
        translation.sub(lineElement.getOrigin());

        /*
                // Computes the 2nd translation (from the intersection point with the scene canvas, to the point of the "real" scene)
		Point3i intersectionPointSceneIndices = voxelSpace.getSceneIndices (intersectionPoint);
                
                if(intersectionPointSceneIndices.x != 0 || intersectionPointSceneIndices.y != 0 || intersectionPointSceneIndices.z != 0){
                    System.out.println("test");
                }
                
		Vector3d secondTranslation = new Vector3d (intersectionPointSceneIndices.x*voxelSpace.getBoundingBoxSize ().x,intersectionPointSceneIndices.y*voxelSpace.getBoundingBoxSize ().y,intersectionPointSceneIndices.z*voxelSpace.getBoundingBoxSize ().z);
         */
        // Careful: Length is the length of the 1st translation !
        double l = translation.length();

        // Computes the total translation (1st translation + 2nd translation)
        //translation.sub (secondTranslation);
        // Return the voxel indices, the line length, and the translation
        return new VoxelCrossingContext(intersectionPointVoxelIndices, l, translation);

    }
    
    //(algorithme de smit)
    public static Intersection getIntersectionLineBoundingBox(Point3d startPoint, Point3d endPoint, BoundingBox3d boundingBox3D) {

        double tmin, tmax, tymin, tymax, tzmin, tzmax;

        Point3d[] bounds = new Point3d[]{boundingBox3D.min, boundingBox3D.max};

        Vector3d direction = new Vector3d(endPoint.x - startPoint.x, endPoint.y - startPoint.y, endPoint.z - startPoint.z);
        direction.normalize();
        Vector3d invDirection = new Vector3d(1.0/direction.x, 1.0/direction.y, 1.0/direction.z);
        int sign[] = new int[]{(invDirection.x < 0)? 1 : 0, (invDirection.y < 0)? 1 : 0 , (invDirection.z < 0)? 1 : 0};

        tmin = (bounds[sign[0]].x - startPoint.x) * invDirection.x;
        tmax = (bounds[1 - sign[0]].x - startPoint.x) * invDirection.x;
        tymin = (bounds[sign[1]].y - startPoint.y) * invDirection.y;
        tymax = (bounds[1 - sign[1]].y - startPoint.y) * invDirection.y;

        if ((tmin > tymax) || (tymin > tmax)) {
            return null;
        }
        if (tymin > tmin || Double.isNaN(tmin)) {
            tmin = tymin;
        }
        if (tymax < tmax || Double.isNaN(tmax)) {
            tmax = tymax;
        }

        tzmin = (bounds[sign[2]].z - startPoint.z) * invDirection.z;
        tzmax = (bounds[1 - sign[2]].z - startPoint.z) * invDirection.z;

        if ((tmin > tzmax) || (tzmin > tmax)) {
            return null;
        }
        if (tzmin > tmin || Double.isNaN(tmin)) {
            tmin = tzmin;
        }
        if (tzmax < tmax || Double.isNaN(tmax)) {
            tmax = tzmax;
        }

        if(tmax<tmin){
            //System.out.println("test");
        }

        return new Intersection(tmin, new Vector3d());
    }

    public VoxelCrossingContext getFirstVoxelV2(LineElement lineElement) {

        Point3d intersectionPointV2 = new Point3d();

        if (voxelSpace.getBoundingBox().contains(lineElement.getOrigin())) {
            // If the line origin is already in the scene canvas, just get its origin point
            intersectionPointV2.add(lineElement.getOrigin());
        } else {
            // Computes intersection with the scene canvas

            /*if(lineElement.getOrigin().x == 0 && lineElement.getOrigin().y == 0 && lineElement.getOrigin().z == 0){
                lineElement.getOrigin().x +=  0.00001; //patch, need to be handle properly later
                lineElement.getOrigin().y +=  0.00001;
                lineElement.getOrigin().z +=  0.00001;
            }*/
            
            //Intersection intersection = getIntersectionLineBoundingBox(lineElement.getOrigin(), lineElement.getEnd(), voxelSpace.getBoundingBox());
            
            Intersection intersection = sceneCanvas.getNearestIntersection(lineElement);

            /*if(intersection == null || intersectionV2 == null){
                
                if((intersection == null && intersectionV2 != null) || intersectionV2 == null && intersection != null){
                    System.out.println("test");
                }
            }else if(intersection.distance != intersectionV2.distance){
                System.out.println("test");
            }*/
            
           
            if (intersection != null) {

                // If the intersection exists, get the intersection point
                LineElement e = new LineSegment(lineElement.getOrigin(), lineElement.getDirection(), intersection.distance);
                intersectionPointV2 = recalculateIntersection(e, intersection);
            } else {
                // Else (no intersection & no inside), bye bye.
                return null;
            }
        }

        Point3i intersectionPointVoxelIndices = voxelSpace.getVoxelIndices(intersectionPointV2);
        if (intersectionPointVoxelIndices == null) {

            return null;
        }

        // Computes the 1st translation (from line origin, to intersection point with the scene canvas)
        Vector3d translation = new Vector3d(intersectionPointV2);
        translation.sub(lineElement.getOrigin());

        // Careful: Length is the length of the 1st translation !
        double l = translation.length();

        // Return the voxel indices, the line length, and the translation
        return new VoxelCrossingContext(intersectionPointVoxelIndices, l, translation);

    }

    public Point3i getVoxelIndicesFromPoint(Point3d point) {
        return voxelSpace.getVoxelIndices(point);
    }

    public String getInformations() {

        StringBuilder sb = new StringBuilder();
        sb.append("Voxel space informations:\n");
        sb.append("\t\t\tCorner Inf\t:\t").append(voxelSpace.getBoundingBox().getMin()).append("\n");
        sb.append("\t\t\tCorner Sup\t:\t").append(voxelSpace.getBoundingBox().getMax()).append("\n");
        sb.append("\t\t\tDimensions\t:\t").append(voxelSpace.getSplitting()).append("\n");
        sb.append("\t\t\tToricity \t:\t").append(voxelSpace.isToric()).append("\n");

        return sb.toString();
    }

    /**
     * Return the inf. corner coordinates of the specified voxel
     *
     * @param voxel voxel
     * @return corner coordinates
     */
    public Point3d getInfCorner(Point3i voxel) {
        return voxelSpace.getVoxelInfCorner(voxel);
    }

    /**
     * Return the sup. corner coordinates of the specified voxel
     *
     * @param voxel voxel
     * @return corner coordinates
     */
    public Point3d getSupCorner(Point3i voxel) {
        Point3i i = new Point3i(voxel);
        i.add(new Point3i(1, 1, 1));
        return voxelSpace.getVoxelInfCorner(i);
    }

    /**
     * Return the relative coordinates of a point
     *
     * @param point point
     * @return relative coordinates of the point
     */
    public Point3d getRelativePoint(Point3d point) {
        return voxelSpace.getRelativePoint(point);
    }

    public VoxelSpace getVoxelSpace() {
        return voxelSpace;
    }
}
