package uk.ac.ed.inf;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.time.Clock;

/**
 * The drone class contains methods about navigation, order initialization that a drone need for delivery
 */
public class Drone {
    public static final int BATTERY = 2000;
    public static final double APPLETON_LONGITUDE = -3.186874;
    public static final double APPLETON_LATITUDE = 55.944494;
    public static final LngLat APPLETON_TOWER = new LngLat(APPLETON_LONGITUDE, APPLETON_LATITUDE);
    private DataParser dataParser;
    public boolean prepareToReturn;
    private final LngLat startPos;
    private LngLat dronePos;
    private LngLat currGoal;
    private int remainBattery;
    private int ticksSinceStartOfCalculation;
    private List<Flightpath> flightpaths;
    private List<Order> orderDelivered;
    private List<Order> allOrders;

    /* getters  */

    /**@return the list of the orders that have already been delivered by drone */
    public List<Order> getOrderDelivered(){
        return this.orderDelivered;
    }

    /**@return the list of all orders at a given days */
    public List<Order> getAllOrders(){
        return this.allOrders;
    }

    /**@return the list of flightpaths that moved by drone */
    public List<Flightpath> getFlightpaths(){
        return this.flightpaths;
    }

    /**@return the amount of remain battery that drone currently has */
    public int getRemainBattery(){
        return this.remainBattery;
    }


    /**
     * Create the drone
     * @param dataParser the dataParser parse the required data by drone for order delivery
     */
    public Drone(DataParser dataParser) {
        this.dataParser = dataParser;
        this.startPos = APPLETON_TOWER;
        this.dronePos = APPLETON_TOWER;
        this.currGoal = null;
        this.remainBattery = BATTERY;
        this.prepareToReturn = false;
        this.flightpaths = new ArrayList<>();
        this.orderDelivered = new ArrayList<>();
        this.allOrders = new ArrayList<>();
        this.ticksSinceStartOfCalculation = 0;
    }

