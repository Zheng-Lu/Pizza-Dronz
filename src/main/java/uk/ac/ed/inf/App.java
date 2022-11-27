package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.mapbox.geojson.Feature;
import org.json.JSONException;

import static uk.ac.ed.inf.Order.OrderOutcome.ValidButNotDelivered;


/**
 * The main class for the processing order delivery, control the motion of drone,
 * and generating the result files
 */
public class App {
    /**
     * Check validity of input date
     * @param date The given date
     * Return true if the given date is in specific date format and exists, false otherwise
     */
    private static boolean isDateValid(String date)
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

    /**
     * Check validity of input url
     * @param url The given url
     * Returns true if url is valid, false otherwise
     */
    private static boolean isValidURL(String url)
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
     * Main method
     *
     * @param args
     * <ul>
     * <li>[0] - Given date</li>
     * <li>[1] - Base address of the web server</li>
     * <li>[2] - Seed for randomness</li>
     * </ul>
     */
//    public static void main(String[] args) {
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
//            // Initialize order data parsed from server
//            DataParser dataParser = new DataParser(baseAddress, year, month, day);
//            Drone drone = new Drone(dataParser);
//
//            // Initialize all the orders at given data
//            drone.initializeOrders(baseAddress);
//            List<Order> orders = drone.getAllOrders();
//
//            // deliver all orders
//            for (int i = 0; i < orders.size(); i++){
//                Order currOrder = orders.get(i);
//
//                if (currOrder.getOrderOutcome().equals(ValidButNotDelivered.toString())) {
//
//                    if (drone.haveNoEnoughBattery(currOrder)){
//                        System.out.println("DRONE: No enough battery to deliver the rest orders");
//                        break;
//                    }
//
//                    drone.droneMove(currOrder);
//
//                } else {
//                    System.out.printf("DRONE: Invalid Order {orderNo: %s} %n", currOrder.getOrderNo());
//                    System.out.println("-----> " + currOrder.getOrderOutcome() + "\n");
//                }
//            }
//            drone.getOrdersStatistics();
//
//            List<Flightpath> flightpath = drone.getFlightpaths();
//            List<Feature> landmarks = dataParser.getLandmarks();
//
//            ResultsWriter resultsWriter = new ResultsWriter(year,month,day);
//
//            // create the result files
//            resultsWriter.writeDroneGeojson(landmarks, flightpath);
//            resultsWriter.writeFlightpathJson(flightpath);
//            resultsWriter.writeDeliveriesJson(drone.getAllOrders());
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

        LocalDate startDate = LocalDate.parse("2023-01-01");
        LocalDate endDate   = LocalDate.parse("2023-05-31");
        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {

            Instant startTiming = Instant.now();

            if (isDateValid(String.valueOf(date)) && isValidURL("https://ilp-rest.azurewebsites.net")) {
                String year = String.valueOf(date).split("-")[0];
                String month = String.valueOf(date).split("-")[1];
                String day = String.valueOf(date).split("-")[2];

                String baseAddress = "https://ilp-rest.azurewebsites.net";

                String seed = "cabbage";

                // Initialize order data parsed from server
                DataParser dataParser = new DataParser(baseAddress, year, month, day);
                Drone drone = new Drone(dataParser);

                // Initialize all the orders at given data
                drone.initializeOrders(baseAddress);
                List<Order> orders = drone.getAllOrders();

                // deliver all orders
                for (int i = 0; i < orders.size(); i++){
                    Order currOrder = orders.get(i);

                    if (currOrder.getOrderOutcome().equals(ValidButNotDelivered.toString())) {

                        if (drone.haveNoEnoughBattery(currOrder)){
                            System.out.println("DRONE: No enough battery to deliver the rest orders");
                            break;
                        }

                        drone.droneMove(currOrder);

                    } else {
                        System.out.printf("DRONE: Invalid Order {orderNo: %s} %n", currOrder.getOrderNo());
                        System.out.println("-----> " + currOrder.getOrderOutcome() + "\n");
                    }
                }
                drone.getOrdersStatistics();

                List<Flightpath> flightpath = drone.getFlightpaths();
                List<Feature> landmarks = dataParser.getLandmarks();

                ResultsWriter resultsWriter = new ResultsWriter(year,month,day);

                // create the result files
                resultsWriter.writeDroneGeojson(landmarks, flightpath);
                resultsWriter.writeFlightpathJson(flightpath);
                resultsWriter.writeDeliveriesJson(drone.getAllOrders());

            } else {
                if (!isDateValid(args[0])){
                    throw new IllegalArgumentException("Invalid date input");
                } else if (!isValidURL(args[1])) {
                    throw new IllegalArgumentException("Invalid URL input");
                }
            }

            Instant endTiming = Instant.now();
            System.out.println(Duration.between(startTiming, endTiming));
        }

        Instant end = Instant.now();
        System.out.println(Duration.between(start, end) + "\n");
    }


}
