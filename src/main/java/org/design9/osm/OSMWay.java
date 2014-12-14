package org.design9.osm;

import java.util.List;

/**
 * Created by Daniel Glasson on 13/12/2014.
 */
public class OSMWay {
    protected String name;
    protected List<Long> nodeIDs;

    public OSMWay(String name, List<Long> nodeIDs) {
        this.name = name;
        this.nodeIDs = nodeIDs;
    }
}
