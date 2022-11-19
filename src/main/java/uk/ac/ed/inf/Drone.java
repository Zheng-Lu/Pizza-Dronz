package uk.ac.ed.inf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.time.Clock;

public class Drone {
    public static final int BATTERY = 2000;
    public static final double APPLETON_LONGITUDE = -3.186874;
    public static final double APPLETON_LATITUDE = 55.944494;
    public static final LngLat APPLETON_TOWER = new LngLat(APPLETON_LONGITUDE, APPLETON_LATITUDE);
    public boolean prepareToReturn;
    private final LngLat startPos;
    private LngLat dronePos;
    private LngLat currGoal;
    private int remainBattery;
    private int ticksSinceStartOfCalculation;
    private List<Flightpath> flightpaths;
    private final MapInitialization mapInitialization;
    private List<Order> orderDelivered;
    private List<Order> allOrders;

    /** getters  */
    public List<Order> getOrderDelivered(){
        return this.orderDelivered;
    }
    public List<Order> getAllOrders(){
        return this.allOrders;
    }

    public List<Flightpath> getFlightpaths(){
        return this.flightpaths;
    }

    public int getRemainBattery(){
        return this.remainBattery;
    }
    public LngLat getCurrGoal() { return this.currGoal; }
    public LngLat getStartPos() { return this.startPos; }



    /**
     * Create the drone
     * @param startPos starting position of the drone, usually Appleton Tower
     */
    public Drone(MapInitialization mapInitialization, LngLat startPos) {
        this.mapInitialization = mapInitialization;
        this.startPos = startPos;
        this.dronePos = new LngLat (startPos.lng, startPos.lat);
        this.currGoal = null;
        this.remainBattery = BATTERY;
        this.prepareToReturn = false;
        this.flightpaths = new ArrayList<>();
        this.orderDelivered = new ArrayList<>();
        this.allOrders = new ArrayList<>();
        this.ticksSinceStartOfCalculation = 0;

    }

    public void initializeOrders(Restaurant[] restaurants, String year, String month, String day) throws JSONException, ParseException {
        DataReadWrite dataReadWrite = new DataReadWrite();
        List<JSONObject> rawOrders = dataReadWrite.readOrders(year,month,day);

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

            this.allOrders.add(new Order(orderNo, orderDate, restaurantLoc, creditCardNumber, creditCardExpiry,
                    cvv, orderItems, priceTotalInPence, orderOutcome));
        }

        // Sort orders according to distance from Appleton Tower in ascending way
        this.allOrders.sort(((o1, o2) -> Double.compare(o2.getDistance(), o1.getDistance())));
        Collections.reverse(this.allOrders);
    }

    public void getOrdersStatistics() {
        int validNumOrders = 0;

        for (Order order : this.allOrders){
            if (order.getOrderOutcome().equals(Order.OrderOutcome.ValidButNotDelivered.toString()) ||
                    order.getOrderOutcome().equals(Order.OrderOutcome.Delivered.toString())){
                validNumOrders += 1;
            }
        }
        System.out.println("Number of invalid orders: " + (getAllOrders().size() - validNumOrders));
        System.out.println("Orders delivered: " + getOrderDelivered().size() + "/" + validNumOrders);
        System.out.println("Remaining battery after delivery: " + getRemainBattery() + "\n");
    }


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

                LngLat newPos = this.dronePos.move(this.mapInitialization, this.currGoal);

                end = clock.millis();
                this.ticksSinceStartOfCalculation += end - start;

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
                    this.ticksSinceStartOfCalculation += end - start;

                    Flightpath hoverMove = new Flightpath(order.getOrderNo(), this.dronePos.getLng(),
                            this.dronePos.getLat(), -999, this.dronePos.getLng(), this.dronePos.getLat(),
                            this.ticksSinceStartOfCalculation);

                    this.flightpaths.add(hoverMove);

                    this.remainBattery -= 1;

                    this.prepareToReturn = true;
                    this.currGoal = this.startPos;

                }
            } else {
                Collections.reverse(backtrack_path);

                for (Flightpath thisMove : backtrack_path) {

                    this.flightpaths.add(thisMove);

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
                    this.ticksSinceStartOfCalculation += end - start;

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

    // TODO: Implement bug2 algorithm
    private void Bug2Algorithm(LngLat goalPos){

    }

}
