package uk.ac.ed.inf;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.json.JSONArray;
import org.json.JSONException;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import org.json.JSONObject;

public class DataReadWrite {
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
    public List<Feature> readRestaurants(){
        String urlString = "https://ilp-rest.azurewebsites.net/" + "restaurants.geojson";
        connect (urlString);

        List<Feature> restaurants = FeatureCollection.fromJson(response.body()).features();
        return restaurants;
    }


    public List<Feature> readLandmarks(){
        String urlString = "https://ilp-rest.azurewebsites.net/" + "all.geojson";
        connect (urlString);

        List<Feature> landmarks = FeatureCollection.fromJson(response.body()).features();
        return landmarks;
    }

    /**
     * Read the orders from server by a date
     * @param year year required
     * @param month month required
     * @param day day required
     * @return a hashmap of key = order number and value
     */
    public List<JSONObject> readOrders(String year, String month, String day) throws JSONException {
        String fullDate = year + "-" + month + "-" + day;
        String urlString = "https://ilp-rest.azurewebsites.net/" + "orders/" + fullDate;
        connect (urlString);

        JSONArray jsonArray = new JSONArray(response.body());

        List<JSONObject> orderObjects = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            orderObjects.add(jsonArray.getJSONObject(i));
        }

        return orderObjects;
    }

    // TODO: add ticksSinceStartOfCalculation into file
    /**
     * Write the flightpath json file containing all flight steps made by the drone in a given day
     * @param flightpaths the flightpath of the drone in a given day
     */
    public void writeFlightpathJson(List<Flightpath> flightpaths, String year,
                                    String month, String day) {
        //Creating a JSONArray object
        JSONArray jsonArray = new JSONArray();

        //Creating a JSONObject object
        try {
            for (Flightpath path : flightpaths) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("orderNo", path.orderNo);
                jsonObject.put("fromLongitude", path.fromLongitude);
                jsonObject.put("fromLatitude", path.fromLatitude);
                jsonObject.put("angle", path.angle);
                jsonObject.put("toLongitude", path.toLongitude);
                jsonObject.put("toLatitude", path.toLatitude);
                jsonArray.put(jsonObject);
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            FileWriter file = new FileWriter("flightpath-" + year + "-" + month + "-" + day + ".json");
            file.write(jsonArray.toString());
            file.close();
        } catch (IOException e) {
            System.err.println("Fatal error: Unable to generate flightpath json");
            e.printStackTrace();
        }
        System.out.println("JSON file created");
    }


    /**
     * Write the flightpath json file containing all flight steps made by the drone in a given day
     * @param orders the orders of the drone in a given day
     */
    public void writeDeliveriesJson(List<Order> orders, String year,
                                    String month, String day) {
        //Creating a JSONArray object
        JSONArray jsonArray = new JSONArray();

        //Creating a JSONObject object
        try {
            for (Order order : orders) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("orderNo", order.getOrderNo());
                jsonObject.put("outcome", order.getOrderOutcome());
                jsonObject.put("costInPence", order.getPriceTotalInPence());
                jsonArray.put(jsonObject);
            }

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        try {
            FileWriter file = new FileWriter("deliveries-" + year + "-" + month + "-" + day + ".json");
            file.write(jsonArray.toString());
            file.close();
        } catch (IOException e) {
            System.err.println("Fatal error: Unable to generate deliveries json");
            e.printStackTrace();
        }
        System.out.println("JSON file created");
    }
}
