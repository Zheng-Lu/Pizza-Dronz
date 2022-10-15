package uk.ac.ed.inf;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Map {

    /** constants */
    public static final double APPLETON_LONGITUDE = -3.186874;
    public static final double APPLETON_LATITUDE = 55.944494;

    private String year;
    private String month;
    private String day;

    private LngLat startPos;
    private List<Polygon> noFlyZones;
    private List<LngLat> restaurant;

    /**
     * Create the map for a day
     * @param year year from input
     * @param month month from input
     * @param day day from input
     */
    public Map(String year, String month, String day) {
        this.noFlyZones = new ArrayList<Polygon>();
        this.restaurant = new ArrayList<LngLat>();
        this.year = year;
        this.month = month;
        this.day = day;
        this.startPos = new LngLat(APPLETON_LONGITUDE, APPLETON_LATITUDE);

        initializeMap();

    }

    /**
     * Set up the no-fly-zones, landmarks, orders of the map
     */
    private void initializeMap() {

    }
}
