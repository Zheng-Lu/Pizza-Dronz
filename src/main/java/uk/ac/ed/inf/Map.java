package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Map {

    /** constants */
    public static final double APPLETON_LONGITUDE = -3.186874;
    public static final double APPLETON_LATITUDE = 55.944494;

    private final String year;
    private final String month;
    private final String day;

    private final ReadData readData;

    private final LngLat startPos;
    private final List<Polygon> noFlyZones;
    private List<LngLat> restaurants;

    /** getters */
    public List<Polygon> getNoFlyZones(){
        return this.noFlyZones;
    }

    public LngLat getStartPos(){
        return this.startPos;
    }


    /**
     * Create the map for a day
     * @param year year from input
     * @param month month from input
     * @param day day from input
     */
    public Map(ReadData readData,String year, String month, String day) {
        this.noFlyZones = new ArrayList<Polygon>();
        this.restaurants = new ArrayList<LngLat>();
        this.readData = readData;
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

        // read and setup no-fly-zones from web server
        List<Feature> noFlyZoneRaw = this.readData.readNoFlyZones();
        for (Feature value : noFlyZoneRaw) {
            Polygon zone = (Polygon) value.geometry();
            this.noFlyZones.add(zone);
        }
    }

//    private Order initializeOrder() {
//        Order order;
//
//        // fetch coordinates of restaurant location from server
//
//
//    }


}
