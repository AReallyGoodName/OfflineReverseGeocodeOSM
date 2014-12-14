package org.design9;

import org.design9.kdtree.KDTree;
import org.design9.osm.OSMReader;

import java.io.IOException;
import java.util.List;

/**
 * Created by Daniel Glasson on 13/12/2014.
 */
public class OfflineReverseGeocode {
    KDTree<GeoNode> kdTree = null;

    public static void main(String[] args) {
        System.out.println("Reading OSM map!"); // Display the string.;
        try {
            List<GeoNode> nodes = OSMReader.openPBFFile("C:\\Projects\\offlinereversegeocode\\Sydney.osm.pbf");
            KDTree<GeoNode> kdTree = new KDTree<GeoNode>(nodes);
            System.out.println(kdTree.findNearest( new GeoNode( -33.79, 151.202 ) ));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
