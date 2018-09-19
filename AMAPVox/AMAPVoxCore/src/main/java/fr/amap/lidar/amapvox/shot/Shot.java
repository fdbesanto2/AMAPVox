/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.shot;

import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineSegment;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author Julien Heurtebize
 */
public class Shot {

    public final int index;
    public Point3d origin;
    public Vector3d direction;
    public double ranges[];
    private double angle = Double.NaN;
    public Echo[] echoes;

    public Shot(int index, Point3d origin, Vector3d direction, double ranges[]) {
        
        this.index = index;
        this.origin = origin;
        this.direction = direction;
        this.ranges = ranges;
        initEchoes();
    }

    /**
     * Copy constructor
     *
     * @param shot the shot to copy
     */
    public Shot(Shot shot) {
        
        this.index = shot.index;
        this.origin = new Point3d(shot.origin);
        this.direction = new Vector3d(shot.direction);
        this.angle = shot.angle;
        if (shot.ranges != null) {
            this.ranges = new double[shot.ranges.length];
            System.arraycopy(shot.ranges, 0, this.ranges, 0, shot.ranges.length);
        }
        initEchoes();
    }

    private void initEchoes() {

        echoes = new Echo[Math.max(getEchoesNumber(), 1)];

        if (getEchoesNumber() > 0) {
            for (int i = 0; i < echoes.length; i++) {
                LineSegment seg = new LineSegment(origin, direction, ranges[i]);
                echoes[i] = new Echo(i, new Point3d(seg.getEnd()));
            }
        } else {
            // empty shot
            LineSegment seg = new LineSegment(origin, direction, 999999);
            echoes[0] = new Echo(-1, new Point3d(seg.getEnd()));
        }
    }

    public int getEchoesNumber() {
        return ranges == null ? 0 : ranges.length;
    }

    private void calculateAngle() {

        this.angle = Math.toDegrees(Math.acos(Math.abs(direction.z)));
    }

    public double getAngle() {

        if (Double.isNaN(angle)) {
            calculateAngle();
        }

        return angle;
    }

    public boolean isEmpty() {

        if (ranges == null) {
            return true;
        } else {
            return ranges.length == 0;
        }
    }

    public double getFirstRange() {
        if (isEmpty()) {
            return Double.NaN;
        } else {
            return ranges[0];
        }
    }
    
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Shot ").append(index).append(", ").append(getEchoesNumber()).append(" echoes");
        str.append("\n").append("  origin ").append(origin).append(" direction ").append(direction);
        if (getEchoesNumber() > 0) {
            str.append("\n  ");
            for (int k = 0; k < getEchoesNumber(); k++) {
                str.append("Echo ").append(k).append( " ").append((float) ranges[k]).append("m ");
            }
        }
        return str.toString();
    }

    public class Echo {

        public final int rank;
        public final Point3d location;
        public final Shot shot;

        public Echo(int rank, Point3d location) {
            this.rank = rank;
            this.location = location;
            this.shot = Shot.this;
        }
    }
}
