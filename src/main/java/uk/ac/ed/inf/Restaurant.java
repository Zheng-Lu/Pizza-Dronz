package uk.ac.ed.inf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URL;

/**
 * The class stores the information of a restaurant (e.g., restaurant's name, location, and menu)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Restaurant {
    @JsonProperty("name")
    private String name;
    @JsonProperty("longitude")
    private Double longitude;
    @JsonProperty("latitude")
    private Double latitude;
    @JsonProperty("menu")
    private Menu[] menus;

    /* Getters */

    /** @return the name of the restaurant */
    public String getRestaurantName() {
        return name;
    }

    /** @return the longitude of the restaurant location */
    public Double getLongitude() {
        return longitude;
    }

    /** @return the latitude of the restaurant location */
    public Double getLatitude() {return latitude;}

    /** @return the pizza menus of the restaurant */
    public Menu[] getMenu() {
        return menus;
    }


    /**
     * Method that retrieving restaurant data from the REST service
     * @param serverBaseAddress url of server base address
     * @return an array of Restaurants which are defined (including the menus)
     */
    public static Restaurant[] getRestaurantsFromRestServer(URL serverBaseAddress) {
        ObjectMapper mapper = new ObjectMapper();
        Restaurant[] restaurants = new Restaurant[0];
        String endpoint = "/restaurants";

        try {
            restaurants = mapper.readValue(new URL(serverBaseAddress + endpoint), Restaurant[].class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return restaurants;
    }


    /**
     * Find the corresponding restaurant from given restaurant array according to a given ordered item
     * @param restaurants Array of participated restaurants
     * @param orderItem Item from order, also known as pizza name
     * @return the corresponding restaurant
     */
    public static Restaurant getRestaurantByItem(Restaurant[] restaurants, String orderItem) {
        for (Restaurant restaurant : restaurants){
            for (Menu menu : restaurant.getMenu()) {
                if (orderItem.equals(menu.getName())) {
                    return restaurant;
                }
            }
        }
        return null;
    }
}
