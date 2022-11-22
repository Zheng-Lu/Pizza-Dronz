package uk.ac.ed.inf;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.mapbox.geojson.Feature;

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

    public static void main(String[] args) throws MalformedURLException{
        if (isDateValid(args[0]) && isValidURL(args[1])) {
            String[] date = args[0].split("-");
            String year = date[0];
            String month = date[1];
            String day = date[2];

            String baseAddress = args[1];

            String seed = args[2];

            // Initialize order data parsed from server
            DataParser dataParser = new DataParser(baseAddress, year, month, day);
            Drone drone = new Drone(dataParser);
            Restaurant[] restaurants = Restaurant.getRestaurantsFromRestServer(new URL(baseAddress));

            drone.initializeOrders(restaurants);
            List<Order> orders = drone.getAllOrders();

            // move the drone to deliver all orders
            for (int i = 0; i < orders.size(); i++){
                Order currOrder = orders.get(i);

                if (currOrder.getOrderOutcome().equals(ValidButNotDelivered.toString())) {

                    double minMoves = drone.getMinMoves(currOrder);

                    if (minMoves > drone.getRemainBattery()){
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
            List<Feature> landmarks = dataParser.getLandmarks();

            ResultsWriter resultsWriter = new ResultsWriter(year,month,day);

            // log the files
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
    }
}
