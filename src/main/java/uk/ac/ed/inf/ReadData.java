package uk.ac.ed.inf;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import com.google.gson.Gson;
import com.google.gson.*;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

public class ReadData {
    private static final HttpClient client = HttpClient.newHttpClient();
    private static HttpResponse<String> response;

    /**
     * Connect to the web server by a given url
     * @param urlString url of the web
     * @throws IOException if the web server could not be accessed
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
    public List<Feature> readNoFlyZones(){
        String urlString = "https://ilp-rest.azurewebsites.net/" + "no-fly-zones.geojson";
        connect (urlString);

        List<Feature> noFlyZones = FeatureCollection.fromJson(response.body()).features();
        return noFlyZones;
    }

    /**
     * Fetch no-fly zones from web server
     * @return all no-fly zones
     */
    public static List<Feature> readRestaurants(){
        String urlString = "https://ilp-rest.azurewebsites.net/" + "restaurants.geojson";
        connect (urlString);

        List<Feature> restaurants = FeatureCollection.fromJson(response.body()).features();
        return restaurants;
    }

    /**
     * Read the orders from server by a date
     * @param year year required
     * @param month month required
     * @param day day required
     * @return a hashmap of key = order number and value
     */
    public JSONArray readOrders(String year, String month, String day) throws JSONException {
        String fullDate = year + "-" + month + "-" + day;
        String urlString = "https://ilp-rest.azurewebsites.net/" + "orders/" + fullDate;
        connect (urlString);

        String jsonStr = response.body();
        JSONArray jsonArray = new JSONArray(jsonStr);

        return jsonArray;
    }

    public static void main(String[] args) throws JSONException {
//        String urlString = "https://ilp-rest.azurewebsites.net/" + "orders/" + "2023-01-01";
//        connect (urlString);
//        String jsonStr = response.body();
//        System.out.println(jsonStr);
//
//        JSONArray jsonArray = new JSONArray(response.body());
//        JSONObject jsonpObject = jsonArray.getJSONObject(0);
//
//        for (int i = 0; i < jsonArray.length(); i++) {
//            JSONObject object = jsonArray.getJSONObject(i);
//            System.out.println(object.get("orderNo"));
//        }
        System.out.println(readRestaurants());
    }

}
