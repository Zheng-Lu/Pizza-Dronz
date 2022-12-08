package uk.ac.ed.inf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The class representing an order
 */
public class Order {

    private final String orderNo;
    private final String orderDate;
    private final LngLat restaurantLoc;
    private final String creditCardNumber;
    private final String creditCardExpiry;
    private final String cvv;
    private final String[] orderItems;
    private final int priceTotalInPence;
    private String orderOutcome;

    /**
     * Create the order
     * @param orderNo the eight-character hexadecimal string assigned to this order in the orders REST
     * service endpoint
     * @param orderDate the date the order was created
     * @param restaurantLoc participated restaurant locations
     * @param creditCardNumber customer's credit card number that they use for order payment
     * @param creditCardExpiry the expiry data of customer's credit card, usually in MM/YY format
     * @param cvv the 3-4 digits number at the backside of customer's credit card
     * @param orderItems items of the order, an array of pizza names
     * @param priceTotalInPence total cost of the order, include the 100p fixed delivery charge
     * @param orderOutcome the outcome of an order
     */
    public Order(String orderNo, String orderDate, LngLat restaurantLoc, String creditCardNumber, String creditCardExpiry,
                 String cvv, String[] orderItems, int priceTotalInPence, String orderOutcome) {

        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.restaurantLoc = restaurantLoc;
        this.creditCardNumber = creditCardNumber;
        this.creditCardExpiry = creditCardExpiry;
        this.cvv = cvv;
        this.orderItems = orderItems;
        this.priceTotalInPence = priceTotalInPence;
        this.orderOutcome = orderOutcome;

    }

    public String getOrderNo() {return this.orderNo;}

    public LngLat getRestaurantLoc() {
        return this.restaurantLoc;
    }

    public int getPriceTotalInPence() {
        return this.priceTotalInPence;
    }

    public String getOrderOutcome() {
        return this.orderOutcome;
    }

    public double getDistance(){
        return this.restaurantLoc.distanceTo(Drone.APPLETON_TOWER);
    }


    /** Change the status of an order to OrderOutcome.Delivered when it got delivered by drone */
    public void markDelivered() {this.orderOutcome = OrderOutcome.Delivered.toString();}


    /**
     * The enum class represents 10 types of the outcome of an order
     */
    public enum OrderOutcome {
        /** The order is delivered */
        Delivered,

        /** The order is valid but have not been delivered yet */
        ValidButNotDelivered,

        /** The credit card number used in the payment of the order is invalid */
        InvalidCardNumber,

        /** The expiry date of credit card that used for order payment is invalid */
        InvalidExpiryDate,

        /** The cvv of credit card given by customer for order payment is invalid */
        InvalidCvv,

        /** Total cost retrieved from web server is not equal to actual total cost */
        InvalidTotal,

        /** The name of pizza cannot be found in a given restaurant's menu */
        InvalidPizzaNotDefined,

        /** The number of pizzas per delivery carried by drone is either zero or greater than shipping limit */
        InvalidPizzaCount,

        /** Pizzas in an order do not come from the same pizza restaurant */
        InvalidPizzaCombinationMultipleSuppliers,

        /** Invalid */
        Invalid
    }