    /**
     * Initialize all daily orders for drone before flying
     * @param baseAddress list of participated restaurants retrieved from the web server
     */
    public void initializeOrders(String baseAddress) {
        List<JSONObject> rawOrderData = this.dataParser.getRawOrderData();
        Restaurant[] restaurants;
        try {
            restaurants = Restaurant.getRestaurantsFromRestServer(new URL(baseAddress));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        for (JSONObject rawOrder: rawOrderData) {
            JSONArray numItems = (JSONArray) rawOrder.get("orderItems");
            String[] orderItems = new String[numItems.length()];

            for (int i = 0; i < numItems.length(); i++) {
                orderItems[i] = numItems.get(i).toString();
            }

            Restaurant restaurant = Restaurant.getRestaurantByItem(restaurants, orderItems[0]);
            LngLat restaurantLoc = null;
            if (restaurant != null) {
                restaurantLoc = new LngLat(restaurant.getLongitude(), restaurant.getLatitude());
            }

            String orderNo = rawOrder.get("orderNo").toString();
            String orderDate = rawOrder.get("orderDate").toString();
            String creditCardNumber = rawOrder.get("creditCardNumber").toString();
            String creditCardExpiry = rawOrder.get("creditCardExpiry").toString();
            String cvv = rawOrder.get("cvv").toString();
            int priceTotalInPence = (int) rawOrder.get("priceTotalInPence");

            String orderOutcome;
            try {
                orderOutcome = Order.getOrderOutcome(restaurants, orderItems, creditCardNumber, creditCardExpiry,
                        cvv, orderDate, priceTotalInPence);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            this.allOrders.add(new Order(orderNo, orderDate, restaurantLoc, creditCardNumber, creditCardExpiry,
                    cvv, orderItems, priceTotalInPence, orderOutcome));
        }

        // Sort orders according to distance from Appleton Tower in ascending way
        this.allOrders.sort(((o1, o2) -> Double.compare(o2.getDistance(), o1.getDistance())));
        Collections.reverse(this.allOrders);
    }

    /**
     * Get delivery statistics displayed in terminal
     */
    public void getOrdersStatistics() {
        int validNumOrders = 0;

        for (Order order : this.allOrders){
            if (order.getOrderOutcome().equals(Order.OrderOutcome.ValidButNotDelivered.toString()) ||
                    order.getOrderOutcome().equals(Order.OrderOutcome.Delivered.toString())){
                validNumOrders += 1;
            }
        }
        System.out.println("<-------------Result------------->");
        System.out.println("Number of invalid orders: " + (getAllOrders().size() - validNumOrders));
        System.out.println("Orders delivered: " + getOrderDelivered().size() + "/" + validNumOrders);
        System.out.println("Remaining battery after delivery: " + getRemainBattery() + "\n");
    }


    /**
     * Check whether the drone have enough battery to deliver the given order
     * @param order given order
     * @return true if the drone have no enough battery to deliver, false otherwise
     */
    public boolean haveNoEnoughBattery(Order order){
        LngLat restaurantLoc = order.getRestaurantLoc();
        double minDist = this.startPos.distanceTo(restaurantLoc)*2;
        return minDist/LngLat.LENGTH_OF_MOVE > getRemainBattery();
    }


    /**
     * Method that actually move the drone to deliver given order
     * @param order given order
     */
    public void droneMove(Order order){
        Clock clock = Clock.systemDefaultZone();
        long start;
        long end;

        System.out.printf("DRONE: Currently delivering order {orderNo: %s} %n", order.getOrderNo());
        int lastTimeRemainBattery = this.remainBattery;

        this.currGoal = order.getRestaurantLoc();

        List<Flightpath> backtrack_path = new ArrayList<>();

        // move the drone
        while (this.remainBattery > 0){
            if (!this.prepareToReturn){
                // move a step and record it

                start = clock.millis();

                LngLat newPos = this.dronePos.move(this.dataParser, this.currGoal);

                end = clock.millis();

                this.ticksSinceStartOfCalculation += end - start + 1;
                Flightpath thisMove = new Flightpath(order.getOrderNo(), this.dronePos.getLng(),
                        this.dronePos.getLat(), this.dronePos.getAngle(), newPos.getLng(), newPos.getLat(),
                        this.ticksSinceStartOfCalculation);

                this.flightpaths.add(thisMove);
                backtrack_path.add(thisMove);

                this.dronePos.setLngLat(newPos);
                this.remainBattery -= 1;


                // hover if the drone is close to its target, while recording this step
                if (this.dronePos.closeTo(this.currGoal)) {
                    start = clock.millis();

                    this.dronePos = this.dronePos.nextPosition(LngLat.Direction.Null);

                    end = clock.millis();
                    this.ticksSinceStartOfCalculation += end - start + 1;

                    Flightpath hoverMove = new Flightpath(order.getOrderNo(), this.dronePos.getLng(),
                            this.dronePos.getLat(), LngLat.Direction.Null.getValue(), this.dronePos.getLng(), this.dronePos.getLat(),
                            this.ticksSinceStartOfCalculation);

                    this.flightpaths.add(hoverMove);

                    this.remainBattery -= 1;

                    this.prepareToReturn = true;
                    this.currGoal = this.startPos;



                }
            } else {
                Collections.reverse(backtrack_path);

                for (Flightpath thisMove : backtrack_path) {

                    this.ticksSinceStartOfCalculation += 1;

                    Flightpath returnMove = new Flightpath(thisMove.orderNo, thisMove.fromLongitude,
                            thisMove.fromLatitude, thisMove.angle, thisMove.toLongitude, thisMove.toLatitude,
                            this.ticksSinceStartOfCalculation);


                    this.flightpaths.add(returnMove);

                    this.dronePos.setLngLat(new LngLat(thisMove.fromLongitude, thisMove.fromLatitude));

                    this.remainBattery -= 1;
                }

                // hover if the drone is close to its target, while recording this step
                if (this.dronePos.closeTo(this.currGoal)) {

                    orderDelivered.add(order);
                    order.markDelivered();
                    System.out.printf("-----> Order {orderNo: %s} delivered %n", order.getOrderNo());

                    start = clock.millis();
                    this.dronePos = this.dronePos.nextPosition(LngLat.Direction.Null);

                    end = clock.millis();
                    this.ticksSinceStartOfCalculation += end - start + 1;

                    Flightpath hoverMove = new Flightpath(order.getOrderNo(), this.dronePos.getLng(),
                            this.dronePos.getLat(), -999, this.dronePos.getLng(), this.dronePos.getLat(),
                            this.ticksSinceStartOfCalculation);

                    this.flightpaths.add(hoverMove);

                    this.remainBattery -= 1;
                    this.prepareToReturn = false;

                }

                System.out.println("-----> Took " + (lastTimeRemainBattery - this.remainBattery) + " Moves \n");

                break;
            }
        }
    }
}
