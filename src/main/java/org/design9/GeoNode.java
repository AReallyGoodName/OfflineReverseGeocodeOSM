package org.design9;

import org.design9.kdtree.KDNodeComparator;

import java.util.Comparator;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toRadians;

/**
 * Created by Daniel Glasson on 14/12/2014.
 */
public class GeoNode extends KDNodeComparator<GeoNode> {
    public String name;
    public double point[] = new double[3]; // The 3D coordinates of the point

    public GeoNode(String name, double latitude, double longitude) {
        this.name = name;
        setPoint(latitude, longitude);
    }

    GeoNode(double latitude, double longitude) {
        name = "Search";
        setPoint(latitude, longitude);
    }

    private void setPoint( double latitude, double longitude ) {
        point[0] = cos(toRadians(latitude)) * cos(toRadians(longitude));
        point[1] = cos(toRadians(latitude)) * sin(toRadians(longitude));
        point[2] = sin(toRadians(latitude));
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    protected double squaredDistance(GeoNode other) {
        double x = this.point[0] - other.point[0];
        double y = this.point[1] - other.point[1];
        double z = this.point[2] - other.point[2];
        return (x*x) + (y*y) + (z*z);
    }

    @Override
    protected double axisSquaredDistance(GeoNode other, int axis) {
        double distance = point[axis] - other.point[axis];
        return distance * distance;
    }

    @Override
    protected Comparator<GeoNode> getComparator(int axis) {
        return GeoNameComparator.values()[axis];
    }

    protected static enum GeoNameComparator implements Comparator<GeoNode> {
        x {
            @Override
            public int compare(GeoNode a, GeoNode b) {
                return Double.compare(a.point[0], b.point[0]);
            }
        },
        y {
            @Override
            public int compare(GeoNode a, GeoNode b) {
                return Double.compare(a.point[1], b.point[1]);
            }
        },
        z {
            @Override
            public int compare(GeoNode a, GeoNode b) {
                return Double.compare(a.point[2], b.point[2]);
            }
        };
    }
}