package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.io.FileWriter;
import java.io.IOException;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.LineString;
import org.json.JSONException;
import com.mapbox.geojson.Geometry;

/**
 * Hello world!
 *
 */
public class App
{
    public static boolean isDateValid(String date)
    {
        try {
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setLenient(false);
            df.parse(date);

            String[] d = date.split("-");
            int year = Integer.parseInt(d[0]);
            if ( year <= 1975  || d[1].length() != 2 || d[2].length() != 2) {
                return false;
            }

            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    /* Returns true if url is valid */
    public static boolean isValidURL(String url)
    {
        /* Try creating a valid URL */
        try {
            new URL(url).toURI();
            return true;
        }

        // If there was an Exception
        // while creating URL object
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Create the flightpath geojson file
     * @param allFlightpath all flightpath of the day
     * @param year required year
     * @param month required month
     * @param day required day
     * @throws IOException when failed to create Geojson file
     */
    private static void createGeojsonFile(List<Flightpath> allFlightpath, String year,
                                          String month, String day) {
        // convert flightpath objects to linestring
        List<Point> flightpathPoints = new ArrayList<>();
        flightpathPoints.add(Point.fromLngLat(allFlightpath.get(0).fromLongitude, allFlightpath.get(0).fromLatitude));

        for (Flightpath fp : allFlightpath){
            flightpathPoints.add(Point.fromLngLat(fp.toLongitude, fp.toLatitude));
        }
        LineString flightpathLineString = LineString.fromLngLats(flightpathPoints);
        Feature flightpathFeature = Feature.fromGeometry( (Geometry) flightpathLineString );

        // convert flightpath to one feature in a feature collection
        ArrayList<Feature> flightpathList = new ArrayList<Feature>();
        flightpathList.add(flightpathFeature);

        DataReadWrite dataReadWrite = new DataReadWrite();
        ArrayList<Feature> landmarks = (ArrayList<Feature>) dataReadWrite.readLandmarks();

        flightpathList.addAll(landmarks);

        FeatureCollection flightpathFC = FeatureCollection.fromFeatures(flightpathList);

        String flightpathJson = flightpathFC.toJson();

        // write the geojson file
        try {
            FileWriter myWriter = new FileWriter(
                    "drone-" + year + "-" + month + "-" + day + ".geojson");
            myWriter.write(flightpathJson);
            myWriter.close();
            System.out.println("Drone Geojson created");
        } catch (IOException e) {
            System.err.println("Fatal error: Unable to generate Drone Geojson");
            e.printStackTrace();
        }
    }


//    public static void main(String[] args) throws MalformedURLException, JSONException, ParseException {
//        if (isDateValid(args[0]) && isValidURL(args[1])) {
//            String[] date = args[0].split("-");
//            String year = date[0];
//            String month = date[1];
//            String day = date[2];
//
//            String baseAddress = args[1];
//
//            String seed = args[2];
//
//            // Initialize map, drone and restaurants
//            DataReader readData = new DataReader();
//            Map map = new Map(readData, year, month, day);
//            Drone drone = new Drone(map, map.getStartPos());
//            Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(new URL(baseAddress));
//
//            List<Order> orders = Order.initializeOrders(restaurants, year, month, day);
//
//            // move the drone to deliver all orders
//            for (int i = 0; i < orders.size(); i++){
//
//                if (drone.getRemainBattery() <= 0 ){
//                    System.out.println("DRONE: out of battery");
//                    break;
//                }
//                if (drone.returned){
//                    break;
//                }
//                drone.moveDrone(orders.get(i));
//            }
//            System.out.println("Remaining battery after delivery: " + drone.getRemainBattery());
//
//            List<Order> delivered = drone.getOrderDelivered();
//            List<Flightpath> flightpath = drone.getFlightpaths();
//
//            // log the files
//            createGeojson(flightpath, year, month, day);
//
//
//        } else {
//            if (!isDateValid(args[0])){
//                throw new IllegalArgumentException("Invalid date input");
//            } else if (!isValidURL(args[1])) {
//                throw new IllegalArgumentException("Invalid URL input");
//            }
//        }
//    }


    public static void main(String[] args) throws MalformedURLException, JSONException, ParseException {
        Instant start = Instant.now();

        String arg0 = "2023-05-30";
        String arg1 = "https://ilp-rest.azurewebsites.net";
        String arg2 = "cabbage";

        if (isDateValid(arg0) && isValidURL(arg1)) {
            String[] date = arg0.split("-");
            String year = date[0];
            String month = date[1];
            String day = date[2];

            String baseAddress = arg1;

            String seed = arg2;

            // Initialize map, drone and restaurants
            DataReadWrite readData = new DataReadWrite();
            Map map = new Map(readData, year, month, day);
            Drone drone = new Drone(map, map.getStartPos());
            Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(new URL(baseAddress));

            List<Order> orders = Drone.initializeOrders(restaurants, year, month, day);

            // move the drone to deliver all orders
            for (int i = 0; i < orders.size(); i++){
                Order currentOrder = map.popClosestOrder();

                if (drone.getRemainBattery() <= 0 ){
                    System.out.println("DRONE: out of battery");
                    break;
                }
                if (drone.returned){
                    break;
                }

                if (currentOrder.getOrderOutcome().equals(Order.OrderOutcome.ValidButNotDelivered.toString())) {
                    System.out.printf("DRONE: Currently delivering order {orderNo: %s} %n", currentOrder.getOrderNo());
                    drone.moveDrone(currentOrder);
                }
            }
            System.out.println("Remaining battery after delivery: " + drone.getRemainBattery());

            List<Order> delivered = drone.getOrderDelivered();
            List<Flightpath> flightpath = drone.getFlightpaths();

            DataReadWrite dataReadWrite = new DataReadWrite();

            // log the files
            createGeojsonFile(flightpath, year, month, day);
            dataReadWrite.writeFlightpathJson(flightpath, year, month, day);
            dataReadWrite.writeDeliveriesJson(delivered, year, month, day);


        } else {
            if (!isDateValid(args[0])){
                throw new IllegalArgumentException("Invalid date input");
            } else if (!isValidURL(args[1])) {
                throw new IllegalArgumentException("Invalid URL input");
            }
        }

        Instant end = Instant.now();
        System.out.println(Duration.between(start, end));
    }
}
