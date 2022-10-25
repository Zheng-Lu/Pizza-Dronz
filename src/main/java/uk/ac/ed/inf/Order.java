package uk.ac.ed.inf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Order {

    private final String orderNo;
    private final String orderDate;
    private final LngLat restaurantLoc;
    private final String creditCardNumber;
    private final String creditCardExpiry;
    private final String cvv;
    private final List<String> orderItems;
    private final int priceTotalInPence;

    public Order(String orderNo, String orderDate, LngLat restaurantLoc, String creditCardNumber, String creditCardExpiry,
                 String cvv, List<String> orderItems, int priceTotalInPence) {

        this.orderNo = orderNo;
        this.orderDate = orderDate;
        this.restaurantLoc = restaurantLoc;
        this.creditCardNumber = creditCardNumber;
        this.creditCardExpiry = creditCardExpiry;
        this.cvv = cvv;
        this.orderItems = orderItems;
        this.priceTotalInPence = priceTotalInPence;
    }

    public String getOrderNo() {
        return this.orderNo;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public LngLat getRestaurantLoc() {
        return this.restaurantLoc;
    }

    public String getCreditCardNumber() {
        return this.creditCardNumber;
    }

    public String getCreditCardExpiry() {
        return this.creditCardExpiry;
    }

    public String getCvv() {
        return this.cvv;
    }

    public List<String> getOrderItems() {
        return this.orderItems;
    }

    public int getPriceTotalInPence() {
        return this.priceTotalInPence;
    }




    public enum OrderOutcome {
        Delivered,
        ValidButNotDelivered,
        InvalidCardNumber,
        InvalidExpiryDate,
        InvalidCvv,
        InvalidTotal,
        InvalidPizzaNotDefined,
        InvalidPizzaCount,
        InvalidPizzaCombinationMultipleSuppliers,
        Invalid
    }

    /**
     * check if given card number is valid
     * @param cardNumber restaurants that participate in pizza delivery
     * @return true if given card number is valid, or false otherwise
     * @throws NullPointerException If the given input is null.
     */
    public static boolean isValidCardNumber(String cardNumber){
        int sum = 0;
        boolean alternate = false;
        for (int i = cardNumber.length() - 1; i >= 0; i--)
        {
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
    public static boolean isValidCardExpiry(String expiryDate, String orderMonth, String orderYear) throws ParseException {
        String orderDate = orderMonth + "/" + orderYear;
        SimpleDateFormat cardExpiryFormat = new SimpleDateFormat("MM/yy");
        SimpleDateFormat orderDateFormat = new SimpleDateFormat("MM/yyyy");
        orderDateFormat.setLenient(false);
        cardExpiryFormat.setLenient(false);
        Date expiry = cardExpiryFormat.parse(expiryDate);
        Date currOrder = orderDateFormat.parse(orderDate);

        return currOrder.before(expiry) || currOrder.equals(expiry);
    }


    /**
     * check if given cvv is valid using regular expression
     * @param cvv CVV (Card Verification Value) number
     * @return true if given cvv is valid, or false otherwise
     * @throws NullPointerException If the given input is null.
     */
    public static boolean isValidCVV(String cvv) {
        String regex = "^[0-9]{3,4}$";
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
    public static int getDeliveryCost(Restaurant[] participants, String[] orderedPizza) throws Exception {
        int deliveryCost = 100;
        int numPizza = orderedPizza.length;
        Queue<String> remain = new LinkedList<>(Arrays.asList(orderedPizza));

        // Check if it exceeds the delivery maximum
        if (numPizza > 4 || numPizza < 1) {
            throw new Exception(String.valueOf(OrderOutcome.InvalidPizzaCount));
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
     * @param participants restaurants that participate in pizza delivery
     * @param orderItems the ordered pizza being delivery
     * @param creditCardNumber
     * @param creditCardExpiry
     * @param cvv
     * @param orderDate
     * @param priceTotalInPence
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

        return "";
    }



    public static void main(String[] args) throws JSONException, ParseException {
        ReadData readData = new ReadData();
        JSONArray jsonArray = readData.readOrders("2023","01", "31");
         for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            String creditCardNumber = object.get("creditCardNumber").toString();
            System.out.println(creditCardNumber.length() + ":" + isValidCardNumber(creditCardNumber));

            String cvv = object.get("cvv").toString();
            System.out.println(cvv + ":" + isValidCVV(cvv));

            String expiryDate = object.get("creditCardExpiry").toString();
            System.out.println(expiryDate + ":" + isValidCardExpiry(expiryDate, "01", "2023"));
        }

        System.out.println(isValidCardExpiry("02/23", "02", "2023"));
    }

}
