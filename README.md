OfflineReverseGeocodeOSM
========================

Offline reverse geocoding using OSM datasets.

Similar to https://github.com/AReallyGoodName/OfflineReverseGeocode but instead of using geonames it uses openmaps.

Current usage:

List<GeoNode> nodes = OSMReader.openPBFFile("C:\\Projects\\offlinereversegeocode\\Sydney.osm.pbf");

KDTree<GeoNode> kdTree = new KDTree<GeoNode>(nodes); 

System.out.println(kdTree.findNearest( new GeoNode( -33.79, 151.202 ) ));

System.out.println(kdTree.findNearest( new GeoNode( -34.56, 152.345 ) ));

Prints 
North Willoughby
Little Bay

Uses osm pdf datasets. Currently only does suburbs but will do streets soon.
