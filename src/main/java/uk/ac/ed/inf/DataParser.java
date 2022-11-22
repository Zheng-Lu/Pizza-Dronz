package uk.ac.ed.inf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.mapbox.geojson.*;
import org.json.JSONArray;
//import org.json.JSONException;
import org.json.JSONObject;

public class DataParser {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static HttpResponse<String> response;
    private List<Polygon> noFlyZones;
    private List<LngLat> restaurantLocs;
    private List<JSONObject> rawOrderData;
    private List<Feature> landmarks;

    /** getters */
    public List<Polygon> getNoFlyZones(){return this.noFlyZones;}
    public List<LngLat> getRestaurantLocs() {return this.restaurantLocs;}
    public List<Feature> getLandmarks() {return this.landmarks;}
    public int getOrderLength(){return this.rawOrderData.size();}
    public List<JSONObject> getRawOrderData() {return this.rawOrderData;}

    public DataParser(String baseAddress, String year, String month, String day) {
        this.noFlyZones = readNoFlyZones(baseAddress);
        this.restaurantLocs = readRestaurants(baseAddress);
        this.landmarks = readLandmarks(baseAddress);
        try {
            this.rawOrderData = readOrders(baseAddress, year, month, day);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Connect to the web server by a given url
     * @param urlString url of the web
     */
    private static void connect(String urlString){

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(urlString))
                    .build();
            response =
                    client.send(request, BodyHandlers.ofString());

            // catch errors
            if (response.statusCode() != 200){
                System.err.println("Fatal error: Unable to connect to " + urlString + ".");
                System.exit(1);
            }
        } catch (IOException | InterruptedException e){
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Fetch no-fly zones from web server
     * @return all no-fly zones
     */
    public List<Polygon> readNoFlyZones(String baseAddress){
        String urlString = baseAddress + "/no-fly-zones.geojson";
        connect (urlString);
        List<Feature> noFlyZoneFeatures = FeatureCollection.fromJson(response.body()).features();
        List<Polygon> noFlyZones = new ArrayList<>();

        if (noFlyZoneFeatures != null) {
            for (Feature feature : noFlyZoneFeatures) {
                Polygon zone = (Polygon) feature.geometry();
                noFlyZones.add(zone);
            }
        }

        return noFlyZones;
    }

    /**
     * Fetch all locations of restaurants from web server
     * @return all locations of restaurants
     */
    public List<LngLat> readRestaurants(String baseAddress){
        String urlString = baseAddress + "/restaurants.geojson";
        connect (urlString);
        List<Feature> restaurantFeatures = FeatureCollection.fromJson(response.body()).features();
        List<LngLat> restaurantLocs = new ArrayList<>();
        if (restaurantFeatures != null) {
            for (Feature feature : restaurantFeatures){
                Point p = (Point) feature.geometry();
                LngLat loc = null;
                if (p != null) {
                    loc = new LngLat(p.longitude(), p.latitude());
                }
                restaurantLocs.add(loc);
            }
        }
        return restaurantLocs;
    }


    public List<Feature> readLandmarks(String baseAddress){
        String urlString = baseAddress + "/all.geojson";
        connect (urlString);

        return FeatureCollection.fromJson(response.body()).features();
    }

    /**
     * Read the orders from server by a date
     * @param year year required
     * @param month month required
     * @param day day required
     * @return a hashmap of key = order number and value
     */
    public List<JSONObject> readOrders(String baseAddress, String year, String month, String day){
        String fullDate = year + "-" + month + "-" + day;
        String urlString = baseAddress + "/orders/" + fullDate;
        connect (urlString);

        JSONArray jsonArray = new JSONArray(response.body());

        List<JSONObject> orderObjects = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            orderObjects.add(jsonArray.getJSONObject(i));
        }

        return orderObjects;
    }
}
