package uk.ac.ed.inf;

import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.geojson.Feature;
import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Map {

    /** constants */
    public static final double APPLETON_LONGITUDE = -3.186874;
    public static final double APPLETON_LATITUDE = 55.944494;

    public static final LngLat APPLETON_TOWER = new LngLat(APPLETON_LONGITUDE, APPLETON_LATITUDE);

    private final String year;
    private final String month;
    private final String day;

    private final DataReadWrite dataReadWrite;

    private final LngLat startPos;
    private final List<Polygon> noFlyZones;
    private List<LngLat> restaurantLocs;
    private List<Order> orders;

    /** getters */
    public List<Polygon> getNoFlyZones(){return this.noFlyZones;}

    public LngLat getStartPos(){return this.startPos;
    }

    public List<LngLat> getRestaurantLocs() {return restaurantLocs;}
    public int getOrderLength(){return this.orders.size();}


    /**
     * Create the map for a day
     * @param year year from input
     * @param month month from input
     * @param day day from input
     */
    public Map(DataReadWrite dataReadWrite, String year, String month, String day) {
        this.noFlyZones = new ArrayList<Polygon>();
        this.restaurantLocs = new ArrayList<LngLat>();
        this.dataReadWrite = dataReadWrite;
        this.year = year;
        this.month = month;
        this.day = day;
        this.startPos = new LngLat(APPLETON_LONGITUDE, APPLETON_LATITUDE);
        this.orders = new ArrayList<Order>();

        initializeMap();
    }

    /**
     * Set up the no-fly-zones, landmarks, orders of the map
     */
    private void initializeMap() {

        // read and setup no-fly-zones from web server
        List<Feature> noFlyZoneFeatures = this.dataReadWrite.readNoFlyZones();
        for (Feature feature : noFlyZoneFeatures) {
            Polygon zone = (Polygon) feature.geometry();
            this.noFlyZones.add(zone);
        }

        // read and setup restaurant locations from web server
        List<Feature> restaurantFeatures = this.dataReadWrite.readRestaurants();
        for (Feature feature : restaurantFeatures){
            Point p = (Point) feature.geometry();
            LngLat loc = new LngLat(p.longitude(), p.latitude());
            this.restaurantLocs.add(loc);
        }

        // read and setup orders


        try {
            String baseAddress = "https://ilp-rest.azurewebsites.net/";
            Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(new URL(baseAddress));

            this.orders = Drone.initializeOrders(restaurants, this.year, this.month, this.day);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Pop the closest order from a copy list of today's order
     * @return the closest order from Appleton Tower
     */
    public Order popClosestOrder(){
        if (this.orders.size() == 0){
            return null;
        }

        Order closestOrder = this.orders.get(0);
        double minDist = Double.POSITIVE_INFINITY;
        for (Order order : this.orders){
            if (order.getDistance() < minDist){
                minDist = order.getDistance();
                closestOrder = order;
            }
        }
        // pop it from the list
        this.orders.remove(closestOrder);
        return closestOrder;

    }

}
