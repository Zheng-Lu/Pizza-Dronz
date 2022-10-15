package uk.ac.ed.inf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Order {

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

}