    /**
     * check if given card number is valid
     * @param cardNumber restaurants that participate in pizza delivery
     * @return true if given card number is valid, or false otherwise
     * @throws NullPointerException If the given input is null.
     */
    private static boolean isValidCardNumber(String cardNumber){
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--)
        {
            try {
                int n = Integer.parseInt(cardNumber.substring(i, i + 1));
                if (alternate)
                {
                    n *= 2;
                    if (n > 9)
                    {
                        n = (n % 10) + 1;
                    }
                }
                sum += n;
                alternate = !alternate;
            } catch (NumberFormatException e) {
                // Invalid if given card number does not have the appropriate format
                // such as including extra spaces, or special characters
                return false;
            }

        }
        return (sum % 10 == 0) && (cardNumber.length() == 16);
    }


    /**
     * check if given card number is valid
     * @param expiryDate expiry date on card
     * @param orderMonth the month the customer ordered pizza
     * @param orderYear the year the customer ordered pizza
     * @return true if given expiry date is valid, or false otherwise
     * @throws NullPointerException If the given input is null.
     */
    private static boolean isValidCardExpiry(String expiryDate, String orderMonth, String orderYear){
        SimpleDateFormat cardExpiryFormat = new SimpleDateFormat("MM/yy");
        SimpleDateFormat orderDateFormat = new SimpleDateFormat("MM/yyyy");
        orderDateFormat.setLenient(false);
        cardExpiryFormat.setLenient(false);
        Date expiry;
        Date currOrder;
        try {
            expiry = cardExpiryFormat.parse(expiryDate);
            currOrder = orderDateFormat.parse(orderMonth + "/" + orderYear);
        } catch (ParseException e) {
            return false;
        }

        return currOrder.before(expiry) || currOrder.equals(expiry);
    }


    /**
     * check if given cvv is valid using regular expression
     * @param cvv CVV (Card Verification Value) number
     * @return true if given cvv is valid, or false otherwise
     * @throws NullPointerException If the given input is null.
     */
    private static boolean isValidCVV(String cvv) {
        String regex = "^[0-9]{3}$";
        Pattern p = Pattern.compile(regex);

        if (cvv == null) {
            return false;
        }

        Matcher m = p.matcher(cvv);

        return m.matches();
    }


    /**
     * get the delivery cost, including delivery charge of 1 pound (100 pence)
     * @param participants restaurants that participate in pizza delivery
     * @param orderedPizza the ordered pizza being delivery
     * @return cost in pence of having all of these items delivered by drone,
     * including the standard delivery charge of 100p per delivery
     * @throws NullPointerException If the given input is null.
     */
    private static int getDeliveryCost(Restaurant[] participants, String[] orderedPizza) throws Exception {
        int deliveryCost = 100;
        int numPizza = orderedPizza.length;
        Queue<String> remain = new LinkedList<>(Arrays.asList(orderedPizza));

        // Check if it exceeds the delivery maximum
        if (numPizza > 4 || numPizza < 1) {
            throw new Exception(String.valueOf(OrderOutcome.InvalidPizzaCount));
        }

        List<String> allMenu = new ArrayList<>();
        for (Restaurant participant : participants){
            // To store the menu which maps items to prices
            for (Menu menu : participant.getMenu()){
                allMenu.add(menu.getName());
            }
        }
        
        for (Restaurant participant : participants){
            // To store the menu which maps items to prices
            HashMap<String, Integer> menuMap = new HashMap<>();
            for (Menu menu : participant.getMenu()){
                menuMap.put(menu.getName(), menu.getPriceInPence());
            }

            for (String pizza : orderedPizza){
                if (menuMap.containsKey(pizza)){
                    deliveryCost += menuMap.get(pizza);
                    remain.poll();
                } else if (!allMenu.contains(pizza)) {
                    throw new Exception(String.valueOf(OrderOutcome.InvalidPizzaNotDefined));
                }
            }

            if (remain.size() != numPizza && remain.size() > 0) {
                throw new Exception(String.valueOf(OrderOutcome.InvalidPizzaCombinationMultipleSuppliers));
            }
        }

        if (remain.size() == numPizza) {
            throw new Exception(String.valueOf(OrderOutcome.InvalidPizzaNotDefined));
        }

        return deliveryCost;
    }


    /**
     * get the delivery cost, including delivery charge of 1 pound (100 pence)
     * @param orderItems the ordered pizza being delivery
     * @param creditCardNumber customer's credit card number
     * @param creditCardExpiry expiry date of customer's credit card
     * @param cvv 3 - 4 digit number
     * @param orderDate the date the customer ordered pizza
     * @param priceTotalInPence total price that one delivery cost (pence)
     * @return cost in pence of having all of these items delivered by drone,
     * including the standard delivery charge of 100p per delivery
     * @throws ParseException If the given orderDate input cannot be parsed .
     */
    public static String getOrderOutcome(Restaurant[] participants,
                                         String[] orderItems,
                                         String creditCardNumber,
                                         String creditCardExpiry,
                                         String cvv,
                                         String orderDate,
                                         int priceTotalInPence) throws ParseException {

        String[] date = orderDate.split("-");
        String year = date[0];
        String month = date[1];


        try{
            int calculatedTotal = getDeliveryCost(participants, orderItems);
            if (calculatedTotal != priceTotalInPence) {
                return String.valueOf(OrderOutcome.InvalidTotal);
            }
        } catch (Exception e) {
            return e.getMessage();
        }

        if (!isValidCardNumber(creditCardNumber)) {
            return String.valueOf(OrderOutcome.InvalidCardNumber);
        }

        if (!isValidCardExpiry(creditCardExpiry, month, year)) {
            return String.valueOf(OrderOutcome.InvalidExpiryDate);
        }

        if (!isValidCVV(cvv)) {
            return String.valueOf(OrderOutcome.InvalidCvv);
        }

        return String.valueOf(OrderOutcome.ValidButNotDelivered);
    }
}
