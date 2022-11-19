package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import org.json.JSONException;

import static uk.ac.ed.inf.Order.OrderOutcome.ValidButNotDelivered;


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


//    public static void main(String[] args) throws MalformedURLException, JSONException, ParseException {
//        Instant start = Instant.now();
//
//        LocalDate startDate = LocalDate.parse("2023-01-01");
//        LocalDate endDate   = LocalDate.parse("2023-06-01");
//        for (LocalDate date = startDate; date.isBefore(endDate); date = date.plusDays(1)) {
//
//            Instant startTiming = Instant.now();
//
//            String arg0 = String.valueOf(date);
//            String arg1 = "https://ilp-rest.azurewebsites.net";
//            String arg2 = "cabbage";
//
//            if (isDateValid(arg0) && isValidURL(arg1)) {
//                String[] inputDate = arg0.split("-");
//                String year = inputDate[0];
//                String month = inputDate[1];
//                String day = inputDate[2];
//
//                String baseAddress = arg1;
//
//                String seed = arg2;
//
//                // Initialize map, drone and restaurants
//                DataReadWrite readData = new DataReadWrite();
//                MapInitialization mapInitialization = new MapInitialization(readData, year, month, day);
//                Drone drone = new Drone(mapInitialization, mapInitialization.getStartPos());
//                Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(new URL(baseAddress));
//
//                drone.initializeOrders(restaurants, year, month, day);
//
//                // move the drone to deliver all orders
//                for (int i = 0; i < drone.getAllOrders().size(); i++){
//                    Order currOrder = drone.getAllOrders().get(i);
//
//                    if (currOrder.getOrderOutcome().equals(ValidButNotDelivered.toString())) {
//
//                        double shortest_dist = drone.getStartPos().distanceTo(currOrder.getRestaurantLoc()) *2 / LngLat.LENGTH_OF_MOVE;
//
//                        if (shortest_dist > drone.getRemainBattery()){
//                            System.out.println("DRONE: No enough battery to deliver the rest orders");
//                            break;
//                        }
//
//                        drone.droneMove(currOrder);
//
//                    } else {
//                        System.out.printf("DRONE: Invalid Order {orderNo: %s} %n", currOrder.getOrderNo());
//                        System.out.println("-----> " + currOrder.getOrderOutcome() + "\n");
//                    }
//                }
//                System.out.println("<-------------Result------------->");
//                drone.getOrdersStatistics();
//
//                List<Flightpath> flightpath = drone.getFlightpaths();
//
//                DataReadWrite dataReadWrite = new DataReadWrite();
//
//                // log the files
//                dataReadWrite.writeDroneGeojson(flightpath, year, month, day);
//                dataReadWrite.writeFlightpathJson(flightpath, year, month, day);
//                dataReadWrite.writeDeliveriesJson(drone.getAllOrders(), year, month, day);
//
//            } else {
//                if (!isDateValid(args[0])){
//                    throw new IllegalArgumentException("Invalid date input");
//                } else if (!isValidURL(args[1])) {
//                    throw new IllegalArgumentException("Invalid URL input");
//                }
//            }
//
//            Instant endTiming = Instant.now();
//            System.out.println(Duration.between(startTiming, endTiming));
//        }
//
//        Instant end = Instant.now();
//        System.out.println(Duration.between(start, end) + "\n");
//    }


    public static void main(String[] args) throws MalformedURLException, JSONException, ParseException {
//        System.out.println(Integer.parseInt(" 96147674042092"));

        Instant start = Instant.now();

        String arg0 = "2023-01-22";
        String arg1 = "https://ilp-rest.azurewebsites.net";
        String arg2 = "cabbage";

        if (isDateValid(arg0) && isValidURL(arg1)) {
            String[] inputDate = arg0.split("-");
            String year = inputDate[0];
            String month = inputDate[1];
            String day = inputDate[2];

            String baseAddress = arg1;

            String seed = arg2;

            // Initialize map, drone and restaurants
            DataReadWrite readData = new DataReadWrite();
            MapInitialization mapInitialization = new MapInitialization(readData, year, month, day);
            Drone drone = new Drone(mapInitialization, mapInitialization.getStartPos());
            Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(new URL(baseAddress));

            drone.initializeOrders(restaurants, year, month, day);

            // move the drone to deliver all orders
            for (int i = 0; i < drone.getAllOrders().size(); i++){
                Order currOrder = drone.getAllOrders().get(i);

                if (currOrder.getOrderOutcome().equals(ValidButNotDelivered.toString())) {

                    double shortest_dist = drone.getStartPos().distanceTo(currOrder.getRestaurantLoc()) *2 / LngLat.LENGTH_OF_MOVE;

                    if (shortest_dist > drone.getRemainBattery()){
                        System.out.println("DRONE: No enough battery to deliver the rest orders");
                        break;
                    }

                    drone.droneMove(currOrder);

                } else {
                    System.out.printf("DRONE: Invalid Order {orderNo: %s} %n", currOrder.getOrderNo());
                    System.out.println("-----> " + currOrder.getOrderOutcome() + "\n");
                }
            }
            System.out.println("<-------------Result------------->");
            drone.getOrdersStatistics();

            List<Flightpath> flightpath = drone.getFlightpaths();

            DataReadWrite dataReadWrite = new DataReadWrite();

            // log the files
            dataReadWrite.writeDroneGeojson(flightpath, year, month, day);
            dataReadWrite.writeFlightpathJson(flightpath, year, month, day);
            dataReadWrite.writeDeliveriesJson(drone.getAllOrders(), year, month, day);

        } else {
            if (!isDateValid(args[0])){
                throw new IllegalArgumentException("Invalid date input");
            } else if (!isValidURL(args[1])) {
                throw new IllegalArgumentException("Invalid URL input");
            }
        }

        Instant end = Instant.now();
        System.out.println(Duration.between(start, end) + "\n");
    }
}
