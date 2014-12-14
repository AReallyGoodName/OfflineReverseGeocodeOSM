package org.design9.osm;

import org.design9.GeoNode;
import org.openstreetmap.osmosis.osmbinary.BinaryParser;
import org.openstreetmap.osmosis.osmbinary.Osmformat;
import org.openstreetmap.osmosis.osmbinary.file.BlockInputStream;
import org.openstreetmap.osmosis.osmbinary.file.BlockReaderAdapter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by Daniel Glasson on 13/12/2014.
 */
public class OSMReader extends BinaryParser {
    // Placenames we care about in the osm data (we want major places and towns)
    private static List<String> relevantPlaceNames = Arrays.asList("city", "borough", "suburb", "town", "village", "hamlet");

    // Ways we care about in the osm data (we want all the drivable public roads)
    private static List<String> relevantStreetNames = Arrays.asList("motorway", "trunk", "primary", "secondary", "tertiary", "unclassified", "residential");

    // Nodes and ways come in at different times but are linked.
    private Map<Long, AbstractMap.SimpleEntry<Double, Double>> savedNodes = new HashMap<Long, AbstractMap.SimpleEntry<Double, Double>>();
    private List<OSMWay> savedWays = new ArrayList<OSMWay>();
    private List<GeoNode> savedPlaces = new ArrayList<GeoNode>();

    public static List<GeoNode> openPBFFile( String file ) throws IOException {
        InputStream input = new FileInputStream(file);
        return openPBFFile(input);
    }

    public static List<GeoNode> openPBFFile( InputStream input ) throws IOException {
        OSMReader reader = new OSMReader();
        new BlockInputStream(input, reader).process();
        return reader.savedPlaces;
    }

    @Override
    protected void parseDense(Osmformat.DenseNodes nodes) {
        long lastId=0; // Nodes are delta encoded
        long lastLat=0;
        long lastLon=0;
        int count = nodes.getIdCount();
        int j = 0 ; // Index into the keysvals array.
        for (int i=0; i < count; i++) {
            long lat=nodes.getLat(i) + lastLat;
            long lon=nodes.getLon(i) + lastLon;
            long id=nodes.getId(i) + lastId;
            lastLat=lat;
            lastLon=lon;
            lastId=id;
            Boolean isPlaceName = false;
            String name = null;
            double latDouble = parseLat(lat);
            double lonDouble = parseLon(lon);
            if (nodes.getKeysValsCount() > 0) { // A dense node set has tags that apply to all the nodes in the set
                while (nodes.getKeysVals(j) != 0) {
                    int keyID = nodes.getKeysVals(j++);
                    int valID = nodes.getKeysVals(j++);
                    String key = getStringById(keyID);
                    if ( key.equals("place") ) {
                        String val = getStringById(valID);
                        if ( relevantPlaceNames.contains(val) ) {
                            isPlaceName = true;
                        }
                    }
                    if ( key.equals("highway") ) {
                        String val = getStringById(valID);
                        if ( relevantStreetNames.contains(val) ) {
                            savedNodes.put(id, new AbstractMap.SimpleEntry<Double, Double>( latDouble, lonDouble) ); // Save it to the list of nodes for lookup for ways
                        }
                    }
                    if ( key.equals("name") ) {
                        name = getStringById(valID);
                    }
                }
                j++; // Skip over the '0' delimiter so we can look at the next node on the next iteration
            }
            if ( isPlaceName ) {
                savedPlaces.add( new GeoNode( name, latDouble, lonDouble ) );
            }
        }
    }

    @Override
    protected void parseNodes(List<Osmformat.Node> nodes) {
        for (Osmformat.Node n : nodes) {
            Boolean isPlaceName = false;
            String name = null;
            double lat = parseLat(n.getLat());
            double lon = parseLon(n.getLon());
            for (int j = 0; j < n.getKeysCount(); j++) {
                // If a tag key "place" and the value "city", "borough", "suburb", "town", "village" or "hamlet" then it is a placename, put it in the lookup
                String key = getStringById(n.getKeys(j));
                if ( key.equals("place") ) {
                    String val = getStringById(n.getVals(j));
                    if ( relevantPlaceNames.contains(val) ) {
                        isPlaceName = true;
                    }
                }
                if ( key.equals("highway") ) {
                    String val = getStringById(n.getVals(j));
                    if ( relevantStreetNames.contains(val) ) {
                        savedNodes.put(n.getId(), new AbstractMap.SimpleEntry<Double, Double>( lat, lon) ); // Save it to the list of nodes for lookup for ways
                    }
                }
                if ( key.equals("name") ) {
                    name = getStringById(n.getVals(j));
                }
            }

            if ( isPlaceName ) {
                // name, lat, lon is now available
                savedPlaces.add( new GeoNode( name, lat, lon ) );
            }
        }
    }

    @Override
    protected void parseWays(List<Osmformat.Way> ways) {
        //System.out.println("Got way");
        for (Osmformat.Way w : ways) {
            Boolean isStreetName = false;
            String streetName = null;
            double lat;
            double lon;
            for (int j = 0; j < w.getKeysCount(); j++) {
                // If the tag key is "highway" and the value "motorway", "trunk", "primary", "secondary", "tertiary", "unclassified" or "residential" then it is a street, put it in the lookup
                String key = getStringById(w.getKeys(j));
                if ( key.equals("highway") ) {
                    String val = getStringById(w.getVals(j));
                    if ( relevantStreetNames.contains(val) ) {
                        isStreetName = true;
                    }
                }
                if ( key.equals("name") ) {
                    streetName = getStringById(w.getVals(j));
                }
            }
            if ( isStreetName ) {
                // Get all the nodes that make up the way and put them into the r-tree street lookup
                List<Long> nodeIDs = new ArrayList<Long>();
                for (Long ref : w.getRefsList()) {
                    // Add these ids to a list of nodes that make up this street so we can look them up on completion
                    nodeIDs.add(ref);
                }
                OSMWay osmWay = new OSMWay(streetName, nodeIDs);
            }
        }
    }

    @Override
    protected void parseRelations(List<Osmformat.Relation> relations) {
    }

    @Override
    protected void parse(Osmformat.HeaderBlock headerBlock) {
    }

    @Override
    public void complete() {
        // Todo: Make polygons for the ways and their nodes and put that into the lookup
    }
}
