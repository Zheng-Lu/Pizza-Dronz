package uk.ac.ed.inf;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The class to write the result produced by drone into files:
 * flightpath-YYYY-MM-DD.json, deliveries-YYYY-MM-DD.json and drone-YYYY-MM-DD.geojson.
 */
public class ResultsWriter {
    private final String RESULT_FOLDER = "resultfiles" + File.separator;
    private final String year;
    private final String month;
    private final String day;

    /**
     * Constructor of the ResultsWriter class,
     * which requires input date to
     * @param year Given year
     * @param month Given month
     * @param day Given day
     */
    public ResultsWriter(String year, String month, String day) {
        this.year = year;
        this.month = month;
        this.day = day;
    }

    /**
     * Write the flightpath json file containing all flight steps made by the drone in a given day
     * @param flightpaths the flightpath of the drone in a given day
     */
    public void writeFlightpathJson(List<Flightpath> flightpaths) {
        String fileName = "flightpath-" + this.year + "-" + this.month + "-" + this.day + ".json";

        //Creating a JSONArray object
        JsonArray jsonArray = new JsonArray();

        //Creating a JSONObject object
        for (Flightpath path : flightpaths) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("orderNo", path.orderNo);
            jsonObject.addProperty("fromLongitude", path.fromLongitude);
            jsonObject.addProperty("fromLatitude", path.fromLatitude);
            jsonObject.addProperty("angle", path.angle);
            jsonObject.addProperty("toLongitude", path.toLongitude);
            jsonObject.addProperty("toLatitude", path.toLatitude);
            jsonObject.addProperty("ticksSinceStartOfCalculation", path.ticksSinceStartOfCalculation);
            jsonArray.add(jsonObject);
        }

        try {
            FileWriter file = new FileWriter(RESULT_FOLDER + fileName);
            file.write(jsonArray.toString());
            file.close();
        } catch (IOException e) {
            System.err.println("Fatal error: Unable to generate flightpath json");
            e.printStackTrace();
        }
        System.out.println(fileName + " created");
    }


    /**
     * Write the flightpath json file containing all flight steps made by the drone in a given day
     * @param orders the orders of the drone in a given day
     */
    public void writeDeliveriesJson(List<Order> orders) {
        String fileName = "deliveries-" + this.year + "-" + this.month + "-" + this.day + ".json";

        //Creating a JSONArray object
        JsonArray jsonArray = new JsonArray();

        //Creating a JSONObject object
        for (Order order : orders) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("orderNo", order.getOrderNo());
            jsonObject.addProperty("outcome", order.getOrderOutcome());
            jsonObject.addProperty("costInPence", order.getPriceTotalInPence());
            jsonArray.add(jsonObject);
        }

        try {
            FileWriter file = new FileWriter(RESULT_FOLDER + fileName);
            file.write(jsonArray.toString());
            file.close();
        } catch (IOException e) {
            System.err.println("Fatal error: Unable to generate deliveries json");
            e.printStackTrace();
        }
        System.out.println(fileName + " created");
    }


    /**
     * Create the geojson file to render the flight path of drone on GeoJson map
     * @param allFlightpath all flightpath of the day
     * @param landmarks all landmarks retrieved from all.geojson, such as
     *                  blue and yellow markers(restaurants and appleton tower),
     *                  semi-transparent red polygons(no-fly zones),
     *                  outer grey rectangle(central area)
     *
     */
    public void writeDroneGeojson(List<Feature> landmarks, List<Flightpath> allFlightpath) {
        String flightpathJson;
        String fileName = "drone-" + this.year + "-" + this.month + "-" + this.day + ".geojson";

        if (allFlightpath.size() == 0) {
            flightpathJson = "";
        } else {
            // convert flightpath objects to linestring
            List<Point> flightpathPoints = new ArrayList<>();
            flightpathPoints.add(Point.fromLngLat(allFlightpath.get(0).fromLongitude, allFlightpath.get(0).fromLatitude));

            for (Flightpath fp : allFlightpath){
                flightpathPoints.add(Point.fromLngLat(fp.toLongitude, fp.toLatitude));
            }
            LineString flightpathLineString = LineString.fromLngLats(flightpathPoints);
            Feature flightpathFeature = Feature.fromGeometry(flightpathLineString);

            // convert flightpath to one feature in a feature collection
            List<Feature> flightpathList = new ArrayList<>();
            flightpathList.add(flightpathFeature);
            flightpathList.addAll(landmarks);

            FeatureCollection flightpathFC = FeatureCollection.fromFeatures(flightpathList);

            flightpathJson = flightpathFC.toJson();
        }

        // write the geojson file
        try {
            FileWriter file = new FileWriter(RESULT_FOLDER + fileName);
            file.write(flightpathJson);
            file.close();
        } catch (IOException e) {
            System.err.println("Fatal error: Unable to generate Drone Geojson");
            e.printStackTrace();
        }
        System.out.println(fileName + " created");
    }
}
