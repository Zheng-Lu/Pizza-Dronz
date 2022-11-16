package uk.ac.ed.inf;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.List;


public class Deliveries {
    String orderNo;
    String orderOutcome;
    int costInPence;

    /**
     * Create one step in flightpath
     * @param orderNo order number
     * @param orderOutcome the OrderOutcome value for this order, as a string
     * @param costInPence the total cost of the order as an integer, including the standard £1 delivery
     * charge.
     */
    public Deliveries(String orderNo, String orderOutcome, int costInPence){
        this.orderNo = orderNo;
        this.orderOutcome = orderOutcome;
        this.costInPence = costInPence;
    }

}
