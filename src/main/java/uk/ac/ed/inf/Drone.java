package uk.ac.ed.inf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class Drone {
    public static final int BATTERY = 2000;
    public static final double APPLETON_LONGITUDE = -3.186874;
    public static final double APPLETON_LATITUDE = 55.944494;
    public static final LngLat APPLETON_TOWER = new LngLat(APPLETON_LONGITUDE, APPLETON_LATITUDE);
    public boolean returned;
    private final LngLat startPos;
    private LngLat dronePos;
    private int remainBattery;
    private int numPizzaCarried;
    private List<Flightpath> flightpaths;
    private final Map map;
    private List<Order> orderDelivered;

    /** getters  */
    public List<Order> getOrderDelivered(){
        return this.orderDelivered;
    }

    public List<Flightpath> getFlightpaths(){
        return this.flightpaths;
    }

    public int getRemainBattery(){
        return this.remainBattery;
    }



    /**
     * Create the drone
     * @param startPos starting position of the drone, usually Appleton Tower
     */
    public Drone(Map map, LngLat startPos) {
        this.map = map;
        this.startPos = startPos;
        this.dronePos = new LngLat (startPos.lng, startPos.lat);
        this.remainBattery = BATTERY;
        this.returned = false;
        this.flightpaths = new ArrayList<Flightpath>();
        this.orderDelivered = new ArrayList<Order>();
    }

    public static List<Order> initializeOrders(Restaurant[] restaurants, String year, String month, String day) throws JSONException, ParseException {
        DataReadWrite dataReadWrite = new DataReadWrite();
        List<JSONObject> rawOrders = dataReadWrite.readOrders(year,month,day);
        List<Order> orders = new ArrayList<>();

        for (JSONObject rawOrder: rawOrders) {
            JSONArray numItems = (JSONArray) rawOrder.get("orderItems");
            String[] orderItems = new String[numItems.length()];

            for (int i = 0; i < numItems.length(); i++) {
                orderItems[i] = numItems.get(i).toString();
            }

            Restaurant restaurant = Restaurant.getRestaurantFromItem(restaurants, orderItems[0]);
            LngLat restaurantLoc = new LngLat(restaurant.getLongitude(), restaurant.getLatitude());

            String orderNo = rawOrder.get("orderNo").toString();
            String orderDate = rawOrder.get("orderDate").toString();
            String creditCardNumber = rawOrder.get("creditCardNumber").toString();
            String creditCardExpiry = rawOrder.get("creditCardExpiry").toString();
            String cvv = rawOrder.get("cvv").toString();
            int priceTotalInPence = (int) rawOrder.get("priceTotalInPence");

            String orderOutcome = Order.getOrderOutcome(restaurants, orderItems, creditCardNumber, creditCardExpiry,
                    cvv, orderDate, priceTotalInPence);

            orders.add(new Order(orderNo, orderDate, restaurantLoc, creditCardNumber, creditCardExpiry,
                    cvv, orderItems, priceTotalInPence, orderOutcome));
        }

        return orders;
    }

    /**
     * Calculate the path and move the drone to deliver the current order
     */
    public void moveDrone(Order order){
        ArrayList<LngLat> routeOfOrder = planRoute(order);
        double heuristic = 0.0;
        boolean prepareToReturn = false;

        // calculate heuristic distance: straight line distance connecting the route
        for (int i = 0; i < routeOfOrder.size() - 1; i++){
            heuristic += routeOfOrder.get(i).distanceTo(routeOfOrder.get(i+1));
        }

        // return to starting position if the drone does not have enough battery left to deliver the order
        // according to heuristic
        if (heuristic / LngLat.LENGTH_OF_MOVE > this.remainBattery){
            routeOfOrder.clear();
            addLocToRoute(this.dronePos, this.startPos, routeOfOrder);
            prepareToReturn = true;
            System.out.println("DRONE: not enough battery, give up current order and return");

            // set the drone to return if the order is the final order today
        } else if (this.orderDelivered.size() + 1 == this.map.getOrderLength()){
            addLocToRoute(routeOfOrder.get(routeOfOrder.size() - 1), this.startPos, routeOfOrder);
            prepareToReturn = true;
        }

        // move the drone
        moveSteps(order, routeOfOrder, prepareToReturn);
    }


    /**
     * Helper function to actually move the drone
     * @param order current order
     * @param routeOfOrder the planned route to deliver the current order
     * @param prepareToReturn if the drone have finished today's order and ready to return
     */
    private void moveSteps(Order order, ArrayList<LngLat> routeOfOrder, boolean prepareToReturn){
        while (this.remainBattery > 0){
            // move a step and record it
            LngLat newPos = this.dronePos.move(this.map, routeOfOrder.get(0));

            Flightpath thisMove = new Flightpath(order.getOrderNo(), this.dronePos.getLng(),
                    this.dronePos.getLat(), this.dronePos.getAngle(), newPos.getLng(), newPos.getLat());

            this.flightpaths.add(thisMove);

            this.dronePos.setLngLat(newPos);
            this.remainBattery -= 1;

            // hover if the drone is close to its target, while recording this step
            if (this.dronePos.closeTo(routeOfOrder.get(0))) {
                this.dronePos = this.dronePos.nextPosition(LngLat.Direction.Null);

                Flightpath hoverMove = new Flightpath(order.getOrderNo(), this.dronePos.getLng(), this.dronePos.getLat(),
                        -999, this.dronePos.getLng(), this.dronePos.getLat());
                this.flightpaths.add(hoverMove);

                this.remainBattery -= 1;
                routeOfOrder.remove(0);

                // delivered the last order of the day
                if ((!routeOfOrder.isEmpty()) && routeOfOrder.get(0).equals(this.startPos)){
                    orderDelivered.add(order);
                    System.out.println("DRONE: the last order delivered, begin to return");
                }
            }
            // delivered the current order
            if (routeOfOrder.isEmpty() && !prepareToReturn) {
                orderDelivered.add(order);
                order.markDelivered();
                System.out.printf("DRONE: current order {orderNo: %s} delivered %n",
                        order.getOrderNo());
                break;
                // the drone returned to starting position
            } else if (routeOfOrder.isEmpty() && this.dronePos.closeTo(this.startPos)){
                this.returned = true;
                System.out.println("DRONE: returned to starting position");
                break;
            }
        }

    }


    /**
     * Plan the route of the drone for current order,
     * connecting shops and customer and avoiding restricted area
     * @param order the current order
     * @return the route
     */
    private ArrayList<LngLat> planRoute(Order order){
        ArrayList<LngLat> route = new ArrayList<LngLat>();

        addLocToRoute(this.dronePos, order.getRestaurantLoc(), route);
        addLocToRoute(order.getRestaurantLoc(), APPLETON_TOWER, route);

        return route;
    }

    /**
     * Helper function to plan the route of the drone for current order,
     * add a desired position for the drone
     * @param prePos previous position for the drone to visit
     * @param nextPos desired position for the drone
     * @param route the route of the drone
     */
    private void addLocToRoute(LngLat prePos, LngLat nextPos, ArrayList<LngLat> route ){
        route.add(nextPos);
    }


    private void Bug2Algorithm(LngLat goalPos){


    }

}
